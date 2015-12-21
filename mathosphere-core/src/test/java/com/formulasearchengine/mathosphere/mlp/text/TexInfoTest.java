package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;

import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Moritz on 12.12.2015.
 */
public class TexInfoTest {

  @Test
  public void testGetIdentifiers() throws Exception {
    FlinkMlpCommandConfig cfg = FlinkMlpCommandConfig.test();
    final HashMultiset<String> expected = HashMultiset.create();
    expected.addAll(Lists.newArrayList("E", "m", "c"));
    Assert.assertEquals(expected, TexInfo.getIdentifiers("E=mc^2",cfg.getTexvcinfoUrl()));
  }
}