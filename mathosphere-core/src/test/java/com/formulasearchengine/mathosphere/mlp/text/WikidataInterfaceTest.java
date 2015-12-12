package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by Moritz on 12.12.2015.
 */
public class WikidataInterfaceTest {

	@Test
	public void testGetEntities() throws Exception {
		final ArrayList<String> expected = Lists.newArrayList("Q12916");
		Assert.assertEquals(expected, WikidataInterface.getEntities("real number"));
	}
}