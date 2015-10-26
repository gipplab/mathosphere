
package com.formulasearchengine.wikitext;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CaseType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CaseType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="first-letter"/>
 *     &lt;enumeration value="case-sensitive"/>
 *     &lt;enumeration value="case-insensitive"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CaseType", namespace = "http://www.mediawiki.org/xml/export-0.10/")
@XmlEnum
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public enum CaseType {

    @XmlEnumValue("first-letter")
    FIRST_LETTER("first-letter"),
    @XmlEnumValue("case-sensitive")
    CASE_SENSITIVE("case-sensitive"),
    @XmlEnumValue("case-insensitive")
    CASE_INSENSITIVE("case-insensitive");
    private final String value;

    CaseType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CaseType fromValue(String v) {
        for (CaseType c: CaseType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
