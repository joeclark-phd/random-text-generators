# Random Text Generators by joeclark.net

This package defines an interface and implementation of a procedural random text generator that can be used, for example, to generate character or place names for an adventure game.

The interface, **RandomTextGenerator**, could be used with more than one type of procedural generation method.

Currently there is only a single implementation of the interface: **MarkovTextGenerator**.  

- That class ingests a stream of String data (for example, from a file) and trains a multiple-order Markov chain model on that data.  
- New strings are generated randomly based on the statistical frequencies of character sequences in the training data. So, for example, if the training data contains lots of occurrences of "th", the letter "t" will be followed by "h" in many of the random strings.
- The result is new strings that sound like they belong in the training set.  So if you feed it a dataset of ancient Greek names, you'll get new, original names that sound like they fit into that genre.
- A Bayesian prior is defined so that every character sequence in the alphabet of the training data has a small chance of occurring even if it doesn't occur in the training data; thereby injecting some true randomness and making up for possibly limited training data.

**MarkovTextGenerator** is based on an algorithm described by JLund3 at: http://roguebasin.roguelikedevelopment.org/index.php?title=Names_from_a_high_order_Markov_Process_and_a_simplified_Katz_back-off_scheme
and my own implementation of it in Python which can be found at https://github.com/joeclark-phd/roguestate/blob/master/program/namegen.py.

## Examples

With **MarkovTextGenerator** trained on a file of ancient Roman names (/src/test/resources/romans.txt), order 3, prior 0.005F, minLength 4, maxLength 12, I generated these 25 names in 181ms (including the training):

    caelis
    potiti
    venatius
    nentius
    caranoratus
    domidus
    cerius
    octovergilio
    soceanus
    melus
    pilianus
    petrentius
    favenaeus
    lucia
    sily
    naso
    herenialio
    surus
    eulo
    fulcherialio
    recunobaro
    caelius
    wasyllvianus
    atric
    dula
 
Setting the endsWith parameter to "a" filters out some passably female-sounding names.  ("ia","na", and "la" are also good filters):

    thea
    tertia
    nigelasca
    cellasca
    mercuribosma
    supera
    lasca
    vagnenna
    verula
    limeta
    variwawrzma
    juba
    armina
    ocessanga
    juba
    vediskozma
    lucia
    salatera
    cimylla
    pulcita
    isarina
    critula
    pulcita
    galla
    esdranicola

An alternative strategy is simply to train the generator on a single-sex dataset.  Here for example are the results of training the generator with a file of female Viking names (/src/test/resources/vikings_female.txt).  The **MarkovTextGenerator** automatically inferred the alphabet from the training data including all of the special Scandinavian characters.
    
    øviyrsa
    holm
    drid
    halla
    tonna
    freyngtdrun
    grelod
    asvid
    fastrid
    hild
    geirhild
    ingeltorg
    thorhild
    hallgeot
    sibergljot
    drid
    sæurijorgärd
    kiti
    ingulfrid
    hard
    gudland
    inga
    ginna
    ingrta
    skuld