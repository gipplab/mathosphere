
package com.formulasearchengine.wikitext;

import javax.annotation.Generated;
import javax.xml.bind.annotation.*;

import java.math.BigInteger;


/**
 * <p>Java class for ContributorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ContributorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"
 * minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"
 * minOccurs="0"/>
 *         &lt;element name="ip" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="deleted" type="{http://www.mediawiki.org/xml/export-0.10/}DeletedFlagType"
 * />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContributorType", namespace = "http://www.mediawiki.org/xml/export-0.10/", propOrder = {
    "username",
    "id",
    "ip"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class ContributorType {

  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String username;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @XmlSchemaType(name = "nonNegativeInteger")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected BigInteger id;
  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected String ip;
  @XmlAttribute(name = "deleted")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected DeletedFlagType deleted;

  /**
   * Gets the value of the username property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getUsername() {
    return username;
  }

  /**
   * Sets the value of the username property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setUsername(String value) {
    this.username = value;
  }

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
   * Gets the value of the ip property.
   *
   * @return possible object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public String getIp() {
    return ip;
  }

  /**
   * Sets the value of the ip property.
   *
   * @param value allowed object is {@link String }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setIp(String value) {
    this.ip = value;
  }

  /**
   * Gets the value of the deleted property.
   *
   * @return possible object is {@link DeletedFlagType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public DeletedFlagType getDeleted() {
    return deleted;
  }

  /**
   * Sets the value of the deleted property.
   *
   * @param value allowed object is {@link DeletedFlagType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public void setDeleted(DeletedFlagType value) {
    this.deleted = value;
  }

}
