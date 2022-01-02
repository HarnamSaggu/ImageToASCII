import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.abs


val map = mapCharacters("@Ø&0\$8øD#üæSÞ¢£auc×*:°DA00u\\·,´ ")

fun main() {
//	val map = mutableMapOf<Double, Char>()
//	for (charIndex in (0..255)) {
//		if (!charIndex.toChar().toString().matches("[\u0000-\u001F]".toRegex())) {
//			val char = charIndex.toChar()
//			val image = generateImage(char)
//			map[(image.getAverageBrightness() * 1_000).toInt() / 1_000.0] = char
//		}
//	}
//	val sorted = map.toMutableMap().toSortedMap(compareByDescending { it })
//	var str = ""
//	sorted.values.forEach { str += it }
//	val file = File("C:/Users/theto/IdeaProjects/ImageToASCII/src/main/resources/chars/blob.txt")
//	file.writeText(str)

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

fun generateImage(char: Char): BufferedImage {
	val img = BufferedImage(50, 80, BufferedImage.TYPE_INT_RGB)
	val g2d = img.createGraphics()
	val font = Font("Consolas", Font.BOLD, 72)
	g2d.font = font
	val fm = g2d.fontMetrics
	g2d.color = Color.WHITE
	g2d.fillRect(0, 0, img.width, img.height)
	g2d.color = Color.BLACK
	g2d.drawString(char.toString(), 6, fm.ascent + 10)
	g2d.dispose()

	try {
		ImageIO.write(img, "png", File("C:\\Users\\theto\\IdeaProjects\\ImageToASCII\\src\\main\\resources\\chars\\${((img.getAverageBrightness() * 100_000).toInt() / 100_000.0).toString().padEnd(7, '0')}.png"))
	} catch (ex: IOException) {
		ex.printStackTrace()
	}

	return img
}
