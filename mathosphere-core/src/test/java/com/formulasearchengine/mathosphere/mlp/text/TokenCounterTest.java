package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import junit.framework.TestCase;

import org.apache.flink.api.java.tuple.Tuple2;

public class TokenCounterTest extends TestCase {
  public void testCountTokens() throws Exception {
    TokenCounter ct = new TokenCounter();
    Multiset<Tuple2<String, String>> count = ct.countTokens(this.getClass().getClassLoader().getResourceAsStream("tokens.json"));
    assertEquals(11, count.count(new Tuple2<>("TEX_ONLY", "H")));
    assertEquals(9788, count.size());
    ImmutableSet<Multiset.Entry<Tuple2<String, String>>> entries = Multisets.copyHighestCountFirst(count).entrySet();
    for (Multiset.Entry<Tuple2<String, String>> tuple2Entry : entries) {
      System.out.println(tuple2Entry.getElement().toString() + ":" + tuple2Entry.getCount());
    }
  }

  public void testCountIdentifier() throws Exception {
    TokenCounter ct = new TokenCounter();
    Multiset<String> count = ct.countIdentifer(this.getClass().getClassLoader().getResourceAsStream("identifier.json"));
    assertEquals(5, count.count("H"));
    assertEquals(1443, count.size());
    ImmutableSet<Multiset.Entry<String>> entries = Multisets.copyHighestCountFirst(count).entrySet();
    for (Multiset.Entry<String> tuple2Entry : entries) {
      System.out.println(tuple2Entry.getElement() + ":" + tuple2Entry.getCount());
    }
  }
}
