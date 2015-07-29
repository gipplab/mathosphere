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
		this.setRuns( runs );
		this.xmlns = xmlns;
	}

	public Results( Results results ) {
		this.runs = cloneRuns( results.getRuns() );
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
		this.runs.add( new Run( run ) );
	}

	public void setRuns( List<Run> runs ) {
		this.runs = cloneRuns( runs );
	}

	public List<Run> getRuns() {
		return runs;
	}

	private static List<Run> cloneRuns( List<Run> runs ) {
		if ( runs != null ) {
			final List<Run> out = new ArrayList<>();
			for ( final Run run : runs ) {
				final Run runCopy = new Run( run );
				out.add( runCopy );
			}
			return out;
		} else {
			return null;
		}
	}

	public int getNumRuns() {
		return runs.size();
	}
}
