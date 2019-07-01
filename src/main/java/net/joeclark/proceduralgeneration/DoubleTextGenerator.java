package net.joeclark.proceduralgeneration;

/**
 * A RandomTextGenerator which combines the output of two other RandomTextGenerators.  This could be used, for example,
 * to combine a random first name and a random last name
 */
public class DoubleTextGenerator implements RandomTextGenerator {

    private RandomTextGenerator firstStringGenerator;
    private RandomTextGenerator secondStringGenerator;
    private String separator;

    private int firstMin;
    private int firstMax;
    private String firstStart;
    private String firstEnd;
    private int secondMin;
    private int secondMax;
    private String secondStart;
    private String secondEnd;

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

    /**
     * Sets parameters that will be used when the first RandomTextGenerator's generateOne() method is called.  Set any
     * parameter to zero or null (depending on type) to keep the implementation's default.
     */
    public void setFirstParameters(int minLength, int maxLength, String startsWith, String endsWith) {
        this.firstMin = minLength;
        this.firstMax = maxLength;
        this.firstStart = startsWith;
        this.firstEnd = endsWith;
    }

    /**
     * Sets parameters that will be used when the second RandomTextGenerator's generateOne() method is called.  Set any
     * parameter to zero or null (depending on type) to keep the implementation's default.
     */
    public void setSecondParameters(int minLength, int maxLength, String startsWith, String endsWith) {
        this.secondMin = minLength;
        this.secondMax = maxLength;
        this.secondStart = startsWith;
        this.secondEnd = endsWith;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public String generateOne() {
        return firstStringGenerator.generateOne( firstMin, firstMax, firstStart, firstEnd ) + separator +
                secondStringGenerator.generateOne( secondMin, secondMax, secondStart, secondEnd );
    }

    @Override
    public String generateOne(int minLength, int maxLength, String startsWith, String endsWith) {
        return generateOne();
    }
}
