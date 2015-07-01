package com.formulasearchengine.mathosphere.basex.types;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Stores qvar in Ntcir format.
 * Created by jjl4 on 6/24/15.
 */
@XStreamAlias("qvar")
public class Qvar {
	@XStreamAlias("for")
	@XStreamAsAttribute
	private final String queryQvarID;

	@XStreamAlias("xref")
	@XStreamAsAttribute
	private final String qvarID;

	public Qvar( String queryQvarID, String qvarID ) {
		this.queryQvarID = queryQvarID;
		this.qvarID = qvarID;
	}
}
