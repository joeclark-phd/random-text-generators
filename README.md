# Random Text Generators by joeclark.net

[![Maven Central](https://img.shields.io/maven-central/v/net.joeclark.proceduralgeneration/randomtextgenerators.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22net.joeclark.proceduralgeneration%22%20AND%20a:%22randomtextgenerators%22)
[![MIT License](https://img.shields.io/github/license/joeclark-phd/random-text-generators.svg)](https://github.com/joeclark-phd/random-text-generators/blob/master/LICENSE.md)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7311d1355e094c29a6e05fd352e0cae2)](https://www.codacy.com/app/joeclark-phd/random-text-generators?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=joeclark-phd/random-text-generators&amp;utm_campaign=Badge_Grade)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/joeclark-phd/random-text-generators.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/joeclark-phd/random-text-generators/context:java)

API reference: [https://joeclark-phd.github.io/random-text-generators](https://joeclark-phd.github.io/random-text-generators/)

Example output: [https://github.com/joeclark-net/procedural-generation-examples](https://github.com/joeclark-phd/procedural-generation-examples)

This package defines an interface and implementation of a procedural random text generator that can be used, for example, to generate character or place names for an adventure game.

## Usage

Maven users, add this dependency to your POM:

    <dependency>
      <groupId>net.joeclark.proceduralgeneration</groupId>
      <artifactId>randomtextgenerators</artifactId>
      <version>1.0.1</version>
    </dependency>

### RandomTextGenerator

The package offers an interface, **RandomTextGenerator**, with a single method:
 
- `String generateOne()` yields a new, procedurally-generated text string.

Currently there are five implementations of the interface: 

- MarkovTextGenerator
- MarkovTextCasePreservingGenerator
- ClusterChainGenerator
- RandomDrawGenerator
- DoubleTextGenerator

### MarkovTextGenerator

Quick start:

    RandomTextGenerator markov = new MarkovTextGenerator().train(myTextStream);
    System.out.println(markov.generateOne());
    
(or with all the optional configuration...)

    RandomTextGenerator markov = new MarkovTextGenerator().withOrder(2).withPrior(0.01).withStartFilter("J").withEndFilter("ia").withMinLength(3).withMaxLength(15).withRandom(myRandom).train(myTextStream);
    System.out.println(markov.generateOne());

The big idea of Markov-chain random text generation is that you collect statistics on which characters follow other characters.  So if a particular language uses "th" a lot, "t" should often be followed by "h" in the randomly-generated text.  This class ingests a `Stream<String>` of training data to build up a Markov model, and uses it to generate new strings. However, the Markov-chain approach has a number of caveats:

First, looking only at two-character sequences isn't very sophisticated. The model would be smarter if you looked back more than one letter.  For example, your model could know that "ot" and "nt" are often followed by "h" but "st" is not. The problem with that is that you will have far fewer examples of every 3-character, 4-character, or n-character sequences in your training data than you will have of 2-character sequences.  If a sequence never occurs in your training data, it can never occur in your output.  Because there are fewer examples, your output will be less random.

Based on an algorithm [described by JLund3 at RogueBasin](http://roguebasin.roguelikedevelopment.org/index.php?title=Names_from_a_high_order_Markov_Process_and_a_simplified_Katz_back-off_scheme),  which I have also [implemented in Python](https://github.com/joeclark-phd/roguestate/blob/master/program/namegen.py), MarkovTextGenerator mitigates these issues in a couple of ways:

- It develops models of multiple "orders", that is, of multiple lengths of character sequences.  If the generator encounters a new sequence of three characters like "jav", it will first check if it has trained a model on that sequence.  If not, it will fall back to check if it has a model for "av", failing that, it will certainly have a model for what comes after "v".  I call this a 3rd-order model and it is the default.

- A Bayesian prior probability is added to every character in the alphabet in every model, so some truly random character sequences not seen in the training data are possible.  The alphabet is inferred from the training data, so any UTF-8 characters should be possible.  The default prior is a relative probability of 0.005.  Truly random output becomes more likely with a larger alphabet and with fewer trained character sequences, so you may want to play with this parameter: increase it to increase the randomness, or decrease it to make the output more like the training data.

MarkovTextGenerator ignores case, converting your input text and filters to lowercase and returning lowercase strings.

#### MarkovTextCasePreservingGenerator

A subclass of MarkovTextGenerator that learns and reproduces upper/lower case usage in the training data.  With a given dataset, this model may learn less effectively from the training data because it builds separate models for "A" and "a" (to give an example) instead of combining observations.  However, it may be preferable if the input data has interesting uses of capitalization (such as names that begin with "Mc" and "Mac" followed by capitals) that you want to re-generate.  Any start/end filter(s) you configure will also be case-sensitive.

### ClusterChainGenerator

Quick start:

    ClusterChainGenerator ccgen = new ClusterChainGenerator().train(myTextStream);
    System.out.println(ccgen.generateOne());
    
(or with all the optional configuration...)

    ClusterChainGenerator ccgen = new ClusterChainGenerator().withMaxOrder(2).withStartFilter("J").withEndFilter("ia").withMinLength(3).withMaxLength(15).withRandom(myRandom).train(myTextStream).andAddPriors(0.01);
    System.out.println(ccgen.generateOne());

A class that uses a vowel/consonant clustering algorithm to generate new random text.  Based loosely on [an algorithm described by Kusigrosz at RogueBasin](http://roguebasin.roguelikedevelopment.org/index.php?title=Cluster_chaining_name_generator), it scans input text for clusters of vowels and clusters of consonants, after converting it all to lowercase, keeping track of all clusters that have been observed to follow any given cluster.  For example, "Elizabeth" would yield clusters `#-e-l-i-z-a-b-e-th-#` and "Anne" would yield `#-a-nn-e-#` where "`#`" is a control character marking the start or end of a string. 

Much like MarkovTextGenerator, the implementation is based on a multi-order Markov chain (one difference is that priors aren't added by default and must be added explicitly after training). Internally we would keep track of the possible successors of each cluster, e.g.:

```
# -> [e,a]
e -> [l,th,#]
a -> [b,nn]
th -> [#]
...etc...
```

The `generateOne()` method takes a random walk through the cluster chain, only following paths that were found in the training data.  To continue our example, a new string could begin with "e" or "a", with equal likelihood, an "e" could be followed by "l", by "th", or by the end of a string, and so on.  With this training dataset of only two words, you could get a few different results, e.g.:

```
elizanneth
abelizanne
anneth
...etc...
```

Each newly generated candidate string is compared to filters (minLength, maxLength, startsWith, endsWith) and returned if it passes.  If the candidate string is filtered out, we generate another, until one passes. (Be aware that if you configure very difficult-to-match filters, generation time may increase greatly.  If you set up impossible-to-match filters, e.g. requiring characters that aren't in the training data set's alphabet, you will get an infinite loop.

### RandomDrawGenerator

Quick start:

    RandomTextGenerator randomdraw = new RandomDrawGenerator().train(myTextStream);
    System.out.println(randomdraw.generateOne());

(or with all the optional configuration...)
    
    RandomTextGenerator randomdraw = new RandomDrawGenerator().withStartFilter("J").withEndFilter("ia").withMinLength(3).withMaxLength(15).withRandom(myRandom).train(myTextStream);
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

### ClusterChainGenerator examples

The ClusterChainGenerator does what the MarkovTextGenerator does, but with clusters of consonants or vowels instead of individual characters.  Using the same training dataset of Roman names, I generated these 25 random Roman names in 83ms:

    festus           minus            frumerinus      clodius          aebuteo
    acitalina        docilusius       marcellus       rectus           placilius
    tertulus         brictius         viber           nepius           salvian
    burrus           stertinus        gordianus       sevtonius        protus
    allobrogicus     didicus          christianus     quietus          hosidonax
    
## Release notes

**Release 1.1**

- added `ClusterChainGenerator`

**Release 1.0.1**

- `MarkovNameGenerator.train()` no longer clears the alphabet and model before running.  Therefore, you can now train a model on multiple input streams.

**Release 1.0**

- initial launch including `MarkovTextGenerator`, `RandomDrawGenerator`, and `DoubleTextGenerator`
