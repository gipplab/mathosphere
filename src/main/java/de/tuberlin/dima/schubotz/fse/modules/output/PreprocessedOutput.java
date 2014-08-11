package de.tuberlin.dima.schubotz.fse.modules.output;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import eu.stratosphere.api.java.ExecutionEnvironment;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by jjl4 on 8/7/14.
 */
public class PreprocessedOutput implements Output {
    static {

    }

    @Override
    public Collection<Option> getOptionsAsIterable() {
        return Collections.emptyList();
    }

    @Override
    public void configure(ExecutionEnvironment env, DataStorage data) {


    }
}
