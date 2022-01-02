import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs


val map = mapCharacters("@Ø&0\$8øD#üæSÞ¢£auc×*:°DA00u\\·,´ ")

fun main() {
	val filepath = "src/main/resources/liberty.png"
	val image = convertImageToASCII(filepath, map)
	val lines = image.split("\n".toRegex())
	var framedImage = "#${"-".repeat(lines[0].length)}#\n"
	for (line in lines) framedImage += "|$line|\n"
	framedImage += "#${"-".repeat(lines[0].length)}#"
	println(framedImage)
}

fun convertImageToASCII(filepath: String, map: Map<Double, Char>, resolution: Int = 12, charWidth: Int = 3): String {
	val file = File(filepath)
	if (!file.exists() || !file.isFile) return "File not found"
	if (map.isEmpty()) return "Map is empty"
	val image = ImageIO.read(file) ?: return "Error when reading image"

	var asciiImage = ""
	for (y in 0 until image.height / resolution) {
		for (x in 0 until image.width / resolution) {
			val averageBrightness = image.getSubimage(x * resolution, y * resolution, resolution, resolution).getAverageBrightness()
			asciiImage += map[map.keys.closestValue(averageBrightness)].toString().repeat(charWidth)
		}
		asciiImage += "\n"
	}

	return asciiImage.substring(0, asciiImage.length - 1)
}

private fun BufferedImage.getAverageBrightness(): Double {
	var total = 0.0
	for (y in 0 until this.height) {
		for (x in 0 until this.width) {
			total += this.getPixelBrightness(x, y)
		}
	}
	return total / (this.width * this.height)
}

fun mapCharacters(characters: String): Map<Double, Char> {
	val size = characters.length.toDouble()
	return characters.toCharArray().associateBy { characters.indexOf(it) / size }
}

fun Set<Double>.closestValue(value: Double) = minByOrNull { abs(value - it) }

fun BufferedImage.getPixelBrightness(x: Int, y: Int): Double {
	val color = this.getRGB(x, y)
	val red = color ushr 16 and 0xFF
	val green = color ushr 8 and 0xFF
	val blue = color ushr 0 and 0xFF
	return (red * 0.2126 + green * 0.7152 + blue * 0.0722) / 255.0
}
