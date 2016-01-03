package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by Moritz on 12.12.2015.
 */
public class WikidataInterfaceTest {

  @Test
  public void testGetEntities() throws Exception {
    final ArrayList<String> expected = Lists.newArrayList("Q12916");
    try {
      Assert.assertEquals(expected.get(0), WikidataInterface.getEntities("real number").get(0));
    } catch (UnknownHostException h) {
      // TODO: Figure out how to mark a test as skipped
    }
  }
}