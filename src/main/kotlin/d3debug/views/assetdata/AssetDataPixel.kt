package d3debug.views.assetdata

import d3cp.AssetCp
import d3debug.domain.AssetType
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import tornadofx.*
import java.io.InputStream
import java.nio.ByteBuffer


class ByteBufferBackedInputStream(private var buf: ByteBuffer) : InputStream() {

    init {
//        buf.reset()
    }

    override fun read(): Int {
        return if (!buf.hasRemaining()) {
            -1
        } else buf.get().toInt() and 0xFF
    }

    override fun read(bytes: ByteArray, off: Int, len: Int): Int {
        if (!buf.hasRemaining()) {
            return -1
        }

        val minLen = Math.min(len, buf.remaining())
        buf.get(bytes, off, minLen)
        return minLen
    }
}

fun swapRgba(buf: ByteArray) {

    for (i in 0 until buf.size step 4) {
        val r = buf[i]
        val b = buf[i + 2]

        buf[i] = b
        buf[i + 2] = r
    }
}

fun swapRgb(buf: ByteArray) {
    for (i in 0 until buf.size step 3) {
        val r = buf[i]
        val b = buf[i + 2]

        buf[i] = b
        buf[i + 2] = r
    }
}

fun ByteArray.swapAt(i1: Int, i2: Int) {
    val tmp = this[i1]
    this[i1] = this[i2]
    this[i2] = tmp
}

fun ByteArray.flipScanlines(stride: Int) {
    for (y in 0 until size / 2 step stride) {
        for (x in 0 until stride) {
            swapAt(y + x, size - stride - y + x)
        }
    }
}



class AssetDataPixel(val reader: AssetCp.AssetPixelData.Reader) : AssetData() {
    override val assetType: AssetType = AssetType.Image
    override val previewSize = SimpleDoubleProperty(this, "previewSize", 100.0)

    init {
        previewNode.set( ImageView().apply {
            imageProperty = imageProperty()
            fitWidthProperty().bind(previewSize)
            fitHeightProperty().bind(previewSize)
        })
    }



    lateinit var imageProperty : ObjectProperty<Image?>// = SimpleObjectProperty<Image>(this, "image", null)
    var image by imageProperty


    private fun createImageFromReader(): Image? {
        return when (reader.which()) {
            AssetCp.AssetPixelData.Which.STORED -> createImageFromPixelDataStored(reader.stored)
            AssetCp.AssetPixelData.Which.COOKED -> createImageFromPixelDataCooked(reader.cooked)
            else -> null
        }

    }


    enum class AssetCookedPixelFormat(val pixelSize: Int, val byteBgraInstance: PixelFormat<ByteBuffer>?, val fixBytes: ((ByteArray) -> Unit)?) {
        RGBA(4, PixelFormat.getByteBgraInstance(), ::swapRgba),
        RGB(3, PixelFormat.getByteRgbInstance(), null),
        BGR(3, PixelFormat.getByteRgbInstance(), ::swapRgb),
        Luminance(1, null, null),
        Dxt1(0, null, null),
        Bc3(0, null, null),
        Undefined(0, null, null)
    }

    private fun intToAssetPixelFormat(v: Int) = AssetCookedPixelFormat.values()[v.coerceIn(0 until AssetCookedPixelFormat.values().size)]
//
//    fun toJavafxPixelformat(f: AssetCookedPixelFormat) = when (f) {
//        AssetCookedPixelFormat.RGBA -> PixelFormat.getByteBgraInstance()
//        AssetCookedPixelFormat.RGB -> PixelFormat.getByteRgbInstance()
//        AssetCookedPixelFormat.BGR -> PixelFormat.getByteRgbInstance()
//        else -> null
//    }


    private fun createImageFromPixelDataCooked(cooked: AssetCp.AssetPixelDataCooked.Reader): Image? {
        if (!cooked.hasLevelData() || !cooked.hasLevels() || cooked.levels.size() < 1) {
            return null
        }


        val assetPixelFormat = intToAssetPixelFormat(cooked.pixelFormat)
        assetPixelFormat.byteBgraInstance?.let {
//            cooked.pixelFormat

            val width = cooked.levels[0].width
            val height = cooked.levels[0].height
            val data = cooked.levelData[0].asByteBuffer()

            val image = WritableImage(width, height)
            val pixelWriter = image.pixelWriter
            val bytes = ByteArray(data.limit())
            data.get(bytes)

            assetPixelFormat.fixBytes?.invoke(bytes)
            bytes.flipScanlines(width * assetPixelFormat.pixelSize)

            pixelWriter.setPixels(0, 0, width, height, it, bytes, 0, width * assetPixelFormat.pixelSize)


            return image
        }
        return null

    }


    private fun createImageFromPixelDataStored(pixelData: AssetCp.AssetPixelDataStored.Reader) : Image {

//        val format = image.pixelReader.pixelFormat
//
//        if (format.type == PixelFormat.Type.BYTE_INDEXED) {
//            return createNormalMapFromHeightMap(image)
//        }

        return Image(ByteBufferBackedInputStream(pixelData.data.asByteBuffer()), 0.0, 0.0, true, true)
    }

    override fun loadSync() {
        if (image == null) {
            image = createImageFromReader()

        }
    }

    override fun loadAsync() {
        if (image != null) {
            return
        }

        runAsync {
            //                        Thread.sleep(1000)

            //            try {
//                val it =
            createImageFromReader()
//            } catch( x :IllegalArgumentException )
//            {
//                null
//            }
        } ui {
            if (image == null && it != null) {
                image = it
            }
        }
    }
}