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
import org.capnproto.Serialize
import org.capnproto.Text
import java.io.File
import java.nio.channels.FileChannel

internal class FramedAssetBundle(val filename: String) : AssetLoader {
    val file = File(filename)

    val idIndex: AssetCp.FramedAssetBundle.IdIndex.Reader
    val nameIndex: AssetCp.FramedAssetBundle.NameIndex.Reader
    val offsetTable: AssetCp.FramedAssetBundle.OffsetTable.Reader

    override val assets: Sequence<Asset>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.


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
            namePairs.add(Pair(idIndex.index[i], nameIndex.sortedNames[i]))
        }

        idPairs.sortBy { it.first }
        namePairs.sortBy { it.first }
        val nameIds = idPairs.map { it.second }.zip(namePairs.map { it.second })

        for ((id, name) in nameIds) {
            println( "${id.idHigh} ${id.idLow} $name")
        }
    }
}