import akka.event.slf4j.Logger
import com.formulasearchengine.mathosphere.mlp.MachineLearningRelationFinder
import com.formulasearchengine.mathosphere.mlp.features.FeatureVector
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput
import org.apache.flink.api.common.functions.{FlatMapFunction, MapFunction}
import org.apache.flink.api.scala._
import org.apache.flink.api.scala.utils.DataSetUtils
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.ml.common.LabeledVector
import org.apache.flink.ml.math.{DenseVector, Vector}
import org.apache.flink.ml.classification.SVM
import weka.core.Instances


object MachineLearner {
  /** random seeds obtained from random.org 2016-11-24 06:24:33 UTC - see https://xkcd.com/221/ */
  val random1 = List(99, 53, 70, 23, 86, 19, 84, 76, 34, 82, 32, 50, 47, 63, 54, 20, 74, 94, 46, 18, 22, 42, 100, 88, 96, 24, 31, 7, 12, 17, 26, 33, 29, 25, 79, 90, 56, 81, 4, 72, 27, 80, 60, 97, 5, 11, 67, 37, 2, 78, 6, 21, 51, 28, 91, 35, 8, 71, 14, 41, 52, 55, 62, 38, 30, 66, 58, 75, 93, 40, 48, 49, 89, 15, 69, 61, 65, 85, 9, 77, 98, 1, 43, 3, 83, 16, 36, 95, 44, 92, 59, 68, 45, 13, 39, 73, 87, 10, 57, 64)
  val random2 = List(41, 11, 50, 68, 2, 5, 52, 51, 69, 88, 21, 34, 57, 83, 63, 53, 37, 7, 10, 58, 77, 98, 89, 84, 29, 32, 6, 74, 31, 43, 39, 17, 59, 44, 20, 85, 60, 13, 33, 97, 71, 72, 48, 93, 73, 78, 15, 27, 25, 55, 12, 75, 67, 45, 79, 86, 82, 23, 61, 56, 3, 19, 1, 95, 94, 18, 36, 9, 40, 4, 22, 24, 92, 70, 35, 62, 16, 81, 90, 76, 28, 14, 47, 66, 30, 65, 54, 8, 26, 99, 42, 80, 100, 96, 49, 38, 87, 46, 91, 64)
  val random3 = List(58, 99, 72, 22, 24, 87, 94, 23, 50, 55, 47, 27, 49, 89, 18, 93, 86, 31, 97, 53, 85, 96, 69, 29, 1, 2, 39, 30, 71, 100, 9, 98, 21, 68, 19, 4, 25, 20, 84, 38, 10, 43, 95, 44, 65, 70, 35, 41, 79, 15, 63, 26, 13, 81, 77, 14, 5, 83, 37, 32, 40, 8, 48, 56, 45, 52, 33, 82, 3, 28, 78, 7, 46, 91, 67, 12, 92, 74, 80, 57, 17, 54, 36, 16, 60, 73, 61, 11, 90, 88, 62, 75, 6, 66, 51, 34, 64, 42, 59, 76)
  val random4 = List(19, 32, 51, 86, 8, 31, 77, 10, 42, 67, 99, 78, 81, 25, 22, 89, 68, 97, 73, 85, 90, 27, 49, 95, 38, 45, 63, 87, 98, 24, 93, 70, 30, 47, 13, 11, 100, 29, 34, 41, 36, 58, 26, 72, 91, 35, 12, 1, 53, 56, 33, 76, 74, 62, 23, 6, 7, 44, 50, 88, 79, 75, 15, 84, 21, 52, 64, 59, 82, 5, 16, 55, 39, 60, 96, 71, 20, 80, 94, 66, 48, 43, 3, 9, 46, 65, 14, 54, 2, 61, 17, 28, 92, 18, 83, 37, 57, 40, 69, 4)
  val random5 = List(4, 59, 62, 66, 26, 77, 5, 6, 70, 34, 87, 50, 94, 20, 84, 30, 47, 49, 74, 24, 21, 18, 38, 39, 56, 33, 82, 79, 41, 58, 22, 40, 80, 8, 64, 42, 2, 11, 89, 57, 73, 76, 98, 10, 48, 35, 91, 27, 65, 25, 63, 61, 53, 17, 14, 9, 96, 54, 31, 32, 16, 3, 68, 71, 99, 37, 88, 81, 43, 36, 75, 29, 12, 95, 85, 83, 78, 67, 7, 46, 60, 69, 97, 19, 86, 15, 55, 51, 92, 72, 13, 52, 90, 45, 23, 93, 28, 1, 100, 44)


  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.createCollectionsEnvironment
    env.getConfig.enableSysoutLogging()
    val machineLearningRelationFinder = new MachineLearningRelationFinder()
    val data: DataSet[WikiDocumentOutput] = new DataSet[WikiDocumentOutput](machineLearningRelationFinder.find(env.getJavaEnv));
    data.writeAsText("c:/tmp/output/data.txt", WriteMode.OVERWRITE)
    /*val dataWithIndex: DataSet[(Long, FeatureVector)] = data.zipWithIndex
    val partitions: Integer = 10;
    val predictions: List[(Double, Double)] = List[(Double, Double)]();
    for (i <- 0 until partitions) {
      val train: DataSet[LabeledVector] = dataWithIndex
        .filter(t => !(random1.slice(i * partitions, (i + 1) * partitions) contains t._2.getqId()))
        .map(t => new LabeledVector(t._2.truePositive, new DenseVector(t._2.values)))
      train.writeAsText("c:/tmp/output/train_" + i + ".txt", WriteMode.OVERWRITE)
      val test: DataSet[(Long, FeatureVector)] = dataWithIndex
        .filter(t => random1.slice(i * partitions, (i + 1) * partitions) contains t._2.getqId())
      test.writeAsText("c:/tmp/output/test_" + i + ".txt", WriteMode.OVERWRITE)
      val svm = SVM()
        .setBlocks(env.getParallelism)
        .setIterations(100)
        .setRegularization(0.001)
        .setStepsize(0.5)
        .setSeed(42)
      svm.fit(train)

      val predictionPairs = svm.evaluate(test.map(e => (new DenseVector(e._2.values), e._2.truePositive)))
      //predictionPairs.print()
      //val predictionWithIndex = predictionPairs.zipWithIndex
      // val classifications: DataSet[(Double, FeatureVector)] = predictionWithIndex.join(test).where(0).equalTo().map(e => (e._1._2._2, e._2._2))
      //val truePositives = classifications.filter(e => e._1 == e._2.truePositive)
      predictions.+:(predictionPairs);
      predictionPairs.writeAsText("c:/tmp/output/result_" + i + ".txt", WriteMode.OVERWRITE)
    }*/
    env.execute("foo")
    //val c = predictionPairs.cross(test).filter(a => a._1._1.equalsVector(a._2)).filter(a => !a._1._2.equals(a._2.label))
    //System.out.println(c.count())
    // val predJoined = predictionPairs.join(astroTest).where("_1").equalTo("_1._2")
    //svm.evaluate(predictionPairs).filter(a => a._1 != a._2).print()
    //predJoined.print()
  }
}