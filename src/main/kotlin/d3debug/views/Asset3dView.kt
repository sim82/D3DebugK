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
import d3debug.views.assetdata.AssetDataMesh
import d3debug.views.assetdata.createAssetData
import javafx.animation.Animation
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point3D
import javafx.scene.Group
import javafx.scene.PerspectiveCamera
import javafx.scene.SceneAntialiasing
import javafx.scene.SubScene
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.MeshView
import javafx.scene.transform.Rotate
import javafx.scene.transform.Translate
import javafx.util.Duration
import tornadofx.*

operator fun Point3D.times(factor: Double): Point3D = multiply(factor)
operator fun Point3D.plus(other: Point3D): Point3D = add(other)

class Asset3dController : Controller() {
    val forwardProperty = SimpleBooleanProperty(this, "forward", false)
    var forward by forwardProperty

    val backwardProperty = SimpleBooleanProperty(this, "backward", false)
    var backward by backwardProperty

    val strafLeftProperty = SimpleBooleanProperty(this, "strafLeft", false)
    var strafLeft by strafLeftProperty

    val strafRightProperty = SimpleBooleanProperty(this, "strafRight", false)
    var strafRigth by strafRightProperty

    val forwardDirProperty = SimpleObjectProperty<Point3D>(this, "forwardDir", Point3D(0.0, 0.0, 1.0))
    var forwardDir: Point3D by forwardDirProperty

    val rightDirProperty = SimpleObjectProperty<Point3D>(this, "rightDir", Point3D(-1.0, 0.0, 0.0))
    var rightDir: Point3D by rightDirProperty

    val viewOriginProperty = SimpleObjectProperty<Point3D>(this, "viewOrigin", Point3D(0.0, 0.0, -50.0))
    var viewOrigin: Point3D by viewOriginProperty

    val viewOriginTransformProperty = SimpleObjectProperty<Translate>(this, "viewOriginTransform", Translate())
    var viewOriginTransform: Translate by viewOriginTransformProperty

    val forwardVelocityProperty = SimpleObjectProperty<Point3D>(this, "forwardVelocity", Point3D(0.0, 0.0, 0.0))
    var forwardVelocity: Point3D by forwardVelocityProperty

    val rightVelocityProperty = SimpleObjectProperty<Point3D>(this, "rightVelocity", Point3D(0.0, 0.0, 0.0))
    var rightVelocity: Point3D by rightVelocityProperty

    val viewLatProperty = SimpleDoubleProperty(this, "viewLat", 0.0)
    var viewLat by viewLatProperty

    val viewLonProperty = SimpleDoubleProperty(this, "viewLon", 0.0)
    var viewLon by viewLonProperty

    val viewLatTransformProperty = SimpleObjectProperty<Rotate>(this, "viewLatTransform", Rotate())
    var viewLatTransform: Rotate by viewLatTransformProperty

    val viewLonTransformProperty = SimpleObjectProperty<Rotate>(this, "viewLonTransform", Rotate())
    var viewLonTransform: Rotate by viewLonTransformProperty

    init {
        viewLatTransform.axis = Rotate.X_AXIS
        viewLonTransform.axis = Rotate.Y_AXIS
    }
}

class Asset3dView : View() {
    val controller by inject<AssetsController>()
    val asset3dController by inject<Asset3dController>()

    private val viewportSize = 2000

    private fun buildSceneFromAssets(): Group {
        val group = Group()

        controller.assetGroups
                .flatMap {it.assets}
                .mapNotNull {createAssetData(it) as? AssetDataMesh }
                .flatMap {it.readMeshes(controller::assetByName).asIterable() }
                .forEach { (appearance, mesh) ->

            println( appearance )
            val meshView = MeshView(mesh)
            meshView.material = appearance.material
            group.add(meshView)
        }
        return group
    }

