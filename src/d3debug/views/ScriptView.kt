package d3debug.views

import d3debug.viewmodels.ScriptModel
import tornadofx.*

class ScriptView : View() {
    val scriptModel: ScriptModel by inject()

    override val root = textarea(scriptModel.sourcecode) {
    }
}