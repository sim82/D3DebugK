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

import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial
import java.io.File
import java.io.Reader
import javax.json.Json
import javax.json.JsonObject

fun readAppearances(input: Reader): Sequence<Appearance> {
    val jsonReader = Json.createReader(input)
    val root = jsonReader.read() as? JsonObject ?: return emptySequence()

    return root.asSequence().map { (k, v) ->
        val app = v as? JsonObject ?: return@map null

        Appearance(k, app.getString("primaryImage"))
    }.filterNotNull()
}

data class Appearance(val name: String, val primaryImage: String) {

    val material by lazy<Material> {
        val basePath = File("/home/sim/src_3dyne/dd_081131_exec/dd1/arch00.dir")

        listOf(".png", ".jpg").forEach {
            val f = File(basePath, primaryImage + it)
            if (f.canRead()) {
                println(f)

                val mat = PhongMaterial()
                mat.diffuseMap = Image(f.inputStream())//Color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble(), 1.0))
                return@lazy mat
            }
        }

        throw RuntimeException( "cannot read primaryImage")
    }
}