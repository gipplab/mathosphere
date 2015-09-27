package mlp;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigTest {

    @Test
    public void test() {
        String[] args = { "-in", "c:/tmp/mlp/input/", "-out", "c:/tmp/mlp/output/" };
        Config config = Config.from(args);
        assertEquals("c:/tmp/mlp/input/", config.getDataset());
        assertEquals("c:/tmp/mlp/output/", config.getOutputDir());
    }

}
