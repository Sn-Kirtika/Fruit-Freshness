package data_processing

import java.io.File
import com.github.mjakubowski84.parquet4s._
import scala.io.Source

case class ImageSample(
              image: Array[Array[Double]],
              label: Int
            )

object DataFrameCreation {
  
  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "C:\\hadoop")

    val data_dir = "data"

    val fresh_dir = new File(data_dir + "/processed/fresh")

    val fresh_files = fresh_dir.list()//.slice(0, 5)

    val fresh_arrays = fresh_files.map { f =>
      readCsv(data_dir + "/processed/fresh/" + f)
    }

    val fresh_data: Seq[ImageSample] =
      fresh_arrays.map(arr => ImageSample(arr, 1))

    val rotten_dir = new File(data_dir + "/processed/rotten")

    val rotten_files = rotten_dir.list()//.slice(0, 5)

    val rotten_arrays = rotten_files.map { f =>
      readCsv(data_dir + "/processed/rotten/" + f)
    }

    val rotten_data: Seq[ImageSample] =
      rotten_arrays.map(arr => ImageSample(arr, 0))

    val combined: Seq[ImageSample] =
      fresh_data ++ rotten_data

    // Charger dans un parquet
    ParquetWriter.of[ImageSample].writeAndClose(com.github.mjakubowski84.parquet4s.Path(data_dir + "/dataframe/data.parquet"), combined)
  }

  def readCsv(path: String): Array[Array[Double]] = {
    val source = Source.fromFile(path)
    try {
      source.getLines().toArray.map { line =>
        line.split(",").map(_.toDouble)
      }
    } finally {
      source.close()
    }
  }



}
