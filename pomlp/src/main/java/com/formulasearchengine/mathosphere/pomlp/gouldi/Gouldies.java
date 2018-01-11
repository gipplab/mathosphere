package com.formulasearchengine.mathosphere.pomlp.gouldi;

import java.nio.file.Path;
import java.util.LinkedHashMap;

import com.formulasearchengine.mathmltools.mml.elements.Goldener;
import com.formulasearchengine.mathosphere.pomlp.util.GoldUtils;

public class Gouldies extends LinkedHashMap<Integer,Goldener> {

    private Path p;

    public void augment() {
        forEach((key, value) -> {
            value.augment();
            GoldUtils.writeGoldFile(p.resolve(key+".json"), value);
        });

    }

    public Goldener put(Integer key, JsonGouldiBean value) {
        return super.put(key, new Goldener(value));
    }

    public void setOutputPath(Path p) {
        this.p = p;
    }
}
