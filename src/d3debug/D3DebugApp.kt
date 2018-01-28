package d3debug

import d3debug.controllers.ScriptsController
import d3debug.viewmodels.ScriptModel
import d3debug.views.ScriptList
import d3debug.views.ScriptView
import tornadofx.*
import javafx.application.*


//class d3debug.D3DebugApp : App(d3debug.TfxTest::class)

class D3DebugApp : App(Workspace::class) {

    override fun onBeforeShow(view: UIComponent) {
        with(workspace.leftDrawer)
        {
            item("Scripts") {
                this += ScriptList()
                expanded = true
            }
        }
        workspace.dock<ScriptView>()

    }
}



fun main(args: Array<String>) {
    Application.launch(D3DebugApp::class.java, *args)
}