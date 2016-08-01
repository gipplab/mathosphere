## Mathosphere

### About
Mathosphere consists of the following components:
* A baseX ad hock formula search system
* A flink batch processing system
* A rest interface

The baseX backend manages the data used for ad hock retrieval for MathSearch on
Wikipedia or DRMF.
The rest interface provides an interface for ad hock retrieval. The MediaWiki MathSearch
extension serves as a frontend.
The flink batch processing component is used for long running data analysis and batch queries.

### Releases and old versions
Currently, there is no Mathosphere release available. Version 3.0.0 is the first version that is going to be released to the public [![MavenCentral](https://maven-badges.herokuapp.com/maven-central/com.formulasearchengine/mathosphere/badge.svg)](maven-badges.herokuapp.com/maven-central/com.formulasearchengine/matosphere/)

However, the MathML query generator is available from maven central [![MavenCentral](https://maven-badges.herokuapp.com/maven-central/com.formulasearchengine/mathmlquerygenerator/badge.svg)](maven-badges.herokuapp.com/maven-central/com.formulasearchengine/mathmlquerygenerator/)
Note we are using a development version of MathML query generator for this project, under a submodule.

Version 1.0.0-SNAPSHOT is tightly coupled to Stratosphere 0.2.x and was focused on batch formula search.
The code is available from (TU-Berlin/mathosphere-history). The research prototype was build explicitly for the NTCIR-10.
We demonstrate the principle of separating the challenges of handing huge dadaists from principal
question in MIR. See our [Querying large Collections of Mathematical Publications paper](https://www.researchgate.net/publication/259291837_Querying_large_Collections_of_Mathematical_Publications_-NTCIR10_Math_Task).

Version 2.0.0-SNAPSHOT is based on Apache Flink.
This research prototype analysing fundamental factors of formula similarity is build for the NTCIR-11 conference.
See our [paper](http://research.nii.ac.jp/ntcir/workshop/OnlineProceedings11/pdf/NTCIR/Math-2/04-NTCIR11-MATH-SchubotzM.pdf)

We are using the [NTCIR-11 Wikipedia](http://ntcir11-wmc.nii.ac.jp/index.php/NTCIR-11-Math-Wikipedia-Task) dataset
(specifically the augmentedWikiDump.xml from [this host](http://demo.formulasearchengine.com/images/)) for as additional
training dataset.

### Master branch tests
[![Build Status](https://drone.io/github.com/TU-Berlin/mathosphere2/status.png)](https://drone.io/github.com/TU-Berlin/mathosphere2/latest)
<!--
[![Build Status](https://travis-ci.org/TU-Berlin/mathosphere.svg)](https://travis-ci.org/TU-Berlin/mathosphere)
-->
[![Coverage Status](https://coveralls.io/repos/TU-Berlin/mathosphere/badge.svg)](https://coveralls.io/r/TU-Berlin/mathosphere )

### Building this project
Run the following to initialize submodules after cloning this project:
```
git submodule init
git submodule update
```

Run the following to pull latest changes from each submodules' repo
```
git submodule update --remote --merge
```
### Troubleshooting
If test fail due to encoding problems in windows, set the environment variable
```
JAVA_TOOL_OPTIONS = -Dfile.encoding=UTF8
```
as suggested on [stackoverflow.](http://stackoverflow.com/a/28470840)
