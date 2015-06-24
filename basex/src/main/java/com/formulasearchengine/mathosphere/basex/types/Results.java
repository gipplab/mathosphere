package com.formulasearchengine.mathosphere.basex.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores results in NTCIR format.
 *
 * @author Tobias Uhlich
 * @author Thanh Phuong Luu
 */
public class Results {
	private List<Run> runs;

	public Results() {
		this.runs = new ArrayList<>();
	}

	public Results( List<Run> runs ) {
		this.runs = new ArrayList<>( runs );
	}

	public void addRun( Run run ) {
		this.runs.add( run );
	}

	public void setRuns( List<Run> runs ) {
		this.runs = runs;
	}

	public List<Run> getRuns() {
		return runs;
	}

	public String toXML() {
		final StringBuilder runsXMLBuilder = new StringBuilder();
		for ( final Run run : runs ) {
			runsXMLBuilder.append( run.toXML() );
		}

		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\">\n"
				+ runsXMLBuilder.toString() + "</results>\n";
	}
}
