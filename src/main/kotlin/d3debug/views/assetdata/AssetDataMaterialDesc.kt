package d3debug.views.assetdata

import d3cp.AssetCp
import d3debug.domain.AssetType
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Group
import javafx.scene.PerspectiveCamera
import javafx.scene.SubScene
import javafx.scene.image.PixelFormat
import javafx.scene.paint.PhongMaterial
import javafx.scene.transform.Translate
import tornadofx.*


class AssetDataMaterialDesc(val reader: AssetCp.AssetMaterialDesc.Reader) : AssetData() {
    override val previewSize = SimpleDoubleProperty(this, "previewSize", 100.0)

    val material : PhongMaterial

    init {
        val mat = PhongMaterial()
        material = mat
        if (reader.isPhong) {
            val phong = reader.phong!!

            fun findPixelData(name: String): AssetDataPixel? {
                return Global.assetByName(name)?.let { asset ->
                    createAssetData(asset) as? AssetDataPixel
                }
            }

            if (phong.hasDiffuseMap()) {

                findPixelData(phong.diffuseMap.toString())?.let {
                    it.loadAsync()
                    mat.diffuseMapProperty().bind(it.imageProperty)
//                        mat.diffuseMap = it.image
                }
            }
            if (phong.hasNormalMap()) {
                findPixelData(phong.normalMap.toString())?.let {
                    it.loadSync()

                    val pixelFormat = it.image?.pixelReader?.pixelFormat!!
                    if (pixelFormat.type == PixelFormat.Type.BYTE_INDEXED) {
                        mat.bumpMap = createNormalMapFromHeightMap(it.image!!)
                    }
                    else {
                        mat.bumpMap = it.image!!
                    }

                }
            }
            if (phong.hasSpecularMap()) {
                findPixelData(phong.specularMap.toString())?.let {
                    it.loadAsync()
                    mat.specularMapProperty().bind(it.imageProperty)
//                        mat.specularMap = it.image
                }
            }
//            mat.specularPower = 100.0

        }


//                mat.diffuseMap = image2
//        previewNode.set(javafx.scene.shape.Box(100.0, 100.0, 100.0).apply {
//            material = mat
//            widthProperty().bind(previewSize)
//            heightProperty().bind(previewSize)
//            depthProperty().bind(previewSize)
//
//            rotationAxis = Point3D(0.0, 1.0, 0.0)
//            rotateProperty().bind(previewSize)
//        })
//
//        previewNode.set(javafx.scene.shape.Sphere(50.0).apply{
//            material = mat
//            radiusProperty().bind(previewSize.multiply(0.5))
//        })
//

        val group = Group()
        val sphere = javafx.scene.shape.Sphere(50.0).apply {
            material = mat
            radiusProperty().bind(previewSize.multiply(0.5))


            transforms += Translate(50.0, 50.0, 0.0).apply {
                translateXProperty().bind(previewSize.multiply(0.5))
                translateYProperty().bind(previewSize.multiply(0.5))

            }
        }

        group += sphere

        group += PerspectiveCamera(true).apply {
            nearClip = 0.01
            farClip = 1000.0
            fieldOfView = 60.0
            transforms += Translate(0.0, 0.0, -20.0)
        }


        val subscene = SubScene(group, 100.0, 100.0)

        subscene.widthProperty().bind(previewSize)
        subscene.heightProperty().bind(previewSize)

        previewNode.set(subscene)
    }



    override fun loadSync() {

    }


    override val assetType: AssetType = AssetType.Appearance

}