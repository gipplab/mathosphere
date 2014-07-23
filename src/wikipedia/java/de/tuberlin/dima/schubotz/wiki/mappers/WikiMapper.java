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
	 * Hashset of all query latex, taken from broadcast variable
	 */
	HashSet<String> latex;
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

	@Override
	public void open(Configuration parameters) {
		latex = new HashSet<String>();
		Collection<WikiQueryTuple> queries = getRuntimeContext().getBroadcastVariable( "Queries" );
		for (WikiQueryTuple query : queries) {
			String[] tokens = query.getLatex().split(STR_SPLIT); //get list of latex
			for ( String token : tokens ) {
				if (!token.equals("")) {
					latex.add(token);
				}
			}
		}
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
				LOG.warn("Jsoup was unable to parse: " + in);
			}
			return;
		}
		
		String docID = doc.select("title").first().text();
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

		for (Element MathElement : MathElements) {
            //Assume only one root element and that it is semantic
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
			Elements RenderedElements = new Elements();
			boolean hitAnnotation = false; //flag for seeing if hit annotation tags yet
			for (Element curElement : MMLElements) { 
				if (curElement.tagName().equals("m:annotation-xml")) {
					hitAnnotation = true;
					String encoding = curElement.attr("encoding");
					if (encoding.equals("MathML-Presentation")) {
						PmmlElements.addAll(curElement.children());
					} else if (encoding.equals("MathML-Content")) {
						CmmlElements.addAll(curElement.children());
					}
				} else { //hit non annotation tag, check if well-formed 
					if (hitAnnotation) { 
						if (LOG.isWarnEnabled()) { //unexpected, log so that we can improve on this
							LOG.warn("Tags appear after annotation, ignoring: " + in);
						}
					}else {
						RenderedElements.add(curElement);
					}
				}
			}
		}
			
		Elements LatexElements = MathElements.select("annotation[encoding=application/x-tex]");
		String wikiLatex = ExtractHelper.extractLatex(LatexElements, STR_SPLIT);
		
		out.collect(new WikiTuple(docID, wikiLatex, null, null));
	}
}
