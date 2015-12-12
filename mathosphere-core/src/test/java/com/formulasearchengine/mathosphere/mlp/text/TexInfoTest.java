package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by Moritz on 12.12.2015.
 */
public class TexInfoTest {

	@Test
	public void testGetIdentifiers() throws Exception {
		final ArrayList<String> expected = Lists.newArrayList("E", "m", "c");
		Assert.assertEquals(expected,TexInfo.getIdentifiers("E=mc^2"));
	}
}