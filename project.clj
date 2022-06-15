(defproject jsonpath-replacer "0.1.0-SNAPSHOT"
  :description "Replace values defined by JSONPath"
  :url "https://github.com/eig114/jsonpath-replacer"
  :license {:name "The MIT License"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.jayway.jsonpath/json-path "2.4.0"]
                 [org.clojure/tools.cli "1.0.206"]]
  :repl-options {:init-ns jsonpath-replacer.core}
  :main jsonpath-replacer.core
  :aot :all)
