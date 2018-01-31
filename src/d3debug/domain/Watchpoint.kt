package d3debug.domain

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import tornadofx.*

class Trace(localNames : ArrayList<String>)
{
    val localNamesProperty = SimpleListProperty<String>( this, "localNames", FXCollections.observableArrayList(localNames) )
    val localNames by localNamesProperty
}

class Watchpoint(scriptId: Int, line: Int, id: Int? = null) {
    val idProperty = SimpleIntegerProperty(this, "id", id ?: -1)
    var id by idProperty

    val scriptIdProperty = SimpleIntegerProperty(this, "scriptId", scriptId)
    var scriptId by scriptIdProperty

    val lineProperty = SimpleIntegerProperty(this, "line", line)
    var line by lineProperty

    val tracesPropert = SimpleListProperty<Trace>( this, "traces", FXCollections.observableArrayList<Trace>())
    var traces by tracesPropert

    fun addTrace(localNames: ArrayList<String>) {
        traces.add(Trace(localNames))
    }
}