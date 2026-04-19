import org.apache.spark.sql.SparkSession
import spark.implicits._

case class FruitImage(
                       path: String,
                       label: String,
                       fruit: String,
                       width: Int,
                       height: Int,
                       isUsable: Boolean,
                       split: String
                     )

case class GlobalStat(metric: String, value: Double)
case class LabelStat(label: String, count: Int)
case class LabelPercentage(label: String, percentage: Double)
case class FruitStat(fruit: String, count: Int)
case class FruitLabelStat(fruit: String, label: String, count: Int)
case class SplitStat(split: String, count: Int)
case class SplitLabelStat(split: String, label: String, count: Int)
case class DuplicateStat(path: String, count: Int)
case class OptionalTextStat(metric: String, value: String)
case class OptionalDoubleStat(metric: String, value: Double)

object Stats {

  def totalImages(images: List[FruitImage]): Int =
    images.size

  def countByLabel(images: List[FruitImage]): Map[String, Int] =
    images.groupBy(_.label).view.mapValues(_.size).toMap

  def percentageByLabel(images: List[FruitImage]): Map[String, Double] = {
    val total = images.size.toDouble
    if (total == 0) Map.empty
    else images.groupBy(_.label).view.mapValues(_.size * 100.0 / total).toMap
  }

  def countByFruit(images: List[FruitImage]): Map[String, Int] =
    images.groupBy(_.fruit).view.mapValues(_.size).toMap

  def countByFruitAndLabel(images: List[FruitImage]): Map[(String, String), Int] =
    images.groupBy(img => (img.fruit, img.label)).view.mapValues(_.size).toMap

  def usageRate(images: List[FruitImage]): Double =
    if (images.isEmpty) 0.0
    else images.count(_.isUsable).toDouble * 100.0 / images.size

  def rejectedImages(images: List[FruitImage]): Int =
    images.count(img => !img.isUsable)

  def countBySplit(images: List[FruitImage]): Map[String, Int] =
    images.groupBy(_.split).view.mapValues(_.size).toMap

  def countBySplitAndLabel(images: List[FruitImage]): Map[(String, String), Int] =
    images.groupBy(img => (img.split, img.label)).view.mapValues(_.size).toMap

  def duplicatePaths(images: List[FruitImage]): List[(String, Int)] =
    images
      .groupBy(_.path)
      .view
      .mapValues(_.size)
      .toList
      .filter { case (_, count) => count > 1 }
      .sortBy { case (_, count) => -count }

  def majorityLabel(images: List[FruitImage]): Option[String] =
    if (images.isEmpty) None
    else Some(images.groupBy(_.label).maxBy(_._2.size)._1)

  def averageWidth(images: List[FruitImage]): Option[Double] =
    if (images.isEmpty) None
    else Some(images.map(_.width).sum.toDouble / images.size)

  def averageHeight(images: List[FruitImage]): Option[Double] =
    if (images.isEmpty) None
    else Some(images.map(_.height).sum.toDouble / images.size)

  def averageResolution(images: List[FruitImage]): Option[Double] =
    if (images.isEmpty) None
    else Some(images.map(img => img.width * img.height).sum.toDouble / images.size)

  def usableImages(images: List[FruitImage]): Int =
    images.count(_.isUsable)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("FruitStats")
      .master("local[*]")
      .getOrCreate()


    val images = List(
      FruitImage("img1.jpg", "fresh",  "apple",  100, 100, true,  "train"),
      FruitImage("img2.jpg", "rotten", "apple",  110, 100, true,  "train"),
      FruitImage("img3.jpg", "fresh",  "banana", 120, 120, true,  "train"),
      FruitImage("img4.jpg", "rotten", "banana", 90,  95,  false, "test"),
      FruitImage("img5.jpg", "fresh",  "orange", 130, 140, true,  "test"),
      FruitImage("img2.jpg", "rotten", "apple",  110, 100, true,  "train")
    )

    val outputBasePath = "output/stats"

