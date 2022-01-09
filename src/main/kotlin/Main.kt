import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.pow

fun main() {
	val filepath = "src/main/resources/liberty.png"
	val image = convertImageToASCII(filepath, charWidth = 2)
	val lines = image.split("\n".toRegex())
	var framedImage = "#${"-".repeat(lines[0].length)}#\n"
	for (line in lines) framedImage += "|$line|\n"
	framedImage += "#${"-".repeat(lines[0].length)}#"
	File("src/main/resources/libertyASCII.txt").writeText(framedImage)
}

fun convertImageToASCII(
	filepath: String,
	map: Map<Double, Char> = mapCharacters(),
	resolution: Int = 12,
	charWidth: Int = 3,
	groupModifiers: Map<Int, Double> = mapOf(4 to 0.1, 3 to -0.1),
	groupingTolerance: Double = 0.1,
): String {
	val file = File(filepath)
	if (!file.exists() || !file.isFile) return "File not found"
	if (map.isEmpty()) return "Map is empty"
	val image = ImageIO.read(file) ?: return "Error when reading image"

	val height = image.height / resolution
	val width = (image.width / resolution) * charWidth

	val asciiImage = MutableList(height) { MutableList(width) { 0.0 } }
	for (y in 0 until height) {
		for (x in 0 until image.width / resolution) {
			val averageBrightness =
				image.getSubimage(
					x * resolution,
					y * resolution,
					resolution,
					resolution
				).getAverageBrightness()
			val asciiString = map.keys.closestValue(averageBrightness) ?: 1.0
			for (i in 0 until charWidth) {
				asciiImage[y][(x * charWidth) + i] = asciiString
			}
		}
	}

	val asciiImageGrouped = asciiImage.map { it.toMutableList() }.toMutableList()
	val yIndices = 0 until height
	val xIndices = 0 until width
	for (y in yIndices) {
		for (x in xIndices) {
			if ((x - 1 !in xIndices) || (x + 1 !in xIndices) || (y - 1 !in yIndices) || (y + 1 !in yIndices))
				continue

			val thisVal = asciiImage[y][x]
			val calcLevel = { mod: Pair<Int, Int> ->
				if (abs(asciiImage[y + mod.first][x + mod.second] - thisVal) <= groupingTolerance)
					1
				else
					0
			}
			val indexes = listOf(Pair(0, -1), Pair(0, 1), Pair(-1, 0), Pair(1, 0))
			val level = indexes.sumOf { calcLevel(it) }
			asciiImageGrouped[y][x] = map.keys.closestValue(thisVal + (groupModifiers[level] ?: 0.0)) ?: thisVal
		}
	}

	return asciiImageGrouped.joinToString("\n") { it.map { x -> map[x] }.joinToString("") }
}

fun BufferedImage.getAverageBrightness(): Double {
	var total = 0.0
	for (y in 0 until this.height) {
		for (x in 0 until this.width) {
			total += this.getPixelBrightness(x, y)
		}
	}
	return total / (this.width * this.height)
}

fun BufferedImage.getPixelBrightness(x: Int, y: Int): Double {
	val color = this.getRGB(x, y)
	val red = color ushr 16 and 0xFF
	val green = color ushr 8 and 0xFF
	val blue = color ushr 0 and 0xFF
	return (red * 0.2126 + green * 0.7152 + blue * 0.0722) / 255.0
}

enum class Weight(val calc: (x: Double) -> Double) {
	LIGHT({ x -> x }),
	SEMI_LIGHT({ x -> -(1.0 / (1.32 * (x - 1.5))) - 0.5 }),
	SEMI_DARK({ x -> x.pow(3) })
}

enum class CharacterSet(val set: String) {
	STANDARD("@%#*+=-:. "),
	EXTENDED_STANDARD("\$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. "),
	COMPLEX("@Ø&0\\\$8øD#üæSÞ¢£auc×*:°DA00u\\\\·,´ ")
}

fun mapCharacters(characters: String = CharacterSet.STANDARD.set, calc: (x: Double) -> Double = Weight.LIGHT.calc): Map<Double, Char> {
	val size = characters.length.toDouble()
	return characters.toCharArray().associateBy {
		val x = characters.indexOf(it) / size
		calc(x)
	}
}

fun Set<Double>.closestValue(value: Double) = minByOrNull { abs(value - it) }
