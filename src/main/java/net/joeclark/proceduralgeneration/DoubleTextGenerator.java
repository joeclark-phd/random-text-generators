package net.joeclark.proceduralgeneration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RandomTextGenerator which combines the output of two other RandomTextGenerators.  This could be used, for example,
 * to combine a random first name and a random last name
 */
public class DoubleTextGenerator implements RandomTextGenerator {

    private static final Logger logger = LoggerFactory.getLogger( DoubleTextGenerator.class );

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
        logger.info("Initialized new DoubleTextGenerator instance");
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public String generateOne() {
        String newText = firstStringGenerator.generateOne() + separator + secondStringGenerator.generateOne();
        logger.debug("new random text string generated and returned: {}", newText);
        return newText;
    }

}
