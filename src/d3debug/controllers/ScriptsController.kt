package d3debug.controllers

import d3debug.DebugConnection
import d3debug.domain.Script
import d3debug.domain.Watchpoint
import d3debug.viewmodels.ScriptModel
import javafx.collections.FXCollections
import tornadofx.*

class ScriptsController : Controller() {
    private val debugConnection = DebugConnection()
    val values = FXCollections.observableArrayList<Script>()!!

    val watchpoints = FXCollections.observableArrayList<Watchpoint>()!!

    val scriptModel : ScriptModel by inject()

    init {
        debugConnection.scriptInfoRequest {
            for ((index, name) in it) {
                val script = Script(index, name)
                values.add(index, script)
                script.sourcecode = "Please wait"
            }
        }

        scriptModel.id.onChange {
            requestSource(it!!.toInt())
        }

        watchpoints.add(Watchpoint(1, 7, 666))
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
}