package d3debug.views

import d3debug.controllers.ScriptsController
import d3debug.viewmodels.ScriptModel
import tornadofx.*

class ScriptList : View() {
    val controller: ScriptsController by inject()
    val scriptModel: ScriptModel by inject()

    override val root = listview(controller.values) {
        bindSelected(scriptModel)
        onUserSelect(1) {
            controller.requestSource(it.id)
        }
        cellFormat {
            text = it.name
        }
    }
}