    val total = totalImages(images)
    val usable = usableImages(images)
    val rejected = rejectedImages(images)
    val usage = usageRate(images)
    val majority = majorityLabel(images)
    val avgWidth = averageWidth(images)
    val avgHeight = averageHeight(images)
    val avgResolution = averageResolution(images)

    val globalStats = List(
      GlobalStat("total_images", total.toDouble),
      GlobalStat("usable_images", usable.toDouble),
      GlobalStat("rejected_images", rejected.toDouble),
      GlobalStat("usage_rate_percent", usage)
    )

    val byLabel = countByLabel(images)
      .toList
      .sortBy { case (_, count) => -count }
      .map { case (label, count) => LabelStat(label, count) }

    val labelPercentages = percentageByLabel(images)
      .toList
      .sortBy { case (_, percentage) => -percentage }
      .map { case (label, percentage) => LabelPercentage(label, percentage) }

    val byFruit = countByFruit(images)
      .toList
      .sortBy { case (_, count) => -count }
      .map { case (fruit, count) => FruitStat(fruit, count) }

    val byFruitAndLabel = countByFruitAndLabel(images)
      .toList
      .sortBy { case ((fruit, label), _) => (fruit, label) }
      .map { case ((fruit, label), count) => FruitLabelStat(fruit, label, count) }

    val bySplit = countBySplit(images)
      .toList
      .sortBy { case (_, count) => -count }
      .map { case (split, count) => SplitStat(split, count) }

    val bySplitAndLabel = countBySplitAndLabel(images)
      .toList
      .sortBy { case ((split, label), _) => (split, label) }
      .map { case ((split, label), count) => SplitLabelStat(split, label, count) }

    val duplicates = duplicatePaths(images)
      .map { case (path, count) => DuplicateStat(path, count) }

    val textStats = List(
      OptionalTextStat("majority_label", majority.getOrElse("none"))
    )

    val numericOptionalStats =
      List(
        avgWidth.map(v => OptionalDoubleStat("average_width", v)),
        avgHeight.map(v => OptionalDoubleStat("average_height", v)),
        avgResolution.map(v => OptionalDoubleStat("average_resolution", v))
      ).flatten

    println("=== GLOBAL STATS ===")
    globalStats.foreach(println)

    println("=== COUNT BY LABEL ===")
    byLabel.foreach(println)

    println("=== PERCENTAGE BY LABEL ===")
    labelPercentages.foreach(println)

    println("=== COUNT BY FRUIT ===")
    byFruit.foreach(println)

    println("=== COUNT BY FRUIT AND LABEL ===")
    byFruitAndLabel.foreach(println)

    println("=== COUNT BY SPLIT ===")
    bySplit.foreach(println)

    println("=== COUNT BY SPLIT AND LABEL ===")
    bySplitAndLabel.foreach(println)

    println("=== DUPLICATES ===")
    duplicates.foreach(println)

    println("=== TEXT STATS ===")
    textStats.foreach(println)

    println("=== NUMERIC OPTIONAL STATS ===")
    numericOptionalStats.foreach(println)

    spark.createDataset(images)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/images")

    spark.createDataset(globalStats)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/global_stats")

    spark.createDataset(byLabel)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/by_label")

    spark.createDataset(labelPercentages)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/label_percentages")

    spark.createDataset(byFruit)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/by_fruit")

    spark.createDataset(byFruitAndLabel)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/by_fruit_and_label")

    spark.createDataset(bySplit)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/by_split")

    spark.createDataset(bySplitAndLabel)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/by_split_and_label")

    spark.createDataset(duplicates)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/duplicates")

    spark.createDataset(textStats)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/text_stats")

    spark.createDataset(numericOptionalStats)
      .write
      .mode("overwrite")
      .parquet(s"$outputBasePath/numeric_optional_stats")

    println(s"Tous les fichiers Parquet ont été écrits dans : $outputBasePath")

    spark.stop()
  }
}