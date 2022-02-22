# wmd4j

**wmd4j** is a Java library for computing [Word Mover's Distance](https://github.com/mkusner/wmd) (WMD) between two text documents. It provides the same functionality as [Word2Vec.wmdistance](https://radimrehurek.com/gensim/models/word2vec.html#gensim.models.word2vec.Word2Vec.wmdistance) in Gensim.

wmd4j depends on [deeplearning4j](https://deeplearning4j.konduit.ai/) [WordVectors interface](https://javadoc.io/doc/org.deeplearning4j/deeplearning4j-nlp/latest/org/deeplearning4j/models/embeddings/wordvectors/WordVectors.html) for word vector manipulation and uses an optimized version of [JFastEMD](https://github.com/telmomenezes/JFastEMD) (Earth Mover's Distance transportation problem) underneath, which is about 1.8x faster.

This is a forked and updated version of [crtomirmajer/wmd4j](https://github.com/crtomirmajer/wmd4j).

# Usage

```java

WordVectors vectors = WordVectorSerializer.loadStaticModel(new File(word2vecPath));
WordMovers wordMovers = WordMovers.builder().wordVectors(vectors).build();

wordMovers.distance("obama speaks to the media in illinois", "the president greets the press in chicago");
```

# Validation

wmd4j is validated against Gensim's wmdistance results on custom word2vec model.
