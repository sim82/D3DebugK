package d3debug.views.assetdata

import d3cp.AssetCp
import d3debug.controllers.AssetsController
import d3debug.domain.Asset
import d3debug.domain.AssetType
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import java.util.*

class Global {
    companion object : tornadofx.Component() {
        private val assetsControler: AssetsController by inject()

        fun assetByName(name: String): Asset? = assetsControler.assetByName(name)

        val assetDataMap = hashMapOf<UUID, AssetData>()

        fun createAssetData(asset: Asset): AssetData =
                assetDataMap.getOrPut(UUID.fromString(asset.uuid)) {
                    when (asset.reader.which()) {
                        AssetCp.Asset.Which.PIXEL_DATA -> AssetDataPixel(asset.reader.pixelData)
                        AssetCp.Asset.Which.MESH_DATA -> AssetDataMesh(asset.reader.meshData)
                        AssetCp.Asset.Which.MATERIAL_DESC -> AssetDataMaterialDesc(asset.reader.materialDesc)
                        AssetCp.Asset.Which.PCM_DATA -> AssetDataPcm(asset.reader.pcmData)
                        else -> throw RuntimeException("unhandled asset type ${asset.reader.which().name}")
                    }
                }

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
}


abstract class AssetData {
    abstract val assetType: AssetType

    abstract val previewSize: DoubleProperty
    val previewNode = SimpleObjectProperty<Node?>(this, "previewNode", null)
    val previewImage = SimpleObjectProperty<Image?>(this, "previewImage", Global.fallbackImage)

//    val detail
    abstract fun loadSync()
    open fun loadAsync() = loadSync()
}

fun createAssetData(asset: Asset): AssetData = Global.createAssetData(asset)

inline fun <reified T : AssetData> withAssetData(asset: Asset, f : (T) -> Unit ) =
        (createAssetData(asset) as? T)?.let {assetData ->
            f(assetData)
        }
