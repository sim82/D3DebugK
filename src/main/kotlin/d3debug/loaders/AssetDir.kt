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
import org.capnproto.Serialize
import java.io.File
import java.nio.channels.FileChannel

internal class AssetDir(private val rootPath: String) : AssetLoader {
    private val rootDir = File(rootPath)

    private val indexReader by lazy {
        val rootDir = File(rootPath)
        val indexFile = File(rootDir, "index")

        if (!(indexFile.isFile && indexFile.canRead())) {
            throw RuntimeException("cannot read index file ${indexFile.toURI()}")
        }
        indexFile.inputStream().channel.use { indexFileChannel ->
            val mappedBuffer = indexFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, indexFileChannel.size())
            Serialize.read(mappedBuffer)?.getRoot(AssetCp.AssetIndex.factory)!!

        }
    }

    override val assets: Sequence<AssetReaderFactory>
        get() = Sequence({ indexReader.headers.iterator() }).map {
            val uuid = it.uuid.toString()
            val assetFile = File(rootDir, uuid)

            object : AssetReaderFactory {
                override val name: String get() = it.name.toString()
                override val uuid: String = uuid
                override val reader: AssetCp.Asset.Reader
                    get() = assetFile.inputStream().channel.use { assetFileChannel ->
                        val assetMappedBuffer = assetFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, assetFileChannel.size())
                        Serialize.read(assetMappedBuffer).getRoot(AssetCp.Asset.factory)!!
                    }
            }
        }
}
