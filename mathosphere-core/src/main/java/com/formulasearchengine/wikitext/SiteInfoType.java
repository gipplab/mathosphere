
package com.formulasearchengine.wikitext;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SiteInfoType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SiteInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sitename" type="{http://www.w3.org/2001/XMLSchema}string"
 * minOccurs="0"/>
 *         &lt;element name="dbname" type="{http://www.w3.org/2001/XMLSchema}string"
 * minOccurs="0"/>
 *         &lt;element name="base" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="generator" type="{http://www.w3.org/2001/XMLSchema}string"
 * minOccurs="0"/>
 *         &lt;element name="case" type="{http://www.mediawiki.org/xml/export-0.10/}CaseType"
 * minOccurs="0"/>
 *         &lt;element name="namespaces" type="{http://www.mediawiki.org/xml/export-0.10/}NamespacesType"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SiteInfoType", namespace = "http://www.mediawiki.org/xml/export-0.10/", propOrder = {
    "sitename",
    "dbname",
    "base",
    "generator",
    "_case",
    "namespaces"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class SiteInfoType {

  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String sitename;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String dbname;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @XmlSchemaType(name = "anyURI")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String base;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String generator;
  @XmlElement(name = "case", namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @XmlSchemaType(name = "NMTOKEN")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected CaseType _case;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected NamespacesType namespaces;

  /**
   * Gets the value of the sitename property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getSitename() {
    return sitename;
  }

  /**
   * Sets the value of the sitename property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setSitename(String value) {
    this.sitename = value;
  }

  /**
   * Gets the value of the dbname property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getDbname() {
    return dbname;
  }

  /**
   * Sets the value of the dbname property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setDbname(String value) {
    this.dbname = value;
  }

  /**
   * Gets the value of the base property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getBase() {
    return base;
  }

  /**
   * Sets the value of the base property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setBase(String value) {
    this.base = value;
  }

  /**
   * Gets the value of the generator property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getGenerator() {
    return generator;
  }

  /**
   * Sets the value of the generator property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setGenerator(String value) {
    this.generator = value;
  }

  /**
   * Gets the value of the case property.
   *
   * @return possible object is {@link CaseType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public CaseType getCase() {
    return _case;
  }

  /**
   * Sets the value of the case property.
   *
   * @param value allowed object is {@link CaseType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setCase(CaseType value) {
    this._case = value;
  }

  /**
   * Gets the value of the namespaces property.
   *
   * @return possible object is {@link NamespacesType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public NamespacesType getNamespaces() {
    return namespaces;
  }

  /**
   * Sets the value of the namespaces property.
   *
   * @param value allowed object is {@link NamespacesType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setNamespaces(NamespacesType value) {
    this.namespaces = value;
  }

}
