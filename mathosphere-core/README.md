Mathematical Language Processing
================================
[![Build Status](https://travis-ci.org/TU-Berlin/project-mlp.svg?branch=master)](https://travis-ci.org/TU-Berlin/project-mlp)

# Checkout

* if test fail on windows make sure to checkout the files with unix-style line endings. This is most easily done by
`git config --global core.autocrlf false`

* in the git for windows installation process this would be the option #2 Checkout as-is, commit Unix-style line endings Git will not perform any conversion when checking out text files.

# Run
* compile the maven project
* run the jar (mathoshpere-core-3.0.0-SNAPSHOT-jar-with-dependencies)
* try `java -jar mathoshpere-core-3.0.0-SNAPSHOT-jar-with-dependencies` to see the list of possible commands
* try `java -jar mathoshpere-core-3.0.0-SNAPSHOT-jar-with-dependencies list -in *in file* --tex` to extract the identifiers from a wikipedia article
* try `java -jar mathoshpere-core-3.0.0-SNAPSHOT-jar-with-dependencies extract -in *in file* --tex` to extract identifiers and corresponding set of definitions from a wikipedia article
* test data can be found in `mathosphere-core\src\test\resources\com\formulasearchengine\mathosphere\mlp\performance` and `mathosphere-core\src\test\resources\com\formulasearchengine\mathosphere\mlp\gold`

* this project can also be run on a [flink](https://flink.apache.org/) cluster for fast analysis of large text corpora 



