package net.joeclark.proceduralgeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkovTextGenerator implements RandomTextGenerator {

    static final int DEFAULT_ORDER = 3;
    static final float DEFAULT_PRIOR = 0.005F;
    static final char CONTROL_CHAR = '#';  // to indicate beginning and end of input; must not be in the data's alphabet
    static final int DEFAULT_MIN_LENGTH = 4;
    static final int DEFAULT_MAX_LENGTH = 12;

    private int datasetLength;
    private int order;
    private float prior;
    private Set<Character> alphabet = new HashSet<>();
    private Map<String, List<Character>> observations = new HashMap<>();
    private Map<String, Map<Character,Float>> model = new HashMap<>();


    public MarkovTextGenerator() { this(DEFAULT_ORDER, DEFAULT_PRIOR); } // defaults for order and prior
    public MarkovTextGenerator(int order, float prior) {
        this.order = order;
        this.prior = prior;
    }
    public MarkovTextGenerator(Stream<String> rawWords) {
        this();
        this.train(rawWords);
    }
    public MarkovTextGenerator(int order, float prior, Stream<String> rawWords) {
        this(order,prior);
        this.train(rawWords);
    }

    // for JUnit tests only
    int getDatasetLength() { return datasetLength; }
    int getOrder() { return order; }
    float getPrior() { return prior; }
    Set<Character> getAlphabet() { return alphabet; }
    Map<String, List<Character>> getObservations() { return observations; }
    Map<String, Map<Character,Float>> getModel() { return model; }

    // public interface
    public void setOrder(int order) { this.order = order; }
    public void setPrior(float prior) { this.prior = prior; }
    public boolean isTrained() { return datasetLength > 0; }

    public void train(Stream<String> rawWords) {

        alphabet.clear();
        alphabet.add(CONTROL_CHAR);
        observations.clear();
        model.clear();

        datasetLength = (int) rawWords
                .map(String::toLowerCase)
                .map(String::trim)
                .peek( w -> this.alphabet.addAll(w.chars().mapToObj(s->(char)s).collect(Collectors.toList())))
                .peek(this::analyzeWord)
                .count();
        // alphabet is now populated
        // observations map is now populated

        observations.entrySet().forEach( s -> {
            String k = s.getKey();
            Map<Character,Long> frequencies = s.getValue().stream()
                    .collect(Collectors.groupingBy(c->c,Collectors.counting()));
            Map<Character,Float> relativeProbabilities = new HashMap<>();
            alphabet.forEach( a -> {
                relativeProbabilities.put(a, frequencies.containsKey(a) ? (float) frequencies.get(a) : prior);
            });
            model.put(k,relativeProbabilities);
        });
        // model is now populated
    }

    // used in training, runs once for each String in the training set to add to the observations map
    private void analyzeWord(String word) {
        word = word + CONTROL_CHAR;
        for(int o=1;o<=order;o++) {
            word = CONTROL_CHAR + word;
            for (int i = 0; i < word.length() - o; i++) {
                String prefix = word.substring(i, i + o);
                Character suffix = word.charAt(i + o);
                observations.computeIfAbsent(prefix, k -> new ArrayList<>()).add(suffix);
            }
        }

    }

    public String generateOne() {
        return generateOne(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH,null,null);  // defaults
    }
    public String generateOne(int minLength, int maxLength, String startsWith, String endsWith) throws IllegalStateException {
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
                    (newName.length() < minLength+order+1) ||
                    (newName.length() > maxLength+order+1) ||
                    ((startsWith == null) ? false : (newName.indexOf(CONTROL_CHAR+startsWith) == -1)) ||
                    ((endsWith == null) ? false : (newName.indexOf(endsWith+CONTROL_CHAR) == -1))
            );
            //System.out.println(newName.substring(order,newName.length()-1));
            return newName.substring(order, newName.length() - 1); // strip off control characters
        }
    }

    Character randomCharacter(String prefix) {  // prefix length will equal this.order
        Map<Character,Float> bestModel = null;
        int o = order;
        while(bestModel==null && o>0) {
            if (model.containsKey(prefix.substring(prefix.length()-o))) {
                bestModel = model.get(prefix.substring(prefix.length()-o));
            } else {
                o--;
            }
        }
        float sumOfWeights = bestModel.values().stream().reduce(0.0F, (a,b) -> a+b);
        float randomRoll = sumOfWeights * (float) Math.random();
        for(Character c: bestModel.keySet()) {
            if (randomRoll > bestModel.get(c)) {
                randomRoll -= bestModel.get(c);
            } else {
                return c;
            }
        }
        System.out.println('!');
        return '!'; // this should never occur unless the prefix doesn't exist in the model
    }

}
