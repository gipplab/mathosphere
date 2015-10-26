
package com.formulasearchengine.wikitext;

import javax.annotation.Generated;
import javax.xml.bind.annotation.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for PageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ns" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="redirect" type="{http://www.mediawiki.org/xml/export-0.10/}RedirectType" minOccurs="0"/>
 *         &lt;element name="restrictions" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="revision" type="{http://www.mediawiki.org/xml/export-0.10/}RevisionType"/>
 *           &lt;element name="upload" type="{http://www.mediawiki.org/xml/export-0.10/}UploadType"/>
 *         &lt;/choice>
 *         &lt;element name="discussionthreadinginfo" type="{http://www.mediawiki.org/xml/export-0.10/}DiscussionThreadingInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PageType", namespace = "http://www.mediawiki.org/xml/export-0.10/", propOrder = {
    "title",
    "ns",
    "id",
    "redirect",
    "restrictions",
    "revisionOrUpload",
    "discussionthreadinginfo"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class PageType {

    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String title;
    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected BigInteger ns;
    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @XmlSchemaType(name = "positiveInteger")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected BigInteger id;
    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected RedirectType redirect;
    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String restrictions;
    @XmlElements({
        @XmlElement(name = "revision", namespace = "http://www.mediawiki.org/xml/export-0.10/", type = RevisionType.class),
        @XmlElement(name = "upload", namespace = "http://www.mediawiki.org/xml/export-0.10/", type = UploadType.class)
    })
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected List<Object> revisionOrUpload;
    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected DiscussionThreadingInfo discussionthreadinginfo;

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the ns property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public BigInteger getNs() {
        return ns;
    }

    /**
     * Sets the value of the ns property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setNs(BigInteger value) {
        this.ns = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setId(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the redirect property.
     * 
     * @return
     *     possible object is
     *     {@link RedirectType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public RedirectType getRedirect() {
        return redirect;
    }

    /**
     * Sets the value of the redirect property.
     * 
     * @param value
     *     allowed object is
     *     {@link RedirectType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setRedirect(RedirectType value) {
        this.redirect = value;
    }

    /**
     * Gets the value of the restrictions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getRestrictions() {
        return restrictions;
    }

    /**
     * Sets the value of the restrictions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setRestrictions(String value) {
        this.restrictions = value;
    }

    /**
     * Gets the value of the revisionOrUpload property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the revisionOrUpload property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRevisionOrUpload().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RevisionType }
     * {@link UploadType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public List<Object> getRevisionOrUpload() {
        if (revisionOrUpload == null) {
            revisionOrUpload = new ArrayList<Object>();
        }
        return this.revisionOrUpload;
    }

    /**
     * Gets the value of the discussionthreadinginfo property.
     * 
     * @return
     *     possible object is
     *     {@link DiscussionThreadingInfo }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public DiscussionThreadingInfo getDiscussionthreadinginfo() {
        return discussionthreadinginfo;
    }

    /**
     * Sets the value of the discussionthreadinginfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscussionThreadingInfo }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setDiscussionthreadinginfo(DiscussionThreadingInfo value) {
        this.discussionthreadinginfo = value;
    }

}
