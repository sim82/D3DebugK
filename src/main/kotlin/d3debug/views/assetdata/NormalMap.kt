package d3debug.views.assetdata

import com.sun.prism.PixelFormat
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.image.WritablePixelFormat
import javafx.scene.paint.Color
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.math.sqrt


private data class Normal(var x: Short, var y: Short, var z: Short, var mag: Short)

private fun convertHeightFieldToNormalMap(pixels: ShortArray, w: Int, h: Int, wr: Int, hr: Int, scale: Float): Array<Color> {

    val nmap = Array<Color>(w * h, { Color.BLACK })
    val nmap2 = ByteArray(w * h * 4)


    val oneOver255 = 1.0f / 255.0f

    for (i in 0 until h) {
        for (j in 0 until w) {
            /* Expand [0,255] texel values to the [0,1] range. */
            val c = pixels[i * wr + j] * oneOver255
            /* Expand the texel to its right. */
            val cx = pixels[i * wr + (j + 1) % wr] * oneOver255
            /* Expand the texel one up. */
            val cy = pixels[((i + 1) % hr) * wr + j] * oneOver255
            val dcx = scale * (c - cx)
            val dcy = scale * (c - cy)

            /* Normalize the vector. */
            val sqlen = dcx * dcx + dcy * dcy + 1.0f
            val reciplen = 1.0f / sqrt(sqlen)
            val nx = dcy * reciplen
            val ny = -dcx * reciplen
            val nz = reciplen

            /* Repack the normalized vector into an RGB unsigned byte
         vector in the normal map image. */
            nmap[i * w + j] = Color.color((nx.toDouble() + 1.0) / 2.0, (ny.toDouble() + 1.0) / 2.0, (nz.toDouble() + 1.0) / 2.0)
//            nmap[i * w + j].red = nx //= (128 + 127 * nx).toShort()
//            nmap[i * w + j].y //= (128 + 127 * ny).toShort()
//            nmap[i * w + j].z //= (128 + 127 * nz).toShort()

            val offsBase = i * w + j
            nmap2[offsBase + 0] = (Byte.MAX_VALUE * nx).toByte()
            nmap2[offsBase + 1] = (Byte.MAX_VALUE * ny).toByte()
            nmap2[offsBase + 2] = (Byte.MAX_VALUE * nz).toByte()
            nmap2[offsBase + 3] = Byte.MIN_VALUE

            //      __chk( nmap[i*w+j].nz > 127 );

            /* The highest resolution mipmap level always has a
         unit length magnitude. */
            //nmap[i * w + j].mag = 255
        }
    }

//    FileOutputStream("/tmp/test.ppm").use {fs ->
//        fs.write("P3\n $w $h\n".toByteArray())
//        for( i in 0 until h) {
//            for( j in 0 until w) {
//                val offsBase = i * w + j
//                val x = nmap2[offsBase + 0].toInt() + Byte.MAX_VALUE
//                val y = nmap2[offsBase + 1].toInt() + Byte.MAX_VALUE
//                val z = nmap2[offsBase + 2].toInt() + Byte.MAX_VALUE
//                fs.write( "$x $y $z ".toByteArray())
//            }
//        }
//    }

    return nmap;
}

fun createNormalMapFromHeightMap(image: Image): Image {


    val width = image.width.toInt()
    val height = image.height.toInt()
    val inputBuf = ShortArray(width * height)
//    image.pixelReader.getPixels(0, 0, width, height, format., inputBuf, 0, width)

    val reader = image.pixelReader

    for (y in 0 until height) {
        for (x in 0 until width ) {
            val c = reader.getColor(x, y)
            inputBuf[y * width + x] = (c.red * 255.0).toShort()
        }
    }

    val nmap = convertHeightFieldToNormalMap(inputBuf, width, height, width, height, 1.0f)

    val normapMap = WritableImage(width, height)
    val normalWriter = normapMap.pixelWriter
    for (y in 0 until height) {
        for (x in 0 until width) {
            val n = nmap[y * width + x]
            normalWriter.setColor(x, y, n)
        }
    }
    return normapMap
//    return null
}
