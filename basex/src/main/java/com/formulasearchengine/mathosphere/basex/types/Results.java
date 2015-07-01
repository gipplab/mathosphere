package com.formulasearchengine.mathosphere.basex.types;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores results in NTCIR format.
 *
 * @author Tobias Uhlich
 * @author Thanh Phuong Luu
 */
@XStreamAlias("results")
public class Results {
	@XStreamImplicit
	private List<Run> runs;

	@XStreamAlias("xmlns")
	@XStreamAsAttribute
	private String xmlns="http://ntcir-math.nii.ac.jp/";

	public Results() {
		this.runs = new ArrayList<>();
		//hack b/c xstream does not support default values
		this.xmlns = xmlns;
	}

	public Results( List<Run> runs ) {
		this.runs = new ArrayList<>( runs );
		this.xmlns = xmlns;
	}

	public void addRun( Run run ) {
		this.runs.add( run );
	}

	public void setRuns( List<Run> runs ) {
		this.runs = new ArrayList<>( runs );
	}

	public List<Run> getRuns() {
		return runs;
	}

	public int getNumRuns() {
		return runs.size();
	}
}
