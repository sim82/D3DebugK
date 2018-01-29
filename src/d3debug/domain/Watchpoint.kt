package d3debug.domain

import javafx.beans.property.SimpleIntegerProperty
import tornadofx.*

class Watchpoint( id : Int, scriptId : Int, line : Int )
{
    val idProperty = SimpleIntegerProperty( this, "id", id )
    var id by idProperty

    val scriptIdProperty = SimpleIntegerProperty( this, "scriptId", scriptId )
    var scriptId by scriptIdProperty

    val lineProperty = SimpleIntegerProperty( this, "line", line)
    var line by lineProperty
}