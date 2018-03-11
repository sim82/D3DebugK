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

package d3debug.views

import d3debug.controllers.AssetsController
import d3debug.domain.Asset
import d3debug.viewmodels.AssetGroupModel
import javafx.scene.Group
import javafx.scene.PerspectiveCamera
import javafx.scene.Scene
import javafx.scene.SubScene
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.shape.Box
import tornadofx.*


class AssetPreviewFragment : Fragment() {
    private val assetGroupModel: AssetGroupModel by inject()


    override val root = vbox {
        label(assetGroupModel.selectedAssets.value.nameProperty)
        imageview(assetGroupModel.selectedAssets.value.image2) {
            val box = Box(100.0, 100.0, 100.0)
            val group = Group(box)
            group.rotate = 10.0
            add(group)
        }


    }

//    override fun onDock() {
//        super.onDock()
//
//        val scene = root.scene;
//        root.scene.camera = PerspectiveCamera()
//
//    }

//    override val root = Scene(Group(), 800.0, 600.0, true );)

    init {


//        val scene = Scene( Box(100.0, 100.0, 100.0), 800.0, 600.0, true );
//        scene.fill = Color.RED
//        primaryStage.scene = scene
    }

}

class AssetGroupFragment : Fragment() {
    private val assetsController: AssetsController by inject()
    private val assetGroupModel: AssetGroupModel by inject()

    override val root = vbox {
        hgrow = Priority.ALWAYS

        slider(64, 512, 200) {
            assetsController.iconSizeProperty = valueProperty()
        }

        datagrid<Asset>() {
            vgrow = Priority.ALWAYS

//            val scrollbar = find
            bindSelected(assetGroupModel.selectedAssets)

            focusModel.focusedItemProperty().onChange {
                println("focused ${it?.name}")
            }

            onDoubleClick {
                openInternalWindow(AssetPreviewFragment::class)
            }

            itemsProperty.bind(assetGroupModel.assets)

            assetsController.iconSizeProperty?.let { iconSize ->
                cellWidthProperty.bind(iconSize)
                cellHeightProperty.bind(iconSize)
            }

            cellCache {
                if (it.image2 == null) {
                    it.image2 = assetsController.fallbackImage
                }
                it.loadImageAsync()
                imageview(it.imageProperty) {
                    assetsController.iconSizeProperty?.let { iconSize ->
                        fitWidthProperty().bind(iconSize)
                        fitHeightProperty().bind(iconSize)
                    }
                }
            }
            label("bla")
        }
    }

}