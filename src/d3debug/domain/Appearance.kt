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

import javafx.scene.paint.PhongMaterial
import java.io.Reader
import javax.json.Json
import javax.json.JsonObject

typealias AssetFactory = (String) -> Asset?

fun readAppearances(input: Reader, assetFactory: AssetFactory): Sequence<Appearance> {
    val jsonReader = Json.createReader(input)
    val root = jsonReader.read() as? JsonObject ?: return emptySequence()

    return root.asSequence().map { (k, v) ->
        val app = v as? JsonObject ?: return@map null

        Appearance(k, assetFactory, app) //.getString("primaryImage"))
    }.filterNotNull()
}

class Appearance(val name: String, val assetFactory: AssetFactory, app: JsonObject) {

    class MaterialInput(config: JsonObject) {
        val diffuse: String? = config.getString("image", null)
        val normalMap: String? = null //config.getString("bumpmap", null)
        val specularMap: String? = config.getString("glossmap", null)

//        init {
//            if (config.getString("glossmap", null) != null) {
//                println("gloss")
//            }
//        }
    }

    val materialConfig = MaterialInput(app.getJsonObject("shaderConfig")!!)
    val material = PhongMaterial()

    init {
        if (materialConfig.diffuse != null) {
            assetFactory(materialConfig.diffuse)?.let {
                it.loadImageAsync()
                material.diffuseMapProperty().bind(it.imageProperty)
            }
        }
        if (materialConfig.normalMap != null) {
            assetFactory(materialConfig.normalMap)?.let {
                it.loadImageAsync()
                material.bumpMapProperty().bind(it.imageProperty)
            }
        }
        if (materialConfig.specularMap != null) {
            assetFactory(materialConfig.specularMap)?.let {
                it.loadImageAsync()
                material.specularMapProperty().bind(it.imageProperty)
            }
        }
    }
}