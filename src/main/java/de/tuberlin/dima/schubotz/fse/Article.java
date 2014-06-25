package de.tuberlin.dima.schubotz.fse;

import java.util.Map;
import java.util.regex.Pattern;

public class Article {
	public String name;
	public String content;
	private static final Pattern html = Pattern.compile("<html.+html>", Pattern.DOTALL); 
	
	public Article(String name, String raw) {
		this.name = name;
		String formatted = html.matcher(raw).group(0);
		this.content = formatted;
	}
	
	
	
	@Override
	public String toString() {
		return name;
	}

}
