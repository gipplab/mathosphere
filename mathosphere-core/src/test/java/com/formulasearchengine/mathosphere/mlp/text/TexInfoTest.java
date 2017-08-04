package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;

import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;

import org.junit.Assert;
import org.junit.Test;

import static org.apache.http.client.cache.CacheResponseStatus.CACHE_MISS;

/**
 * Created by Moritz on 12.12.2015.
 */
public class TexInfoTest {

  @Test
  public void testGetIdentifiers() throws Exception {
    FlinkMlpCommandConfig cfg = FlinkMlpCommandConfig.test();
    final HashMultiset<String> expected = HashMultiset.create();
    expected.addAll(Lists.newArrayList("E", "m", "c"));
    Assert.assertEquals(expected, TexInfo.getIdentifiers("E=mc^2", cfg.getTexvcinfoUrl()));
  }

  @Test
  public void testCache() throws Exception {
    FlinkMlpCommandConfig cfg = FlinkMlpCommandConfig.test();
    final HashMultiset<String> expected = HashMultiset.create();
    expected.addAll(Lists.newArrayList("E", "m", "c"));
    Assert.assertEquals(expected, TexInfo.getIdentifiers("E=mc^2", cfg.getTexvcinfoUrl()));
    Assert.assertEquals(CACHE_MISS,TexInfo.getCacheResponseStatus());
    TexInfo.getIdentifiers("E=mc^2", cfg.getTexvcinfoUrl());
    switch (TexInfo.getCacheResponseStatus()) {
      case CACHE_HIT:
        System.out.println("A response was generated from the cache with " +
                "no requests sent upstream");
        break;
      case CACHE_MODULE_RESPONSE:
        System.out.println("The response was generated directly by the " +
                "caching module");
        break;
      case CACHE_MISS:
        System.out.println("The response came from an upstream server");
        break;
      case VALIDATED:
        System.out.println("The response was generated from the cache " +
                "after validating the entry with the origin server");
        break;
    }
  }
}