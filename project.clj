(def slf4j-version "1.7.36")

(defproject jsonpath-replacer "0.2.0"
  :description "Replace values defined by JSONPath"
  :url "https://github.com/eig114/jsonpath-replacer"
  :license {:name "The MIT License"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [com.jayway.jsonpath/json-path "2.7.0"]
                 ;;slf4j needed for jsonpath
                 [org.slf4j/slf4j-api ~slf4j-version]
                 ]
  :profiles {
             ;; Only include NOP logger when building executable
             ;; uberjar or doing dev stuff. Otherwise assume user will
             ;; have their own slf4j implementation on classpath.
             ;; see https://www.slf4j.org/codes.html#StaticLoggerBinder
             :uberjar {:dependencies [[org.slf4j/slf4j-nop ~slf4j-version]
                                      [com.github.clj-easy/graal-build-time "0.1.4"]]
                       :jvm-opts ["-Dclojure.compiler.elide-meta=[:doc :file :line :added]"
                                  "-Dclojure.compiler.direct-linking=true"]}
             :dev     {:dependencies [[org.slf4j/slf4j-nop ~slf4j-version]]}
             }
  :plugins [[com.github.clj-kondo/lein-clj-kondo "0.2.0"]]
  :repl-options {:init-ns jsonpath-replacer.core}
  :main jsonpath-replacer.core
  :aot :all)
