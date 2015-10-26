
package com.formulasearchengine.wikitext;

import javax.annotation.Generated;
import javax.xml.bind.annotation.*;
import java.math.BigInteger;


/**
 * <p>Java class for DiscussionThreadingInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DiscussionThreadingInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ThreadSubject" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ThreadParent" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="ThreadAncestor" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="ThreadPage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ThreadID" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="ThreadAuthor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ThreadEditStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ThreadType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DiscussionThreadingInfo", namespace = "http://www.mediawiki.org/xml/export-0.10/", propOrder = {
    "threadSubject",
    "threadParent",
    "threadAncestor",
    "threadPage",
    "threadID",
    "threadAuthor",
    "threadEditStatus",
    "threadType"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class DiscussionThreadingInfo {

    @XmlElement(name = "ThreadSubject", namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String threadSubject;
    @XmlElement(name = "ThreadParent", namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @XmlSchemaType(name = "positiveInteger")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected BigInteger threadParent;
    @XmlElement(name = "ThreadAncestor", namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @XmlSchemaType(name = "positiveInteger")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected BigInteger threadAncestor;
    @XmlElement(name = "ThreadPage", namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String threadPage;
    @XmlElement(name = "ThreadID", namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @XmlSchemaType(name = "positiveInteger")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected BigInteger threadID;
    @XmlElement(name = "ThreadAuthor", namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String threadAuthor;
    @XmlElement(name = "ThreadEditStatus", namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String threadEditStatus;
    @XmlElement(name = "ThreadType", namespace = "http://www.mediawiki.org/xml/export-0.10/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String threadType;

    /**
     * Gets the value of the threadSubject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getThreadSubject() {
        return threadSubject;
    }

    /**
     * Sets the value of the threadSubject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setThreadSubject(String value) {
        this.threadSubject = value;
    }

    /**
     * Gets the value of the threadParent property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public BigInteger getThreadParent() {
        return threadParent;
    }

    /**
     * Sets the value of the threadParent property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setThreadParent(BigInteger value) {
        this.threadParent = value;
    }

    /**
     * Gets the value of the threadAncestor property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public BigInteger getThreadAncestor() {
        return threadAncestor;
    }

    /**
     * Sets the value of the threadAncestor property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setThreadAncestor(BigInteger value) {
        this.threadAncestor = value;
    }

    /**
     * Gets the value of the threadPage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getThreadPage() {
        return threadPage;
    }

    /**
     * Sets the value of the threadPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setThreadPage(String value) {
        this.threadPage = value;
    }

    /**
     * Gets the value of the threadID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public BigInteger getThreadID() {
        return threadID;
    }

    /**
     * Sets the value of the threadID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setThreadID(BigInteger value) {
        this.threadID = value;
    }

    /**
     * Gets the value of the threadAuthor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getThreadAuthor() {
        return threadAuthor;
    }

    /**
     * Sets the value of the threadAuthor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setThreadAuthor(String value) {
        this.threadAuthor = value;
    }

    /**
     * Gets the value of the threadEditStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getThreadEditStatus() {
        return threadEditStatus;
    }

    /**
     * Sets the value of the threadEditStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setThreadEditStatus(String value) {
        this.threadEditStatus = value;
    }

    /**
     * Gets the value of the threadType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getThreadType() {
        return threadType;
    }

    /**
     * Sets the value of the threadType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-10-26T02:56:58+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setThreadType(String value) {
        this.threadType = value;
    }

}
