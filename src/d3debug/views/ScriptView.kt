package d3debug.views

import d3debug.viewmodels.ScriptModel
import d3debug.viewmodels.WatchpointModel
import tornadofx.*

class ScriptView : View() {
    private val scriptModel: ScriptModel by inject()

    override val root = textarea(scriptModel.sourcecode) {
    }
}

class WatchpointView : View() {
    private val watchpointModel : WatchpointModel by inject()

    override val root = vbox {
        label(watchpointModel.id)
        label(watchpointModel.scriptId)
        label(watchpointModel.line)
    }
}