(ns jsonpath-replacer.json-path-test
  (:import 
   [com.jayway.jsonpath Option])
  (:require
   [clojure.test :refer :all]
   #_{:clj-kondo/ignore [:refer-all]}
   [jsonpath-replacer.json-path :refer :all]))

(def json-ctx
  "Default context"
  (make-parse-context Option/SUPPRESS_EXCEPTIONS Option/ALWAYS_RETURN_LIST))

(deftest json-read-jsonpath-test
  (testing "Parser reads values as ReadContext"
    (is (instance? com.jayway.jsonpath.ReadContext
                   (parse-json json-ctx (.toCharArray "{\"a\":123}")))))
  (testing "Parser reads single value by jsonpath as single element collection"
    (let [ctx (parse-json json-ctx (.toCharArray "{\"a\":123}"))]
      (is (instance? java.util.Collection
           (json-path-read ctx "$.a")))
      (is (= 1 (count (json-path-read ctx "$.a"))))
      (is (= 123 (first (json-path-read ctx "$.a")))))))

(deftest json-write-jsonpath-test
  (testing "Single existing value update"
    (let [ctx (parse-json json-ctx (.toCharArray "{\"a\":123}"))]
      (is (= 1234
             (first (json-path-read (json-path-set ctx "$.a" 1234)
                                    "$.a"))))
      (is (= "{\"a\":1234}"
             (.jsonString (json-path-set ctx  "$.a" 1234))))))
  (testing "Single non-existing value update does nothing"
    (let [ctx (parse-json json-ctx (.toCharArray "{\"a\":123}"))]
      (is (nil?
             (first (json-path-read (json-path-set ctx "$.b" 1234)
                                    "$.b"))))
      (is (= "{\"a\":123}"
             (.jsonString (json-path-set ctx  "$.b" 1234))))))
  (testing "Multiple values update"
    (let [ctx (parse-json json-ctx (.toCharArray "{\"a\":123, \"b\":\"testval\"}"))
          ctx-updated (json-path-set ctx "$.*" 1234)]
      (is (= 1234
             (first (json-path-read ctx-updated "$.a"))))
      (is (= 1234
             (first (json-path-read ctx-updated "$.b"))))
      (is (every? #(= 1234 %)
                 (json-path-read ctx-updated "$.*"))))))
