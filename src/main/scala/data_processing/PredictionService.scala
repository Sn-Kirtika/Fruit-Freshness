package data_processing

import ai.onnxruntime.{OrtEnvironment, OrtSession}
import ai.onnxruntime._

import java.nio.FloatBuffer
import scala.jdk.CollectionConverters._
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths, StandardCopyOption}
import javax.imageio.ImageIO

object PredictionService {
  val data_dir = "data"
  val model_dir = "model"
  def main(args: Array[String]): Unit = {

    val dir = Paths.get(s"${data_dir}/input")
    val files = Files.list(dir).iterator().asScala.toList

    files.foreach { file =>

      val transformed_data = convert_image(file.toString)
      transformed_data match {
        case Some(data) => {
          val env = OrtEnvironment.getEnvironment
          val session = env.createSession(s"${model_dir}/fruit_model.onnx", new OrtSession.SessionOptions())

          val tensor = OnnxTensor.createTensor(
            env,
            FloatBuffer.wrap(data.flatten.map(_.toFloat)),
            Array(1L, 32L, 32L, 1L)
          )

          val inputName = session.getInputNames.iterator().next()
          val inputs = Map(inputName -> tensor).asJava
          val results = session.run(inputs)

          val output = results.get(0).getValue.asInstanceOf[Array[Array[Float]]]

          val prediction = output(0)(0)
          val rotten_fresh = if (prediction > .5) "FRESH" else "ROTTEN"

          val csv_file = new File(s"$data_dir/predictions.csv")
          val writer = new BufferedWriter(new FileWriter(csv_file, true))
          writer.write(s"${file.getFileName},$prediction,$rotten_fresh")
          writer.newLine()
          writer.close()

          Files.move(
            file,
            Paths.get(file.toString.replaceFirst("input", "archive")),
            StandardCopyOption.REPLACE_EXISTING
          )
        }
        case None => println(s"Error Reading : ${file}")
      }

    }

  }

  def convert_image(path: String): Option[Array[Array[Double]]] = {
    val image: BufferedImage = ImageIO.read(new File(path))
    if (image == null) return None
    val resized_image = resize(image)
    //ImageIO.write(resized_image, "PNG", new File("./Square Carpet Selfie.png"))
    //print("width: " + resized_image.getWidth())
    //println("height: " + resized_image.getHeight())
    Some(imageToMatrix(resized_image))
  }

  def resize(img: BufferedImage, newW: Int = 32, newH: Int = 32): BufferedImage = {
    val tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH)
    val resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB)
    val g2d = resized.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    g2d.dispose()
    resized
  }

  def imageToMatrix(img: BufferedImage): Array[Array[Double]] = {
    val w = img.getWidth
    val h = img.getHeight

    Array.tabulate(h, w) { (y, x) =>
      val pixel = img.getRGB(x, y)

      val r = (pixel >> 16) & 0xff
      val g = (pixel >> 8) & 0xff
      val b = pixel & 0xff

      val gray = 0.299 * r + 0.587 * g + 0.114 * b

      gray / 255.0
    }
  }

}
