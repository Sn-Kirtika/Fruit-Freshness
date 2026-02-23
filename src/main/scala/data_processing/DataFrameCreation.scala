package data_processing

import java.io.File
import pd.DataFrame

object DataFrameCreation {
  
  def main(args: Array[String]): Unit = {
    val data_dir = "data"

    val fresh_dir = new File(data_dir + "/processed/fresh")

    val fresh_files = fresh_dir.list()//.slice(0,5)

    val fresh_dfs = fresh_files.map(f => DataFrame.io.csv(header = false, delimiter = ',').read(data_dir + "/processed/fresh/" + f))
    val fresh_arrays = fresh_dfs.map(df => df.columns.map(col => df(col).map(_.toString.toDouble).toArray).toArray)

    val df_fresh = DataFrame(image = fresh_arrays, label = Seq.fill(fresh_arrays.length)(1))

    val rotten_dir = new File(data_dir + "/processed/rotten")

    val rotten_files = rotten_dir.list()//.slice(0, 5)

    val rotten_dfs = rotten_files.map(f => DataFrame.io.csv(header = false, delimiter = ',').read(data_dir + "/processed/rotten/" + f))
    val rotten_arrays = rotten_dfs.map(df => df.columns.map(col => df(col).map(_.toString.toDouble).toArray).toArray)

    val df_rotten = DataFrame(image = rotten_arrays, label = Seq.fill(rotten_arrays.length)(0))

    val df_combined = DataFrame.union(df_fresh, df_rotten)
    println(df_combined)
    // Charger dans un parquet ou directement en Python
  }



}
