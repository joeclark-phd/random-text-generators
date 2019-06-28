
package net.joeclark.proceduralgeneration;

/**
 * An interface for an object that produces randomly-generated text strings on demand. More information at https://github.com/joeclark-phd/random-text-generators
 */
public interface RandomTextGenerator {

    String generateOne();
    String generateOne(int minLength, int maxLength, String startsWith, String endsWith);

}
