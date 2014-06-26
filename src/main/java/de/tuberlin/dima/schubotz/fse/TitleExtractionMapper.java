package de.tuberlin.dima.schubotz.fse;


import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.tuple.Tuple4;
import eu.stratosphere.types.StringValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleExtractionMapper extends MapFunction<String, Tuple4<String, Integer, Integer, StringValue>> {
	/**
	 * The Constant FILENAME_INDICATOR.
	 */
	final static String FILENAME_INDICATOR = "Filename";
	final static Pattern filnamePattern = Pattern
		.compile( "<ARXIVFILESPLIT\\\\n" + FILENAME_INDICATOR + "=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">" );
	private final Tuple4<String, Integer, Integer, StringValue> result = new Tuple4<>();

	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param value The input value.
	 * @return The value produced by the map function from the input value.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public Tuple4<String, Integer, Integer, StringValue> map (String value) throws Exception {
		SectionTuple sectionTuple = new SectionTuple();
		//Split into lines 0: ARXIVFILENAME, 1: HTML
		String[] lines = value.trim().split( "\\n", 2 );
		if ( lines.length < 2 ){
			System.out.println("ignoring record without Head row");
			return new Tuple4<>(null,0,0,new StringValue( "" )  );
		}

		Matcher matcher = filnamePattern.matcher( lines[0] );
		if ( matcher.find() ) {
			//System.out.println("Found correct record"+matcher.group( 0 ));
			result.f0 = matcher.group( 1 );
			result.f1 = Integer.parseInt( matcher.group( 2 ) );
			result.f2 = Integer.parseInt( matcher.group( 3 ) );
			result.f3 = new StringValue( lines[1] );
			return result;
		} else {
			System.out.println("ignoring record without accurate filename info");
			return new Tuple4<>(  );
		}
	}
}
