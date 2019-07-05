package net.joeclark.proceduralgeneration;

/**
 * A RandomTextGenerator which combines the output of two other RandomTextGenerators.  This could be used, for example,
 * to combine a random first name and a random last name
 */
public class DoubleTextGenerator implements RandomTextGenerator {

    private RandomTextGenerator firstStringGenerator;
    private RandomTextGenerator secondStringGenerator;
    private String separator;

    /**
     * Instantiate a new DoubleTextGenerator
     * @param first Any implementation of RandomTextGenerator
     * @param second Any implementation of RandomTextGenerator
     * @param separator A string to place between the two random strings. If null, a single space will be used.
     */
    public DoubleTextGenerator(RandomTextGenerator first, RandomTextGenerator second, String separator) {
        this.firstStringGenerator = first;
        this.secondStringGenerator = second;
        this.separator = (separator == null) ? " " : separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public String generateOne() {
        return firstStringGenerator.generateOne() + separator +
                secondStringGenerator.generateOne();
    }

}
