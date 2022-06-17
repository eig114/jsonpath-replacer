(ns jsonpath-replacer.core-test
  (:import 
   [java.io File])
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [jsonpath-replacer.core :refer :all]
            [jsonpath-replacer.messages :as msg]
            [clojure.test :refer :all]))

(def input-json-val
  "Input json to be used in tests"
  "{\"a\":\"a value\",\"b\":\"b value\",\"c\":{\"a\":\"nested value a\",\"b\":\"nested value b\"}}")

(def ^java.io.File current-input-file
  "File containing input json from `input-json-val`, to be used for
  tests. Defined by [[input-json-fixture]]"
  nil)

(def current-input-file-name
  "Absolute file name for `current-input-file`. Defined
  by [[input-json-fixture]]"
  nil)

(defn input-json-fixture
  "Create temporary file to be used as input. Write `input-json-val`
  there. This file will be deleted on JVM exit.
  Also define symbols `current-input-file` and
  `current-input-file-name` containing [[java.io.File]] for temporary
  file and it's absolute path"
  [test-fn]
  (let [in-file (File/createTempFile "input-json-" ".json")]
    (try
      (spit in-file input-json-val)
      (with-redefs [current-input-file in-file
                    current-input-file-name (.getAbsolutePath in-file)]
        (test-fn))
      (finally (.deleteOnExit in-file)))))

(use-fixtures :once input-json-fixture)

(deftest core-arg-parse
  (testing "Valid arguments are parsed with no errors"
    (is (as-> ["-i" current-input-file-name "-o" "tmp-out.json"] it
          (parse-opts it cli-opts)
          (it :errors)
          (nil? it))))
  (testing "Arguments are parsed correctly, with default values"
    (are [argv k value] (let [parsed-args (parse-opts argv cli-opts)]
                            (= value ((parsed-args :options) k)))
      ["-i" current-input-file-name "-o" "tmp-out.json"] :in-file current-input-file-name
      ["-i" current-input-file-name "-o" "tmp-out.json"] :out-file "tmp-out.json"
      ["-o" "tmp-out.json"] :in-file System/in
      ["-i" current-input-file-name] :out-file System/out
      [] :out-file System/out
      [] :in-file System/in))
  (testing "Illegal arguments are rejected by -main function"
    (are [argv expected-err] (= expected-err (.trim (with-out-str (apply -main argv))))
      [] msg/error-jsopath-missing
      ["-i" "i-dont-exist.json"] (str "Failed to validate \"-i i-dont-exist.json\": " msg/error-input-not-existing)
      ["-i" current-input-file-name] msg/error-jsopath-missing
      ["-i" current-input-file-name "-o" "tmp-out.json"] msg/error-jsopath-missing
      ["-i" current-input-file-name "-o" "tmp-out.json" "$.a"] msg/error-relacement-missing)))


(deftest core-logic
  (testing "Simple value substititution works correctly"
    (are  [json-path replacement result] (= result
                                            (with-redefs [write-json (fn [_writer x] x)] ;; supress stdout output
                                              (-main "-c" "-i" current-input-file-name json-path replacement)))
      ;; replace top-level "a" with "REPLACED"
      "$.a" "REPLACED"
      "{\"a\":\"REPLACED\",\"b\":\"b value\",\"c\":{\"a\":\"nested value a\",\"b\":\"nested value b\"}}"
      ;; replace nested "a" with "REPLACED"
      "$.c.a" "REPLACED"
      "{\"a\":\"a value\",\"b\":\"b value\",\"c\":{\"a\":\"REPLACED\",\"b\":\"nested value b\"}}"
      ;; replace ALL "a" with "REPLACED"
      "$..a" "REPLACED"
      "{\"a\":\"REPLACED\",\"b\":\"b value\",\"c\":{\"a\":\"REPLACED\",\"b\":\"nested value b\"}}"
      ;; replace top-level "a" with string "{\"b\":123, \"c\":\"456\"}"
      "$.a" "{\"b\":123, \"c\":\"456\"}"
      "{\"a\":\"{\\\"b\\\":123, \\\"c\\\":\\\"456\\\"}\",\"b\":\"b value\",\"c\":{\"a\":\"nested value a\",\"b\":\"nested value b\"}}"))
  (testing "JSON value substitution also works"
    (are  [json-path replacement result] (= result
                                            (with-redefs [write-json (fn [_writer x] x)] ;; supress stdout output
                                              (-main "-c" "-j" "-i" current-input-file-name json-path replacement)))
      ;; replace top-level "a" with {"b":123, "c":"456"}
      "$.a" "{\"b\":123, \"c\":\"456\"}"
      "{\"a\":{\"b\":123,\"c\":\"456\"},\"b\":\"b value\",\"c\":{\"a\":\"nested value a\",\"b\":\"nested value b\"}}")))
