package mlp.rus;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import mlp.rus.RuleBasedPosTagger.PosTag;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;

public class RusPosAnnotator implements Annotator {

    private final RuleBasedPosTagger tagger = new RuleBasedPosTagger();

    @Override
    public void annotate(Annotation annotation) {
        List<CoreLabel> list = annotation.get(TokensAnnotation.class);
        for (CoreLabel token : list) {
            String textToken = token.get(TextAnnotation.class);
            PosTag tag = tagger.posTag(textToken);
            token.set(CoreAnnotations.PartOfSpeechAnnotation.class, tag.getPennTag());
        }
    }

    @Override
    public Set<Requirement> requires() {
        return Collections.singleton(TOKENIZE_REQUIREMENT);
    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        return Collections.singleton(POS_REQUIREMENT);
    }
}
