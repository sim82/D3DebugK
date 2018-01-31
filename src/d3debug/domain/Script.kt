/*
 * Copyright (c) 2018 Simon A. Berger
 * Licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

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