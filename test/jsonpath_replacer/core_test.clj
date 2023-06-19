(ns jsonpath-replacer.core-test
  (:import 
   [java.io File])
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   #_{:clj-kondo/ignore [:refer-all]}
   [jsonpath-replacer.core :refer :all]
   [jsonpath-replacer.messages :as msg]
   [jsonpath-replacer.json-path :as jsp]
   [clojure.test :refer :all])
  (:import
   [com.jayway.jsonpath Option]))

(def json-val-for-replacement
  "Input json to be used in replacement tests"
  "{\"a\":\"a value\",
    \"b\":\"b value\",
    \"c\":{\"a\":\"nested value a\",
           \"b\":\"nested value b\"}}")

(def json-val-for-extraction
  "Input json to be used in extraction tests"
  "{\"stringLiteral\": \"string\",
    \"numericLiteral\": 123,
    \"booleanLiteral\": true,
    \"nullLiteral\": null}")

(def input-file-name-for-replacement
  "Absolute file name for json to be used in replacement tests.
  Defined by [[input-json-fixture]]"
  nil)

(def input-file-name-for-extraction
  "Absolute file name for json to be used in extraction tests.
  Defined by [[input-json-fixture]]"
  nil)

(defn input-json-fixture
  "Create temporary files to be used as input. Write
  `json-val-for-replacement` and `json-val-for-extraction`
  there. These files will be deleted on JVM exit."
  [test-fn]
  (let [in-file-replace (File/createTempFile "input-json-" ".json")
        in-file-extract (File/createTempFile "input-xtract-json-" ".json")]
    (try
      (spit in-file-replace json-val-for-replacement)
      (spit in-file-extract json-val-for-extraction)      
      (with-redefs [input-file-name-for-replacement (.getAbsolutePath in-file-replace)
                    input-file-name-for-extraction (.getAbsolutePath in-file-extract)]
        (test-fn))
      (finally (.deleteOnExit in-file-replace)
               (.deleteOnExit in-file-extract)))))

(use-fixtures :once input-json-fixture)

(deftest core-arg-parse
  (testing "Valid arguments are parsed with no errors"
    (is (as-> ["-i" input-file-name-for-replacement "-o" "tmp-out.json"] it
          (parse-opts it cli-opts)
          (it :errors)
          (nil? it))))
  (testing "Arguments are parsed correctly, with default values"
    (are [argv k value] (let [parsed-args (parse-opts argv cli-opts)]
                            (= value ((parsed-args :options) k)))
      ["-i" input-file-name-for-replacement "-o" "tmp-out.json"] :in-file input-file-name-for-replacement
      ["-i" input-file-name-for-replacement "-o" "tmp-out.json"] :out-file "tmp-out.json"
      ["-o" "tmp-out.json"] :in-file System/in
      ["-i" input-file-name-for-replacement] :out-file System/out
      [] :out-file System/out
      [] :in-file System/in))
  (testing "Illegal arguments are rejected by -main function"
    (are [argv expected-err] (= expected-err (.trim (with-out-str (apply -main argv))))
      [] msg/error-jsopath-missing
      ["-i" "i-dont-exist.json"] (str "Failed to validate \"-i i-dont-exist.json\": " msg/error-input-not-existing)
      ["-i" input-file-name-for-replacement] msg/error-jsopath-missing
      ["-i" input-file-name-for-replacement "-o" "tmp-out.json"] msg/error-jsopath-missing
      ["-i" input-file-name-for-replacement "-o" "tmp-out.json" "$.a"] msg/error-relacement-missing)))


(deftest substitution-logic
  (testing "Simple value substititution works correctly"
    (are  [json-path replacement result] (= result
                                            (get-result json-path
                                                        replacement
                                                        input-file-name-for-replacement
                                                        true
                                                        false
                                                        (jsonpath-replacer.core/default-context)))
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
      "{\"a\":\"{\\\"b\\\":123, \\\"c\\\":\\\"456\\\"}\",\"b\":\"b value\",\"c\":{\"a\":\"nested value a\",\"b\":\"nested value b\"}}"
      ))
  (testing "JSON value substitution works correctly"
    (are  [json-path replacement result] (= result
                                            (let [json-context (jsonpath-replacer.core/default-context)]
                                              (get-result json-path
                                                          (generate-json-replacement json-context replacement)
                                                          input-file-name-for-replacement
                                                          true
                                                          false
                                                          json-context)))
      ;; replace top-level "a" with {"b":123, "c":"456"}
      "$.a" "{\"b\":123, \"c\":\"456\"}"
      "{\"a\":{\"b\":123,\"c\":\"456\"},\"b\":\"b value\",\"c\":{\"a\":\"nested value a\",\"b\":\"nested value b\"}}"))
  )

(deftest extract-logic
  (testing "Simple extraction works correctly"
    (are [json-path result] (= result
                               (get-result json-path
                                           nil ;; should contain replacement, which is ignored in this mode
                                           input-file-name-for-extraction
                                           false
                                           true
                                           (jsonpath-replacer.core/extraction-context)))
      "$.stringLiteral" "\"string\""
      "$.numericLiteral" "123"
      "$.booleanLiteral" "true"
      "$.nullLiteral" "null"
      )))
