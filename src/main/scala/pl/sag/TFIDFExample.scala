package pl.sag

import breeze.linalg.SparseVector
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix, MatrixEntry, RowMatrix}
// $example on$
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
// $example off$

object TFIDFExample {

  def cosSim(input: RDD[Seq[String]]) = {
    val hashingTF = new HashingTF()
    val tf = hashingTF.transform(input)
    tf.cache()
    val idf = new IDF().fit(tf)
    val tfidf = idf.transform(tf)

    val mat = new RowMatrix(tfidf)
    val irm = new IndexedRowMatrix(mat.rows.zipWithIndex.map {
      case (v, i) =>
        IndexedRow(i, v)
    })

    irm.toCoordinateMatrix.transpose.toRowMatrix.columnSimilarities
  }

  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir", "C:\\winutils")
    val conf = new SparkConf().setAppName("TFIDFExample").setMaster("local[2]").set("spark.executor.memory", "1g")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    // $example on$
    // Load documents (one per line).
    val documents: RDD[Seq[String]] = sc.textFile("test.txt")
      .map(_.split(" ").toSeq)

    documents.foreach(x => println(x))

    val cosSimr = cosSim(documents)

    val transformedRDD = cosSimr.entries.map{case MatrixEntry(row: Long, col:Long, sim:Double) => Array(row,col,sim).mkString(",")}

    println("Result:")
    val res = transformedRDD.collect()
    res.foreach(x => println(x))



//    val hashingTF = new HashingTF()
//    val tf: RDD[Vector] = hashingTF.transform(documents)
//
//    // While applying HashingTF only needs a single pass to the data, applying IDF needs two passes:
//    // First to compute the IDF vector and second to scale the term frequencies by IDF.
//    tf.cache()
//    val idf = new IDF().fit(tf)
//    val tfidf: RDD[Vector] = idf.transform(tf)
//
//    // spark.mllib IDF implementation provides an option for ignoring terms which occur in less than
//    // a minimum number of documents. In such cases, the IDF for these terms is set to 0.
//    // This feature can be used by passing the minDocFreq value to the IDF constructor.
//    val idfIgnore = new IDF(minDocFreq = 2).fit(tf)
//    val tfidfIgnore: RDD[Vector] = idfIgnore.transform(tf)
//    // $example off$
//
//    println("tfidf: ")
//    tfidf.foreach(x => println(x))
//
//    println("tfidfIgnore: ")
//    tfidfIgnore.foreach(x => println(x))
//
//    val irm = new IndexedRowMatrix(rowMatrix.rows.zipWithIndex.map {
//      case (v, i) => IndexedRow(i, v)
//    })
//
//    irm.toCoordinateMatrix.transpose.toRowMatrix.columnSimilarities

    sc.stop()
  }
}