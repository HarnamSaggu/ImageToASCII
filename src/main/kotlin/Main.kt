import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
	val image = ImageIO.read(File("src/main/resources/range.png"))
	for (i in 0 until image.width) {
		println(image.getPixelBrightness(i, 0))
	}
}

fun BufferedImage.getPixelBrightness(x: Int, y: Int): Double {
	val color = this.getRGB(x, y)
	val red = color ushr 16 and 0xFF
	val green = color ushr 8 and 0xFF
	val blue = color ushr 0 and 0xFF
	return (red * 0.2126 + green * 0.7152 + blue * 0.0722) / 255.0
}
