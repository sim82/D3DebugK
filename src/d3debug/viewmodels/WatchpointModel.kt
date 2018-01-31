package d3debug.viewmodels

import d3debug.domain.Watchpoint
import tornadofx.*

class WatchpointModel : ItemViewModel<Watchpoint>() {
    val id = bind(Watchpoint::idProperty)
    val scriptId = bind(Watchpoint::scriptIdProperty)
    val line = bind(Watchpoint::lineProperty)
    val traces = bind(Watchpoint::tracesPropert)
}

