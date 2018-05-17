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

package d3debug.loaders

import d3cp.AssetCp
import net.jpountz.lz4.LZ4Factory
import org.capnproto.Serialize
import org.capnproto.Text
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*

object ConvertLongToUuid {
    val bb = ByteBuffer.allocate(16)

    fun convert(idLow: Long, idHigh: Long): UUID {
        bb.order(ByteOrder.LITTLE_ENDIAN)
        bb.putLong(0, idHigh)
        bb.putLong(8, idLow)
        bb.order(ByteOrder.BIG_ENDIAN)

        return UUID(bb.getLong(8), bb.getLong(0))
    }
}

internal class FramedAssetBundle(val filename: String) : AssetLoader {
    private val lz4Factory = LZ4Factory.fastestInstance()

    private val file = File(filename)
    private val inputChannel = file.inputStream().channel

    private val indexSection by lazy {
        val headerSection = Serialize.read(inputChannel).getRoot(AssetCp.FramedAssetBundle.HeaderSection.factory)

        if (headerSection.magick != 0x1234567812345678) {
            throw RuntimeException("bad headerSection.magick")
        }

        val indexMapping = inputChannel.map(FileChannel.MapMode.READ_ONLY, headerSection.indexSectionOffset, headerSection.indexSectionSize)
        Serialize.read(indexMapping).getRoot(AssetCp.FramedAssetBundle.IndexSection.factory)!!
    }

    private val nameIdPairs: List<Pair<AssetCp.FramedAssetBundle.IdIndex.Uuid.Reader, Text.Reader>> by lazy {
        val idIndex: AssetCp.FramedAssetBundle.IdIndex.Reader = indexSection.idIndex
        val nameIndex: AssetCp.FramedAssetBundle.NameIndex.Reader = indexSection.nameIndex

        val idPairs = arrayListOf<Pair<Long, AssetCp.FramedAssetBundle.IdIndex.Uuid.Reader>>()
        val namePairs = arrayListOf<Pair<Long, Text.Reader>>()

        for (i in 0 until idIndex.index.size()) {
            idPairs.add(Pair(idIndex.index[i], idIndex.sortedIds[i]))
            namePairs.add(Pair(nameIndex.index[i], nameIndex.sortedNames[i]))
        }

        idPairs.sortBy { it.first }
        namePairs.sortBy { it.first }
        idPairs.map { it.second }.zip(namePairs.map { it.second })
    }


    override val assets: Sequence<AssetReaderFactory>
        get() = nameIdPairs.asSequence().withIndex().map { (i, p) ->
            val (id, name) = p

            val uuid = ConvertLongToUuid.convert(id.idLow, id.idHigh)

//            println("${id.idHigh} ${id.idLow} $name $uuid")

            val offsetTable: AssetCp.FramedAssetBundle.OffsetTable.Reader = indexSection.offsetTable

            val offset = offsetTable.offsets[i]
            val size = offsetTable.sizes[i]
            val usize = offsetTable.uncompressedSizes[i]


            object : AssetReaderFactory {
                override val name = name.toString()
                override val uuid = uuid.toString()
                override val reader : AssetCp.Asset.Reader
                    get() {
                        val buf = ByteBuffer.allocate(size.toInt())
                        inputChannel.read(buf, offset)
                        buf.rewind()

                        val ubuf = ByteBuffer.allocate(usize.toInt())
                        lz4Factory.fastDecompressor().decompress(buf, ubuf)

                        ubuf.rewind()
                        return Serialize.read(ubuf).getRoot(AssetCp.Asset.factory)
                    }
            } as AssetReaderFactory
        }
}