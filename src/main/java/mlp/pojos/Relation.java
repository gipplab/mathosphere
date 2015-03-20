package mlp.pojos;


public class Relation {

    private String documentTitle;
    private String identifier;
    private double score;
    private Word word;
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

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
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

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    @Override
    public String toString() {
        return "Relation [" + identifier + ", score=" + score + ", word=" + word + "]";
    }

}
