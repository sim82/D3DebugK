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

package d3debug

import d3debug.controllers.AssetsController
import d3debug.views.*
import javafx.application.Application
import tornadofx.*


//class d3debug.D3DebugApp : App(d3debug.TfxTest::class)

class D3DebugApp : App(Workspace::class) {

    val assetController: AssetsController by inject()

    override fun onBeforeShow(view: UIComponent) {
        Thread.setDefaultUncaughtExceptionHandler(null)
        with(workspace.leftDrawer)
        {
            item("Scripts") {
                this += ScriptList()
                expanded = true
            }
            item("Watchpoints") {
                this += WatchpointList()
            }
        }
        with(workspace.bottomDrawer) {
            item("Watchpoints") {
                this += WatchpointView()

            }
            item("Exec") {
                this += ExecuteView()
            }
        }

        workspace.dock<ScriptView>()

    }
}


fun main(args: Array<String>) {

    Application.launch(D3DebugApp::class.java, *args)
}