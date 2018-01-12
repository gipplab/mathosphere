package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andre Greiner-Petter
 */
public enum ContentType {
    WikiData("<format>(.*?)</format>", "text/x-wiki"),
    HTML("<meta.*?content=\"(.*)?;.*?>", "text/html"),
    UNKNOWN(".","");

    private Pattern pattern;
    private String mime;

    ContentType( String pattern, String mime ){
        this.pattern = Pattern.compile(pattern);
        this.mime = mime;
    }

    public static ContentType getContentTypeByText(String content){
        for ( ContentType ct : ContentType.values() ){
            if ( ct == UNKNOWN ) continue; // skip the unknown tester

            Matcher matcher = ct.pattern.matcher( content );
            if ( matcher.find() && matcher.group(1).equals( ct.mime ) )
                return ct;
        }
        return UNKNOWN;
    }
}
