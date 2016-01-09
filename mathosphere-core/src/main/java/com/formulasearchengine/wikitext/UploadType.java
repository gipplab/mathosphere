
package com.formulasearchengine.wikitext;

import javax.annotation.Generated;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;

import java.math.BigInteger;


/**
 * <p>Java class for UploadType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="UploadType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="contributor" type="{http://www.mediawiki.org/xml/export-0.10/}ContributorType"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"
 * minOccurs="0"/>
 *         &lt;element name="filename" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="src" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UploadType", namespace = "http://www.mediawiki.org/xml/export-0.10/", propOrder = {
    "timestamp",
    "contributor",
    "comment",
    "filename",
    "src",
    "size"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class UploadType {

  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @XmlSchemaType(name = "dateTime")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected XMLGregorianCalendar timestamp;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected ContributorType contributor;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String comment;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String filename;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @XmlSchemaType(name = "anyURI")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String src;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
  @XmlSchemaType(name = "positiveInteger")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected BigInteger size;

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
   * Gets the value of the comment property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getComment() {
    return comment;
  }

  /**
   * Sets the value of the comment property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setComment(String value) {
    this.comment = value;
  }

  /**
   * Gets the value of the filename property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getFilename() {
    return filename;
  }

  /**
   * Sets the value of the filename property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setFilename(String value) {
    this.filename = value;
  }

  /**
   * Gets the value of the src property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getSrc() {
    return src;
  }

  /**
   * Sets the value of the src property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setSrc(String value) {
    this.src = value;
  }

  /**
   * Gets the value of the size property.
   *
   * @return possible object is {@link BigInteger }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public BigInteger getSize() {
    return size;
  }

  /**
   * Sets the value of the size property.
   *
   * @param value allowed object is {@link BigInteger }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setSize(BigInteger value) {
    this.size = value;
  }

}
