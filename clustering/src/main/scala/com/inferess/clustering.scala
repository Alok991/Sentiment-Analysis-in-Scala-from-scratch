import java.io.PrintWriter
import java.io.File

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.{Source => sc}



object clustering {


  def main(args: Array[String]): Unit = {

     /*
     Part 1:  Download programmatically UTF-8 text of Sentiment, Inc. from https://www.gutenberg.org/ebooks/37653
      */
     val corpus : String = downloadCorpus()

     /*
     Part 2:  Sentence splitting, Tokenizing

     TODO: Sentence splitting can be better considering all the special cases like U.S. and Section1. etc.
      */

     val splitSentencesFromCorpus: String => Array[String] = cleanCorpus _ andThen extractParagraphs _ andThen extractSentences _


     // This Array of String contains all the sentences of corpus
     val sentencesList :Array[String] = splitSentencesFromCorpus(corpus)

     //This Array contains all the words(tokens) of corpus
     val tokens : Array[String] = extractWords(sentencesList)

     /*
     Part 3:  Calculate TF.IDFs
     As per discussion, We don't have multiple documents to calculate IDF part of TF.IDF
     Hence we can take a similar measure for the measuring how much rare a word is.
      */

     val rarityIndex  : mutable.Map[String, Double] = getRarityIndex(tokens)


     /*
     Part 4:  Cluster sentences into 10 clusters using cosine similarity weighted by TF.IDF{in our case, the measure we got in part 3}
              Print 10 sentences from each cluster.
      */
     val uniqueWordArray: Array[String] = rarityIndex.keys.toArray

     val allSentVectors : Array[SentenceVector] = getAllNormalisedSentVectors(uniqueWordArray,sentencesList,rarityIndex)

     val numOfIterations = 20

    clusterAndPrint(uniqueWordArray, rarityIndex, allSentVectors, numOfIterations)


   }

  def downloadCorpus(): String = {
    val CORPUS :String = "corpus"

    //could not find FileWriting in Scala so used java's File writer
    if(!new File(CORPUS).exists){
      //Given URL
      val URL : String = "https://www.gutenberg.org/ebooks/37653"

      //Downloading the Webpage Content
      println("Downloading corpus...")
      val pageContent = sc.fromURL(URL).mkString


      // the web link supplied does not contain the corpus but contains a link to it.
      // We will parse using Regex and see if we match "Plain Text UTF-8" and get the link
      val linkRegex = """<a\s+href=\"//(.+?)\".+>(.+)</a>""".r
      val allLinks = linkRegex.findAllIn(pageContent)

      // searching all the matched groups for our link
      while(allLinks.hasNext){
        if(allLinks.group(2).equals("Plain Text UTF-8")){
          val corpusContent = sc.fromURL("http://"+allLinks.group(1)).mkString
          new PrintWriter(CORPUS) { write(corpusContent); close }
        }
        allLinks.next
      }
      println("Corpus has been downloaded and saved!")
    }
    else{
      println("corpus found locally skipping download...")
    }
    return sc.fromFile(CORPUS).mkString
  }

  /*
  This function cleans the corpus and removes unecessary characters like *,-,_ etc.
   */
  def cleanCorpus(corpus: String): String = {
    // removing unecessary characters
    // I dont know why underscore is creating problem in scala regex (It works fine in python regex module (re))
    return corpus.replaceAll("""["\*-]"""," ").replaceAll("_","")

  }

  /*
  This function extract the paragraph from the corpus
   */
  def extractParagraphs(corpus: String): Array[String] = {
    return corpus.split("""(?:\r\n){2,}""")
  }

  /*
  This function return the an array of all sentences in corpus given the paragraphs
   */
  def extractSentences(paraList: Array[String]) : Array[String] = {
    var sentences = new ArrayBuffer[String]()
    for(para <- paraList){
      para.split("""\.""").map(_.replaceAll("\r\n"," ")).map(sentences.append(_))
    }
    // removiing all sentences with word count one or less
    return sentences.filter(_.trim.length>1).toArray
  }

  /*
  This function extracts words from sentences list
   */
  def extractWords(sentenceList : Array[String]) : Array[String] = {
    var wordList = new ArrayBuffer[String]()
    for(sent : String <- sentenceList){
      wordList.++=(getWordsFromSentence(sent))
    }
    return wordList.toArray
  }

  /*
  A utility function used in extractWords function
   */
  def getWordsFromSentence(sentence : String) : Array[String] = {
    return sentence.split(" ")
  }

  /*
  This function return the Rarity as map with words as key and rarity as value
  (Rarity defined as the inverse probability of occurrence of a word, rarer the word more is the rarity value)
   */
  def getRarityIndex(tokens : Array[String]) : mutable.Map[String, Double] = {
    val rarity = mutable.Map[String,Double]()

    // getOrElse function works on options if(None) return default value supplied else if (Some) returns actual value

    // counting thr number of occurence of a perticular word and storing in a map
    for(tok <- tokens)  rarity.update(tok,rarity.get(tok).getOrElse(0.0)+1.0)

    // instead of TF.IDF we are using inverse Probability of a token as rarity factor
    // more the rarity factor less frequent the word is

    val totalOccurences = rarity.values.sum*1.0/1000

    for((key,value) <- rarity)
      rarity.update(key,totalOccurences/value)

    return rarity
  }

