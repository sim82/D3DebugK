package d3debug.domain

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*

class Script(val id: Int, name: String, sourcecode: Array<String>? = null) {
    val nameProperty = SimpleStringProperty(this, "name", name)
    var name: String by nameProperty

    var sourcecodeRequested = false

    val sourcecodeProperty = SimpleStringProperty(this, "sourcecode", sourcecode?.joinToString("\n"))
    var sourcecode: String by sourcecodeProperty

    val sourcecode2Property = SimpleListProperty<String>(this, "sourcecode2", sourcecode?.let { FXCollections.observableArrayList<String>(sourcecode.asList()) })
    var sourcecode2: ObservableList<String> by sourcecode2Property
}