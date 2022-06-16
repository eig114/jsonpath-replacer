(ns jsonpath-replacer.core-test
  (:import 
   [java.io File])
  (:require [clojure.test :refer :all]
            [jsonpath-replacer.core :refer :all]
            [jsonpath-replacer.messages :as msg]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]))

(def input-json-val
  "input json"
  "{\"a\":\"a value\",\"b\":\"b value\",\"c\":{\"a\":\"nested value a\",\"b\":\"nested value b\"}}")

(defn input-json-fixture
  "Create temporary file to be used as input. Write `input-json-val`
  there. This file will be deleted on JVM exit.
  Also define symbols `current-input-file` and
  `current-input-file-name` containing [[java.io.File]] for temporary
  file and it's absolute path"
  [test-fn]
  (let [in-file (java.io.File/createTempFile "input-json-" ".json")]
    (try
      (spit in-file input-json-val)
      (def current-input-file in-file)
      (def current-input-file-name (.getAbsolutePath in-file))
      ;;
      (test-fn)
      ;;
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
  (testing "Substititution works correctly"
    (is (= "{\"a\":\"REPLACED\",\"b\":\"b value\",\"c\":{\"a\":\"nested value a\",\"b\":\"nested value b\"}}"
           (with-redefs [jsonpath-replacer.core/write-json (fn [w x] x)] ;; supress stdout output
             (-main "-c" "-i" current-input-file-name "$.a" "REPLACED"))))
    ))
