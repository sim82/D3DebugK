package d3debug.views

import d3debug.viewmodels.NormalMapModel
import javafx.scene.Parent
import tornadofx.*

class NormalMapView : View() {
    val model : NormalMapModel by inject()

    override val root = vbox {
        button {
            text = "load"
            setOnAction {
                model.loadImage()
            }
        }

        hbox {
            imageview {
                imageProperty().bind(model.inputImageProperty)
            }
            imageview {
                imageProperty().bind(model.outputImageProperty)
            }
        }
    }
}