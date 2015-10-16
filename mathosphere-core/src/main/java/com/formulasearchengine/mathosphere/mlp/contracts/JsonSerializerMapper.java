package com.formulasearchengine.mathosphere.mlp.contracts;

import com.fasterxml.jackson.jr.ob.JSON;
import org.apache.flink.api.common.functions.MapFunction;

public class JsonSerializerMapper<T> implements MapFunction<T, String> {

  @Override
  public String map(T value) throws Exception {
    return JSON.std.asString(value);
  }

}
