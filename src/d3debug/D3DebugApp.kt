package d3debug

import d3debug.views.*
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
            item("Watchpoints") {
                this += WatchpointList()
            }
        }
        with(workspace.bottomDrawer) {
            item("Watchpoints") {
                this += WatchpointView()

            }
            item( "Exec" ) {
                this += ExecuteView()
            }
        }

        workspace.dock<ScriptView>()

    }
}



fun main(args: Array<String>) {
    Application.launch(D3DebugApp::class.java, *args)
}