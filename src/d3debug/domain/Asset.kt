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
import d3debug.loaders.AssetReaderFactory
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.shape.Mesh
import javafx.scene.shape.TriangleMesh
import javafx.scene.shape.VertexFormat
import tornadofx.*
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


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

        val minLen = Math.min(len, buf.remaining())
        buf.get(bytes, off, minLen)
        return minLen
    }
}


//inline fun ByteArray.swapSubrange(width: Int, step: Int) {
//    for (i in 0 until size step step) {
//        for (j in 0 until width / 2) {
//            swapAt(i + j, i + width - j - 1)
//        }
//    }
//}

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

class Asset(val factory: AssetReaderFactory) {
    val uuidProperty = SimpleStringProperty(this, "uuid", factory.uuid)
    var uuid by uuidProperty

    val nameProperty = SimpleStringProperty(this, "name", factory.name)
    var name by nameProperty

    val imageProperty = SimpleObjectProperty<Image>(this, "image", null)
    var image2 by imageProperty

    var imageLoaded = false

    fun readMeshes(appearances: Map<String, Appearance>): Sequence<Pair<Appearance, Mesh>> {
        val reader = factory.reader

        if (!(reader.isMeshData && reader.meshData.hasAttributeArrayInterleavedList())) {
            return emptySequence()
        }

        val a = reader.meshData.attributeArrayInterleavedList.size()

        val appearancesList = reader.meshData.appearanceList.asSequence().map { appearances[it.toString()] }


        val meshes = appearancesList.zip(reader.meshData.attributeArrayInterleavedList.asSequence()).map { (app, array) ->
            app ?: return@map null

            val position = array.attributes.find {
                it.hasName() && it.name.toString() == "a_position"
            } ?: return@map null

            val texcoord = array.attributes.find {
                it.hasName() && it.name.toString() == "a_texcoord"
            } ?: return@map null


            val mesh = TriangleMesh(VertexFormat.POINT_TEXCOORD)

            val numVertex = array.numVertex
            val points = mesh.points
            points.resize(numVertex * 3)

            val texCoords = mesh.texCoords
            texCoords.resize(numVertex * 2)

            val attributeBuffer = array.attributeArray.asByteBuffer()

            for (i in 0 until numVertex) {

                attributeBuffer.position(i * array.attributeStride + position.offset)
                val positionArray = attributeBuffer.slice().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()


                val order = positionArray.order()

                points.set(i * 3 + 0, positionArray[0])
                points.set(i * 3 + 1, positionArray[1])
                points.set(i * 3 + 2, positionArray[2])


                attributeBuffer.position(i * array.attributeStride + texcoord.offset)
                val texcoordBuffer = attributeBuffer.slice().order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()

                texCoords.set(i * 2 + 0, texcoordBuffer[0])
                texCoords.set(i * 2 + 1, texcoordBuffer[1])
            }

            val indexArray = array.indexArray.asByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()

            val numIndex = array.numIndex
            val faces = mesh.faces
            faces.resize(numIndex * 2)
            for (i in 0 until numIndex step 3) {
                // change winding to CCW
                faces.set(i * 2 + 0, indexArray[i + 2].toInt())
                faces.set(i * 2 + 1, indexArray[i + 2].toInt())
                faces.set(i * 2 + 2, indexArray[i + 1].toInt())
                faces.set(i * 2 + 3, indexArray[i + 1].toInt())
                faces.set(i * 2 + 4, indexArray[i].toInt())
                faces.set(i * 2 + 5, indexArray[i].toInt())
            }

            Pair(app, mesh)
        }.filterNotNull()

        return meshes
    }


//    fun asMesh(h: (Mesh) -> Unit) {
//        val reader = factory.reader
//
//        if (!(reader.isMeshData && reader.meshData.hasAttributeArrayInterleavedList())) {
//            return
//        }
//
////        val attributeArrayList = reader.meshData.attributeArrayInterleavedList
//
//
//        for (attributeArray in reader.meshData.attributeArrayInterleavedList) {
//
//            val position = attributeArray.attributes.find {
//                it.hasName() && it.name.toString() == "a_position"
//            } ?: continue
//
//            attributeArray.
//        }
//    }


    private fun createImageFromReader(): Image? {
        val reader = factory.reader
        return when (reader.which()) {
            AssetCp.Asset.Which.PIXEL_DATA -> when (reader.pixelData.which()) {
                AssetCp.AssetPixelData.Which.STORED -> createImageFromPixelDataStored(reader.pixelData.stored)
                AssetCp.AssetPixelData.Which.COOKED -> createImageFromPixelDataCooked(reader.pixelData.cooked)
                else -> null
            }
            else -> null
        }
    }


    enum class AssetPixelFormat(val pixelSize: Int, val byteBgraInstance: PixelFormat<ByteBuffer>?, val fixBytes: ((ByteArray) -> Unit)?) {
        RGBA(4, PixelFormat.getByteBgraInstance(), ::swapRgba),
        RGB(3, PixelFormat.getByteRgbInstance(), null),
        BGR(3, PixelFormat.getByteRgbInstance(), ::swapRgb),
        Luminance(1, null, null),
        Dxt1(0, null, null),
        Bc3(0, null, null),
        Undefined(0, null, null)
    }

    fun intToAssetPixelFormat(v: Int) = AssetPixelFormat.values()[v.coerceIn(0 until AssetPixelFormat.values().size)]
//
//    fun toJavafxPixelformat(f: AssetPixelFormat) = when (f) {
//        AssetPixelFormat.RGBA -> PixelFormat.getByteBgraInstance()
//        AssetPixelFormat.RGB -> PixelFormat.getByteRgbInstance()
//        AssetPixelFormat.BGR -> PixelFormat.getByteRgbInstance()
//        else -> null
//    }


    private fun createImageFromPixelDataCooked(cooked: AssetCp.AssetPixelDataCooked.Reader): Image? {
        if (!cooked.hasLevelData() || !cooked.hasLevels() || cooked.levels.size() < 1) {
            return null
        }


        val assetPixelFormat = intToAssetPixelFormat(cooked.pixelFormat)
        assetPixelFormat.byteBgraInstance?.let {
            cooked.pixelFormat

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


    private fun createImageFromPixelDataStored(pixelData: AssetCp.AssetPixelDataStored.Reader) =
            Image(ByteBufferBackedInputStream(pixelData.data.asByteBuffer()), 128.0, 128.0, true, true)


    fun loadImageAsync() {
        if (imageLoaded) {
            return
        }

//        javafx.application.Platform.runLater {
        runAsync {
            //            Thread.sleep(1000)

            //            try {
//                val it =
            createImageFromReader()
//            } catch( x :IllegalArgumentException )
//            {
//                null
//            }
        } ui {
            if (it != null) {
                image2 = it
                imageLoaded = true
            }
        }
//        println("ret")
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


