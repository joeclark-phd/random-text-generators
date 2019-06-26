package net.joeclark.proceduralgeneration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MarkovTextGenerator should")
class MarkovTextGeneratorTest {

    MarkovTextGenerator markovTextGenerator;
    List<String> names = Arrays.asList("John", "Jane", "Jeremy", "Jeffrey");

    @Test
    @DisplayName("can be instantiated with its no-argument constructor")
    void canBeInstantiatedWithNoArguments() {
        new MarkovTextGenerator();
    }

    @Nested
    @DisplayName("when instantiated with no arguments")
    class WhenNoArguments {

        @BeforeEach
        void createInstance() {
            markovTextGenerator = new MarkovTextGenerator();
        }

        @Test
        @DisplayName("should have the default order and prior")
        void shouldHaveDefaultOrderAndPrior() {
            assertEquals(MarkovTextGenerator.DEFAULT_ORDER,markovTextGenerator.getOrder());
            assertEquals(MarkovTextGenerator.DEFAULT_PRIOR,markovTextGenerator.getPrior());
        }

        @Test
        @DisplayName("knows it hasn't been trained")
        void knowsItHasntBeenTrained() {
            assertFalse(markovTextGenerator.isTrained(), "reports that it HAS been trained");
        }

        @Test
        @DisplayName("should throw exception if asked for a name")
        void shouldReturnNullName() {
            assertThrows(IllegalStateException.class,markovTextGenerator::generateOne,"didn't throw exception when asked for a name untrained");
        }

        @Test
        @DisplayName("can be trained after creation")
        void canBeTrainedAfterCreation() {
            int initialDatasetLength = markovTextGenerator.getDatasetLength();
            markovTextGenerator.train(names.stream());
            assertNotEquals(markovTextGenerator.getDatasetLength(),initialDatasetLength,"datasetLength did not increase through training");
            assertTrue(markovTextGenerator.isTrained(),"reports that it hasn't been trained");
        }

    }

    @Test
    @DisplayName("can be instantiated with a Stream<String> only")
    void canBeInstantiatedWithAStream() {
        new MarkovTextGenerator(names.stream());
    }

    @Nested
    @DisplayName("when instantiated with a Stream<String>")
    class WhenInstantiatedWithStream {

        @BeforeEach
        void createInstanceWithStream() {
            markovTextGenerator = new MarkovTextGenerator(names.stream());
        }

        @Test
        @DisplayName("should have the default order and prior")
        void shouldHaveDefaultOrderAndPrior() {
            assertEquals(markovTextGenerator.DEFAULT_ORDER,markovTextGenerator.getOrder());
            assertEquals(markovTextGenerator.DEFAULT_PRIOR,markovTextGenerator.getPrior());
        }

        @Test
        @DisplayName("knows it has been trained")
        void knowsItHasTrained() {
            assertEquals(names.size(), markovTextGenerator.getDatasetLength(), "Didn't get the length of the stream right");
            assertTrue(markovTextGenerator.isTrained(),"reports that it hasn't been trained");
        }

        @Test
        @DisplayName("will generate a non-empty string")
        void generateANonEmptyString() {
            String name = markovTextGenerator.generateOne();
            Assertions.assertTrue( name != null && name.length() > 0, "Name is null or empty" );
        }

    }

    @Nested
    @DisplayName("once trained")
    class OnceTrained {

        @BeforeEach
        void createInstanceWithStream() {
            markovTextGenerator = new MarkovTextGenerator(names.stream());
        }

        @Test
        @DisplayName("knows it has been trained")
        void knowsItHasTrained() {
            assertTrue(markovTextGenerator.isTrained(),"reports that it hasn't been trained");
        }

        @Test
        @DisplayName("infers the alphabet from the input")
        void inferAlphabetFromInput() {
            assertTrue(markovTextGenerator.getAlphabet().contains('j'),"Alphabet doesn't contain lower-case j");
            assertFalse(markovTextGenerator.getAlphabet().contains('x'),"Alphabet contains x (which isn't in the input)");
        }

        @Test
        @DisplayName("can be re-trained afresh")
        void canBeRetrainedAfresh() {
            markovTextGenerator.train(Stream.of("Xavier","Xena","Xenophon","Xerxes"));
            assertTrue(markovTextGenerator.getAlphabet().contains('x'),"x was not added to the alphabet");
            assertFalse(markovTextGenerator.getAlphabet().contains('j'),"j was not removed from the alphabet");
        }

        @Test
        @DisplayName("correctly tallies first-order observations")
        void countsFirstOrderObservations() {
            assertEquals(Arrays.asList('j','j','j','j'),markovTextGenerator.getObservations().get("#"), "should have observed lowercase j four times for '#'");
        }

        @Test
        @DisplayName("correctly tallies third-order observations")
        void countsThirdOrderObservations() {
            markovTextGenerator.setOrder(3);
            assertEquals(Arrays.asList('j','j','j','j'),markovTextGenerator.getObservations().get("###"), "should have observed lowercase j four times for '###'");
        }

        @Test
        @DisplayName("random characters are drawn from the training alphabet")
        void randomCharactersAreDrawnFromAlphabet() {
            Character c = markovTextGenerator.randomCharacter("##j");
            assertTrue(markovTextGenerator.getAlphabet().contains(c),"Random character '"+c+"' not in training alphabet");
            assertFalse(c=='!',"randomCharacter() got to the end of its loop without finding a model");
        }

    }

    @Nested
    @DisplayName("randomly generated names")
    class RandomlyGeneratedNames {

        @Test
        @DisplayName("tend to be different")
        void knowsItHasTrained() {
            assertTrue(false,"test not written yet");
        }


    }

}