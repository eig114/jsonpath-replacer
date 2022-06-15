(ns jsonpath-replacer.core
  (:gen-class)
  (:import 
   [com.jayway.jsonpath Option])
  (:require  [jsonpath-replacer.messages :as msg]
             [jsonpath-replacer.json-path :refer :all]
             [clojure.java.io :as io]
             [clojure.tools.cli :refer [parse-opts]]))

(defn write-json-to-file
  "Write JsonContext object `ctx` to file called `file-name`"
  [ctx file-name]
  (with-open [w (io/writer file-name)]
    (.write w (.jsonString ctx))))

(def cli-opts
  [
   ["-i" "--in-file NAME" "Input JSON file"
    :default System/in
    :default-desc "STDIN"
    :validate [#(or (= System/in %)
                    (.exists (io/file %))) msg/error-input-not-existing]]
   ["-o" "--out-file NAME" "Output JSON file"
    :default-desc "STDOUT"
    :default System/out]
   ])

(defn full-parse-opts [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-opts)]
    (cond
      errors (println (clojure.string/join \newline errors))
      (< (count arguments) 1) (println msg/error-jsopath-missing)
      (< (count arguments) 2) (println msg/error-relacement-missing)
      :else {:json-path (first arguments)
             :replacement (second arguments)
             :in-file (options :in-file)
             :out-file (options :out-file)})))

(defn -main
  "Main method.
  Usage: \"<java invocation> JSONPATH REPLACEMENT [-i IN_FILENAME] [-o OUT_FILENAME]\"

  if OUT_FILENAME isn't specified, write results to stdout.
  if IN_FILENAME isn't specified, read from stdin"
  [& args]

  (let [{:keys [json-path replacement in-file out-file]} (full-parse-opts args)
        json-context (make-json-context Option/SUPPRESS_EXCEPTIONS Option/ALWAYS_RETURN_LIST)]
    (when (and json-path replacement in-file out-file)
      (with-open [out-writer (io/writer out-file)]
        (as-> (parse-json json-context in-file) it
          (json-path-set it json-path replacement)
          (.jsonString it)
          (.write out-writer it))))))
  
;; "$..targets[?(@.datasource.type=='prometheus')].datasource.uid"
;; "c:\\users\\Tudiyarov\\bm-nalog.json"
