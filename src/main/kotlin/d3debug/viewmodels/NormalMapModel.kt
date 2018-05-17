package d3debug.viewmodels

import d3debug.views.assetdata.createNormalMapFromHeightMap
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.stage.FileChooser
import tornadofx.*

class NormalMapModel : ViewModel() {
    val inputImageProperty = SimpleObjectProperty<Image>(this, "inputImage", WritableImage(32,32))
    var inputImage by inputImageProperty

    val outputImageProperty = SimpleObjectProperty<Image>(this, "outputImage", WritableImage(32,32))
    var outputImage by outputImageProperty


    init {
        inputImageProperty.onChange {
            it ?: return@onChange
            outputImage = createNormalMapFromHeightMap(it)
        }
    }

    fun loadImage() {
        val filename = FileChooser().showOpenDialog(null)

        filename ?: return

        inputImage = Image(filename.toURI().toString())

        outputImage = createNormalMapFromHeightMap(inputImage)
    }
}