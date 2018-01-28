package d3debug.controllers

import d3debug.DebugConnection
import d3debug.domain.Script
import javafx.collections.FXCollections
import tornadofx.*

class ScriptsController : Controller() {
    val debugConnection = DebugConnection()

    init {
        debugConnection.scriptInfoRequest() {
            for ((index, name) in it) {
                val script = Script(index, name)
                values.add(index, script);
                script.sourcecode = "Please wait"
//                debugConnection.scriptGetRequest(index) { sourcecode ->
//                    script.sourcecode = sourcecode.joinToString("\n")
//                    script.sourcecode2 = FXCollections.observableArrayList<String>(sourcecode.asList())
//                }
            }
        }
    }

    val values = FXCollections.observableArrayList<Script>()
    fun requestSource(id: Int) {
        debugConnection.scriptGetRequest(id) { sourcecode ->
            values[id].sourcecode = sourcecode.joinToString("\n")
            values[id].sourcecode2 = FXCollections.observableArrayList<String>(sourcecode.asList())
        }
    }
}