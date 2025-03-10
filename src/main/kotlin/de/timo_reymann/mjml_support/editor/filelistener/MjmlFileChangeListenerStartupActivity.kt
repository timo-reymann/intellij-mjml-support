package de.timo_reymann.mjml_support.editor.filelistener

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VirtualFileManager

class MjmlFileChangeListenerStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        project.messageBus
            .connect()
            .subscribe(
                VirtualFileManager.VFS_CHANGES,
                MjmlFileChangeListener()
            )
    }
}
