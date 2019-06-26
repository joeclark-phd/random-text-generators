package net.joeclark.proceduralgeneration;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkovTextGenerator implements RandomTextGenerator {

    public static final int DEFAULT_ORDER = 1;
    public static final float DEFAULT_PRIOR = 0.005F;

    private int datasetLength;
    private int order;
    private float prior;
    private Set<Character> alphabet = new HashSet<>();


    public MarkovTextGenerator() { this(DEFAULT_ORDER, DEFAULT_PRIOR); } // defaults for order and prior
    public MarkovTextGenerator(int order, float prior) {
        this.order = order;
        this.prior = prior;
    }
    public MarkovTextGenerator(Stream<String> rawNames) {
        this();
        this.train(rawNames);
    }
    public MarkovTextGenerator(int order, float prior, Stream<String> rawNames) {
        this(order,prior);
        this.train(rawNames);
    }

    public int getDatasetLength() { return datasetLength; }
    public Set<Character> getAlphabet() { return alphabet; }
    public int getOrder() { return order; }
    public float getPrior() { return prior; }

    public void setOrder(int order) { this.order = order; }
    public void setPrior(float prior) { this.prior = prior; }

    public void train(Stream<String> rawNames) {
        this.alphabet.clear();
        this.datasetLength = (int) rawNames
                .map(String::toLowerCase)
                .peek( n -> this.alphabet.addAll(n.chars().mapToObj(s->(char)s).collect(Collectors.toList())))
                .count();
    }


    public String generateOne() throws IllegalStateException {
        if(datasetLength==0) {
            throw new IllegalStateException("model has not yet been trained");
        } else {
            return "Chester";
        }
    }

}
