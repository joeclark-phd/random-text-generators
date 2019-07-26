package net.joeclark.proceduralgeneration;

import org.junit.jupiter.api.DisplayName;
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

}