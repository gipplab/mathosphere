
package com.formulasearchengine.wikitext;

import javax.annotation.Generated;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;

import java.math.BigInteger;


/**
 * <p>Java class for RevisionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RevisionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="parentid" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"
 * minOccurs="0"/>
 *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="contributor" type="{http://www.mediawiki.org/xml/export-0.10/}ContributorType"/>
 *         &lt;element name="minor" type="{http://www.w3.org/2001/XMLSchema}anyType"
 * minOccurs="0"/>
 *         &lt;element name="comment" type="{http://www.mediawiki.org/xml/export-0.10/}CommentType"
 * minOccurs="0"/>
 *         &lt;element name="model" type="{http://www.mediawiki.org/xml/export-0.10/}ContentModelType"/>
 *         &lt;element name="format" type="{http://www.mediawiki.org/xml/export-0.10/}ContentFormatType"/>
 *         &lt;element name="text" type="{http://www.mediawiki.org/xml/export-0.10/}TextType"/>
 *         &lt;element name="sha1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RevisionType", namespace = "http://www.mediawiki.org/xml/export-0.10/", propOrder = {
    "id",
    "parentid",
    "timestamp",
    "contributor",
    "minor",
    "comment",
    "model",
    "format",
    "text",
    "sha1"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class RevisionType {

  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @XmlSchemaType(name = "positiveInteger")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected BigInteger id;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @XmlSchemaType(name = "positiveInteger")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected BigInteger parentid;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @XmlSchemaType(name = "dateTime")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected XMLGregorianCalendar timestamp;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected ContributorType contributor;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected Object minor;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected CommentType comment;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String model;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String format;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected TextType text;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String sha1;

  /**
   * Gets the value of the id property.
   *
   * @return possible object is {@link BigInteger }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public BigInteger getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   *
   * @param value allowed object is {@link BigInteger }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setId(BigInteger value) {
    this.id = value;
  }

  /**
   * Gets the value of the parentid property.
   *
   * @return possible object is {@link BigInteger }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public BigInteger getParentid() {
    return parentid;
  }

  /**
   * Sets the value of the parentid property.
   *
   * @param value allowed object is {@link BigInteger }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setParentid(BigInteger value) {
    this.parentid = value;
  }

  /**
   * Gets the value of the timestamp property.
   *
   * @return possible object is {@link XMLGregorianCalendar }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public XMLGregorianCalendar getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the value of the timestamp property.
   *
   * @param value allowed object is {@link XMLGregorianCalendar }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setTimestamp(XMLGregorianCalendar value) {
    this.timestamp = value;
  }

  /**
   * Gets the value of the contributor property.
   *
   * @return possible object is {@link ContributorType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public ContributorType getContributor() {
    return contributor;
  }

  /**
   * Sets the value of the contributor property.
   *
   * @param value allowed object is {@link ContributorType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setContributor(ContributorType value) {
    this.contributor = value;
  }

  /**
   * Gets the value of the minor property.
   *
   * @return possible object is {@link Object }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public Object getMinor() {
    return minor;
  }

  /**
   * Sets the value of the minor property.
   *
   * @param value allowed object is {@link Object }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setMinor(Object value) {
    this.minor = value;
  }

  /**
   * Gets the value of the comment property.
   *
   * @return possible object is {@link CommentType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public CommentType getComment() {
    return comment;
  }

  /**
   * Sets the value of the comment property.
   *
   * @param value allowed object is {@link CommentType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setComment(CommentType value) {
    this.comment = value;
  }

  /**
   * Gets the value of the model property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getModel() {
    return model;
  }

  /**
   * Sets the value of the model property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setModel(String value) {
    this.model = value;
  }

  /**
   * Gets the value of the format property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getFormat() {
    return format;
  }

  /**
   * Sets the value of the format property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setFormat(String value) {
    this.format = value;
  }

  /**
   * Gets the value of the text property.
   *
   * @return possible object is {@link TextType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public TextType getText() {
    return text;
  }

  /**
   * Sets the value of the text property.
   *
   * @param value allowed object is {@link TextType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setText(TextType value) {
    this.text = value;
  }

  /**
   * Gets the value of the sha1 property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getSha1() {
    return sha1;
  }

  /**
   * Sets the value of the sha1 property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setSha1(String value) {
    this.sha1 = value;
  }

}
