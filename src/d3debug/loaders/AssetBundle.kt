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
import java.io.FileInputStream
import java.nio.channels.FileChannel

class AssetBundle(val filename : String) {
    val assets = HashSet<Asset>()

    init {
        FileInputStream(filename).channel?.use { fileChannel ->
            val map = fileChannel.map(FileChannel.MapMode.READ_ONLY,0,fileChannel.size())!!

            val reader = Serialize.read(map)!!

            reader.getRoot(AssetCp.AssetBundle.factory)?.let { assetBundle ->
                for ( asset in assetBundle.assets)
                {
                    assets.add(Asset(asset))
                }
            }

        }
    }
}