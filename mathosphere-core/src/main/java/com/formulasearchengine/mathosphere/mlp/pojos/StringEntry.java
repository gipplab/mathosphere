package com.formulasearchengine.mathosphere.mlp.pojos;

import com.google.common.collect.Multiset;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Leo on 16.02.2017.
 */
public class StringEntry implements Multiset.Entry {
  private String element;

  public void setElement(String element) {
    this.element = element;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public StringEntry() {
  }

  public StringEntry(String element, int count) {
    this.element = element;
    this.count = count;
  }

  private int count;

  @Override
  public String getElement() {
    return element;
  }

  @Override
  public int getCount() {
    return count;
  }

  public static Set<StringEntry> fromSet(Set<Multiset.Entry<String>> in) {
    Set<StringEntry> set = new HashSet();
    for (Multiset.Entry<String> stringEntry : in) {
      set.add(new StringEntry(stringEntry.getElement(), stringEntry.getCount()));
    }
    return set;
  }
}
