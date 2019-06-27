package net.joeclark.proceduralgeneration;

public interface RandomTextGenerator {

    String generateOne();
    String generateOne(int minLength, int maxLength, String startsWith, String endsWith);

}
