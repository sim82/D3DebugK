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

package d3debug.controllers

import d3debug.DebugConnection
import d3debug.domain.Script
import d3debug.domain.Watchpoint
import d3debug.viewmodels.ScriptModel
import d3debug.viewmodels.WatchpointModel
import javafx.collections.FXCollections
import tornadofx.*

class ScriptsController : Controller() {
    val debugConnection = DebugConnection()
    val values = FXCollections.observableArrayList<Script>()!!

    val watchpoints = FXCollections.observableArrayList<Watchpoint>()!!

    val scriptModel: ScriptModel by inject()
    val watchpointModel: WatchpointModel by inject()

    init {
        debugConnection.scriptInfoRequest {
            for ((index, name) in it) {
                val script = Script(index, name)
                values.add(index, script)
                script.sourcecode = "Please wait"
            }
        }

        debugConnection.subscribeEventWatchpoint { wpId, _, _, localNames ->

            watchpoints.find {
                it.id == wpId
            }?.apply {
                        addTrace(localNames)
                    }
        }

        scriptModel.id.onChange {
            requestSource(it!!.toInt())
        }
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

    fun addWatchpoint(id: Int, line: Int, op: (Watchpoint) -> Unit) =
            debugConnection.addBreakpoint(id, line) { wpId ->
                val wp = Watchpoint(id, line, wpId)
                watchpoints.add(wp)
                op(wp)
            }
}


