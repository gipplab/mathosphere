package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.flink.util.Collector;

import java.util.regex.Pattern;

public class DataPreprocess extends DataPreprocessTemplate<RawDataTuple,DataTuple> {
	private final Pattern WORD_SPLIT;
	private final String STR_SPLIT;

	/**
	 * Empty constructor (for testing)
	 */
	public DataPreprocess(){
		this.WORD_SPLIT= MainProgram.WORD_SPLIT;
		this.STR_SPLIT = MainProgram.STR_SEPARATOR;
	}

	public DataPreprocess(Pattern WORD_SPLIT, String STR_SPLIT) {
		this.WORD_SPLIT = WORD_SPLIT;
		this.STR_SPLIT = STR_SPLIT;
	}

    /**
     * Takes in cleaned document, outputs tuple
     * containing all document data extracted.
     * @param in RawDataTuple
     * @param out DataTuple of document
     */
	public void flatMap(RawDataTuple in, Collector<DataTuple> out) {
        docID = in.getNamedField(RawDataTuple.fields.ID);
        data = in.getNamedField(RawDataTuple.fields.rawData);

		if ( ! setDoc( ) || ! setMath(  ) ){
			return;
		}


//        final StringBuilder outputLatex = new StringBuilder();
//        final StringBuilder cmml = new StringBuilder();
//        final StringBuilder pmml = new StringBuilder();
//        for (final Element MathElement : mathElements ) {
//            ExtractHelper.processMathElement(MathElement, docID, outputLatex, cmml, pmml, STR_SPLIT);
//        }
//
//        //Delete all math so they don't show up in keyword list
//        mathElements.remove();
//
//        // Keyword list generation
//        // WARNING: THIS IS RELIANT ON ALL NON RELEVANT TEXT (SUCH AS
//        // TITLE TEXT) BEING STRIPPED ALREADY
//        final String plainText = doc.text();
//        final StringBuilder keywordList = new StringBuilder();
//        final String[] tokens = WORD_SPLIT.split(plainText.toLowerCase());
// 		for (final String token : tokens) {
//            ExtractHelper.appendSeparator(keywordList, token, STR_SPLIT);
//        }
//
//        //Stringify then output
//        final String outputLatexStr = outputLatex.toString();
//        final String cmmlStr = cmml.toString();
//        final String pmmlStr = pmml.toString();
//        if (outputLatexStr.isEmpty() && cmmlStr.isEmpty() && pmmlStr.isEmpty()) {
//            LOG.info(docID, " has no math.");
//        } else {
//            out.collect(new DataTuple(
//                    docID, outputLatex.toString(), keywordList.toString(), cmml.toString(), pmml.toString()));
//        }
//        LOG.info(docID, " complete!");
	}

}
