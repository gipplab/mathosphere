package de.tuberlin.dima.schubotz.wiki.mappers;

public class WikiMapper extends FlatMapFunction<String, WikiTuple> {
	HashSet<String> latex;
	String STR_SPLIT;
	
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
	@Override
	public void flatMap (String in, Collector<WikiTuple> out) throws Exception {
		//Check for edge cases created from stratosphere split
		if (in.startsWith("<mediawiki")) {
			LOG.debug("Hit mediawiki header document.");
			return;
		}else if (in.startsWith("</mediawiki")) {
			LOG.debug("Hit mediawiki end doc.");
			return;
		}
		if (!in.endsWith("</page>")) {
			in += "</page>";
		}
	}


	

}
