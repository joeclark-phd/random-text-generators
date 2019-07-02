package net.joeclark.proceduralgeneration;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Draws a random name from a training dataset (but lowercase regardless of training data's case)
 */
public class RandomDrawGenerator implements RandomTextGenerator {

    /** {@value}*/
    public static final int DEFAULT_MIN_LENGTH = 4;
    /** {@value}*/
    public static final int DEFAULT_MAX_LENGTH = 12;
    static final char CONTROL_CHAR = '#';  // to indicate beginning and end of input; must not be in the data's alphabet

    private List<String> wordList;
    private Random random;

    // for testing only
    List<String> getWordList() { return wordList; }

    /**
     * @return a random lowercase string with a (default) length of 4 to 12 characters.
     * @throws IllegalStateException if model has not been trained
     */
    @Override
    public String generateOne() {
        return generateOne(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH,null,null);  // defaults
    }

    /**
     * @param minLength defaults to 4
     * @param maxLength defaults to 12
     * @param startsWith (default null) if not null, output will be filtered for results that start with the given string.
     *                   Beware, if you specify a complex string or a string that cannot occur in the alphabet of the
     *                   training data, you may end up with an infinite loop as the program tries to generate a name
     *                   to pass this impossible test.
     * @param endsWith (default null) if not null, output will be filtered for results that end with the given string.
     *                 Beware, if you specify a complex string or a string that cannot occur in the alphabet of the
     *                 training data, you may end up with an infinite loop as the program tries to generate a name
     *                 to pass this impossible test.
     * @return a random lowercase string according to your specifications
     * @throws IllegalStateException if model has not been trained
     */
    @Override
    public String generateOne(int minLength, int maxLength, String startsWith, String endsWith) {
        // if a zero is supplied for either integer parameter, use the default
        int min = minLength == 0 ? DEFAULT_MIN_LENGTH : minLength;
        int max = maxLength == 0 ? DEFAULT_MAX_LENGTH : maxLength;
        // random string will be lowercased; check startsWith and endsWith to mitigate possible errors
        String start = startsWith == null ? null : startsWith.toLowerCase();
        String end = endsWith == null ? null : endsWith.toLowerCase();

        if (!isTrained()) {
            throw new IllegalStateException("model has not yet been trained");
        } else {
            String draw;
            do {
                // add control characters to indicate start and end of word
                draw = CONTROL_CHAR + wordList.get(random.nextInt(wordList.size())) + CONTROL_CHAR;
            } while (
                // conditions for a re-roll
                    (draw.length() < min + 2) ||
                    (draw.length() > max + 2) ||
                    ((start != null) && (!draw.contains(CONTROL_CHAR + start))) ||
                    ((end != null) && (!draw.contains(end + CONTROL_CHAR)))
            );
            return draw.substring(1, draw.length() - 1); // strip off control characters
        }
    }
            /**
             * Initialize the RandomDrawGenerator with a stream of sample Strings. Random draws will be made from this data.
             * @param rawWords a stream of training data
             */
    public RandomDrawGenerator(Stream<String> rawWords) {
        this();
        this.train(rawWords);
    }

    /**
     * Initialize the instance without training data, to train() it later. Not recommended.
     */
    public RandomDrawGenerator() {
        random = new Random();
    }

    /**
     * Ingest a new set of training data, overwriting any data that was previously trained.  Subsequent random draws
     * will be made from this data.
     */
    public void train(Stream<String> rawWords) {
        this.wordList = rawWords.map(String::toLowerCase).collect(Collectors.toList());
    }

    /**
     * @return true if the model was trained or re-trained. Don't attempt to generate names from an untrained model, or you'll get an InvalidStateException!
     */
    public boolean isTrained() {
        return (this.wordList != null && this.wordList.size()>0);
    }


}
