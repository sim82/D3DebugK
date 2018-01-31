package d3debug.views

import d3debug.viewmodels.WatchpointModel
import tornadofx.*

class WatchpointView : View() {
    private val watchpointModel : WatchpointModel by inject()

    override val root = hbox {
        vbox {
            label(watchpointModel.id)
            label(watchpointModel.scriptId)
            label(watchpointModel.line)
        }
        listview( watchpointModel.traces )
    }
}