package net.joeclark.proceduralgeneration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>A class that uses a vowel/consonant clustering algorithm to generate new random text.  Based loosely on
 * <a href="http://roguebasin.roguelikedevelopment.org/index.php?title=Cluster_chaining_name_generator">an algorithm
 * described by Kusigrosz at RogueBasin</a>, it scans input text for clusters of vowels and clusters of consonants,
 * after converting it all to lowercase, keeping track of all clusters that have been observed to follow any given
 * cluster.  For example, "Elizabeth" would yield clusters {@code #-e-l-i-z-a-b-e-th-# } and "Anne" would yield
 * {@code #-a-nn-e-# } where "#" is a control character marking the start or end of a string. Internally we
 * would keep track of the possible successors of each cluster, e.g.:</p>
 *
 * <pre>{@code # -> [e,a]
 * e -> [l,th,#]
 * a -> [b,nn]
 * th -> [#]
 * ...etc...}</pre>
 *
 * <p>The generateOne() method takes a random walk through the cluster chain, only following paths that were found
 * in the training data.  To continue our example, a new string could begin with "e" or "a", with equal likelihood,
 * an "e" could be followed by "l", by "th", or by the end of a string, and so on.  With this training dataset of only
 * two words, you could get a few different results, e.g.:</p>
 *
 * <pre>{@code elizanneth
 * abelizanne
 * anneth
 * ...etc...}</pre>
 *
 * <p>Each newly generated candidate string is compared to filters (minLength, maxLength, startsWith, endsWith)
 * and returned if it passes. If the candidate string is filtered out, we generate another, until one passes.
 * (Be aware that if you configure very difficult-to-match filters, generation time may increase greatly.  If you set
 * up impossible-to-match filters, e.g. requiring characters that aren't in the training data set's alphabet, you will
 * get an infinite loop.)</p>
 */
public class ClusterChainGenerator implements RandomTextGenerator {

    // TODO: implement a subclass that uses a Markov model to track *frequencies* of cluster sequences rather than simply drawing them at random
    // TODO: implement a sub-subclass that does all that MarkovTextGenerator does: multi-order markov models, bayesian priors, etc.

    private static final Logger logger = LoggerFactory.getLogger( ClusterChainGenerator.class );


    /** {@value}*/
    public static final int DEFAULT_MIN_LENGTH = 4;
    /** {@value}*/
    public static final int DEFAULT_MAX_LENGTH = 12;
    /**
     * A long list of characters I found on the Unicode table (unicode-table.com) which seem to be variants of a,e,i,o,u,y for various Latin-based alphabets.  This is the default alphabet for ClusterChainGenerator.  To change it, use .withVowels or .setVowels.
     * 'a','à','á','â','ã','ä','å','ā','ă','ą','ǎ','æ','ǣ','ǟ','ǡ','ǻ','ǽ','ȁ','ȁ','ȧ','e','è','é','ê','ë','ē','ĕ','ė','ę','ě','ǝ','ɘ','ə','ɇ','ȅ','ȇ','ȩ','i','ì','í','î','ï','ĩ','ī','ĭ','į','ı','ĳ','ǐ','ȉ','ȋ','ɨ','ò','ó','ô','õ','ö','ø','ǿ','o','ō','ŏ','ő','œ','ǒ','ǫ','ǭ','ȍ','ȏ','ȫ','ȭ','ȯ','ȱ','u','ù','ú','ú','ü','ũ','ū','ŭ','ů','ű','ǔ','ǖ','ǘ','ǚ','ǜ','ų','ȕ','ȗ','y','ý','ÿ','ŷ','ȳ','ɏ','ʎ' */
    public static final Character[] LATIN_VOWELS = {'a','à','á','â','ã','ä','å','ā','ă','ą','ǎ','æ','ǣ','ǟ','ǡ','ǻ','ǽ','ȁ','ȁ','ȧ','e','è','é','ê','ë','ē','ĕ','ė','ę','ě','ǝ','ɘ','ə','ɇ','ȅ','ȇ','ȩ','i','ì','í','î','ï','ĩ','ī','ĭ','į','ı','ĳ','ǐ','ȉ','ȋ','ɨ','ò','ó','ô','õ','ö','ø','ǿ','o','ō','ŏ','ő','œ','ǒ','ǫ','ǭ','ȍ','ȏ','ȫ','ȭ','ȯ','ȱ','u','ù','ú','ú','ü','ũ','ū','ŭ','ů','ű','ǔ','ǖ','ǘ','ǚ','ǜ','ų','ȕ','ȗ','y','ý','ÿ','ŷ','ȳ','ɏ','ʎ'};
    /** 'a','e','i','o','u','y','w' */
    public static final Character[] ENGLISH_VOWELS = {'a','e','i','o','u','y','w'};
    // TODO: add a Greek vowel set
    // TODO: add other language vowel sets

    static final char CONTROL_CHAR = '\u001F';  // to indicate beginning and end of input; must not be in the data's alphabet


    private int minLength = DEFAULT_MIN_LENGTH;
    private int maxLength = DEFAULT_MAX_LENGTH;
    private String startFilter;
    private List<String> startFilterClusters; // holds the startFilter broken down into clusters
    private String endFilter;
    private List<String> endFilterClusters; // holds the endFilter broken down into clusters
    // todo: add a regex match option
    private Random random = new Random();


    private Set<Character> vowels = new HashSet<>(Arrays.asList(LATIN_VOWELS));



    private MultiOrderMarkovChain<String> clusterChain = new MultiOrderMarkovChain<>();
    private Integer longestClusterLength = 0;

    // setters
    public void setMinLength(int minLength) { this.minLength = minLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    public void setVowels(Character[] vowelset) { this.vowels = new HashSet<>(Arrays.asList(vowelset)); }
    public void setStartFilter(String startFilter) {
        this.startFilter = startFilter.toLowerCase();
        // also break it down into an array of vowel/consonant clusters
        startFilterClusters = clusterize(startFilter);
    }
    public void setEndFilter(String endFilter) {
        this.endFilter = endFilter.toLowerCase();
        // also break it down into an array of vowel/consonant clusters
        endFilterClusters = clusterize(endFilter);
    }
    public void setRandom(Random random) { this.random = random; }
    // getters
    public int getDatasetLength() { return clusterChain.getNumTrainedSequences(); }
    public int getMaxLength() { return maxLength; }
    public int getMinLength() { return minLength; }
    public Set<Character> getVowels() { return vowels; }
    public String getStartFilter() { return startFilter; }
    public String getEndFilter() { return endFilter; }


    /**
     * Utility function to break a String down into a {@code List<String>} of its component clusters.  Used internally
     * to pre-process the startFilter and endFilter, but may be of some use to consumers.
     * @param original a String
     * @return a List of vowel and consonant clusters in the order in which they were found
     */
    // TODO: test this alone
    public List<String> clusterize(String original) {
        logger.trace("original string: {}",original);
        List<String> clusters = new ArrayList<>();
        StringBuilder newCluster = new StringBuilder();
        char[] chars = original.toCharArray();
        boolean vowelCluster = vowels.contains(chars[0]);
        for(char c: chars) {
            if( (vowels.contains(c)) == vowelCluster ) {
                newCluster.append(c);
            } else {
                clusters.add(newCluster.toString());
                newCluster = new StringBuilder();
                newCluster.append(c);
                vowelCluster = !vowelCluster;
            }
        }
        clusters.add(newCluster.toString());
        logger.trace("clusterized string: {}",clusters);
        return clusters;
    }
    private List<String> addControlChars(List<String> clusterlist) {
        clusterlist.add(0,String.valueOf(CONTROL_CHAR));
        clusterlist.add(String.valueOf(CONTROL_CHAR));
        return clusterlist;
    }



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
     * @param vowelset the set of Characters that will form vowel clusters and separate consonant clusters
     * @return the same ClusterChainGenerator
     */
    public ClusterChainGenerator withVowels(Character[] vowelset) {
        this.vowels = new HashSet<>(Arrays.asList(vowelset));
        return this;
    }

    /**
     * @param startFilter a String that the beginning of the output must match, for example, a letter you want it to start with
     * @return the same ClusterChainGenerator
     */
    public ClusterChainGenerator withStartFilter(String startFilter) {
        setStartFilter(startFilter);
        return this;
    }

    /**
     * @param endFilter a String that the end of the output must match
     * @return the same ClusterChainGenerator
     */
    public ClusterChainGenerator withEndFilter(String endFilter) {
        setEndFilter(endFilter);
        return this;
    }

    /**
     * Ingest a new set of training data.
     */
    public ClusterChainGenerator train(Stream<String> rawWords) {
        clusterChain.train(
                rawWords
                    .map(String::toLowerCase)
                    .map(String::trim)
                    .map(this::clusterize)
                    .map(this::addControlChars)
        );
        //System.out.println(clusterChain);
        Optional<Integer> maxClusterLength = clusterChain.allKnownStates().stream().map(String::length).max( Comparator.comparing(Integer::valueOf) );
        if( maxClusterLength.isPresent() ) { this.longestClusterLength = maxClusterLength.get(); }

        logger.info("ingested a stream of training data. model derived from {} text strings containing {} clusters",clusterChain.getNumTrainedSequences(),clusterChain.getNumKnownState()-1);
        return this;
    }



    /**
     * @return true if the model was trained. Don't attempt to generate names from an untrained model, or you'll get an InvalidStateException!
     */
    public boolean isTrained() { return clusterChain.hasModel(); }




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
            String nextcluster;
            String returnText;
            List<String> word;
            int wordlength;
            do {
                // generate another word

                wordlength = 0;
                word = new ArrayList<>( Arrays.asList( String.valueOf(CONTROL_CHAR) ) );

                if ( startFilterClusters != null ) {
                    if ( !clusterChain.allKnownStates().containsAll(startFilterClusters) ) {
                        throw new IllegalArgumentException("startFilter contains cluster(s) not found in the training data");
                    } else {
                        word.addAll(startFilterClusters);
                    }
                }

                while ( word.size()==1 || !word.get(word.size()-1).equals(String.valueOf(CONTROL_CHAR)) ) {
                    // if near end and possible, add endfilter
                    if (( endFilterClusters != null ) && (wordlength >= maxLength - longestClusterLength - endFilter.length() - 1)) {
                        if (clusterChain.allPossibleNext(word.subList(word.size() - 1, word.size())).contains(endFilterClusters.get(0))) {
                            // if it is possible for the last randomly drawn cluster to transition to the specified end filter, add it and exit the loop
                            word.addAll(endFilterClusters);
                            word.add(String.valueOf(CONTROL_CHAR));
                            break;
                        }
                    }

                    // draw another cluster
                    nextcluster = clusterChain.unweightedRandomNext(word);
                    word.add( nextcluster );
                    wordlength += nextcluster.length();
                    logger.debug("word under development: {}",word);
                }

                returnText = String.join("",word);
                logger.debug("new candidate text string generated, about to check filters: {}", returnText);
            } while (
                // conditions for a re-roll
                    (returnText.length() < minLength + 2) ||
                    (returnText.length() - 2 > maxLength) ||
                    ((endFilter != null) && (!returnText.contains(endFilter + CONTROL_CHAR)))
            );
            returnText = returnText.substring(1, returnText.length() - 1); // strip off control characters
            logger.debug("new random text string generated and returned: {}", returnText);
            return returnText;
        }
    }



}
