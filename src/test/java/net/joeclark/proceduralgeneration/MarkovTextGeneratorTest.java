package net.joeclark.proceduralgeneration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MarkovTextGenerator...")
class MarkovTextGeneratorTest {

    MarkovTextGenerator markovTextGenerator;
    List<String> names = Arrays.asList("John", "Jane", "Jeremy", "Jeffrey");
    List<String> moreNames = Arrays.asList(
            "Aphrodite","Artemis","Athena","Apollo","Ares","Demeter","Dionysus","Hades","Hephaestus","Hermes",
            "Hestia","Poseidon","Zeus","Coeus","Crius","Cronus","Hyperion","Iapetus","Mnemosyne","Oceanus","Phoebe",
            "Rhea","Tethys","Theia","Themis","Asteria","Astraeus","Atlas","Aura","Clymene","Dione","Helios","Selene",
            "Eos","Epimetheus","Eurybia","Eurynome","Lelantos","Leto","Menoetius","Metis","Ophion","Pallas","Perses",
            "Prometheus","Styx" // from wikipedia's list of greek mythological figures
    );

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
    @DisplayName("can be instantiated and chained to the .train() function")
    void canBeInstantiatedWithAStream() {
        new MarkovTextGenerator().train(names.stream());
    }

    @Nested
    @DisplayName("when instantiated with a Stream<String>")
    class WhenInstantiatedWithStream {

        @BeforeEach
        void createInstanceWithStream() {
            markovTextGenerator = new MarkovTextGenerator().train(names.stream());
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

        @Test
        @DisplayName("always draws characters from the input alphabet")
        void alwaysDrawsCharactersFromAlphabet() {
            int numDangerChars = 0;
            String dangerString = Character.toString(MarkovTextGenerator.DANGER_CHAR);
            // the danger char was coming up very rarely due to, i think, rounding errors
            // in randomCharacter() where randomRoll would sometimes be greater than
            // sumOfWeights. to fix it, i changed prior to a double (to match
            // Random.random()) and changed >= to > in a key line. since it was so rare
            // to begin with, checking ten thousand random strings seems like a good way
            // to make sure the problem doesn't return
            for(int i=0;i<10000;i++) {
                numDangerChars += markovTextGenerator.generateOne().contains(dangerString) ? 1 : 0;
            }
            assertEquals(0,numDangerChars);
        }

    }

    @Nested
    @DisplayName("once trained")
    class OnceTrained {

        @BeforeEach
        void createInstanceWithStream() {
            markovTextGenerator = new MarkovTextGenerator().withOrder(3).withPrior(0.005F).train(names.stream());
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
            assertEquals(Arrays.asList('j','j','j','j'),markovTextGenerator.getObservations().get(Character.toString(MarkovTextGenerator.CONTROL_CHAR)), "should have observed lowercase j four times for '#'");
        }

        @Test
        @DisplayName("correctly tallies third-order observations")
        void countsThirdOrderObservations() {
            markovTextGenerator.setOrder(3);
            String prefix = Character.toString(MarkovTextGenerator.CONTROL_CHAR) + Character.toString(MarkovTextGenerator.CONTROL_CHAR) + Character.toString(MarkovTextGenerator.CONTROL_CHAR);
                    assertEquals(Arrays.asList('j','j','j','j'),markovTextGenerator.getObservations().get(prefix), "should have observed lowercase j four times for '###'");
        }

        @Test
        @DisplayName("random characters are drawn from the training alphabet")
        void randomCharactersAreDrawnFromAlphabet() {
            Character c = markovTextGenerator.randomCharacter("##j");
            assertTrue(markovTextGenerator.getAlphabet().contains(c),"Random character '"+c+"' not in training alphabet");
            assertNotEquals('!', (char) c, "randomCharacter() got to the end of its loop without finding a model");
        }

    }

    @Nested
    @DisplayName("randomly generated names")
    class RandomlyGeneratedNames {

        @BeforeEach
        void createInstanceWithStream() {
            markovTextGenerator = new MarkovTextGenerator().train(moreNames.stream());
        }

//        @Test
//        @DisplayName("tend to be different")
//        void tendToBeDifferent() {
//            // this test can fail due to legitimate randomness and should not be used in production
//            String name1 = markovTextGenerator.generateOne();
//            String name2 = markovTextGenerator.generateOne();
//            String name3 = markovTextGenerator.generateOne();
//            assertFalse((name1 == name2) && (name1 == name3),"three random names in a row were identical. this is possible with a small training set, but shouldn't happen often.  try running the test again.");
//        }

        @Test
        @DisplayName("can be specified to start with a given string")
        void canStartWithGivenString() {
            markovTextGenerator.setStartFilter("z");
            String name = markovTextGenerator.generateOne();
            assertTrue(name.startsWith("z"),"random name didn't start with given string");
        }

        @Test
        @DisplayName("will start with a lowercase string even if the startFilter input was uppercase")
        void canStartWithGivenStringRegardlessOfInputCase() {
            markovTextGenerator.setStartFilter("Z");
            String name = markovTextGenerator.generateOne();
            assertTrue(name.startsWith("z"),"random name didn't start with lowercase string");
        }

        @Test
        @DisplayName("can be specified to end with a given string")
        void canEndWithGivenString() {
            markovTextGenerator.setEndFilter("eus");
            String name = markovTextGenerator.generateOne();
            assertTrue(name.endsWith("eus"),"random name didn't end with given string");
        }

        @Test
        @DisplayName("will end with a lowercase string even if the endFilter input was uppercase")
        void canEndWithGivenStringRegardlessOfInputCase() {
            markovTextGenerator.setEndFilter("EUS");
            String name = markovTextGenerator.generateOne();
            assertTrue(name.endsWith("eus"),"random name didn't end with lowercase string");
        }

        @Test
        @DisplayName("can be held to a specified length range")
        void canBeHeldToSpecifiedLengthRange() {
            markovTextGenerator.setMinLength(5);
            markovTextGenerator.setMaxLength(6);
            String name = markovTextGenerator.generateOne();
            assertTrue(name.length()>=5 && name.length()<=6,"name was not in specified length range");
        }

        @Test
        @DisplayName("are predictable if the same random seed is used")
        void arePredictableWithAGivenRandomSeed() {
            markovTextGenerator.setRandom(new Random(12345));
            MarkovTextGenerator anotherMarkov = new MarkovTextGenerator().withRandom(new Random(12345)).train(moreNames.stream());
            assertEquals(markovTextGenerator.generateOne(),anotherMarkov.generateOne(),"two MarkovTextGenerators with the same random seed produced different strings");
        }

    }

    @Test
    @DisplayName("can be instantiated from a file")
    void canBeInstantiatedWithAFile() {
        String fileName = "src/test/resources/romans.txt";
        try(Stream<String> stream = Files.lines(Paths.get(fileName))) {
            new MarkovTextGenerator().train(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("can be instantiated by streaming a resource")
    void canBeInstantiatedByStreamingResource() {
        try(Stream<String> stream = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/romans.txt"))).lines()) {
            new MarkovTextGenerator().train(stream);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}