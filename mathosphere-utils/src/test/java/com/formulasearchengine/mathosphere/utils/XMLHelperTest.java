package com.formulasearchengine.mathosphere.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class XMLHelperTest {

	@Test
	public void testString2Doc () throws Exception {
		Assert.assertNull( XMLHelper.String2Doc( "<open><open2></open2>", true ) );
		Assert.assertNotNull( XMLHelper.String2Doc( "<simple />", true ) );
		XMLHelper x = new XMLHelper(); //Does not really make sense
	}
}