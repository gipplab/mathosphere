package mlp;

import java.io.Serializable;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Config implements Serializable {

    private static final String DEFAULT_POS_MODEL = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

    @Parameter(names = { "-in", "--inputDir" }, description = "path dir with wikidump")
    private String dataset;

    @Parameter(names = { "-out", "--outputDir" }, description = "path to output dir")
    private String outputdir;

    @Parameter(names = { "-pos", "--posModel" }, description = "POS model to use")
    private String model = DEFAULT_POS_MODEL;

    @Parameter(names = { "-l", "--language" }, description = "Language of the input")
    private String language = "en";

    @Parameter(names = { "-a", "--alpha" })
    private double alpha = 1.0;

    @Parameter(names = { "-b", "--beta" })
    private double beta = 1.0;

    @Parameter(names = { "-g", "--gamma" })
    private double gamma = 0.1;

    @Parameter(names = { "-t", "--threshold" })
    private double threshold = 0.8;

    private Config() {
    }

    public static Config test() {
        String dataset = "c:/tmp/mlp/input/";
        String outputdir = "c:/tmp/mlp/output/";

        return new Config(dataset, outputdir);
    }

    public static Config from(String[] args) {
        if (args.length == 0) {
            return test();
        }

        Config config = new Config();
        JCommander commander = new JCommander();
        commander.addObject(config);
        commander.parse(args);
        return config;
    }

    public Config(String dataset, String outputdir) {
        this.dataset = dataset;
        this.outputdir = outputdir;
    }

    public Config(String dataset, String outputdir, String model, String language, double alpha, double beta,
            double gamma, double threshold) {
        this.dataset = dataset;
        this.outputdir = outputdir;
        this.model = model;
        this.language = language;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.threshold = threshold;
    }

    public String getDataset() {
        return dataset;
    }

    public String getOutputDir() {
        return outputdir;
    }

    public String getModel() {
        return model;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }

    public double getGamma() {
        return gamma;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getLanguage() {
        return language;
    }
}
