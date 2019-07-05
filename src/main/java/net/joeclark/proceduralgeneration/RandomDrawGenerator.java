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
    public static final int DEFAULT_MIN_LENGTH = 0;
    /** {@value}*/
    public static final int DEFAULT_MAX_LENGTH = Integer.MAX_VALUE;

    // todo: pick a less common character
    static final char CONTROL_CHAR = '#';  // to indicate beginning and end of input; must not be in the data's alphabet


    private int minLength = DEFAULT_MIN_LENGTH;
    private int maxLength = DEFAULT_MAX_LENGTH;
    private String startsWith;
    private String endsWith;
    // todo: add a regex match option
    private Random random;
    // todo: add the option to set a custom RNG

    private List<String> wordList;

    // for testing only
    List<String> getWordList() { return wordList; }

    // setters
    public void setMinLength(int minLength) { this.minLength = minLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    public void setStartsWith(String startsWith) { this.startsWith = startsWith.toLowerCase(); }
    public void setEndsWith(String endsWith) { this.endsWith = endsWith.toLowerCase(); }
    // getters
    public int getMaxLength() { return maxLength; }
    public int getMinLength() { return minLength; }
    public String getStartsWith() { return startsWith; }
    public String getEndsWith() { return endsWith; }



    /**
     * Initialize a new RandomDrawGenerator. A new instance begins with the default values for minLength, maxLength,
     * startsWith, and endsWith.  After initialization, you must train the model on a stream of input Strings before
     * generating names, optionally first setting parameters such as minLength and maxLength, e.g.:
     * <code>new RandomDrawGenerator.withMinLength(4).withMaxLength(12),train(streamOfStrings)</code>
     */
    public RandomDrawGenerator() {
        random = new Random();
    }

    /**
     * @param minLength the minimum length of output text you'll accept
     * @return the same RandomDrawGenerator
     */
    public RandomDrawGenerator withMinLength(int minLength) {
        this.minLength = minLength;
        return this;
    }

    /**
     * @param maxLength the maximum length of output text you'll accept
     * @return the same RandomDrawGenerator
     */
    public RandomDrawGenerator withMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * @param startsWith a String that the beginning of the output must match, for example, a letter you want it to start with
     * @return the same RandomDrawGenerator
     */
    public RandomDrawGenerator withStart(String startsWith) {
        this.startsWith = startsWith.toLowerCase();
        return this;
    }

    /**
     * @param endsWith a String that the end of the output must match
     * @return the same RandomDrawGenerator
     */
    public RandomDrawGenerator withEnd(String endsWith) {
        this.endsWith = endsWith.toLowerCase();
        return this;
    }

    /**
     * Ingest a new set of training data, overwriting any data that was previously trained.  Subsequent random draws
     * will be made from this data.
     */
    public RandomDrawGenerator train(Stream<String> rawWords) {
        this.wordList = rawWords.map(String::toLowerCase).collect(Collectors.toList());
        return this;
    }

    /**
     * @return true if the model was trained or re-trained. Don't attempt to generate names from an untrained model, or you'll get an InvalidStateException!
     */
    public boolean isTrained() {
        return (this.wordList != null && this.wordList.size()>0);
    }




    /**
     * @return a random string from the training dataset (but lowercase). If you have set filters such
     * as maximum and minimum length, or a starting and ending sequence, be careful that those filters are not
     * impossible given the training data. You could end up with an infinite loop or an exception if your
     * conditions are impossible to satisfy.
     * @throws IllegalStateException if model has not been trained
     */
    @Override
    public String generateOne() {

        if (!isTrained()) {
            throw new IllegalStateException("model has not yet been trained");
        } else {
            String draw;
            do {
                // add control characters to indicate start and end of word
                draw = CONTROL_CHAR + wordList.get(random.nextInt(wordList.size())).toLowerCase() + CONTROL_CHAR;
            } while (
                // conditions for a re-roll
                    (draw.length() < minLength + 2) ||
                    (draw.length() - 2 > maxLength) ||
                    ((startsWith != null) && (!draw.contains(CONTROL_CHAR + startsWith))) ||
                    ((endsWith != null) && (!draw.contains(endsWith + CONTROL_CHAR)))
            );
            return draw.substring(1, draw.length() - 1); // strip off control characters
        }
    }



}
