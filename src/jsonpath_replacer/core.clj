(ns jsonpath-replacer.core
  "Main namespace containing `-main` function and CLI processing."
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [jsonpath-replacer.json-path :as jsp]
   [jsonpath-replacer.messages :as msg])
  (:import 
   [com.jayway.jsonpath
    Option
    ReadContext
    ParseContext]
   [com.jayway.jsonpath.internal JsonFormatter]))
(set! *warn-on-reflection* true)

(def cli-opts
  [
   ["-i" "--in-file INPUT_NAME" "Input JSON file"
    :default System/in
    :default-desc "default STDIN"
    :validate [#(or (= System/in %)
                    (.exists (io/file %))) msg/error-input-not-existing]]
   ["-o" "--out-file OUTPUT_NAME" "Output JSON file"
    :default-desc "default STDOUT"
    :default System/out]
   ["-c" "--compact" "Compact output"]
   ["-j" "--json-replacement" "Treat replacement as json instead of a plain string"]
   ["-h" "--help" "Print usage info"]
   ["-x" "--extract" "Instead of replacing, extract selected jsonpath spec"]
   ])

(defn- usage
  "Get usage for this program.
  `compiled-opts` must be result of [[parse-opts]] invocation."
  [compiled-opts]
  (format "java -jar jsonpath-replacer.jar OPTIONS JSONPATH_SPEC REPLACEMENT

JSONPATH_SPEC is JSONPath to match replaced values
REPLACEMENT is a replacement string
OPTIONS are optional, and are as follows:
%s
" (compiled-opts :summary)))

(defn- full-parse-opts
  "Parse and validate command line argument array, return option map.
  Result contains keys:
  :json-path
  :replacement
  :in-file
  :out-file
  :compact
  :json-replacement
  :extract

  If validation fails, print error to stdout and return nil."
  [args]
  (let [parsed-opts (parse-opts args cli-opts)
        {:keys [options arguments errors]} parsed-opts]
    (cond
      errors (println (str/join \newline errors))
      (options :help) (println (usage parsed-opts))
      (< (count arguments) 1) (println msg/error-jsopath-missing)
      (and (< (count arguments) 2) (not (options :extract))) (println msg/error-relacement-missing)
      :else {:json-path (first arguments)
             :replacement (second arguments)
             :compact (options :compact)
             :json-replacement (options :json-replacement)
             :extract (options :extract)
             :in-file (options :in-file)
             :out-file (options :out-file)})))

(defn- default-context []
  (jsp/make-parse-context Option/SUPPRESS_EXCEPTIONS Option/ALWAYS_RETURN_LIST))

(defn- extraction-context []
  (jsp/make-parse-context Option/SUPPRESS_EXCEPTIONS))

;; todo remove when https://github.com/json-path/JsonPath/issues/762 is fixed
(defn- to-json [^ParseContext json-context obj]
  (if (or (instance? Number obj)
          (instance? Boolean obj))    
    (net.minidev.json.JSONValue/toJSONString obj)
    (.jsonString (.parse json-context obj))))

(defn write-json [writer js]
  (spit writer js)
  js)

(defn -main
  "Main method.
  Usage: \"<java invocation> JSONPATH REPLACEMENT [-i IN_FILENAME] [-o OUT_FILENAME] [-c] [-j]\"

  if OUT_FILENAME isn't specified, write results to stdout.
  if IN_FILENAME isn't specified, read from stdin
  if -c options is present, output json in compact form
  if -j option is present, treat JSONPATH as json instead of a plain string
  if -x option is present, extract JSONPATH from input. REPLACEMENT is ignored

  When called as a clojure function, return updated json."
  [& args]

  (let [{:keys [json-path replacement in-file out-file compact extract json-replacement]} (full-parse-opts args)
        json-context (if extract (extraction-context) (default-context))
        ;; of json-replacement is present, treat replacement as json
        replacement (if json-replacement
                      (->> replacement
                           (char-array)
                           (jsp/parse-json json-context)
                           (.json))
                      replacement)]
    (when (and json-path in-file out-file)
      (with-open [out-writer (io/writer out-file)]
        (as-> (jsp/parse-json json-context in-file) it
          (if extract
            (to-json json-context (jsp/json-path-read it json-path))
            (.jsonString ^ReadContext (jsp/json-path-set it json-path replacement)))
          (if compact
            it
            (JsonFormatter/prettyPrint it))
          (write-json out-writer it))))))
