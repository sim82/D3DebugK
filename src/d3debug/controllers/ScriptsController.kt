package d3debug.controllers

import d3debug.DebugConnection
import d3debug.domain.Script
import d3debug.domain.Watchpoint
import d3debug.viewmodels.ScriptModel
import d3debug.viewmodels.WatchpointModel
import javafx.collections.FXCollections
import tornadofx.*

class ScriptsController : Controller() {
    val debugConnection = DebugConnection()
    val values = FXCollections.observableArrayList<Script>()!!

    val watchpoints = FXCollections.observableArrayList<Watchpoint>()!!

    val scriptModel : ScriptModel by inject()
    val watchpointModel : WatchpointModel by inject()

    init {
        debugConnection.scriptInfoRequest {
            for ((index, name) in it) {
                val script = Script(index, name)
                values.add(index, script)
                script.sourcecode = "Please wait"
            }
        }

        debugConnection.subscribeEventWatchpoint {
            wpId, scriptId, line, localNames ->

            watchpoints.find {
                it.id == wpId
            }?.apply {
                addTrace( localNames )
            }
        }

        scriptModel.id.onChange {
            requestSource(it!!.toInt())
        }
    }


    private fun requestSource(id: Int) {
        if (values[id].sourcecodeRequested) {
            return
        }

        values[id].sourcecodeRequested = true

        debugConnection.scriptGetRequest(id) { sourcecode ->
            values[id].sourcecode = sourcecode.joinToString("\n")
            values[id].sourcecode2 = FXCollections.observableArrayList<String>(sourcecode.asList())
        }
    }

    fun addWatchpoint(id: Int, line: Int, op : (Watchpoint) -> Unit ) {
        val wp = Watchpoint(id, line)

        debugConnection.addBreakpoint(id,  line) { wpId ->

            val wp = Watchpoint(id, line, wpId)
            watchpoints.add(wp)
            op(wp)
        }
    }
}