package d3debug.views

import d3debug.controllers.ScriptsController
import d3debug.viewmodels.ExecuteModel
import tornadofx.*

class ExecuteView : View() {
//    private val controller : ScriptsController by inject()
    private val executeModel : ExecuteModel by inject()

    override val root = vbox {
        textarea(executeModel.consoleTextProperty) {

        }
        hbox {
            button("exec") {
                action {
                    executeModel.execute()
                }
            }
            textfield(executeModel.inputTextProperty) {

            }
        }
    }
}