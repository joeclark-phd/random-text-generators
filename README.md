# Random Text Generators by joeclark.net

[![Maven Central](https://img.shields.io/maven-central/v/net.joeclark.proceduralgeneration/randomtextgenerators.svg)](https://repo1.maven.org/maven2/net/joeclark/proceduralgeneration/randomtextgenerators/1.0/)
[![MIT License](https://img.shields.io/github/license/joeclark-phd/random-text-generators.svg)](https://choosealicense.com/licenses/mit/)


This package defines an interface and implementation of a procedural random text generator that can be used, for example, to generate character or place names for an adventure game.

## Usage

Maven users, add this dependency to your POM:

    <dependency>
      <groupId>net.joeclark.proceduralgeneration</groupId>
      <artifactId>randomtextgenerators</artifactId>
      <version>1.0</version>
    </dependency>

### RandomTextGenerator

The package offers an interface, **RandomTextGenerator**, with a single method:
 
- `String generateOne()` yields a new, procedurally-generated text string.

Currently there are four implementations of the interface: 

- MarkovTextGenerator
- MarkovTextCasePreservingGenerator
- RandomDrawGenerator
- DoubleTextGenerator

### MarkovTextGenerator

Quick start:

    RandomTextGenerator markov = new MarkovTextGenerator().train(myTextStream);
    System.out.println(markov.generateOne());
    
(or with all the optional configuration...)

    RandomTextGenerator markov = new MarkovTextGenerator().withStartFilter("J").withEndFilter("ia").withOrder(2).withPrior(0.01).withRandom(myRandom).train(myTextStream);
    System.out.println(markov.generateOne());

The big idea of Markov-chain random text generation is that you collect statistics on which characters follow other characters.  So if a particular language uses "th" a lot, "t" should often be followed by "h" in the randomly-generated text.  This class ingests a `Stream<String>` of training data to build up a Markov model, and uses it to generate new strings. However, the Markov-chain approach has a number of caveats:

First, looking only at two-character sequences isn't very sophisticated. The model would be smarter if you looked back more than one letter.  For example, your model could know that "ot" and "nt" are often followed by "h" but "st" is not. The problem with that is that you will have far fewer examples of every 3-character, 4-character, or n-character sequences in your training data than you will have of 2-character sequences.  If a sequence never occurs in your training data, it can never occur in your output.  Because there are fewer examples, your output will be less random.

Based on an algorithm [described by JLund3 at RogueBasin](http://roguebasin.roguelikedevelopment.org/index.php?title=Names_from_a_high_order_Markov_Process_and_a_simplified_Katz_back-off_scheme),  which I have also [implemented in Python](https://github.com/joeclark-phd/roguestate/blob/master/program/namegen.py), MarkovTextGenerator mitigates these issues in a couple of ways:

- It develops models of multiple "orders", that is, of multiple lengths of character sequences.  If the generator encounters a new sequence of three characters like "jav", it will first check if it has trained a model on that sequence.  If not, it will fall back to check if it has a model for "av", failing that, it will certainly have a model for what comes after "v".  I call this a 3rd-order model and it is the default.

- A Bayesian prior probability is added to every character in the alphabet in every model, so some truly random character sequences not seen in the training data are possible.  The alphabet is inferred from the training data, so any UTF-8 characters should be possible.  Increase the default "prior" to increase the randomness.

MarkovTextGenerator ignores case, converting your input text and filters to lowercase and returning lowercase strings.

#### MarkovTextCasePreservingGenerator

A subclass of MarkovTextGenerator that learns and reproduces upper/lower case usage in the training data.  With a given dataset, this model may learn less effectively from the training data because it builds separate models for "A" and "a" (to give an example) instead of combining observations.  However, it may be preferable if the input data has interesting uses of capitalization (such as names that begin with "Mc" and "Mac" followed by capitals) that you want to re-generate.  Any start/end filter(s) you configure will also be case-sensitive.

### RandomDrawGenerator

Quick start:

    RandomTextGenerator randomdraw = new RandomDrawGenerator().train(myTextStream);
    System.out.println(randomdraw.generateOne());

(or with all the optional configuration...)
    
    RandomTextGenerator randomdraw = new RandomDrawGenerator().withStartFilter("J").withEndFilter("ia").withRandom(myRandom).train(myTextStream);
    System.out.println(randomdraw.generateOne());

This generator simply draws a String at random from a `Stream<String>` of data fed into it.  Useful, if not very sophisticated.  Like MarkovTextGenerator, it allows the consumer to specify a desired minimum length, maximum length, start string, or end string, to filter the randomly-drawn text.

RandomDrawGenerator ignores case, converting your input text and filters to lowercase and returning lowercase strings.

### DoubleTextGenerator

Quick start:

    RandomTextGenerator doubletext = new DoubleTextGenerator(
            new MarkovTextGenerator().train(myFirstTextStream), // for example
            new RandomDrawGenerator().train(myOtherTextStream),  // for example
            "-"
        );
    System.out.println(doubletext.generateOne());

This generator combines the output of two other RandomTextGenerators, which could be useful if you want to generate a combination of first name and last name, or a hyphenated name.  Its constructor takes two RandomDrawGenerators and a String separator (if null, a single space is used by default).


## How to contribute

This package uses what I believe is the standard Maven file structure.  If you fork and clone the repo, your IDE should be able to locate the `pom.xml` and the source and test files.  To use maven to build and test it, simply

    mvn clean test
    
If you'd like to build the JARs and Javadocs, and install the repo to your computer's Maven repository, run

    mvn clean install
    
To contribute new code, corrections, etc., go ahead and make a pull request.  New procedural generation algorithms would be welcomed (code them them as new classes implementing the RandomTextGenerator interface) as would new tests.  If you have a great dataset of training data, please contribute that to the [examples](https://github.com/joeclark-phd/procedural-generation-examples) repository.

## Examples

I've built another project to run examples of output.  You can find it here: [joeclark-net/procedural-generation-examples](https://github.com/joeclark-phd/procedural-generation-examples)

### MarkovTextGenerator examples

With **MarkovTextGenerator** trained on a file of 1360 ancient Roman names (/src/test/resources/romans.txt), order 3, prior 0.005F, minLength 4, maxLength 12, I generated these 25 names in 181ms (including the training):

    caelis           domidus          pilianus        naso             recunobaro  
    potiti           cerius           petrentius      herenialio       caelius     
    venatius         octovergilio     favenaeus       surus            wasyllvianus
    nentius          soceanus         lucia           eulo             atric       
    caranoratus      melus            sily            fulcherialio     dula        
 
Setting the endFilter parameter to "a" filters out some passably female-sounding names.  ("ia","na", and "la" are also good filters):

    thea             supera           variwawrzma     vediskozma       isarina    
    tertia           lasca            juba            lucia            critula    
    nigelasca        vagnenna         armina          salatera         pulcita    
    cellasca         verula           ocessanga       cimylla          galla      
    mercuribosma     limeta           juba            pulcita          esdranicola

An alternative strategy is simply to train the generator on a single-sex dataset.  Here on the left, for example, are the results of training the generator with a file of 146 female Viking names, and on the right, a generator trained on 498 male Viking names.  (These training data can be found in [joeclark-net/procedural-generation-examples](https://github.com/joeclark-phd/procedural-generation-examples).)
    
    FEMALE:                                           MALE:
    
    øviyrsa          thorhild                         iorn             sigfast
    holm             hallgeot                         hersi            osvid
    drid             sibergljot                       øpir             vald
    halla            drid                             solmsteinund     hæmingjal
    tonna            sæurijorgärd                     sumävf           boel
    freyngtdrun      kiti                             slärdar          hundi
    grelod           ingulfrid                        kjxim            kætilbisld
    asvid            hard                             soälverkvott     sumarlid
    fastrid          gudland                          sigfus           kveldun
    hild             inga                             torsteinth       wary
    geirhild         ginna                            spjut            sjägfiæmund
    ingeltorg        ingrta                           hromund          orleif
    
Note that the **MarkovTextGenerator** automatically infers an alphabet from the training data, including Scandinavian characters that aren't on my keyboard.
