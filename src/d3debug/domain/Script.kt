package d3debug.domain

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*

class Script( val id: Int, name: String, sourcecode: Array<String>? = null) {
    val nameProperty = SimpleStringProperty(this, "name", name)
    var name by nameProperty

    val sourcecodeProperty = SimpleStringProperty(this, "sourcecode", sourcecode?.joinToString("\n"))
    var sourcecode by sourcecodeProperty

    val sourcecode2Property = SimpleListProperty<String>(this, "sourcecode2", sourcecode?.let { FXCollections.observableArrayList<String>(sourcecode.asList()) })
    var sourcecode2 by sourcecode2Property
}