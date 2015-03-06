package mlp;

import java.io.Serializable;

public class Config implements Serializable {

    private String dataset;
    private String outputdir;
    private String model;
    private double alpha;
    private double beta;
    private double gamma;
    private double threshold;

    public static Config test() {
        String dataset = "c:/tmp/mlp/input/";
        String outputdir = "c:/tmp/mlp/output/";
        String model = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

        return new Config(dataset, outputdir, model, 1.0, 1.0, 0.5, 0.8);
    }
    
    public static Config from(String[] args) {
        String dataset = args[0];
        String outputdir = args[1];
        String model = args[2];

        double alpha = Double.parseDouble(args[3]);
        double beta = Double.parseDouble(args[4]);
        double gamma = Double.parseDouble(args[5]);
        double threshold = Double.parseDouble(args[6]);

        return new Config(dataset, outputdir, model, alpha, beta, gamma, threshold);
    }

    public Config(String dataset, String outputdir, String model, double alpha, double beta, double gamma,
            double threshold) {
        this.dataset = dataset;
        this.outputdir = outputdir;
        this.model = model;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.threshold = threshold;
    }

    public String getDataset() {
        return dataset;
    }

    public String getOutputdir() {
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

}
