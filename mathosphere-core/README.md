Mathematical Language Processing
================================
[![Build Status](https://travis-ci.org/TU-Berlin/project-mlp.svg?branch=master)](https://travis-ci.org/TU-Berlin/project-mlp)

# Run
* compile the maven project
* run the jar (mathoshpere-core-3.0.0-SNAPSHOT-jar-with-dependencies)
* try `java -jar mathoshpere-core-3.0.0-SNAPSHOT-jar-with-dependencies` to see the list of possible commands
* try `java -jar mathoshpere-core-3.0.0-SNAPSHOT-jar-with-dependencies list -in *in file* --tex` to extract the identifiers from a wikipedia article
* try `java -jar mathoshpere-core-3.0.0-SNAPSHOT-jar-with-dependencies extract -in *in file* --tex` to extract identifiers and corresponding set of definitions from a wikipedia article
* test data can be found in `mathosphere-core\src\test\resources\com\formulasearchengine\mathosphere\mlp\performance` and `mathosphere-core\src\test\resources\com\formulasearchengine\mathosphere\mlp\gold`

* this project can also be run on a [flink](https://flink.apache.org/) cluster for fast analysis of large text corpora 



How to use the machine learning classifier
==========================================

# train & test
* use test data from the [eval_dataset.xml](https://github.com/TU-Berlin/mathosphere/blob/master/mathosphere-core/src/test/resources/com/formulasearchengine/mathosphere/mlp/gold/eval_dataset.xml) and [gold.json](https://github.com/TU-Berlin/mathosphere/blob/master/mathosphere-core/src/test/resources/com/formulasearchengine/mathosphere/mlp/gold/gold.json) 
* `java -jar mathosphere-core-3.0.0-SNAPSHOT-jar-with-dependencies.jar ml -in eval_dataset.xml -out . --goldFile gold.json --tex --threads 10 --writeSvmModel --svmGamma 0.0185881361 --svmCost 1.0`
* this will yield a model and a string2vector filter which can be used to classify instances and detailed statistics of the 10-fold cross evaluation that precedes the training process. 
* if you provide several values for cost and gamma all possible combinations will be trained and tested (i.e. `--svmGamma 0.018 --svmGamma 0.019 --svmCost 1.0 --svmCost 2.0`). This can be used to find optimal svm parameters.

* for faster tex extraction it is advisable to install [mathoid](https://www.mediawiki.org/wiki/Mathoid) locally and use the `--texvcinfo` parameter, e.g. add `--texvcinfo http://localhost:10044/texvcinfo` to the execution parameters.

# classify
* assuming yor machine has n cores, we advise you use n threads.
* `java -jar mathosphere-core-3.0.0-SNAPSHOT-jar-with-dependencies.jar classify -in *wikipedia dump*.xml -out . --tex --threads 10 --stringFilter string_filter_c_1.0_gamma_0.0185881361.model --svmModel svm_model_c_1.0_gamma_0.0185881361.model`
* The result of this will be a folder called `extractedDefiniens/json` with *number of threads* files containing he classifications in json format.
Remarks:
* eval_dataset.xml can be used to test the classification, but that is circular reasoning.
* Always use a string2vector filters and a model that were trained in the same run, the results are otherwise undefined.
* As before it is advisable to use a local mathoid instance.