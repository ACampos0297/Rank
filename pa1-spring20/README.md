# PA-1

## Data

The first step is to unzip the `data.zip` file to view the data for this assignment.

```
$ unzip data.zip
# After it finishes, which may take a minute.
$ ls data
corpus        rel.dev      signal.dev   rel.train	signal.train 
```

## Gradle

We use (gradle)[https://gradle.org/] to build our java code. It takes care of installing Lucene and building the jar file which we will use to run our code.

```
$ ./gradlew build
# This will create a build directory which jar files and other contents.
$ ls build/libs
pa1.jar
```

For windows:

```
$ gradlew.bat build
```

## Index

We use Lucene to index the data corpus which is required to get document frequencies to implement the scorers. `IndexFiles.java` is responsible to build the index. It is not required but you are welcome to see how it works!

```
$ java -jar build/libs/pa1.jar index -docs data/corpus
# creates index dir
$ ls index
```

## Rank

To rank the documents corresponding to a query in the signal files:

```
# java -jar build/libs/pa1.jar <cmd> <data-file> <ranking-algo>
$ java -jar build/libs/pa1.jar rank data/signal.dev baseline
# results saved in ranked.txt
```

## Evaluate using NDCG

After ranking the documents, you can evaluate using the NDCG measure.

```
# java -jar build/libs/pa1.jar <cmd> <ranked-file> <rel-file> <output-file>
$ java -jar build/libs/pa1.jar ndcg ranked.txt data/rel.dev out.txt
```

## Source Dir

```
$ tree src/main/java
src/main/java
├── IndexFiles.java
├── NdcgMain.java
├── PA1.java
├── Rank.java
├── ds
│   ├── Document.java
│   ├── Pair.java
│   └── Query.java
├── scorer
│   ├── AScorer.java
│   ├── BM25Scorer.java
│   ├── BaselineScorer.java
│   └── VSMScorer.java
└── utils
    ├── IndexUtils.java
    └── LoadHandler.java
```

For your assignment, you only need to modify `AScorer.java`, `BM25Scorer.java` and `VSMScorer.java`. Below is breif explanation for each file:
1. `IndexFiles`: uses Lucene to create index of corpus.
2. `NdcgMain`: used to evaluate ranking results.
3. `PA1`: point of entry to all commands explained above.
4. `Rank`: employs `AScorer` to rank documents for a query.
5. `ds`: data-structures to read the data-files.
6. `scorer`: scorers used calculate similarity score required to rank.
7. `utils`:
	- `IndexUtils`: Wrappers around Lucene functions to provide APIs for document frequency.
	- `LoadHandler`: Load data files.
