package com.formulasearchengine.mathosphere.basex.types;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

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

	@XStreamOmitField
	private boolean showTime = true;

	public Results() {
		this.runs = new ArrayList<>();
		//hack b/c xstream does not support default values
		this.xmlns = xmlns;
	}

	public Results( List<Run> runs ) {
		this.runs = new ArrayList<>( runs );
		this.xmlns = xmlns;
	}

	public void setShowTime( boolean showTime ) {
		this.showTime = showTime;
		if ( runs != null ) {
			for ( final Run run : runs ) {
				run.setShowTime( showTime );
			}
		}
	}

	public boolean getShowTime() {
		return this.showTime;
	}

	public void addRun( Run run ) {
		run.setShowTime( showTime );
		this.runs.add( run );
	}

	public void setRuns( List<Run> runs ) {
		this.runs = new ArrayList<>( runs );
		for ( final Run run : runs ) {
			run.setShowTime( showTime );
		}
	}

	public List<Run> getRuns() {
		return runs;
	}

	public int getNumRuns() {
		return runs.size();
	}
}
