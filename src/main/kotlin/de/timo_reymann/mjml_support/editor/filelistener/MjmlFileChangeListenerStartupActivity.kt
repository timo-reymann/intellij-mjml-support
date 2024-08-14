package de.timo_reymann.mjml_support.editor.filelistener

import com.intellij.openapi.externalSystem.autoimport.changes.vfs.VirtualFileChangesListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class MjmlFileChangeListenerStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        VirtualFileChangesListener.installAsyncVirtualFileListener(MjmlFileChangeListener(), project)
    }
}