    private fun createScene3d(group: Group): SubScene {
        val scene3d = SubScene(group, viewportSize.toDouble(), viewportSize * 9.0 / 16, true, SceneAntialiasing.BALANCED)

        val camera = PerspectiveCamera(true).apply {
            nearClip = 0.01
            farClip = 1000.0
            fieldOfView = 60.0
        }


        var dragDownX: Double = 0.0
        var dragDownY: Double = 0.0

        scene3d.setOnMouseClicked { event ->
            println(event)
        }

        scene3d.setOnMouseDragEntered {
            dragDownX = it.x
            dragDownY = it.y
        }
        scene3d.setOnMouseDragged { event ->
            val dX = (event.x - dragDownX).coerceIn(-3.0, 3.0)
            val dY = (event.y - dragDownY).coerceIn(-3.0, 3.0)



            with(asset3dController) {
                viewLon += dX
                while (viewLon < 0) {
                    viewLon += 360.0
                }
                while (viewLon >= 360.0) {
                    viewLon -= 360.0
                }

                viewLat = (viewLat + dY).coerceIn(-90.0, 90.0)

//                println( "x=${event.y} dY=$dY viewLon=$viewLon viewLat=$viewLat")
            }
            dragDownX = event.x
            dragDownY = event.y
        }

        scene3d.fill = Color.rgb(10, 10, 40)
//        scene3d.camera = camera
//        scene3d.camera.transforms.addAll(asset3dController.viewLatTransform, asset3dController.viewLonTransform, asset3dController.viewOriginTransform)

        val correct2dTo3d = Rotate()
        correct2dTo3d.axis = Rotate.Z_AXIS
        correct2dTo3d.angle = 180.0

        group {
            transforms.addAll(asset3dController.viewOriginTransform, asset3dController.viewLonTransform, asset3dController.viewLatTransform, correct2dTo3d)
//            transforms.addAll(asset3dController.viewLonTransform)
            add(camera)
        }.let {
            scene3d.add(it)
        }

        scene3d.camera = camera
        return scene3d
    }

    override val root = vbox {
        //        add(createScene3d(buildScene()))
        add(createScene3d(buildSceneFromAssets()))


    }


    init {
        fun propForKey(keyCode: KeyCode): SimpleBooleanProperty? =
                when (keyCode) {
                    KeyCode.W -> asset3dController.forwardProperty
                    KeyCode.S -> asset3dController.backwardProperty
                    KeyCode.A -> asset3dController.strafLeftProperty
                    KeyCode.D -> asset3dController.strafRightProperty
                    else -> null
                }


        root.sceneProperty().onChange {
            it?.setOnKeyPressed {
                propForKey(it.code)?.value = true
            }
            it?.setOnKeyReleased {
                propForKey(it.code)?.value = false
            }
        }

        val frameDuration = (1000 / 60.0).millis
        timeline {
            keyframe(frameDuration) {
                setOnFinished {
                    frame(frameDuration)
                }
            }
            cycleCount = Animation.INDEFINITE
            play()
        }

    }

    private fun frame(frameTime: Duration) {
        val targetVelcity = 10.0

        with(asset3dController) {
            forwardVelocity = when {
                forward -> forwardDir * targetVelcity
                backward -> forwardDir * (-targetVelcity)
                else -> Point3D.ZERO
            }
            rightVelocity = when {
                strafRigth -> rightDir * targetVelcity
                strafLeft -> rightDir * (-targetVelcity)
                else -> Point3D.ZERO
            }


//            viewLonTransform.

            val vel = viewLonTransform.transform(viewLatTransform.transform(forwardVelocity + rightVelocity))

            viewLonTransform.angle = -viewLon
            viewLatTransform.angle = viewLat


            viewOrigin += vel * frameTime.toSeconds()

            with(viewOriginTransform) {
                x = viewOrigin.x
                y = viewOrigin.y
                z = viewOrigin.z
            }
        }


//        println("forward=${asset3dController.forward} backward=${asset3dController.backward}")

    }
}