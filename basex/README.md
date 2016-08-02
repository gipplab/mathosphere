fse-ntcir11-wmc-basex
=====================

Code used for the BaseX run of the FSE team at the NTCIR-11 Math Wikipedia Subtask

## Requirements
The [Mathosphere](https://github.com/TU-Berlin/mathosphere) project must be cloned to your repository and its submodules
must be initialized.

## Obtain the data
The dataset is available from

https://www.researchgate.net/publication/267980833_NTCIR_11_Math_Wikipedia_Subtask_Dataset_MathWebSearch_harvest

and can be referenced via DOI 10.13140/2.1.1374.9445

## Obtain the queries
The queries are included in this repository and can be downloaded from

https://www.researchgate.net/publication/267979610_NTCIR_11_Math_Wikipedia_Subtask_Topics

The queries can be referenced via DOI 10.13140/2.1.1618.6564

## Steps performed for the submission
```
mkdir /tmp/baseX
cd /tmp/baseX
git clone https://github.com/TU-Berlin/mathosphere . --recursive
mvn install
cd basex
mkdir data
cd data
wget https://github.com/TU-Berlin/mathosphere/releases/download/ntcir-11/NTCIR-11-Wikipedia-Math-MWS-Dump.zip
unzip NTCIR-11-Wikipedia-Math-MWS-Dump.zip
cd ..
wget https://github.com/TU-Berlin/mathosphere/releases/download/ntcir-11/NTCIR11-Math2-queries-participants.xml
cp target/basex-backend-0.0.1-SNAPSHOT-jar-with-dependencies.jar .
time java -Xmx12G -jar basex-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d /tmp/baseX/data -q NTCIR11-Math2-queries-participants.xml -o ./results.xml
```

* Overall runtime 8min 24seconds.
* Submitted to http://ntcir11-wmc.nii.ac.jp/index.php/Special:MathUpload as run 94.
* Up to 96% correct.
* 92% according to the new formula centric match.
* No errors during import.

MathSearchBackend baseX
=======================

[![Build Status](https://travis-ci.org/physikerwelt/mathsearch-backend-basex.svg)](https://travis-ci.org/physikerwelt/mathsearch-backend-basex)

[![MavenCentral](https://maven-badges.herokuapp.com/maven-central/com.formulasearchengine.mathsearch.backend/basex/badge.svg)](maven-badges.herokuapp.com/maven-central/com.formulasearchengine.mathsearch.backend/basex/)

[![Coverage Status](https://coveralls.io/repos/physikerwelt/mathsearch-backend-basex/badge.svg)](https://coveralls.io/r/physikerwelt/mathsearch-backend-basex )
