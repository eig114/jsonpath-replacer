# jsonpath-replacer

Utility to replace values in JSON file by JSONPath

## Usage

To replace all parts matched by JSONPATH with REPLACEMENT

```sh
java -jar jsonpath-replacer-0.2.0-standalone.jar -i INPUT_FILE -o OUTPUT_FILE JSONPATH REPLACEMENT
```

- INPUT_FILE is optional. Takes json from standard input if unset.
- OUTPUT_FILE is optional. Prints results to standard output if unset.
- JSONPATH is a jsonpath matching all values that need to be replaced.
- REPLACEMENT is a string that will replace every value that JSONPATH matches.

To simply extract all matches for JSONPATH:

```sh
java -jar jsonpath-replacer-0.2.0-standalone.jar -x -i INPUT_FILE -o OUTPUT_FILE JSONPATH
```


## Full parameter description

| Parameter                 | Flag? | Description                                                             |
|:--------------------------|:------|:------------------------------------------------------------------------|
| `-i`/`--in-file`          | No    | Input file name (stdin by default)                                      |
| `-o`/`--out-file`         | No    | Output file name (stdout by default)                                    |
| `-c/--compact`            | Yes   | If set, output json is compacted                                        |
| `-j`/`--json-replacement` | Yes   | Treat replacement as json instead of plain text                         |
| `-x`/`--extract`          | Yes   | Instead of replacing matched parts, output them. REPLACEMENT is ignored |
| `-h`/`--help`             | Yes   | Print usage info                                                        |
