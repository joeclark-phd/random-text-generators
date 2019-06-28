# Random Text Generators by joeclark.net

This package defines an interface and implementation of a procedural random text generator that can be used, for example, to generate character or place names for an adventure game.

## RandomTextGenerator

The interface, **RandomTextGenerator**, could be used with more than one type of procedural generation method.  Its sole method is:
 
- `String generateOne()` yields a new, procedurally-generated text string.

Currently there is only a single implementation of the interface: 

### MarkovTextGenerator

The big idea of Markov-chain random text generation is that you collect statistics on which characters follow other characters.  So if a particular language uses "th" a lot, "t" should often be followed by "h" in the randomly-generated text.  This class ingests a `Stream<String>` of training data to build up a Markov model, and uses it to generate new strings. However, the Markov-chain approach has a number of caveats:

First, looking only at two-character sequences isn't very sophisticated. The model would be smarter if you looked back more than one letter.  For example, your model could know that "ot" and "nt" are often followed by "h" but "st" is not. The problem with that is that you will have far fewer examples of every 3-character, 4-character, or n-character sequences in your training data than you will have of 2-character sequences.  If a sequence never occurs in your training data, it can never occur in your output.  Because there are fewer examples, your output will be less random.

Based on an algorithm [described by JLund3 at RogueBasin](http://roguebasin.roguelikedevelopment.org/index.php?title=Names_from_a_high_order_Markov_Process_and_a_simplified_Katz_back-off_scheme),  which I have also [implemented in Python](https://github.com/joeclark-phd/roguestate/blob/master/program/namegen.py), MarkovTextGenerator mitigates these issues in a couple of ways:

- It develops models of multiple "orders", that is, of multiple lengths of character sequences.  If the generator encounters a new sequence of three characters like "jav", it will first check if it has trained a model on that sequence.  If not, it will fall back to check if it has a model for "av", failing that, it will certainly have a model for what comes after "v".  I call this a 3rd-order model and it is the default.

- A Bayesian prior probability is added to every character in the alphabet in every model, so some truly random character sequences not seen in the training data are possible.  The alphabet is inferred from the training data, so any UTF-8 characters should be possible.  Increase the default "prior" to increase the randomness.


## How to build and test

If you have Java 8 and Maven installed:

    git clone https://github.com/joeclark-net/random-text-generators.git
    cd random-text-generators
    mvn test

## Examples

I've built another project to run examples of output.  You can find it here: [joeclark-net/procedural-generation-examples](https://github.com/joeclark-phd/procedural-generation-examples)

With **MarkovTextGenerator** trained on a file of 1360 ancient Roman names (/src/test/resources/romans.txt), order 3, prior 0.005F, minLength 4, maxLength 12, I generated these 25 names in 181ms (including the training):

    caelis           domidus          pilianus        naso             recunobaro  
    potiti           cerius           petrentius      herenialio       caelius     
    venatius         octovergilio     favenaeus       surus            wasyllvianus
    nentius          soceanus         lucia           eulo             atric       
    caranoratus      melus            sily            fulcherialio     dula        
 
Setting the endsWith parameter to "a" filters out some passably female-sounding names.  ("ia","na", and "la" are also good filters):

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
