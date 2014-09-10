## Build instructions

```bash
git submodule init
git submodule update
mvn install
```
[![Build Status](https://drone.io/github.com/TU-Berlin/mathosphere2/status.png)](https://drone.io/github.com/TU-Berlin/mathosphere2/latest)
## Run instructions
If accessing a remote database, link with the following command before executing:
```
ssh -L [localport]:localhost:[serverport] [server]
```

Running the program:
```
stratosphere run -j math-tests.jar -a "[algorithm module name] [input module name] [ARGUMENTS]"

preprocessor on mlp server:
stratosphere run -j math-tests.jar -a "RawToPreprocessed ArxivRawInput -NUM_SUB_TASKS 8 -RUNTAG septthird -DATARAW_FILE file:///mnt/ntcir-math/testdata/gold.xml -OUTPUT_DIR file:///home/user/preprocessOUT/ -QUERY_FILE file:///mnt/ntcir-math/queries/fQuery.xml"

```

If any arguments are missing, the program will print out the first argument's letter and then exit.

List of arguments:
```
Raw data input required arguments
-DATARAW_FILE Path to raw data file (specify arxiv or wiki with the correct input module)
-QUERY_FILE Path to raw query file 
-RUNTAG Name for runtag
-NUM_SUB_TASKS Parellelization number
-OUTPUT_DIR Output directory

Preprocessed input required arguments
-NUM_DOC Document total
-LATEX_DOCS_MAP Path to latex counts file
-KEYWORD_DOCS_MAP Path to keyword counts file
-DATATUPLE_FILE Path to tuple data file
-QUERYTUPLE_FILE Path to tuple query file
```
