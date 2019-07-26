package net.joeclark.proceduralgeneration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Draws a random name from a training dataset (but lowercase regardless of training data's case)
 */
public class ClusterChainGenerator implements RandomTextGenerator {

    // TODO: implement a subclass that uses a Markov model to track *frequencies* of cluster sequences rather than simply drawing them at random
    // TODO: implement a sub-subclass that does all that MarkovTextGenerator does: multi-order markov models, bayesian priors, etc.

    private static final Logger logger = LoggerFactory.getLogger( ClusterChainGenerator.class );

    /** {@value}*/
    public static final int DEFAULT_MIN_LENGTH = 4;
    /** {@value}*/
    public static final int DEFAULT_MAX_LENGTH = 12;

    static final char CONTROL_CHAR = '#';  // to indicate beginning and end of input; must not be in the data's alphabet


    private int minLength = DEFAULT_MIN_LENGTH;
    private int maxLength = DEFAULT_MAX_LENGTH;
    private String startFilter;
    private String endFilter;
    // todo: add a regex match option
    private Random random = new Random();

    // TODO: get more comprehensive lists of vowels and consonants, or ways to check if a character is one or the other
    private Set<Character> vowels = new HashSet<>(Arrays.asList('a','e','i','o','u','y'));
    private Set<Character> consonants = new HashSet<>(Arrays.asList('b','c','d','f','g','h','j','k','l','m','n','p','q','r','s','t','v','w','x','z'));
    // TODO: improve the algorithm so it knows that some characters like 'y' can play both vowel and consonant roles


    private int datasetLength;
    private Map<String,Set<String>> clusterChain = new HashMap<>();

    // for testing only
    Set<Character> getVowels() { return vowels; }
    Set<Character> getConsonants() { return consonants; }


    // setters
    public void setMinLength(int minLength) { this.minLength = minLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    public void setStartFilter(String startFilter) { this.startFilter = startFilter.toLowerCase(); }
    public void setEndFilter(String endFilter) { this.endFilter = endFilter.toLowerCase(); }
    public void setRandom(Random random) { this.random = random; }
    // getters
    public int getDatasetLength() { return datasetLength; }
    public int getMaxLength() { return maxLength; }
    public int getMinLength() { return minLength; }
    public String getStartFilter() { return startFilter; }
    public String getEndFilter() { return endFilter; }




    /**
     * Initialize a new ClusterChainGenerator. A new instance begins with the default values for minLength, maxLength,
     * startFilter, and endFilter.  After initialization, you must train the model on a stream of input Strings before
     * generating text, optionally first setting parameters such as minLength and maxLength, e.g.:
     * <code>new ClusterChainGenerator.withMinLength(3).withMaxLength(10),train(streamOfStrings)</code>
     */
    public ClusterChainGenerator() {
        logger.info("Initialized new ClusterChainGenerator instance");
    }

    /**
     * @param random a Random number generator to be used in generating text
     * @return the same ClusterChainGenerator
     */
    public ClusterChainGenerator withRandom(Random random) {
        this.random = random;
        return this;
    }

    /**
     * @param minLength the minimum length of output text you'll accept
     * @return the same ClusterChainGenerator
     */
    public ClusterChainGenerator withMinLength(int minLength) {
        this.minLength = minLength;
        return this;
    }

    /**
     * @param maxLength the maximum length of output text you'll accept
     * @return the same ClusterChainGenerator
     */
    public ClusterChainGenerator withMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * @param startFilter a String that the beginning of the output must match, for example, a letter you want it to start with
     * @return the same ClusterChainGenerator
     */
    public ClusterChainGenerator withStartFilter(String startFilter) {
        this.startFilter = startFilter.toLowerCase();
        return this;
    }

    /**
     * @param endFilter a String that the end of the output must match
     * @return the same ClusterChainGenerator
     */
    public ClusterChainGenerator withEndFilter(String endFilter) {
        this.endFilter = endFilter.toLowerCase();
        return this;
    }

    /**
     * Ingest a new set of training data.
     */
    public ClusterChainGenerator train(Stream<String> rawWords) {
        rawWords.map(String::toLowerCase)
                .map(String::trim)
                .forEach( w -> {
                    extractClusters(w);
                });

        System.out.println(clusterChain);

        logger.info("ingested a stream of training data. model derived from {} text strings containing {} clusters",datasetLength,clusterChain.size()-1);
        return this;
    }

    private void extractClusters(String w) {

        // we are building a map of preceding clusters to clusters that follow them
        String precedingCluster = String.valueOf(CONTROL_CHAR);
        StringBuilder newCluster = new StringBuilder();

        char[] chars = w.toCharArray();
        boolean vowelCluster = vowels.contains(chars[0]);

        for(char c: chars) {
            if( (vowels.contains(c)) == vowelCluster ) {
                newCluster.append(c);
            } else {
                clusterChain.computeIfAbsent(precedingCluster, k -> new HashSet<>()).add(newCluster.toString());
                precedingCluster = newCluster.toString();
                newCluster = new StringBuilder();
                newCluster.append(c);
                vowelCluster = !vowelCluster;
            }
        }
        clusterChain.computeIfAbsent(precedingCluster, k -> new HashSet<>()).add(String.valueOf(CONTROL_CHAR));
        datasetLength += 1;
    }


    /**
     * @return true if the model was trained. Don't attempt to generate names from an untrained model, or you'll get an InvalidStateException!
     */
    public boolean isTrained() { return datasetLength > 0; }




    /**
     * @return a randomly-generated text string built from cluster sequences from the training data. If you have set
     * filters such as maximum and minimum length, or a starting and ending sequence, be careful that those filters
     * are not impossible given the training data. You could end up with an infinite loop or an exception if your
     * conditions are impossible to satisfy.
     * @throws IllegalStateException if model has not been trained
     */
    @Override
    public String generateOne() {

        if (!isTrained()) {
            throw new IllegalStateException("model has not yet been trained");
        } else {
            StringBuilder word;
            String returnText;

            do {
                // generate another word

                // TODO: start with startFilter or throw exception if impossible

                word = new StringBuilder();
                word.append(CONTROL_CHAR);
                Set<String> possibleClusters = clusterChain.get(String.valueOf(CONTROL_CHAR));
                int i;
                int pick;
                boolean done = false;
                while(done == false) {
                    // draw another cluster

                    // TODO: check if near maxLength and end gracefully
                    // TODO: if near maxLength, and endFilter is in the possibleClusters, end with it

                    i = 0;
                    pick = random.nextInt(possibleClusters.size());
                    for(String s: possibleClusters) {
                        if (i==pick) {
                            if (s.equals(String.valueOf(CONTROL_CHAR))) {
                                done = true;
                                break;
                            }
                            else {
                                word.append(s);
                                possibleClusters = clusterChain.get(s);
                                break;
                            }
                        }
                        i++;
                    }

                }

                word.append(CONTROL_CHAR);
                returnText = word.toString();

                logger.trace("new candidate text string generated, about to check filters: {}", returnText);
            } while (
                // conditions for a re-roll
                    (returnText.length() < minLength + 2) ||
                    (returnText.length() - 2 > maxLength) ||
                    ((startFilter != null) && (!returnText.contains(CONTROL_CHAR + startFilter))) ||
                    ((endFilter != null) && (!returnText.contains(endFilter + CONTROL_CHAR)))
            );
            returnText = returnText.substring(1, returnText.length() - 1); // strip off control characters
            logger.debug("new random text string generated and returned: {}", returnText);
            return returnText;
        }
    }



}
