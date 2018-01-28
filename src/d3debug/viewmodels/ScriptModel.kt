package d3debug.viewmodels

import d3debug.domain.Script
import tornadofx.*

class ScriptModel : ItemViewModel<Script>() {
    val name = bind(Script::nameProperty)
    val sourcecode = bind(Script::sourcecodeProperty)
    val sourcecode2 = bind(Script::sourcecode2Property)
}