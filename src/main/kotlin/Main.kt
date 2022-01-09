import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Rectangle
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.pow

var pixelWidth: Int = 0
var pixelHeight: Int = 0
var font = Font("Consolas", Font.PLAIN, 12)
var fm: FontMetrics? = null
var repeat = 0

fun main() {
//	val map = mapCharacters("@Ø&0\$8øD#üæSÞ¢£auc×*:°DA00u\\·,´ ")
//	val map = mapCharacters("⠿⣿⣛⠿⠟⠛⠖⠌⠂⠀")
	val map =
		mapCharacters("\$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. "
		) { x -> 2.0.pow(x) - 1 }
//		)

	val folder = "C:/Users/theto/OneDrive/Desktop/100th/images_ver/"
	val frameFolder = "C:/Users/theto/OneDrive/Desktop/Every frame from Shrek 1/tool/"
	val resolution = 8
	repeat = 2

	val temp = ImageIO.read((File(frameFolder).listFiles() ?: return)[0])
	val graphics = temp.createGraphics()
	graphics.font = font
	fm = graphics.fontMetrics
	graphics.dispose()
	pixelWidth = (temp.width / resolution) * repeat * (fm?.charWidth('#') ?: 0)
	pixelHeight = (fm?.height ?: 0) * (temp.height / resolution)
	pixelWidth += pixelWidth % 2
	pixelHeight += pixelHeight % 2

	if (pixelWidth == 0 || pixelHeight == 0) {
		println("Oops")
		return
	}

	var time = System.currentTimeMillis()
	for (i in 6_207..10_000) { // TODO CHANGE THIS ****************************************************
		val img = toImage(convertImageToASCII(frameFolder + String.format("frame-%09d.jpeg", i),
			map, resolution, repeat))
		ImageIO.write(img, "jpeg", File(folder + "ASCII_$i.jpeg"))
		println("$i\t${System.currentTimeMillis() - time}")
		time = System.currentTimeMillis()
	}

//	val filepath = "C:\\Users\\theto\\OneDrive\\Desktop\\mara.jpg"
//	val image = convertImageToASCII(filepath, map, 6, 2)
//	val lines = image.split("\n".toRegex())
//	var framedImage = "#${"-".repeat(lines[0].length)}#\n"
//	for (line in lines) framedImage += "|$line|\n"
//	framedImage += "#${"-".repeat(lines[0].length)}#"
//	File("C:\\Users\\theto\\OneDrive\\Desktop\\mara.txt").writeText(framedImage)
}

fun toImage(string: String): BufferedImage {
	val img = BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_RGB)
	val g2d = img.createGraphics()
	g2d.font = font
	g2d.color = Color.WHITE
	g2d.fillRect(0, 0, 3360, 2150)
	g2d.color = Color.BLACK
	for ((i, strings) in string.trimEnd().split("\n".toRegex()).withIndex()) {
		g2d.drawString(strings, 0, (fm?.height ?: 0) * i)
	}
	g2d.dispose()
	return img
}

fun convertImageToASCII(filepath: String, map: Map<Double, Char>, resolution: Int = 12, charWidth: Int = 3): String {
	val file = File(filepath)
	if (!file.exists() || !file.isFile) return "File not found"
	if (map.isEmpty()) return "Map is empty"
	val image = ImageIO.read(file) ?: return "Error when reading image"

	var asciiImage = ""
	for (y in 0 until image.height / resolution) {
		for (x in 0 until image.width / resolution) {
			val averageBrightness =
				image.getSubimage(x * resolution, y * resolution, resolution, resolution).getAverageBrightness()
			asciiImage += map[map.keys.closestValue(averageBrightness)].toString().repeat(charWidth)
		}
		asciiImage += "\n"
	}

	return asciiImage.substring(0, asciiImage.length - 1)
}

fun Set<Double>.closestValue(value: Double) = minByOrNull { abs(value - it) }

private fun BufferedImage.getAverageBrightness(): Double {
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
//	return (red * 0.2126 + green * 0.7152 + blue * 0.0722) / 255.0
	return (red * 0.2126 + green * 0.9213 + blue * 0.0722) / 255.0
	// TODO CHANGE THIS ***********^^^^^^*******************************************************************
}

fun BufferedImage.invertImage() {
	for (y in 0 until this.height) {
		for (x in 0 until this.width) {
			val color = this.getRGB(x, y)
			val red = 255 - (color ushr 16 and 0xFF)
			val green = 255 - (color ushr 8 and 0xFF)
			val blue = 255 - (color ushr 0 and 0xFF)
			this.setRGB(x, y, Color(red, green, blue).rgb)
		}
	}
}

fun mapCharacters(characters: String, calc: (x: Double) -> Double = Weight.LIGHT.calc): Map<Double, Char> {
	val size = characters.length.toDouble()
	return characters.toCharArray().associateBy {
		val x = characters.indexOf(it) / size
		calc(x)
	}
}

enum class Weight(val calc: (x: Double) -> Double) {
	LIGHT({ x -> x }),
	SEMI_LIGHT({ x -> -(1.0 / (1.32 * (x - 1.5))) - 0.5 }),
	SEMI_DARK({ x -> x.pow(3) })
}
