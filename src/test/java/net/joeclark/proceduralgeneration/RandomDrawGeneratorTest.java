package net.joeclark.proceduralgeneration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RandomDrawGenerator...")
class RandomDrawGeneratorTest {

    RandomDrawGenerator randomDrawGenerator;
    List<String> moreNames = Arrays.asList(
            "Aphrodite","Artemis","Athena","Apollo","Ares","Demeter","Dionysus","Hades","Hephaestus","Hermes",
            "Hestia","Poseidon","Zeus","Coeus","Crius","Cronus","Hyperion","Iapetus","Mnemosyne","Oceanus","Phoebe",
            "Rhea","Tethys","Theia","Themis","Asteria","Astraeus","Atlas","Aura","Clymene","Dione","Helios","Selene",
            "Eos","Epimetheus","Eurybia","Eurynome","Lelantos","Leto","Menoetius","Metis","Ophion","Pallas","Perses",
            "Prometheus","Styx" // from wikipedia's list of greek mythological figures
    );


    @Test
    @DisplayName("can be instantiated with a stream of Strings")
    void canBeInstantiatedWithAStream() {
        randomDrawGenerator = new RandomDrawGenerator().train(moreNames.stream());
    }

    @Test
    @DisplayName("can be instantiated first and trained later")
    void canBeInstantiatedAndThenTrained() {
        randomDrawGenerator = new RandomDrawGenerator();
        randomDrawGenerator.train(moreNames.stream());
    }

    @Test
    @DisplayName("can be instantiated by chaining configuration setters")
    void canBeInstantiatedWithChaining() {
        randomDrawGenerator = new RandomDrawGenerator().withMinLength(3).withMaxLength(8).withStartFilter("H").withEndFilter("s").train(moreNames.stream());
    }

    @Nested
    @DisplayName("Once instantiated...")
    class OnceInstantiated {

        @BeforeEach
        void instantiateGenerator() {
            randomDrawGenerator = new RandomDrawGenerator().train(moreNames.stream());
        }

        @Test
        @DisplayName("Draws a random string from the training data (but lowercase)")
        void drawsAStringFromTrainingData() {
            String word = randomDrawGenerator.generateOne();
            //System.out.println(word);
            List<String> lcWords = moreNames.stream().map(String::toLowerCase).collect(Collectors.toList());
            assertTrue(lcWords.contains(word),"Random word was not in training data");
        }

        @Test
        @DisplayName("Draws a string with the specified start and end")
        void drawsAStringWithSpecifiedStartEnd() {
            randomDrawGenerator.setStartFilter("T");
            randomDrawGenerator.setEndFilter("s");
            String word = randomDrawGenerator.generateOne();
            //System.out.println(word);
            assertTrue(word.startsWith("t"),"Random word didn't start with lowercase 't'");
            assertTrue(word.endsWith("s"),"Random word didn't end with lowercase 's'");
        }

        @Test
        @DisplayName("Always draws random strings within specified length range")
        void drawsStringsWithSpecifiedLengthRange() {
            int min = 5;
            int max = 6;
            randomDrawGenerator.setMinLength(min);
            randomDrawGenerator.setMaxLength(max);
            String draw;
            for(int i=0;i<10;i++) {
                //try it ten times
                draw = randomDrawGenerator.generateOne();
                //System.out.println(draw);
                assertTrue(draw.length()>=min && draw.length()<=max,"Random draw didn't fall within specified length range");
            }
        }

        @Test
        @DisplayName("are predictable if the same random seed is used")
        void arePredictableWithAGivenRandomSeed() {
            randomDrawGenerator.setRandom(new Random(12345));
            RandomDrawGenerator anotherGenerator = new RandomDrawGenerator().withRandom(new Random(12345)).train(moreNames.stream());
            assertEquals(randomDrawGenerator.generateOne(),anotherGenerator.generateOne(),"two RandomDrawGenerators with the same random seed produced different strings");
        }



    }

}