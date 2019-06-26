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

    public static final int DEFAULT_ORDER = 3;
    public static final float DEFAULT_PRIOR = 0.005F;
    public static final char CONTROL_CHAR = '#';  // to indicate beginning and end of input; must not be in the data's alphabet

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

    public int getDatasetLength() { return datasetLength; }
    public Set<Character> getAlphabet() { return alphabet; }
    public int getOrder() { return order; }
    public float getPrior() { return prior; }

    public void setOrder(int order) { this.order = order; }
    public void setPrior(float prior) { this.prior = prior; }

    public void train(Stream<String> rawWords) {
        alphabet.clear();
        alphabet.add(CONTROL_CHAR);
        datasetLength = (int) rawWords
                .map(String::toLowerCase)
                .peek( w -> this.alphabet.addAll(w.chars().mapToObj(s->(char)s).collect(Collectors.toList())))
                .peek(this::analyzeWord)
                .count();
        // alphabet is now populated
        // observations map is now populated
        System.out.println(observations);
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
        // model should now be populated
        System.out.println(model);
    }

    private void analyzeWord(String word) {
        System.out.println(word);
        word = word + CONTROL_CHAR;
        for(int o=1;o<=order;o++) {
            word = CONTROL_CHAR + word;
            for (int i = 0; i < word.length() - o; i++) {
                String prefix = word.substring(i, i + o);
                Character suffix = word.charAt(i + o);
                //System.out.println(prefix + " -> " + suffix);
                observations.computeIfAbsent(prefix, k -> new ArrayList<>()).add(suffix);
            }
        }

    }

    public String generateOne() throws IllegalStateException {
        if(datasetLength==0) {
            throw new IllegalStateException("model has not yet been trained");
        } else {
            return "Chester";
        }
    }

}
