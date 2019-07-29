package net.joeclark.proceduralgeneration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClusterChainGenerator...")
class ClusterChainGeneratorTest {

    ClusterChainGenerator clusterChainGenerator;
    List<String> moreNames = Arrays.asList(
            "Aphrodite","Artemis","Athena","Apollo","Ares","Demeter","Dionysus","Hades","Hephaestus","Hermes",
            "Hestia","Poseidon","Zeus","Coeus","Crius","Cronus","Hyperion","Iapetus","Mnemosyne","Oceanus","Phoebe",
            "Rhea","Tethys","Theia","Themis","Asteria","Astraeus","Atlas","Aura","Clymene","Dione","Helios","Selene",
            "Eos","Epimetheus","Eurybia","Eurynome","Lelantos","Leto","Menoetius","Metis","Ophion","Pallas","Perses",
            "Prometheus","Styx" // from wikipedia's list of greek mythological figures
    );

    @DisplayName("Can ingest a Stream of Strings")
    @Test
    void CanIngestStreamOfStrings() {

        new ClusterChainGenerator().train(moreNames.stream());

    }

    @DisplayName("Can be trained from a file")
    @Test
    void CanBeTrainedFromAFile() {

        try(Stream<String> stream = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/romans.txt"))).lines()) {
            new ClusterChainGenerator().train(stream);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @DisplayName("Once trained...")
    @Nested
    class OnceTrained {

        @BeforeEach
        void Train() {
            try(Stream<String> stream = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/romans.txt"))).lines()) {
                clusterChainGenerator = new ClusterChainGenerator().train(stream);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

//        @DisplayName("Can generate some random text")
//        @Test
//        void CanGenerateSomeRandomText() {
//
//            for(int i=0;i<100;i++) {
//                System.out.println( clusterChainGenerator.generateOne() );
//            }
//
//        }

        @DisplayName("Can be returned with start and end filters set")
        @Test
        void CanBeReturnedWithFiltersSet() {
            clusterChainGenerator = clusterChainGenerator.withStartFilter("Marc").withEndFilter("ion");
        }

        @DisplayName("Starts with a specified startFilter")
        @Test
        void StartsWithASpecifiedStartFilter() {
            String filter = "marc";
            clusterChainGenerator.setStartFilter(filter);
            String word = clusterChainGenerator.generateOne();
            //System.out.println(word);
            assertTrue(word.startsWith(filter),"Generated word didn't match startFilter.");
        }

        @DisplayName("Throws exception if startfilter is impossible to follow")
        @Test
        void ThrowsExceptionIfStartFilterIsImpossible() {
            clusterChainGenerator.setStartFilter("mar!");
            assertThrows(IllegalArgumentException.class,() -> clusterChainGenerator.generateOne());
        }

        @DisplayName("Ends with specified endFilter")
        @Test
        void EndsWithSpecifiedEndFilter() {
            String filter = "ion";
            clusterChainGenerator.setEndFilter(filter);
            String word = clusterChainGenerator.generateOne();
            //System.out.println(word);
            assertTrue(word.endsWith(filter),"Generated word didn't match endFilter.");
        }

    }

}