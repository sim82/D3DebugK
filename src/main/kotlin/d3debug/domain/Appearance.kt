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
import d3debug.views.assetdata.AssetDataPixel
import d3debug.loaders.AssetLoader
import d3debug.loaders.AssetReaderFactory
import d3debug.views.assetdata.createAssetData
import javafx.scene.paint.PhongMaterial
import org.capnproto.MessageBuilder
import java.io.File
import java.io.Reader
import java.util.*
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


class FakeAppearanceAssetLoader : AssetLoader {
    override val assets by lazy<Sequence<AssetReaderFactory>> {
        File("/home/sim/src_3dyne/dd_081131_exec/dd1/arch00.dir/appearance/").listFiles { _, name ->
            name.endsWith(".json")
        }.asSequence().map {
            val jsonReader = Json.createReader(it.reader())
            jsonReader ?: return@map emptySequence<Pair<String, JsonObject?>>()

            val root = jsonReader.read() as? JsonObject ?: return@map emptySequence<Pair<String,JsonObject?>>()

            root.asSequence().map { (k, v) ->
                Pair(k, v as? JsonObject)
            }
        }.flatten().map { (appName, jsonObj) ->
            val shaderConfig = jsonObj?.getJsonObject("shaderConfig")

            shaderConfig ?: return@map null

            val mi = Appearance.MaterialInput(shaderConfig)
            val uuid = UUID.randomUUID().toString()

            val builder = MessageBuilder()
            val assetBuilder = builder.initRoot(AssetCp.Asset.factory)
            val header = assetBuilder.initHeader()
            header.setName(appName)
            header.setUuid(uuid)

            if (!mi.build(assetBuilder.initMaterialDesc()))
            {
                return@map null
            }

            object : AssetReaderFactory {
                override val name = appName
                override val uuid: String = uuid
                override val reader = assetBuilder.asReader()
            }
        }.filterNotNull()
    }
}

fun fakeAppearanceLoader(): AssetLoader {
    return FakeAppearanceAssetLoader()
}

class Appearance(val name: String, val assetFactory: AssetFactory, app: JsonObject) {

    class MaterialInput(config: JsonObject) {
        val diffuse: String? = config.getString("image", null)
        val normalMap: String? = config.getString("bumpmap", null)
        val specularMap: String? = config.getString("glossmap", null)

        fun build(builder: AssetCp.AssetMaterialDesc.Builder) : Boolean {
            val phong = builder.initPhong()
            phong ?: return false
            diffuse ?: return false

            phong.setDiffuseMap(diffuse)
            if (normalMap != null) {
                phong.setNormalMap(normalMap)
            }
            if (specularMap != null) {
                phong.setSpecularMap(specularMap)
            }
            return true
        }

//        init {
//            if (config.getString("glossmap", null) != null) {
//                println("gloss")
//            }
//        }
    }

    val materialConfig = MaterialInput(app.getJsonObject("shaderConfig")!!)
    val material = PhongMaterial()
//    val resourceAdapter: ResourceItem by lazy {
//        AppearanceResourceAdapter(this)
//    }


    val diffuseMapAsset by lazy<AssetDataPixel?> {
        materialConfig.diffuse ?: return@lazy null
        assetDataPixel(materialConfig.diffuse)
    }

    val specularMapAsset by lazy<AssetDataPixel?> {
        materialConfig.specularMap ?: return@lazy null
        assetDataPixel(materialConfig.specularMap)
    }
    val normalMapAsset by lazy<AssetDataPixel?> {
        materialConfig.normalMap ?: return@lazy null
        assetDataPixel(materialConfig.normalMap)
    }

    private fun assetDataPixel(diffuse: String): AssetDataPixel? {
        return assetFactory(diffuse)?.let { asset ->
            (createAssetData(asset) as? AssetDataPixel)?.let {
                it.loadAsync()
                it
            }
        }
    }

    init {
        diffuseMapAsset?.let {
            material.diffuseMapProperty().bind(it.imageProperty)
        }
        normalMapAsset?.let {
            material.bumpMapProperty().bind(it.imageProperty)
        }
        specularMapAsset?.let {
            material.specularMapProperty().bind(it.imageProperty)
        }
    }


}