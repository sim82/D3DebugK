package d3debug.views.assetdata

import d3cp.AssetCp
import d3debug.domain.AssetType
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.image.ImageView

class AssetDataMesh(reader: AssetCp.AssetMeshData.Reader) : AssetData() {
    init {
        previewNode.set(ImageView(Global.fallbackImage))
    }

    override fun loadSync() {
    }

    override val previewSize = SimpleDoubleProperty(this, "previewSize", 100.0)

    override val assetType = AssetType.Mesh

}