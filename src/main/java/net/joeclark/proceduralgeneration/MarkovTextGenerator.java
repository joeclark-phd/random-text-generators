
package net.joeclark.proceduralgeneration;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class that uses a Markov model, trained on a {@code Stream<String>} of example text, to generate new,
 * random strings similar to the training data.  Based on <a href="http://roguebasin.roguelikedevelopment.org/index.php?title=Names_from_a_high_order_Markov_Process_and_a_simplified_Katz_back-off_scheme" target="_blank">an
 * algorithm described by JLund3 at RogueBasin</a>.
 */
public class MarkovTextGenerator implements RandomTextGenerator {

    /** {@value}*/
    public static final int DEFAULT_ORDER = 3;
    /** {@value}*/
    public static final double DEFAULT_PRIOR = 0.005D;
    /** {@value}*/
    public static final int DEFAULT_MIN_LENGTH = 4;
    /** {@value}*/
    public static final int DEFAULT_MAX_LENGTH = 12;

    static final char CONTROL_CHAR = '\u001F';  // to indicate beginning and end of input; must not be in the data's alphabet
    static final char DANGER_CHAR = '\u001C';  // a character that should never occur, and would indicate a failure in randomCharacter()

    private int order = DEFAULT_ORDER;
    private double prior = DEFAULT_PRIOR;
    private int minLength = DEFAULT_MIN_LENGTH;
    private int maxLength = DEFAULT_MAX_LENGTH;
    private String startFilter;
    private String endFilter;
    // todo: add a regex match option
    private Random random = new Random();

    private int datasetLength;
    private Set<Character> alphabet = new HashSet<>();
    private Map<String, List<Character>> observations = new HashMap<>();
    private Map<String, Map<Character,Double>> model = new HashMap<>();

    /**
     * Initialize a new MarkovTextGenerator. A new instance begins with the default values for order, prior,
     * minLength, maxLength, startFilter, and endFilter.  After initialization, you must train the model on a stream
     * of input Strings before generating names, optionally first setting parameters such as order and prior, e.g.:
     * <code>new MarkovTextGenerator.withOrder(3).withPrior(0.005D),train(streamOfStrings)</code>
     */
    public MarkovTextGenerator() {
    }

    /**
     * @param random a Random number generator to be used in generating text
     * @return the same MarkovTextGenerator
     */
    public MarkovTextGenerator withRandom(Random random) {
        this.random = random;
        return this;
    }

    /**
     * @param order the "order" of the model (default 3). higher orders are more sophisticated but slower to train, with diminishing returns
     * @return the same MarkovTextGenerator
     */
    public MarkovTextGenerator withOrder(int order) {
        this.order = order;
        return this;
    }

    /**
     * @param prior a Bayesian prior that injects some true randomness (default 0.005). increase it to make your text more random or to make up for sparse training data
     * @return the same MarkovTextGenerator
     */
    public MarkovTextGenerator withPrior(double prior) {
        this.prior = prior;
        return this;
    }

    /**
     * @param minLength the minimum length of output text you'll accept
     * @return the same MarkovTextGenerator
     */
    public MarkovTextGenerator withMinLength(int minLength) {
        this.minLength = minLength;
        return this;
    }

    /**
     * @param maxLength the maximum length of output text you'll accept
     * @return the same MarkovTextGenerator
     */
    public MarkovTextGenerator withMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * @param startFilter a String that the beginning of the output must match, for example, a letter you want it to start with
     * @return the same MarkovTextGenerator
     */
    public MarkovTextGenerator withStartFilter(String startFilter) {
        this.startFilter = startFilter.toLowerCase();
        return this;
    }

    /**
     * @param endFilter a String that the end of the output must match
     * @return the same MarkovTextGenerator
     */
    public MarkovTextGenerator withEndFilter(String endFilter) {
        this.endFilter = endFilter.toLowerCase();
        return this;
    }

    // for JUnit tests only
    int getDatasetLength() { return datasetLength; }
    Set<Character> getAlphabet() { return alphabet; }
    Map<String, List<Character>> getObservations() { return observations; }
    Map<String, Map<Character,Double>> getModel() { return model; }

