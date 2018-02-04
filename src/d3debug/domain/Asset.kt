/*
 * Copyright (c) 2018 Simon A. Berger
 * Licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package d3debug.domain

import d3cp.AssetCp
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import tornadofx.*
import java.io.InputStream
import java.nio.ByteBuffer


class ByteBufferBackedInputStream(internal var buf: ByteBuffer) : InputStream() {

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

        val len = Math.min(len, buf.remaining())
        buf.get(bytes, off, len)
        return len
    }
}

class Asset(val reader: d3cp.AssetCp.Asset.Reader) {
    val uuidProperty = SimpleStringProperty(this, "uuid", reader.header.uuid.toString())
    var uuid by uuidProperty

    val nameProperty = SimpleStringProperty(this, "name", reader.header.name.toString())
    var name by nameProperty

    val imageProperty = SimpleObjectProperty<Image>(this, "image", null)
    var image2 by imageProperty



    private fun createImageFromReader(): Image? = when (reader.which()) {
        AssetCp.Asset.Which.PIXEL_DATA -> when (reader.pixelData.which()) {
            AssetCp.AssetPixelData.Which.STORED -> createImageFromPixelDataStored(reader.pixelData.stored)
            AssetCp.AssetPixelData.Which.COOKED -> createImageFromPixelDataCooked(reader.pixelData.cooked)
            else -> null
        }
        else -> null
    }

    enum class AssetPixelFormat( val v : Int, val pixelSize : Int ) {

        RGBA(0, 4),
        RGB(1, 3),
        BGR(2, 3),
        Luminance(3, 1),
        Dxt1(4, 0),
        Bc3(5, 0),
        Undefined(666, 0)
    }

    fun intToAssetPixelFormat( v : Int ) = when(v)
    {
        0 -> AssetPixelFormat.RGBA
        1 -> AssetPixelFormat.RGB
        2 -> AssetPixelFormat.BGR
        3 -> AssetPixelFormat.Luminance
        4 -> AssetPixelFormat.Dxt1
        5 -> AssetPixelFormat.Bc3
        else -> AssetPixelFormat.Undefined
    }

    fun toJavafxPixelformat( f : AssetPixelFormat ) = when(f)
    {
        AssetPixelFormat.RGBA -> PixelFormat.getByteBgraInstance()
        AssetPixelFormat.RGB -> PixelFormat.getByteRgbInstance()
        AssetPixelFormat.BGR -> PixelFormat.getByteRgbInstance()
        else -> null
    }



    private fun createImageFromPixelDataCooked(cooked: AssetCp.AssetPixelDataCooked.Reader) : Image? {
        if (!cooked.hasLevelData() || !cooked.hasLevels() || cooked.levels.size() < 1 )
        {
            return null
        }


        val assetPixelFormat = intToAssetPixelFormat(cooked.pixelFormat)
        toJavafxPixelformat(assetPixelFormat)?.let {
            cooked.pixelFormat

            val width = cooked.levels[0].width
            val height = cooked.levels[0].height
            val data = cooked.levelData[0].asByteBuffer()

            val image = WritableImage(width, height)
            val pixelWriter = image.pixelWriter
            val bytes = ByteArray(data.limit())
            data.get(bytes)

            pixelWriter.setPixels(0, 0, width, height, it, bytes, 0, width * assetPixelFormat.pixelSize)


            return image
        }
        return null

    }



    private fun createImageFromPixelDataStored(pixelData: AssetCp.AssetPixelDataStored.Reader) =
            Image(ByteBufferBackedInputStream(pixelData.data.asByteBuffer()), 128.0, 128.0, true, true)


    fun loadImageAsync() {

        runAsync {
                        Thread.sleep(300)
//            try {
                createImageFromReader()
//            } catch( x :IllegalArgumentException )
//            {
//                null
//            }
        } ui {
            if (it != null) {
                image2 = it
            }
        }
    }


//    val image: Image
//        get() {
//            if (!(reader.isPixelData() && reader.pixelData.isStored())) {
//                return Image("")
//            }
//
////            val img = Image(File("/home/sim/digiKam_pictures/2004-07-19_23-56-33/dsc00006.jpg").toURI().toString())
//
//            val img = Image(ByteBufferBackedInputStream(reader.pixelData.stored.data.asByteBuffer()), 128.0, 128.0, true, true)
////            val img = Image(FileInputStream("/home/sim/digiKam_pictures/2004-07-19_23-56-33/dsc00006.jpg"))
//            return img
//            //return Image()
//        }
}