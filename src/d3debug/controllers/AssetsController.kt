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

import d3debug.loaders.AssetDir
import d3debug.domain.Asset
import d3debug.loaders.AssetBundle
import javafx.collections.FXCollections
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import tornadofx.*

class AssetsController : Controller() {

//    val assetDir = AssetDir("/home/sim/src_3dyne/dd_081131_exec/bla_cooked")
    val assetDir = AssetBundle("/home/sim/src_3dyne/dd_081131_exec/bla_cooked.bundle")

    val assets = FXCollections.observableArrayList<Asset>(assetDir.assets)!!

    val fallbackImage: Image by lazy {
        val canvas = Canvas(32.0, 32.0)
        val gc = canvas.graphicsContext2D

        val image = WritableImage(32, 32)
        gc.fill = Color.RED
        gc.fillText("default", 10.0, 10.0)
        canvas.snapshot(null, image)

        image
    }
}