package d3debug.views

import d3debug.viewmodels.ScriptModel
import tornadofx.*

class ScriptView : View() {
    private val scriptModel: ScriptModel by inject()

    override val root = textarea(scriptModel.sourcecode) {
        contextmenu {

            item("Add Watchpoint") {
                action {

                    println("caretPosition $caretPosition")
                    scriptModel.addWatchpoint(caretPosition)
                }
            }
        }
    }
}


