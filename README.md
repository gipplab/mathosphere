## Build instructions

```bash
cd lib/MathMLCan
mvn instlall
cd ../..
mvn install
```

TODO: find a way how to automate that. See #26


## Run instructions

```bash
stratosphere run [algorithm module name] [input module name] [ARGUMENTS]
```

If any arguments are missing, the program will print out the first argument's letter and then exit.

List of arguments:
```
Raw data input required arguments
-d Path to raw data file (specify arxiv or wiki with the correct input module)
-q Path to raw query file 
-r Name for runtag
-p Parellelization number
-o Output directory

Preprocessed input required arguments
-n Document total
-l Path to latex counts file
-k Path to keyword counts file
-e Path to tuple data file
-qt Path to tuple query file
```
