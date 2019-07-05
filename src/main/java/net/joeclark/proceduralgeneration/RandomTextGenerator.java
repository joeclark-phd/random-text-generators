
package net.joeclark.proceduralgeneration;

/**
 * An interface for an object that produces randomly-generated text strings on demand. More information at https://github.com/joeclark-phd/random-text-generators
 */
public interface RandomTextGenerator {

    /**
     * Generate a random text string, using the implementation's default configuration.
     * @return a random text string
     */
    String generateOne();

}
