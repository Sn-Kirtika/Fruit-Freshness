package data_processing

import ai.onnxruntime.{OnnxTensor, OrtEnvironment, OrtSession}
import com.github.mjakubowski84.parquet4s._
import java.util.Collections
import java.nio.FloatBuffer

object InferenceService {

  val ModelPath = "model/fruit_model.onnx"
  val DataPath = "data/dataframe"

  def main(args: Array[String]): Unit = {
    val env = OrtEnvironment.getEnvironment
    val sessionOptions = new OrtSession.SessionOptions()

    try {
      val session = env.createSession(ModelPath, sessionOptions)

      // Lecture unique du fichier Parquet
      val records = ParquetReader.as[ImageSample].read(Path(DataPath))

      try {
        records.foreach { sample =>
          val prediction = predict(session, env, sample.image)

          val status = if (prediction == 1) "FRESH" else "ROTTEN"
          // Formatage simple pour que Streamlit puisse lire la console facilement
          println(s"RESULT:${status}")
        }
      } finally {
        records.close()
      }

      session.close()
    } catch {
      case e: Exception =>
        println(s"ERROR:${e.getMessage}")
    } finally {
      env.close()
    }
  }

  def predict(session: OrtSession, env: OrtEnvironment, imageData: Array[Array[Double]]): Int = {
    val flatImage = imageData.flatten.map(_.toFloat)
    val buffer = FloatBuffer.wrap(flatImage)
    val shape = Array(1L, 32L, 32L, 1L)
    val inputTensor = OnnxTensor.createTensor(env, buffer, shape)

    try {
      val inputs = Collections.singletonMap("keras_tensor", inputTensor)
      val results = session.run(inputs)
      try {
        val output = results.get(0).getValue.asInstanceOf[Array[Array[Float]]]
        if (output(0)(0) > 0.5f) 1 else 0
      } finally {
        results.close()
      }
    } finally {
      inputTensor.close()
    }
  }
}