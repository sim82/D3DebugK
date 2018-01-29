package d3debug.views

import d3debug.controllers.ScriptsController
import d3debug.viewmodels.WatchpointModel
import tornadofx.*

class WatchpointList : View() {
    private val controller : ScriptsController by inject()
    private val watchpointModel : WatchpointModel by inject()

    override val root = listview(controller.watchpoints) {
        bindSelected(watchpointModel)

        cellFormat {
            text = "${it.id} ${it.scriptId} ${it.line}"
        }
    }
}