    // setters
    public void setOrder(int order) { this.order = order; }
    public void setPrior(double prior) { this.prior = prior; }
    public void setMinLength(int minLength) { this.minLength = minLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    public void setStartFilter(String startFilter) { this.startFilter = startFilter.toLowerCase(); }
    public void setEndFilter(String endFilter) { this.endFilter = endFilter.toLowerCase(); }
    public void setRandom(Random random) { this.random = random; }
    // getters
    public int getOrder() { return order; }
    public double getPrior() { return prior; }
    public int getMaxLength() { return maxLength; }
    public int getMinLength() { return minLength; }
    public String getStartFilter() { return startFilter; }
    public String getEndFilter() { return endFilter; }

    /**
     * @return true if the model was trained or re-trained. Don't attempt to generate names from an untrained model, or you'll get an InvalidStateException!
     */
    public boolean isTrained() { return datasetLength > 0; }

    /**
     * Build the Markov chain model based on a training dataset.  Do this <i>after</i> setting the desired
     * order and prior, but <i>before</i> attempting to generate names.  If this function is called a second time,
     * it will erase the prior model and train a new one on the new stream of input.
     * @param rawWords a Stream of training data, e.g. from a file.  your random text output will look like the input data
     * @return the same MarkovTextGenerator
     */
    public MarkovTextGenerator train(Stream<String> rawWords) {

        datasetLength = 0;
        alphabet.clear();
        alphabet.add(CONTROL_CHAR);
        observations.clear();
        model.clear();

        rawWords.map(String::toLowerCase)
                .map(String::trim)
                .forEach( w -> {
                    this.alphabet.addAll(w.chars().mapToObj(s->(char)s).collect(Collectors.toList()));
                    analyzeWord(w);
                    datasetLength += 1;
                });
        // alphabet is now populated
        // observations map is now populated
        // datasetLength is now set

        observations.entrySet().forEach( s -> {
            String k = s.getKey();
            Map<Character,Long> frequencies = s.getValue().stream()
                    .collect(Collectors.groupingBy(c->c,Collectors.counting()));
            Map<Character,Double> relativeProbabilities = new HashMap<>();
            alphabet.forEach( a -> {
                relativeProbabilities.put(a, frequencies.containsKey(a) ? (double) frequencies.get(a) : prior);
            });
            model.put(k,relativeProbabilities);
        });
        // model is now populated

        return this;
    }

    // used in training, runs once for each String in the training set to add to the observations map
    private void analyzeWord(String word) {
        StringBuilder wordb = new StringBuilder(word);
        wordb.append(CONTROL_CHAR);
        for(int o=1;o<=order;o++) {
            wordb.insert(0,CONTROL_CHAR);
            for (int i = 0; i < wordb.length() - o; i++) {
                String prefix = wordb.substring(i, i + o);
                Character suffix = wordb.charAt(i + o);
                observations.computeIfAbsent(prefix, k -> new ArrayList<>()).add(suffix);
            }
        }

    }

    /**
     * @return a random string that based on a Markov chain model that is likely to be original but similar
     * to the strings in the training data. If you have set filters such as maximum and minimum length, or a
     * starting and ending sequence, be careful that those filters are not impossible given the training data
     * (for example, the instance infers an alphabet from the training data and will never generate letters
     * outside that alphabet).  You could end up with an infinite loop or an exception if your filters are
     * impossible to match.
     * @throws IllegalStateException if model has not been trained
     */
    @Override
    public String generateOne() {
        if(datasetLength==0) {
            throw new IllegalStateException("model has not yet been trained");
        } else {
            StringBuilder newName = new StringBuilder();

            do {
                newName.delete(0,newName.length());
                for (int i = 0; i < order; i++) {
                    newName.append(CONTROL_CHAR);
                }
                // initialize with startFilter rather than adding it later!
                if(startFilter!=null) { newName.append(startFilter.toLowerCase()); }

                do {
                    Character nextChar = randomCharacter(newName.substring(newName.length() - order));
                    newName.append(nextChar);
                } while (newName.charAt(newName.length() - 1) != CONTROL_CHAR);
            } while(
                    // conditions for a re-roll
                    (newName.length() < minLength+order+1) ||
                    (newName.length() > maxLength+order+1) ||
                    ((startFilter != null) && (newName.indexOf(CONTROL_CHAR + startFilter) == -1)) ||
                    ((endFilter != null) && (newName.indexOf(endFilter + CONTROL_CHAR) == -1))
            );
            //System.out.println(newName.substring(order,newName.length()-1));
            return newName.substring(order, newName.length() - 1); // strip off control characters
        }
    }

    Character randomCharacter(String prefix) {  // prefix length will equal this.order
        Map<Character,Double> bestModel = null;
        int o = order;
        // Find the highest-order model that exists given the last few characters.
        // For example, if "jav" occurs in the training data, that model will exist;
        // if not, maybe there'll be a model for "av", failing that, "v" should have
        // a model (as will every individual character in the training data)
        while(bestModel==null && o>0) {
            if (model.containsKey(prefix.substring(prefix.length()-o))) {
                bestModel = model.get(prefix.substring(prefix.length()-o));
            } else {
                o--;
            }
        }
        if(bestModel==null) {
            throw new IllegalStateException("randomCharacter() found a prefix for which it had no model");
        }
        double sumOfWeights = bestModel.values().stream().reduce(0.0D, (a,b) -> a+b);
        double randomRoll = sumOfWeights * random.nextDouble();
        for(Character c: bestModel.keySet()) {
            if (randomRoll > bestModel.get(c)) {
                randomRoll -= bestModel.get(c);
            } else {
                return c;
            }
        }
        return DANGER_CHAR; // this should never occur unless the prefix doesn't exist in the model
    }

}
