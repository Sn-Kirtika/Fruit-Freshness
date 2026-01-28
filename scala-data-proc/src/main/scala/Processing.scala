import java.io.File
import java.io.PrintWriter
import javax.imageio.ImageIO
import java.awt.Image
import java.awt.image.BufferedImage

object Processing {
  def main(args: Array[String]): Unit = {
    val fresh_dir = new File("../../data/raw/fresh")
    fresh_dir.list().foreach {file =>
      val transformed_data = convert_image(s"${fresh_dir}/${file}")
      transformed_data match {
        case Some(data) => write_file(data, s"../../data/processed/fresh/${file}.csv")
        case None => println(s"Error Reading : ${fresh_dir}/${file}")
      }
    }
    val rotten_dir = new File("../../data/raw/rotten")
    rotten_dir.list().foreach { file =>
      val transformed_data = convert_image(s"${rotten_dir}/${file}")
      transformed_data match {
        case Some(data) => write_file(data, s"../../data/processed/rotten/${file}.csv")
        case None => println(s"Error Reading : ${rotten_dir}/${file}")
      }
    }
  }

  def write_file(data: Array[Array[Double]], path: String): Unit = {
    val file = new File(path)
    val writer = new PrintWriter(file)
    data.foreach { row =>
      writer.println(row.mkString(","))
    }
    writer.close()
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

  def resize(img: BufferedImage, newW: Int = 20, newH: Int = 20): BufferedImage = {
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
      img.getRGB(x, y)
    }
  }
}
