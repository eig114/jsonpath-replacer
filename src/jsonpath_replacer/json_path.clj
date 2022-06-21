(ns jsonpath-replacer.json-path
  "Wrapper functions for com.jayway.jsonpath library"
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:import 
   [com.jayway.jsonpath
    JsonPath
    ParseContext
    ReadContext
    DocumentContext
    WriteContext
    Configuration]))
(set! *warn-on-reflection* true)

(defmacro array-type-hint
  "Construct array from `coll` and put a type hint for it"
  [^clojure.lang.Symbol element-class coll]
  `^{:tag ~(symbol (str "[L" (.getName element-class) ";"))} (into-array ~element-class ~coll))

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
       (~real-name ~@arg-list
        (array-type-hint com.jayway.jsonpath.Predicate ~pred-var)))))

(json-path-func json-path-compile JsonPath/compile [path])
(json-path-func json-path-read .read [^ReadContext json-context ^String path])
(json-path-func json-path-set .set [^WriteContext json-context path new-value])
(json-path-func json-path-add .add [^WriteContext json-context path new-value])

(defn make-parse-context
  "Make ParseContext with `options`"
  ^ParseContext [& options]
  (-> (Configuration/builder)
      (.options (array-type-hint com.jayway.jsonpath.Option options))
      (.build)
      (JsonPath/using)))

(defn parse-json
  "Parse `readable` to DocumentContext."
  ^DocumentContext [^ParseContext ctx readable]
   (with-open [r (io/reader readable)]
     (.parse ctx (slurp r))))
