package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Moritz on 12.12.2015.
 */
public class TexInfoTest {

	@Test
	public void testGetIdentifiers() throws Exception {
		final HashMultiset<String> expected = HashMultiset.create();
		expected.addAll(Lists.newArrayList("E", "m", "c"));
		Assert.assertEquals(expected,TexInfo.getIdentifiers("E=mc^2"));
	}
}