  /*
  This function return the sentence vectors of length == number of unique words in corpus with rarity as values at the words occuring in sentence

  example: This is a Dog --> [rarity(a),0,0,...rarity(is)...,0,...,0,...rarity(dog)...]
   */
  def getAllNormalisedSentVectors(uniqueWordArray: Array[String], sentencesList : Array[String], rarityIndex  : mutable.Map[String, Double]) : Array[SentenceVector] = {

    // array to store all sentence vectors
    val allSentenceVectors = new Array[SentenceVector](sentencesList.length)
    var i=0;
    for(sentence <- sentencesList){
      // creating a sentence vector and adding it to list
      allSentenceVectors.update(i,new SentenceVector(uniqueWordArray,sentence,rarityIndex).normalise())
      i=i+1
    }
    return allSentenceVectors
  }

  /*
  This function return the cosine similarity measure of two sentence Vectors
   */
  def getCosine(vectorA : SentenceVector, vectorB : SentenceVector) : Double = {

    if(vectorA.sentVector.length!=vectorB.sentVector.length)
      return Int.MinValue

    val NormVectorA = vectorA.normalise()
    val NormVectorB = vectorB.normalise()


    var cosine=0.0;
    for(i <- 0 to NormVectorA.sentVector.length-1){
      cosine = cosine + (NormVectorA.sentVector(i) * NormVectorB.sentVector(i))
    }
    return cosine
  }

  /*
  This function returns the resultant vector of a cluster
   */
  def getCentroid(vectors: mutable.Set[SentenceVector]) : Array[Double] = {
    var centroidVec = new Array[Double](vectors.head.sentVector.length)

    // Adding all vectors to find the resultant vector
    for(vector <- vectors.toIterator){
      centroidVec = addVectors(centroidVec, vector.sentVector)
    }

    // normalise the new centroid vector
    val mod = math.sqrt(centroidVec.map(math.pow(_,2)).sum)
    centroidVec = centroidVec.map((_*1.0/mod))

    return centroidVec

  }

  /*
  a utility function to add two vectors
   */
  def addVectors(vecA: Array[Double], vecB: Array[Double]) : Array[Double] = {
    return vecA.zip(vecB).map { case (x, y) => x + y }
  }

  /*
  A utility function to print any 10 sentence from each cluster
   */
  def printTenSentFromEachCluster(vectorClusters: Array[mutable.Set[SentenceVector]]) : Unit = {
    var c=1
    for(currCluster <- vectorClusters){
      println("\nCluster no:  "+c)
      val setSlice = currCluster.slice(0, 10)
      var i=1
      for(sentVec <- setSlice){
        print(i+"---->")
        println(sentVec.sentence.trim)
        i+=1
      }
      c=c+1
    }
  }


  /*
  This is th main clustering function
  we are using k-Means spherical clustering algorithm to classify using cosine similarity (Link in README.txt)
   */
  def clusterAndPrint(uniqueWordArray: Array[String], rarityIndex  : mutable.Map[String, Double], allSentVectors : Array[SentenceVector], numOfInterations: Int) = {

    // number of cluster
    val numOfCluster = 10

    // An array for storing the clusters resultant vector (called as centroid here)
    var KMeansCentroidVectors : Array[SentenceVector] = Array.fill[SentenceVector](numOfCluster)(new SentenceVector(uniqueWordArray, "", rarityIndex))

    // randomize the initial centroid vector
    for(kmVec <- KMeansCentroidVectors) {
      kmVec.sentVector = kmVec.sentVector.map(_ + math.random.doubleValue)
      kmVec.normalise()
    }

    var currIter =0;

    // An array of mutable set to store all the sentence vectors of a that cluster
    var clusters : Array[mutable.Set[SentenceVector]] = new Array[mutable.Set[SentenceVector]](numOfCluster)
    for(i <- 0 to numOfCluster-1){
      clusters(i) = mutable.Set[SentenceVector]()
    }

    println("Started Clustering for "+numOfInterations+ " iterations")
    while(currIter < numOfInterations){
      println("Current Iteration :" + currIter)

      // For all sentence vector finding the maximum cosine similarity with each centroid vector and assigning it to cluster with max value
      for(sentVec <- allSentVectors){

        var maxSimilarity =0.0
        var similarCluster =0
        for(currCluster <- 0 to 9){
          val similarity = getCosine(sentVec,KMeansCentroidVectors(currCluster))
          if(similarity>=maxSimilarity){
            maxSimilarity = similarity
            similarCluster = currCluster
          }
        }
        clusters(similarCluster).add(sentVec)
      }

      // for all clusters calculating the new centrod vector
      for(i <- 0 to numOfCluster-1){
        KMeansCentroidVectors(i).sentVector = getCentroid(clusters(i))
      }

      currIter = currIter+1
    }
    println("Clustering Done!")

    // printing 10 sentences from each cluster
    printTenSentFromEachCluster(clusters)
  }
  /*
This class represent a sentence as a vector of size vocabulary
uniqueWordArray:  An array with unique words
sentence:   sentence who's vector will be created
 */
  class SentenceVector(uniqueWordArray: Array[String], currSentence : String, rarityIndex  : mutable.Map[String, Double]){

    var sentVector : Array[Double] = Array.fill[Double](uniqueWordArray.length)(0.0)
    val sentence = currSentence

    val wordsOfSent : Array[String] = getWordsFromSentence(currSentence)
    for(word <- wordsOfSent){
      sentVector.update(uniqueWordArray.indexOf(word),rarityIndex.get(word).getOrElse(0.0))
    }

    /*
     A vector to normalise the vector
     */
    def normalise () : SentenceVector = {
      val mod = math.sqrt(this.sentVector.map(math.pow(_,2)).sum)
      this.sentVector = this.sentVector.map(_*1.0/mod)
      return this
    }
  }
}
