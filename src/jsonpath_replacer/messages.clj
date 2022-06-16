(ns jsonpath-replacer.messages
  (:gen-class))

(def info-usage-hint
  "Launch with -h or --help for usage information")

(def error-jsopath-missing
  (format "JSONPath is not specified.%n%s" info-usage-hint))

(def error-relacement-missing
  (format "Replacement string is not specified.%n%s" info-usage-hint))

(def error-input-not-existing
  (format "Input file must exist.%n%s" info-usage-hint))
