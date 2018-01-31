package d3debug.viewmodels

import d3debug.controllers.ScriptsController
import d3debug.domain.Script
import tornadofx.*

class ScriptModel : ItemViewModel<Script>() {
    val id = bind(Script::idProperty)
    val name = bind(Script::nameProperty)
    val sourcecode = bind(Script::sourcecodeProperty)
    val sourcecode2 = bind(Script::sourcecode2Property)
    val watchpoints = bind(Script::watchpointsProperty)

    val controller : ScriptsController by inject()

    fun addWatchpoint( scriptPos : Int )
    {
        val substr = sourcecode.value.substring(0, scriptPos)

        // translate caret position -> line
        val line = substr.count {
            it == '\n'
        }

        val wp = controller.addWatchpoint(id.value.toInt(), line) {
            watchpoints.value.add(it);
        }
    }
}

