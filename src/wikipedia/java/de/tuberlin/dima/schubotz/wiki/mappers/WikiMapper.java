package de.tuberlin.dima.schubotz.wiki.mappers;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import de.tuberlin.dima.schubotz.common.utils.ExtractHelper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

@SuppressWarnings("serial")
public class WikiMapper extends FlatMapFunction<String, WikiTuple> {
	/**
	 * See {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#STR_SPLIT}
	 */
	String STR_SPLIT;
	Log LOG = LogFactory.getLog(WikiMapper.class);
	
	/**
	 * @param {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#STR_SPLIT} passed in to ensure serializability
	 */
	@SuppressWarnings("hiding")
	public WikiMapper (String STR_SPLIT) {
		this.STR_SPLIT = STR_SPLIT;
	}

	/**
	 * Takes in wiki string, parses wikiID and latex
	 */
	@Override
	public void flatMap (String in, Collector<WikiTuple> out) throws Exception {
		Document doc;
		
		try {
			doc = Jsoup.parse(in, "", Parser.xmlParser()); //using jsoup b/c wiki html is invalid, also handles entities
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unable to parse XML in wiki: " + in);
			}
			return;
		}
	    String docID;
        if (doc.select("title").first() == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not find title tag, assigning this_was_null: " + in);
            }
            docID = "this_was_null";
        } else {
            docID = doc.select("title").first().text();
        }
		if (docID == null) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("docID was null, assigning this_was_null: " + in);
			}
			docID = "this_was_null";
		}
		
		Elements MathElements = doc.select("math");
		if (MathElements == null) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unable to find math tags: " + in);
			}
			return;
		}

        StringBuilder latex,cmml,pmml;
        latex = cmml = pmml = new StringBuilder();
		for (Element MathElement : MathElements) {
            //Assume only one root element and that it is <semantic>
            Element SemanticElement = null;
            try {
                SemanticElement = MathElement.child(0);
            } catch (NullPointerException e) {
                if (LOG.isWarnEnabled()) {
					LOG.warn("Unable to find semantics elements: " + in); //TODO check if this is common
				}
                return;
            }
            if (MathElement.children().size() > 1) {
                if (LOG.isWarnEnabled()) {
					LOG.warn("Multiple elements under math: " + in); //TODO check if this is common
				}
            }
			Elements MMLElements = SemanticElement.children();
			
			Elements PmmlElements = new Elements(); //how are we handling multiple math tags per wiki?
			Elements CmmlElements = new Elements();
            Elements LatexElements = new Elements();
            String encoding = "";
			//All data is well formed: 1) Presentation MML 2) annotation-CMML 3) annotation-TEX
		    //Any element not under annotation tag is Presentation MML
		    for (Element curElement : MMLElements) {
                encoding = curElement.attr("encoding"); //TODO is this necessary?
                try {
                    if (curElement.tagName().equals("annotation-xml")) {
                        if (encoding.equals("MathML-Content")) {
                            //add namespace information
                            //Again, always assuming one root element per MathML type
                            //E.g. there will never be two <mrow> elements under <annotation-xml>
                            curElement.child(0).attr("xmlns:m", "http://www.w3.org/1998/Math/MathML"); //add namespace information
                            CmmlElements.add(curElement.child(0));
                        }
                    } else if (curElement.tagName().equals("annotation")) {
                        if (encoding.equals("application/x-tex")) {
                            curElement.attr("xmlns:m", "http://www.w3.org/1998/Math/MathML"); //add namespace information
                            LatexElements.add(curElement); //keep annotation tags
                        }
                    } else {
                        curElement.attr("xmlns:m", "http://www.w3.org/1998/Math/MathML");
                        PmmlElements.add(curElement);
                    }
                } catch (Exception e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Badly formatted wiki xml: " + in);
                    }
                    return;
                }
			}
            String curLatex,curCmml,curPmml;
            curLatex = curCmml = curPmml = null;
            try {
                curLatex = ExtractHelper.extractLatex(LatexElements, STR_SPLIT);
			    curCmml = ExtractHelper.extractCanonicalizedDoc(CmmlElements);
			    curPmml = ExtractHelper.extractCanonicalizedDoc(PmmlElements);
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Extraction/canonicalization failed. Exiting: " + in);
                }
                return;
            }

            if (curLatex == null || curCmml == null || curPmml == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Extract helper failed on wiki or wiki has no math: " + in);
                }
                return;
            } else{
                latex.append(curLatex);
                cmml.append(curCmml);
                pmml.append(curPmml);
            }
		} //End loop of MathElement : MathElements

		out.collect(new WikiTuple(docID, latex.toString(), cmml.toString(), pmml.toString()));
	}
}
