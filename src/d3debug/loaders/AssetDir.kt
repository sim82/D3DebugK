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

import d3debug.domain.Asset
import org.capnproto.Serialize
import java.io.File
import java.nio.channels.FileChannel

class AssetDir(val rootPath: String) {
    val assets = HashSet<Asset>()

    init {
        val rootDir = File(rootPath)
        val uidRegex = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        val files = rootDir.listFiles { dir, name ->
            uidRegex.matches(name)
        }!!

        for (file in files) {
            println("file: ${file.toString()}")

            FileChannel.open(file.toPath()).use { fileChannel ->

                val mappedBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())!!
                val reader = Serialize.read(mappedBuffer)!!

                val assetReader = reader.getRoot(d3cp.AssetCp.Asset.factory)!!

                val header = assetReader.header!!
                println("header: ${header.name} ${header.uuid}")

                // TODO: read uuid & name from index and create asset reader on demand
                assets += Asset( {assetReader}, assetReader.header.uuid.toString(), assetReader.header.name.toString())

            }
        }
//        for (val file in rootDir.listFiles()) {
//            rootDir.listFiles()
//        }
    }
}
