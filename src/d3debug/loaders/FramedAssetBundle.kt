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
import d3debug.domain.Asset
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
    val file = File(filename)

    val idIndex: AssetCp.FramedAssetBundle.IdIndex.Reader
    val nameIndex: AssetCp.FramedAssetBundle.NameIndex.Reader
    val offsetTable: AssetCp.FramedAssetBundle.OffsetTable.Reader

    val assetSet = hashSetOf<Asset>()

    override val assets: Sequence<Asset>
        get() = assetSet.asSequence()

    init {

        val indexSection = file.inputStream().channel.use { channel ->
            val headerSection = Serialize.read(channel).getRoot(AssetCp.FramedAssetBundle.HeaderSection.factory)

            if (headerSection.magick != 0x1234567812345678) {
                throw RuntimeException("bad headerSection.magick")
            }

            val indexMapping = channel.map(FileChannel.MapMode.READ_ONLY, headerSection.indexSectionOffset, headerSection.indexSectionSize)
            Serialize.read(indexMapping).getRoot(AssetCp.FramedAssetBundle.IndexSection.factory)
        }
        idIndex = indexSection.idIndex
        nameIndex = indexSection.nameIndex
        offsetTable = indexSection.offsetTable


        val idPairs = arrayListOf<Pair<Long, AssetCp.FramedAssetBundle.IdIndex.Uuid.Reader>>()
        val namePairs = arrayListOf<Pair<Long, Text.Reader>>()

        for (i in 0 until idIndex.index.size()) {
            idPairs.add(Pair(idIndex.index[i], idIndex.sortedIds[i]))
            namePairs.add(Pair(nameIndex.index[i], nameIndex.sortedNames[i]))
        }

        idPairs.sortBy { it.first }
        namePairs.sortBy { it.first }
        val nameIds = idPairs.map { it.second }.zip(namePairs.map { it.second })


        for ((i, p) in nameIds.withIndex()) {
            val (id, name) = p

            val uuid = ConvertLongToUuid.convert(id.idLow, id.idHigh)

            println("${id.idHigh} ${id.idLow} $name $uuid")


            val offset = offsetTable.offsets[i]
            val size = offsetTable.sizes[i]
            val usize = offsetTable.uncompressedSizes[i]
            val factory = LZ4Factory.fastestInstance()


            val buf = ByteBuffer.allocate(size.toInt())
            file.inputStream().channel.read(buf, offset)
            buf.rewind()

            val ubuf = ByteBuffer.allocate(usize.toInt())
            factory.fastDecompressor().decompress(buf, ubuf)

            ubuf.rewind()
            val reader = Serialize.read(ubuf).getRoot(AssetCp.Asset.factory)

            assetSet.add( Asset(object : AssetReaderFactory {
                override val name = name.toString()
                override val uuid = id.toString()
                override val reader: AssetCp.Asset.Reader
                    get() = reader
            }))
        }
    }
}