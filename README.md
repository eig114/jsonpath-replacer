# jsonpath-replacer

Utility to replace values in JSON file by JSONPath

## Usage

java -jar jsonpath-replacer-0.1.0-SNAPSHOT-standalone.jar -i INPUT_FILE -o OUTPUT_FILE JSONPATH REPLACEMENT

- INPUT_FILE is optional. Takes json from standard input if unset.
- OUTPUT_FILE is optional. Prints results to standard output if unset.
- JSONPATH is a jsonpath matching all values that need to be replaced.
- REPLACEMENT is a string that will replace every value that JSONPATH matches.
