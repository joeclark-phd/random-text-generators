package net.joeclark.proceduralgeneration;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkovTextGenerator implements RandomTextGenerator {

    private int datasetLength;
    private int order;
    private float prior;
    private Set<Character> alphabet = new HashSet<>();


    public MarkovTextGenerator(Stream<String> rawNames) {
        this(rawNames,1,0.005F); // sensible defaults?
    }
    public MarkovTextGenerator(Stream<String> rawNames, int order, float prior) {
        this.order = order;
        this.prior = prior;
        this.datasetLength = (int) rawNames
                .map(String::toLowerCase)
                .peek( n -> this.alphabet.addAll(n.chars().mapToObj(s->(char)s).collect(Collectors.toList())))
                .count();
        System.out.println(alphabet);
    }

    public String generateText() {
        return "Chester";
    }

    public int getDatasetLength() { return datasetLength; }
    public int getOrder() { return order; }
    public float getPrior() { return prior; }
    public Set<Character> getAlphabet() { return alphabet; }
}
