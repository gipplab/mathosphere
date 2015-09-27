package mlp.pojos;


public class Relation {

    private String identifier;
    private String definition;
    private double score;
    private int identifierPosition;
    private int wordPosition;
    private Sentence sentence;

    public Relation() {
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(Word word) {
        this.definition = word.getWord();
    }
    
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Sentence getSentence() {
        return sentence;
    }

    public void setSentence(Sentence sentence) {
        this.sentence = sentence;
    }

    public int getIdentifierPosition() {
        return identifierPosition;
    }

    public void setIdentifierPosition(int identifierPosition) {
        this.identifierPosition = identifierPosition;
    }

    public int getWordPosition() {
        return wordPosition;
    }

    public void setWordPosition(int wordPosition) {
        this.wordPosition = wordPosition;
    }

    @Override
    public String toString() {
        return "Relation [" + identifier + ", score=" + score + ", word=" + definition + "]";
    }

}
