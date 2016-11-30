EVALUATION FOR MATHEMATICAL IDENTIFIER DESCRIPTION EXTRACTION RESULTS
=====================================================================
Usage
--
 Build the jar and use the CLI

Example usage: 

`java -jar evaluation-0.0.1-SNAPSHOT.jar eval -in extraction.csv -gold gold.json`

The default gold.json file is located in `src/main/resources/formulasearchengine/mlp/gold/gold.json` or can be obtained from https://github.com/TU-Berlin/mathosphere/blob/sigir16/mathosphere-core/src/test/resources/com/formulasearchengine/mathosphere/mlp/gold/gold.json
The data where the gold standard belongs to can be found at https://github.com/TU-Berlin/mathosphere/blob/master/mathosphere-core/src/test/resources/com/formulasearchengine/mathosphere/mlp/gold/eval_dataset.xml 

extraction.csv
--
The format for the extraction.csv is: `qId , Title, Identifier, Definition` either qId or title can be used to identify the queries. qId is the default.

* **qId**
  The query id of the formula the identifier is found in, as found in the gold standard.
* **Title**
  The title of the formula, as found in the gold standard.
* **Identifier**
  The identifier for which the definition(s) were extracted.
* **Definition**
  The proposed definition.
  The evaluator accepts either the plain definition, inner Wikipedia-links  or  Wikidata-links. Inner Wikipedia-links should be in double [. 
  E.g. for the identifier `W` in formula with `qId = 1` either of the following would be accepted as correct definition:
    * Van der Waerden number
    * [[Van der Waerden number]]
    * Q7913892