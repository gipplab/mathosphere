package com.formulasearchengine.wikitext;

import com.formulasearchengine.mathosphere.mlp.PatternMatchingRelationFinder;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by Moritz on 26.10.2015.
 */
public class MediaWikiTypeTest {
  private MediaWikiType mw;

  @Before
  public void setUp() throws Exception {
    InputStream s = PatternMatchingRelationFinder.class.getResourceAsStream("mrrFullHist.xml");
    mw = JAXB.unmarshal(s, MediaWikiType.class);
  }

  @Test
  public void testGetSiteinfo() throws Exception {
    SiteInfoType info = mw.getSiteinfo();
    assertEquals(info.getSitename(), "Wikipedia");
  }

  @Test
  public void testGetPage() throws Exception {
    for (PageType pageType : mw.getPage()) {
      assertEquals("Mean reciprocal rank", pageType.getTitle());
    }
  }

  @Test
  public void testGetLogitem() throws Exception {
    assertEquals(0, mw.getLogitem().size());
  }

  @Test
  public void testGetVersion() throws Exception {
    assertEquals("0.10", mw.getVersion());
  }

  @Test
  public void testGetLang() throws Exception {
    assertEquals("en", mw.getLang());
  }
}