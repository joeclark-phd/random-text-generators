package net.joeclark.proceduralgeneration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MarkovTextGenerator should")
class MarkovTextGeneratorTest {

    private final MarkovTextGenerator markovTextGenerator = new MarkovTextGenerator(Stream.of("John","Jane","Jack","Jill"));

    @Test
    @DisplayName("generate a non-empty string")
    void generateANonEmptyString() {
        String name = markovTextGenerator.generateText();
        Assertions.assertTrue( name != null && name.length() > 0, "Name is null or empty" );
    }

    @Test
    @DisplayName("receive a stream containing names as input")
    void receiveRawNamesFile() {
        assertEquals(4, markovTextGenerator.getDatasetLength(), "Didn't get the length of the stream right");
    }

    @Test
    @DisplayName("infer the alphabet from the input")
    void inferAlphabetFromInput() {
        assertTrue(markovTextGenerator.getAlphabet().contains('j'),"Alphabet doesn't contain lower-case j");
        assertFalse(markovTextGenerator.getAlphabet().contains('x'),"Alphabet contains x (which isn't in the input)");
    }
}