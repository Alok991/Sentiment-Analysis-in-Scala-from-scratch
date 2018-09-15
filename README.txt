Inferess Programming Challenge

Objectives:

# All parts to be covered with core libraries of Scala only.

Part 1
Download programmatically UTF-8 text of Sentiment, Inc. from https://www.gutenberg.org/ebooks/37653

Links Visited
https://alvinalexander.com/scala/scala-how-to-download-url-contents-to-string-file
http://www.scala-lang.org/api/2.12.3/scala/util/matching/Regex.html
https://stackoverflow.com/questions/6879427/scala-write-string-to-file-in-one-statement


Part 2
Sentence splitting, Tokenizing

Links Visited
https://twitter.github.io/scala_school/pattern-matching-and-functional-composition.html
http://www.scala-lang.org/api/2.12.3/scala/collection/mutable/ArrayBuffer.html

Part 3
Calculate TF.IDFs
As per discussion, We dont have mutilple documents to calculate TF.IDFs, Hence we can take a similar measure for the measuring how much rare a word is.

Links Visited
https://alvinalexander.com/scala/best-practice-option-some-none-pattern-scala-idioms


Part 4
Cluster sentences into 10 clusters using cosine similarity weighted by TF.IDF{in our case, the measure we got in part 3}
Print 10 sentences from each cluster.

Links Visited
http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.111.8125&rep=rep1&type=pdf
https://stats.stackexchange.com/questions/63558/difference-between-standard-and-spherical-k-means-algorithms
https://www.journaldev.com/9810/scala-primary-constructor-indepth
https://stackoverflow.com/questions/35064883/element-wise-sum-of-arrays-in-scala



Part 5
Deliver full project with README as zip. It should be possible for Inferess to run all parts in order.
Include url list for web articles studied along the way. A sample of urls used per part is fine.





Steps to run

0. Download the zip file

1. unzip inferess-programming-challenge.zip

2. cd ./inferess-programming-challenge

3. mvn clean

4. mvn package

5. scala -classpath ./clustering/target/clustering-1.0-SNAPSHOT.jar ./clustering/src/main/scala/com/inferess/clustering.scala



