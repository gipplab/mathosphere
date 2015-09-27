package mlp.contracts;

import org.apache.flink.api.common.functions.MapFunction;

import com.fasterxml.jackson.jr.ob.JSON;

public class JsonSerializerMapper<T> implements MapFunction<T, String> {

    @Override
    public String map(T value) throws Exception {
        return JSON.std.asString(value);
    }

}
