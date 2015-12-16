package com.formulasearchengine.wikidatalistgenerator;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by Moritz on 16.12.2015.
 */
public class ItemStremExtractorTest {

	@Test
	public void testExtract() throws Exception {
		InputStream is = getClass().getResourceAsStream( "sample-q1.json" );
		ItemStremExtractor extractor = new ItemStremExtractor( "en", false );
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		extractor.extract( is, baos );
		assertEquals( "Q1", baos.toString() );
	}
}