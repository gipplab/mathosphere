package de.tuberlin.dima.schubotz.fse;


import java.util.Map;

public class Query {
	public String name;
	//keywordid: keyword
	public Map<String, String> keywords;
	//formulaid: mathml
	public Map<String, String> formulae;

	@Override
	public String toString () {
		return name;
	}
}
