package net.joeclark;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NameBlender should")
class NameBlenderTest {

    private final NameBlender nameBlender = new NameBlender(Stream.of("John","Jane","Jack","Jill"));

    @Test
    @DisplayName("generate a non-empty string")
    void generateANonEmptyString() {
        String name = nameBlender.generateName();
        assertTrue( name != null && name.length() > 0, "Name is null or empty" );
    }

    @Test
    @DisplayName("receive a stream containing names as input")
    void receiveRawNamesFile() {
        assertEquals(4, nameBlender.getDatasetLength(), "Didn't get the length of the stream right");
    }

    @Test
    @DisplayName("infer the alphabet from the input")
    void inferAlphabetFromInput() {
        assertTrue(nameBlender.getAlphabet().contains('j'),"Alphabet doesn't contain lower-case j");
        assertFalse(nameBlender.getAlphabet().contains('x'),"Alphabet contains x (which isn't in the input)");
    }
}