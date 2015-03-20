fse-ntcir11-wmc-basex
=====================

Code used for the BaseX run of the FSE team at the NTCIR-11 Math Wikipedia Subtask

## Obtain the data
The dataset is availible from

http://demo.formulasearchengine.com/images/NTCIR-11-Wikipedia-Math-MWS-Dump.zip

and can be referenced via DOI 10.13140/2.1.1374.9445

## Obtain the queries
The queries are included in this repository and can be downloaded from

http://demo.formulasearchengine.com/images/NTCIR11-Math-mathQueries-participants.xml

The queries can be referenced via DOI 10.13140/2.1.1618.6564

## Steps performed for the submission
```
mkdir /tmp/baseX
cd /tmp/baseX
git clone https://github.com/TU-Berlin/fse-ntcir11-wmc-basex .
mvn install
mkdir data
cd data
wget http://demo.formulasearchengine.com/images/NTCIR-11-Wikipedia-Math-MWS-Dump.zip
unzip NTCIR-11-Wikipedia-Math-MWS-Dump.zip
cd ..
wget http://demo.formulasearchengine.com/images/NTCIR11-Math-mathQueries-participants.xml
cp target/basex-backend-0.0.1-SNAPSHOT-jar-with-dependencies.jar .
time java -Xmx12G -jar basex-backend-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d /tmp/baseX/data -q NTCIR11-Math-mathQueries-participants.xml -o ./results.csv -c
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