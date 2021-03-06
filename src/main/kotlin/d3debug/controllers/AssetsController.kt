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

package d3debug.controllers

import d3debug.domain.Asset
import d3debug.domain.AssetGroup
import d3debug.domain.fakeAppearanceLoader
import d3debug.loaders.loaderFor
import d3debug.viewmodels.AssetGroupModel
import javafx.beans.property.DoubleProperty
import javafx.collections.FXCollections
import tornadofx.*

fun String.prefixBefore(c: Char): String {
    val i = indexOfLast { it == c }
    return if (i == -1) {
        ""
    } else {
        substring(0, i)
    }
}

class AssetsController : Controller() {

    private val assetDir = loaderFor("/home/sim/src_3dyne/dd_081131_exec/bla")
    //    val assetDir = loaderFor("/home/sim/src_3dyne/dd_081131_exec/bla_cooked.bundle")
    //val assetDir = loaderFor("/home/sim/src_3dyne/dd_081131_exec/out.fab")
    val scene = loaderFor("/home/sim/tmp/shadermesh_assets")
    private val appearanceLoader = fakeAppearanceLoader()

//    val assets = FXCollections.observableArrayList<Asset>(assetDir.assets.asIterable() + scene.assets.asIterable())!!

    val assetGroups = FXCollections.observableArrayList<AssetGroup>()!!

    var iconSizeProperty: DoubleProperty? = null

    private val assetGroupModel: AssetGroupModel by inject()

    fun assetByName(name: String): Asset? {
        listOf("", ".png", ".jpg").forEach {
            val asset = assetByName[name + it]
            if (asset != null) {
                return asset
            }
        }
        return null
    }

    private val assetByName = hashMapOf<String, Asset>()
//    fun findAssetByName( name : String ) = assetByName[name]

//    val appearances by lazy<Map<String, Appearance>> {
//        val map = hashMapOf<String, Appearance>()
//
//        File("/home/sim/src_3dyne/dd_081131_exec/dd1/arch00.dir/appearance/").listFiles { _, name ->
//            name.endsWith(".json")
//        }.forEach {
//            readAppearances(it.reader(), ::assetByName).forEach {
//                map[it.name] = it
//            }
//        }
//
//        map
//    }

    init {

        val grouped = sequenceOf(assetDir.assets, scene.assets, appearanceLoader.assets).flatten().groupBy { it.name.prefixBefore('/') }

        for ((groupName, list) in grouped) {
            val assets = list.map { Asset(it) }

            assets.forEach {
                assetByName[it.name] = it
            }

            val group = AssetGroup(groupName)
            group.assets.addAll(assets)

            assetGroups += group

        }

        assetGroups.sortBy { it.name }

        assetGroupModel.selectedAssets.onChange {
            println("selected ${it?.name}")
        }
    }
}