package com.formulasearchengine.wikitext;

import com.formulasearchengine.mathosphere.mlp.PatternMatchingRelationFinder;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Created by Moritz on 26.10.2015.
 */
public class PageTypeTest {
	final private static String HEAD = "<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.10/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.10/ http://www.mediawiki.org/xml/export-0.10.xsd\" version=\"0.10\" xml:lang=\"en\">\n";
	final private static String FOOTER = "</mediawiki>";
	private PageType page;

	@Before
	public void setUp() throws Exception {
		InputStream stream = PatternMatchingRelationFinder.class.getResourceAsStream("mrrFullHist.xml");
		String s = IOUtils.toString(stream, "utf-8");
		Pattern pattern = Pattern.compile("<page>(.*?)</page>",Pattern.DOTALL);
		Matcher m = pattern.matcher(s);
		m.find();
		s = HEAD + m.group(0) + FOOTER;
		MediaWikiType mw = JAXB.unmarshal(new StringReader(s), MediaWikiType.class);
		page = mw.getPage().get(0);
	}

	@Test
	public void testGetTitle() throws Exception {
		assertEquals("Mean reciprocal rank", page.getTitle());
	}

	@Test
	public void testGetNs() throws Exception {
		assertEquals(new BigInteger("0"), page.getNs());
	}

	@Test
	public void testGetId() throws Exception {
		assertEquals(new BigInteger("11184711"), page.getId());
	}

	@Test
	public void testGetRedirect() throws Exception {
		assertNull(page.getRedirect());
	}

	@Test
	public void testGetRestrictions() throws Exception {
		assertNull(page.getRestrictions());
	}

	@Test
	public void testGetRevisionOrUpload() throws Exception {
		assertEquals(38,page.getRevisionOrUpload().size());
	}

	@Test
	public void testGetLastRevision() throws  Exception {
		assertEquals(new BigInteger("666713905"), page.getLastRevision().getId());
	}

	@Test
	public void testGetDiscussionthreadinginfo() throws Exception {
		assertNull(page.getDiscussionthreadinginfo());
	}
}