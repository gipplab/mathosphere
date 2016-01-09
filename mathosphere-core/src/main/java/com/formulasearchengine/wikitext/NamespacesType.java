
package com.formulasearchengine.wikitext;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for NamespacesType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="NamespacesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="namespace" type="{http://www.mediawiki.org/xml/export-0.10/}NamespaceType"
 * maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamespacesType", namespace = "http://www.mediawiki.org/xml/export-0.10/", propOrder = {
    "namespace"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class NamespacesType {

  @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  protected List<NamespaceType> namespace;

  /**
   * Gets the value of the namespace property.
   *
   * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the namespace property.
   *
   * <p> For example, to add a new item, do as follows:
   * <pre>
   *    getNamespace().add(newItem);
   * </pre>
   *
   *
   * <p> Objects of the following type(s) are allowed in the list {@link NamespaceType }
   */
  @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
  public List<NamespaceType> getNamespace() {
    if (namespace == null) {
      namespace = new ArrayList<NamespaceType>();
    }
    return this.namespace;
  }

}
