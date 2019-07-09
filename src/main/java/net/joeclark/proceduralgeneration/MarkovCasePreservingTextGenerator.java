package net.joeclark.proceduralgeneration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An alternative implementation of MarkovTextGenerator that learns and reproduces upper/lower case usage in the
 * training data.  With a given dataset, this model may learn less effectively from the training data because it
 * builds separate models for "A" and "a" (to give an example) instead of combining observations.  However, this
 * implementation may be preferable if the input data has interesting uses of capitalization (such as names that
 * begin with "Mc" and "Mac" followed by capitals) that you want to re-generate.
 */
public class MarkovCasePreservingTextGenerator extends MarkovTextGenerator {

    private static final Logger logger = LoggerFactory.getLogger( MarkovCasePreservingTextGenerator.class );


    public MarkovCasePreservingTextGenerator() {
        logger.info("initialized new MarkovCasePreservingTextGenerator instance");
    }


    @Override
    public void setStartFilter(String startFilter) {
        this.startFilter = startFilter;
    }

    @Override
    public void setEndFilter(String endFilter) {
        this.endFilter = endFilter;
    }

    // the difference in this implementation is that we don't lowercase the training text before making observations
    @Override
    protected void makeObservations(Stream<String> rawWords) {
        rawWords.map(String::trim)
                .forEach( w -> {
                    this.alphabet.addAll(w.chars().mapToObj(s->(char)s).collect(Collectors.toList()));
                    analyzeWord(w);
                    datasetLength += 1;
                });
    }

}
