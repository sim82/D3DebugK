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
import org.capnproto.ReaderOptions
import org.capnproto.Serialize
import java.io.FileInputStream
import java.nio.channels.FileChannel

internal class AssetBundle(val filename: String) : AssetLoader {

    override val assets by lazy {
        FileInputStream(filename).channel.use { fileChannel ->
            val map = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())!!
            val reader = Serialize.read(map, ReaderOptions(1024 * 1024 * 1024, 64))!!

            reader.getRoot(AssetCp.AssetBundle.factory).assets.asSequence().map { asset ->
                object : AssetReaderFactory {
                    override val name = asset.header.name.toString()
                    override val uuid = asset.header.uuid.toString()
                    override val reader = asset
                } as AssetReaderFactory
            }
        }
    }
}


