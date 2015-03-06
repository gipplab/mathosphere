package mlp.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class MathUtils {

    private static final String MATH_TAG_OPEN = "<math";
    private static final String MATH_TAG_CLOSE = "</math>";

    public static List<MathTag> findMathTags(String text) {
        List<MathTag> results = new ArrayList<MathTag>();

        int current = 0;
        while (true) {
            int start = text.indexOf(MATH_TAG_OPEN, current);
            if (start < 0) {
                break;
            }

            int end = text.indexOf(MATH_TAG_CLOSE, start);
            if (end < 0) {
                break;
            }
            current = end + MATH_TAG_CLOSE.length();

            String math = text.substring(start, end + MATH_TAG_CLOSE.length());
            MathMarkUpType markUp = extractMarkupType(math);

            results.add(new MathTag(start, math, markUp));
        }

        return results;
    }

    private static MathMarkUpType extractMarkupType(String math) {
        int closingBracket = math.indexOf(">", MATH_TAG_OPEN.length());
        String augmentationString = math.substring(MATH_TAG_OPEN.length(), closingBracket).trim();
        boolean noAugmention = augmentationString.isEmpty();
        return noAugmention ? MathMarkUpType.LATEX : MathMarkUpType.MATHML;
    }

    public static class MathTag {
        private static final HashFunction HASHER = Hashing.md5();

        private final int position;
        private final String content;
        private final MathMarkUpType markUpType;

        public MathTag(int position, String content, MathMarkUpType markUp) {
            this.position = position;
            this.content = content;
            this.markUpType = markUp;
        }

        public int getPosition() {
            return position;
        }

        public String getContent() {
            return content;
        }

        public String getTagContent() {
            return content.replaceAll("<math.*?>", "").replaceAll("</math>", "");
        }

        public String getContentHash() {
            return HASHER.hashString(content, StandardCharsets.UTF_8).toString();
        }

        public MathMarkUpType getMarkUpType() {
            return markUpType;
        }
    }

    public static enum MathMarkUpType {
        LATEX, MATHML;
    }
}
