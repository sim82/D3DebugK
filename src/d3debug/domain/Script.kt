package d3debug.domain

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import tornadofx.*

class Script(id: Int, name: String, sourcecode: Array<String>? = null) {
    val idProperty = SimpleIntegerProperty(this, "id", id);
    var id by idProperty

    val nameProperty = SimpleStringProperty(this, "name", name)
    var name: String by nameProperty

    var sourcecodeRequested = false

    val sourcecodeProperty = SimpleStringProperty(this, "sourcecode", sourcecode?.joinToString("\n"))
    var sourcecode: String by sourcecodeProperty

    val sourcecode2Property = SimpleListProperty<String>(this, "sourcecode2", sourcecode?.let { FXCollections.observableArrayList<String>(sourcecode.asList()) })
    var sourcecode2: ObservableList<String> by sourcecode2Property

    val watchpointsProperty = SimpleSetProperty<Watchpoint>( this, "watchpoints", FXCollections.observableSet<Watchpoint>())
    var watchpoints: ObservableSet<Watchpoint> by watchpointsProperty
}