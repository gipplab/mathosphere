package com.formulasearchengine.mathosphere.basex;

import org.junit.Test;

import static org.junit.Assert.*;

public class XMLHelperTest {

	@Test
	public void testString2Doc () throws Exception {
		assertNull( XMLHelper.String2Doc( "<open><open2></open2>" ) );
		assertNotNull( XMLHelper.String2Doc( "<simple />" ) );
		XMLHelper x = new XMLHelper(); //Does not really make sense
	}
}