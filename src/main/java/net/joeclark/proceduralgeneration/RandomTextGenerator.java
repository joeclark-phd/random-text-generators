
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

    /**
     * This method allows users to specify a length range, start string, or end string for the output.  Be careful
     * with these; if you specify outcomes that are impossible, the implementation may throw an exception or go into
     * an infinite loop.  Some implementations may ignore these parameters entirely. If any parameter is zero or null,
     * the implementation's default for that parameter should be used.
     * @param minLength minimum length of text output. if 0, implementation's default is used
     * @param maxLength maximum length of text output. if 0, implementation's default is used
     * @param startsWith a String that the output must start with.  if null, the implementation's default is used.
     * @param endsWith a String that the output must start with.  if null, the implementatin's default is used.
     * @return a random text string according to your specifications
     */
    String generateOne(int minLength, int maxLength, String startsWith, String endsWith);

}
