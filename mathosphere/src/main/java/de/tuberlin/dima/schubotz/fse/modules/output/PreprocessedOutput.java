package de.tuberlin.dima.schubotz.fse.modules.output;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import org.apache.commons.cli.Option;
import org.apache.flink.api.java.ExecutionEnvironment;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by jjl4 on 8/7/14.
 */
public class PreprocessedOutput implements Output {
    @Override
    public Collection<Option> getOptionsAsIterable() {
        return Collections.emptyList();
    }

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {


    }
}
