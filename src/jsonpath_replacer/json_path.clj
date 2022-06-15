(ns jsonpath-replacer.json-path
  "Wrapper functions for com.jayway.jsonpath library"
  (:gen-class)
  (:import 
   [com.jayway.jsonpath
    WriteContext
    JsonPath
    Configuration
    Option
    ParseContext
    DocumentContext
    Predicate])
  (:require [clojure.java.io :refer :all]))


(defmacro json-path-func
  "Macro to wrap methods with Predicate varargs at the end.
  Defines function `func-name`, with arguments from `arg-list` and
  [[com.jayway.jsonpath/Predicate]] vararg, which calls function
  `real-name` with those arguments"
  [func-name real-name arg-list]
  (let [pred-var (gensym 'predicates)]
    `(defn ~func-name
       ~(format "Wrap JsonPath/%s with convinient varargs" (name real-name))
       [~@arg-list & ~pred-var]
       (~real-name ~@arg-list (into-array com.jayway.jsonpath.Predicate ~pred-var)))))

(json-path-func json-path-compile JsonPath/compile [path])
(json-path-func json-path-read .read [json-context path])
(json-path-func json-path-set .set [json-context path new-value])
(json-path-func json-path-add .add [json-context path new-value])

(defn make-json-context
  "Make JsonPath with `options`"
  [& options]
  (-> (Configuration/builder)
      (.options (into-array options))
      (.build)
      (JsonPath/using)))

(defn parse-json
  "Parse `readable` to JsonContext, where `readable` can be passed
  to [[reader]] function"
  ([ctx readable]
   (with-open [r (reader readable)]
     (.parse ctx (slurp r)))))
