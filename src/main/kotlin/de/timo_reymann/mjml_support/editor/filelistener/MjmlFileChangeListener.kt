package de.timo_reymann.mjml_support.editor.filelistener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.externalSystem.autoimport.changes.vfs.VirtualFileChangesListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.Topic
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType

val MJML_FILE_CHANGED_TOPIC = Topic.create("MjmlFileChanged", MjmlFileChangedListener::class.java)

/**
 * Listen for changes on mjml files in the current project
 */
interface MjmlFileChangedListener {
    /**
     * Gets called when mjml files changed on disk
     */
    fun onFilesChanged(files : Set<VirtualFile>)
}

open class MjmlFileChangeListener : VirtualFileChangesListener {
    private lateinit var mjmlFiles: MutableSet<VirtualFile>
    private val logger = logger<MjmlFileChangeListener>()

    override fun apply() {
        if (mjmlFiles.isEmpty()) {
            return
        }
        logger.debug("Changed files: $mjmlFiles")

        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_FILE_CHANGED_TOPIC)
            .onFilesChanged(mjmlFiles)
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
