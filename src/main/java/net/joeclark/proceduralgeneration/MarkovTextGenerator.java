
package net.joeclark.proceduralgeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    static final char CONTROL_CHAR = '#';  // to indicate beginning and end of input; must not be in the data's alphabet

    private int datasetLength;
    private int order;
    private double prior;
    private Set<Character> alphabet = new HashSet<>();
    private Map<String, List<Character>> observations = new HashMap<>();
    private Map<String, Map<Character,Double>> model = new HashMap<>();

    /** Initialize a MarkovTextGenerator with a training data set, accepting the default values for order and prior.
     * @param rawWords a Stream of training data, e.g. from a file.  your random text output will look like the input data
     * */
    public MarkovTextGenerator(Stream<String> rawWords) {
        this();
        this.train(rawWords);
    }
    /** A constructor that allows you to override the default settings.
     * @param rawWords a Stream of training data, e.g. from a file.  your random text output will look like the input data
     * @param order the "order" of the model (default 3). higher orders are more sophisticated but slower to train, with diminishing returns
     * @param prior a Bayesian prior that injects some true randomness (default 0.005). increase it to make your text more random or to make up for sparse training data
     * */
    public MarkovTextGenerator(int order, double prior, Stream<String> rawWords) {
        this(order,prior);
        this.train(rawWords);
    }
    /** Initialize a MarkovTextGenerator without training it, accepting default parameters.  Not recommended, because
     * someone might try to generate a random string from your untrained generator and get an IllegalStateException. */
    public MarkovTextGenerator() { this(DEFAULT_ORDER, DEFAULT_PRIOR); } // defaults for order and prior
    /** Initialize a MarkovTextGenerator without training it, specifying parameters.  Not recommended, because
     * someone might try to generate a random string from your untrained generator and get an IllegalStateException.
     * @param order the "order" of the model (default 3). higher orders are more sophisticated but slower to train, with diminishing returns
     * @param prior a Bayesian prior that injects some true randomness (default 0.005). increase it to make your text more random or to make up for sparse training data
     * */
    public MarkovTextGenerator(int order, double prior) {
        this.order = order;
        this.prior = prior;
    }

    // for JUnit tests only
    int getDatasetLength() { return datasetLength; }
    int getOrder() { return order; }
    double getPrior() { return prior; }
    Set<Character> getAlphabet() { return alphabet; }
    Map<String, List<Character>> getObservations() { return observations; }
    Map<String, Map<Character,Double>> getModel() { return model; }

    // public interface
    public void setOrder(int order) { this.order = order; }
    public void setPrior(double prior) { this.prior = prior; }

    /**
     * @return true if the model was trained or re-trained. Don't attempt to generate names from an untrained model, or you'll get an InvalidStateException!
     */
    public boolean isTrained() { return datasetLength > 0; }

    /** If you created the MarkovTextGenerator without a training set (which is not recommended), you can use this
     * method to train it later.  Or you can use this method to re-use an instance that's already been trained on
     * different data.  It will delete and overwrite the instance's existing model, with no memory of the earlier
     * training data.
     * @param rawWords a Stream of training data, e.g. from a file.  your random text output will look like the input data
     */
    public void train(Stream<String> rawWords) {

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
     * @return a random string with a (default) length of 4 to 12 characters.
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
     * @return a random string according to your specifications
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

        if(datasetLength==0) {
            throw new IllegalStateException("model has not yet been trained");
        } else {
            StringBuilder newName = new StringBuilder();
            do {
                newName.delete(0,newName.length());
                for (int i = 0; i < order; i++) {
                    newName.append(CONTROL_CHAR);
                }

                do {
                    Character nextChar = randomCharacter(newName.substring(newName.length() - order));
                    newName.append(nextChar);
                } while (newName.charAt(newName.length() - 1) != CONTROL_CHAR);
            } while(
                    // conditions for a re-roll
                    (newName.length() < min+order+1) ||
                    (newName.length() > max+order+1) ||
                    ((start != null) && (newName.indexOf(CONTROL_CHAR + start) == -1)) ||
                    ((end != null) && (newName.indexOf(end + CONTROL_CHAR) == -1))
            );
            //System.out.println(newName.substring(order,newName.length()-1));
            return newName.substring(order, newName.length() - 1); // strip off control characters
        }
    }

    Character randomCharacter(String prefix) {  // prefix length will equal this.order
        Map<Character,Double> bestModel = null;
        int o = order;
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
        double randomRoll = sumOfWeights * Math.random();
        for(Character c: bestModel.keySet()) {
            if (randomRoll > bestModel.get(c)) {
                randomRoll -= bestModel.get(c);
            } else {
                return c;
            }
        }
        return '!'; // this should never occur unless the prefix doesn't exist in the model
    }

}
