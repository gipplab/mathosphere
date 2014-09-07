package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import junit.framework.TestCase;

public class ArxivCleanerTest extends TestCase {

	public void testGetDelimiter(){
		ArxivCleaner arxivCleaner = new ArxivCleaner();
		assertEquals( "</ARXIVFILESPLIT>",arxivCleaner.getDelimiter() );
	}

}