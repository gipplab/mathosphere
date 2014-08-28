package de.tuberlin.dima.schubotz.common.utils;

import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Created by mas9 on 8/28/14.
 */
public class CMMLInfo {

    private final Document cmmlDoc;

    public CMMLInfo( Document cmml ){
        cmmlDoc = cmml;
    }

    public CMMLInfo( String cmml ) throws IOException, ParserConfigurationException {
        cmmlDoc = XMLHelper.String2Doc(cmml, false);
    }

    public String toString(){
        try {
            return XMLHelper.printDocument(cmmlDoc);
        } catch (Exception e) {
            e.printStackTrace();
            return "cmml not printable";
        }
    }
}
