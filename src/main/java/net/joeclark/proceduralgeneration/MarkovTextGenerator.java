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
    static final float DEFAULT_PRIOR = 0.01F;
    private static final char CONTROL_CHAR = '#';  // to indicate beginning and end of input; must not be in the data's alphabet

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

    public String generateOne() throws IllegalStateException {
        if(datasetLength==0) {
            throw new IllegalStateException("model has not yet been trained");
        } else {
            // TODO: now generate a real random name!
            return "Chester";
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
            if (randomRoll >= bestModel.get(c)) {
                randomRoll -= bestModel.get(c);
            } else {
                System.out.println(c);
                return c;
            }
        }
        System.out.println('!');
        return '!'; // this should never occur unless the prefix doesn't exist in the model
    }

}
