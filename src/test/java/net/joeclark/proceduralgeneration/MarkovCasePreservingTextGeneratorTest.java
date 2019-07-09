package net.joeclark.proceduralgeneration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MarkovCasePreservingTextGenerator...")
class MarkovCasePreservingTextGeneratorTest {

    MarkovTextGenerator mcptGenerator; // builder functions such as "train" return the superclass

    List<String> moreNames = Arrays.asList(
            "Aphrodite","Artemis","Athena","Apollo","Ares","Demeter","Dionysus","Hades","Hephaestus","Hermes",
            "Hestia","Poseidon","Zeus","Coeus","Crius","Cronus","Hyperion","Iapetus","Mnemosyne","Oceanus","Phoebe",
            "Rhea","Tethys","Theia","Themis","Asteria","Astraeus","Atlas","Aura","Clymene","Dione","Helios","Selene",
            "Eos","Epimetheus","Eurybia","Eurynome","Lelantos","Leto","Menoetius","Metis","Ophion","Pallas","Perses",
            "Prometheus","Styx" // from wikipedia's list of greek mythological figures
    );

    @Test
    @DisplayName("Can be instantiated and trained")
    void CanBeInstantiatedAndTrained() {
        mcptGenerator = new MarkovCasePreservingTextGenerator().train(moreNames.stream());
    }

    @Test
    @DisplayName("Can be instantiated and then trained later")
    void CanBeInstantiatedWithoutTraining() {
        mcptGenerator = new MarkovCasePreservingTextGenerator();
        mcptGenerator.train(moreNames.stream());
    }

    @Nested
    @DisplayName("Randomly generated text...")
    class RandomlyGeneratedText {

        @BeforeEach
        void Instantiate() {
            mcptGenerator = new MarkovCasePreservingTextGenerator()
                    .withStartFilter("Z")
                    .withEndFilter("eus")
                    .train(moreNames.stream());
        }

        @Test
        @DisplayName("Starts with a given (capitalized) string")
        void StartsWithACapital() {
            assertTrue(mcptGenerator.generateOne().startsWith("Z"));
        }

        @Test
        @DisplayName("Ends with a given (lowercase) string")
        void EndsWithLowercase() {
            assertTrue(mcptGenerator.generateOne().endsWith("eus"));
        }



    }

}