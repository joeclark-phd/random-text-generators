package net.joeclark.proceduralgeneration;

import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DoubleTextGenerator...")
class DoubleTextGeneratorTest {

    DoubleTextGenerator doubleTextGenerator;

    List<String> greekNames = Arrays.asList(
            "Aphrodite","Artemis","Athena","Apollo","Ares","Demeter","Dionysus","Hades","Hephaestus","Hermes",
            "Hestia","Poseidon","Zeus","Coeus","Crius","Cronus","Hyperion","Iapetus","Mnemosyne","Oceanus","Phoebe",
            "Rhea","Tethys","Theia","Themis","Asteria","Astraeus","Atlas","Aura","Clymene","Dione","Helios","Selene",
            "Eos","Epimetheus","Eurybia","Eurynome","Lelantos","Leto","Menoetius","Metis","Ophion","Pallas","Perses",
            "Prometheus","Styx" // from wikipedia's list of greek mythological figures
    );
    List<String> americanNames = Arrays.asList(
            "Smith","Johnson","Williams","Brown","Jones","Miller","Davis","Garcia","Rodriguez","Wilson","Martinez",
            "Anderson","Taylor","Thomas","Hernandez","Moore","Martin","Jackson","Thompson","White","Lopez","Lee",
            "Gonzalez","Harris","Clark","Lewis","Robinson","Walker","Perez","Hall","Young","Allen","Sanchez","Wright",
            "King","Scott","Green","Baker","Adams","Nelson","Hill","Ramirez","Campbell","Mitchell","Roberts","Carter",
            "Phillips","Evans","Turner","Torres"
    ); // top 50 most common American last names from wikipedia

    @Test
    @DisplayName("Can be instantiated with two RandomTextGenerators")
    void canBeInstantiated() {
        doubleTextGenerator = new DoubleTextGenerator(
                new MarkovTextGenerator().train(greekNames.stream()),
                new MarkovTextGenerator().train(americanNames.stream()),
                " "
        );
    }

    @Nested
    @DisplayName("Once instantiated")
    class onceInstantiated {

        @BeforeEach
        void instantiate() {
            doubleTextGenerator = new DoubleTextGenerator(
                    new MarkovTextGenerator().train(greekNames.stream()),
                    new MarkovTextGenerator().train(americanNames.stream()),
                    " "
            );
        }

        @Test
        @DisplayName("Uses the separator string")
        void usesTheSeparatorString() {
            doubleTextGenerator.setSeparator("-");
            String name = doubleTextGenerator.generateOne();
            assertTrue(name.contains("-"),"random name didn't contain the separator string");
        }


    }


}