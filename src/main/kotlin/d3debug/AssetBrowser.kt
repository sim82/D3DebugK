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

package d3debug

import d3debug.views.Asset3dView
import d3debug.views.AssetView
import d3debug.views.NormalMapView
import javafx.application.Application
import javafx.beans.property.DoubleProperty
import javafx.geometry.Point3D
import javafx.scene.Group
import javafx.scene.PointLight
import javafx.scene.shape.Box
import javafx.util.Duration
import tornadofx.*


class TestViewController : Controller() {
    var sliderProp: DoubleProperty? = null
}

class TestView : View() {
    val controller by inject<TestViewController>()

    override val root = vbox {
        slider(0.0, 1000.0) {
            value

            controller.sliderProp = valueProperty()
        }

        stackpane {
            //        button("bla")
            val box = Box(200.0, 100.0, 100.0)
            box.rotate = 10.0
            val group = Group(box)
            group.rotationAxis = Point3D(1.0, 0.0, 0.0)
            group.rotate = 10.0
            group.rotate(Duration(10.0), 10.0)

            group.translateXProperty().bind(controller.sliderProp)

            val light = PointLight(javafx.scene.paint.Color.AQUA)
            light.translateX = 200.0
            light.translateZ = -100.0
            add(group)
            add(light)
        }
    }


    init {
        root.sceneProperty().onChange { scene ->
            println(scene)

        }
    }


}


class AssetBrowser : App(Workspace::class) {

    override fun onBeforeShow(view: UIComponent) {
//        Thread.setDefaultUncaughtExceptionHandler(null)
        workspace.dock<AssetView>()
        workspace.dock<Asset3dView>()
        workspace.dock<NormalMapView>()
//        workspace.dock<TestView>()
    }
}

fun main(args: Array<String>) {
    Thread.setDefaultUncaughtExceptionHandler { t, e ->  }
    Application.launch(AssetBrowser::class.java, *args)
}