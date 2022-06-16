(defproject jsonpath-replacer "0.1.0-SNAPSHOT"
  :description "Replace values defined by JSONPath"
  :url "https://github.com/eig114/jsonpath-replacer"
  :license {:name "The MIT License"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [com.jayway.jsonpath/json-path "2.4.0"]
                 ;;slf4j needed for jsonpath
                 [org.slf4j/slf4j-api "1.7.36"]
                 ]
  :profiles {
             ;; Only include NOP logger when building executable
             ;; uberjar. Otherwise assume user will have their own
             ;; slf4j implementation on classpath.
             ;; see https://www.slf4j.org/codes.html#StaticLoggerBinder
             :uberjar {:dependencies [[org.slf4j/slf4j-nop "1.7.36"]]}
             }
  :repl-options {:init-ns jsonpath-replacer.core}
  :main jsonpath-replacer.core
  :aot :all)
