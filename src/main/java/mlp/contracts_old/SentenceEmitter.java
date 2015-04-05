///*        __
// *        \ \
// *   _   _ \ \  ______
// *  | | | | > \(  __  )
// *  | |_| |/ ^ \| || |
// *  | ._,_/_/ \_\_||_|
// *  | |
// *  |_|
// *
// * ----------------------------------------------------------------------------
// * "THE BEER-WARE LICENSE" (Revision 42):
// * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
// * can do whatever you want with this stuff. If we meet some day, and you think
// * this stuff is worth it, you can buy me a beer in return.
// * ----------------------------------------------------------------------------
// */
//package mlp.contracts_old;
//
//import java.util.List;
//import java.util.Properties;
//
//import mlp.types.Identifiers;
//import mlp.types.Sentence;
//import mlp.types.WikiDocument;
//import mlp.types.Word;
//import mlp.utils.SentenceUtils;
//
//import org.apache.flink.api.common.functions.RichFlatMapFunction;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.api.java.tuple.Tuple3;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.util.Collector;
//
//import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.util.CoreMap;
//
///**
// * @author rob
// */
//public class SentenceEmitter extends
//        RichFlatMapFunction<Tuple2<String, WikiDocument>, Tuple3<String, Sentence, Double>> {
//
//    private String posModel;
//    private StanfordCoreNLP pipeline;
//
//    public SentenceEmitter(String posModel) {
//        this.posModel = posModel;
//    }
//
//    @Override
//    public void open(Configuration cfg) throws Exception {
//        Properties props = new Properties();
//        props.put("annotators", "tokenize, ssplit, pos");
//        props.put("pos.model", posModel);
//        props.put("tokenize.options", "untokenizable=firstKeep" + ",strictTreebank3=true"
//                + ",ptb3Escaping=true" + ",escapeForwardSlashAsterisk=false");
//        props.put("ssplit.newlineIsSentenceBreak", "two");
//
//        pipeline = new StanfordCoreNLP(props);
//    }
//
//    @Override
//    public void flatMap(Tuple2<String, WikiDocument> value, Collector<Tuple3<String, Sentence, Double>> out)
//            throws Exception {
//        WikiDocument doc = value.f1;
//
//        Identifiers identifiers = doc.getKnownIdentifiers();
//        String plaintext = doc.getPlainText();
//
//        Annotation document = new Annotation(plaintext);
//
//        pipeline.annotate(document);
//        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//
//        int position = 0;
//        for (CoreMap sentence : sentences) {
//            Sentence ps = new Sentence();
//
//            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
//                String word = token.get(TextAnnotation.class);
//                if (identifiers.containsIdentifier(word)) {
//                    ps.add(new Word(word, "ID"));
//                } else {
//                    String pos = token.get(PartOfSpeechAnnotation.class);
//                    ps.add(new Word(word, pos));
//                }
//            }
//
//            ps = postprocessSentence(ps);
//            double val = (double) position / sentences.size();
//            out.collect(new Tuple3<>(value.f0, ps, val));
//
//            position += 1;
//        }
//    }
//
//    private Sentence postprocessSentence(Sentence sentence) {
//        // links
//        sentence = SentenceUtils.joinByTagPattern(sentence, "`` * ''", "LNK");
//        // trim links
//        sentence = SentenceUtils.replaceAllByTag(sentence, "LNK", "^\\s*[\"`']+\\s*|\\s*[\"`']+\\s*$", "");
//        // change tag type for formulas
//        sentence = SentenceUtils.replaceAllByPattern(sentence, "FORMULA", "MATH");
//        // for some reason some signs will be tagged JJ or NN, need to fix this
//        sentence = SentenceUtils.replaceAllByPattern(sentence, "<|=|>|≥|≤|\\|\\/|\\[|\\]", "SYM");
//        // aggregate noun phrases
//        sentence = SentenceUtils.joinByTagPattern(sentence, "NN +", "NN+");
//        sentence = SentenceUtils.joinByTagPattern(sentence, "JJ NN", "NP");
//        sentence = SentenceUtils.joinByTagPattern(sentence, "JJ NN+", "NP+");
//        return sentence;
//    }
//
//}
