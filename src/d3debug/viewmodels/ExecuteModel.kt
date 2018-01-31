package d3debug.viewmodels

import d3debug.controllers.ScriptsController
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class ExecuteModel : ViewModel() {
    val scriptController: ScriptsController by inject()

    val inputTextProperty = SimpleStringProperty(this, "inputText", "")
    var inputText by inputTextProperty

    val consoleTextProperty = SimpleStringProperty(this, "consoleText", "")
    var consoleText by consoleTextProperty

    fun addText(text: String) {
        consoleText += text
    }

    fun execute() {
        scriptController.debugConnection.execute(inputText) { text, error ->
            if (error)
            {
                consoleText += "Error:\n"
            }
            consoleText += text
        }
    }
}