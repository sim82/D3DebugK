package d3debug.views.assetdata

import d3cp.AssetCp
import d3debug.domain.AssetType
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.image.ImageView

class AssetDataPcm(reader: AssetCp.AssetPcmData.Reader) : AssetData() {
    override val assetType: AssetType = AssetType.Unknown
    override val previewSize = SimpleDoubleProperty(this, "previewSize", 100.0)

    init {
        previewNode.set(ImageView(Global.fallbackImage))
    }

    override fun loadSync() {

    }

}