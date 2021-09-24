package de.timo_reymann.mjml_support.editor.filelistener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.externalSystem.autoimport.AsyncFileChangeListenerBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import de.timo_reymann.mjml_support.editor.render.MJML_PREVIEW_FORCE_RENDER_TOPIC
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType

class MjmlFileChangeListener : AsyncFileChangeListenerBase() {
    private lateinit var mjmlFiles: MutableSet<VirtualFile>
    private val logger = logger<MjmlFileChangeListener>()

    override fun apply() {
        if (mjmlFiles.isEmpty()) {
            return
        }
        logger.info("Changed files: $mjmlFiles")
        // TODO Create different topic with file
        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_PREVIEW_FORCE_RENDER_TOPIC)
            .onForcedRender()
    }

    override fun init() {
        mjmlFiles = HashSet()
    }

    override fun updateFile(file: VirtualFile, event: VFileEvent) {
        mjmlFiles.add(file)
    }

    override fun isRelevant(file: VirtualFile, event: VFileEvent): Boolean {
        return event.file != null &&
                !event.file!!.isDirectory &&
                event.file!!.fileType == MjmlHtmlFileType.INSTANCE
    }
}
