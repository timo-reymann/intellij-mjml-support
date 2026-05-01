package de.timo_reymann.mjml_support.editor.rendering

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.io.Decompressor
import de.timo_reymann.mjml_support.settings.MjmlVersion
import de.timo_reymann.mjml_support.util.FileLockFailedException
import de.timo_reymann.mjml_support.util.FileLockUtil
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.util.MessageBusUtil
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path

object BuiltinRenderResourceProvider {
    private val mjmlVersions: MutableMap<MjmlVersion, String> =
        MjmlVersion.entries.associateWith { "?" }.toMutableMap()
    private var mrmlVersion: String = "?"

    private val mapper = jacksonObjectMapper()
    private val logger = logger<BuiltinRenderResourceProvider>()

    fun getBundledMjmlVersion(version: MjmlVersion): String = mjmlVersions[version] ?: "?"
    fun getBundledMrmlVersion(): String = mrmlVersion

    fun getBuiltinWasiRenderer(): ByteArray {
        val stream = FilePluginUtil.getResource(Path(FilePluginUtil.WASI_RENDERER_WASM_NAME))
        return stream.readAllBytes()
    }

    fun copyResources() {
        try {
            extractMrmlVersion()
        } catch (e: Exception) {
            logger.warn("Failed to extract MRML version", e)
        }

        var anyExtracted = false
        for (version in MjmlVersion.entries) {
            if (copyRendererArchive(version)) anyExtracted = true
        }

        // Notify once after all archives are in place so each open preview triggers a single
        // force-render. Firing per-archive caused overlapping setHtml() calls on JCEF that
        // could leave the web view blank while the source viewer still showed correct HTML.
        if (anyExtracted) {
            notifyRendererResourcesChanged()
        }
    }

    private fun copyRendererArchive(version: MjmlVersion): Boolean {
        val archiveName = archiveNameFor(version)
        val rendererZip = FilePluginUtil.getFile(archiveName)
        val lockFile = FilePluginUtil.getFile("$archiveName.lock")
        val finalDir = FilePluginUtil.getFile(version.dirName)
        val stagingDir = FilePluginUtil.getFile("${version.dirName}.tmp")

        try {
            FileLockUtil.runWithLock(lockFile) {
                FilePluginUtil.copyFile("node", archiveName)

                // Extract to a staging directory first, then atomically swap into place. This
                // prevents render calls landing mid-extraction from seeing partial node_modules
                // (e.g. juice present but its transitive web-resource-inliner not yet on disk).
                if (stagingDir.exists()) FileUtil.delete(stagingDir)
                Decompressor.Zip(rendererZip.toPath()).extract(stagingDir.toPath())

                if (finalDir.exists()) FileUtil.delete(finalDir)
                try {
                    Files.move(
                        stagingDir.toPath(),
                        finalDir.toPath(),
                        StandardCopyOption.ATOMIC_MOVE
                    )
                } catch (_: Exception) {
                    // Atomic move can fail across filesystems; fall back to a regular move.
                    Files.move(stagingDir.toPath(), finalDir.toPath())
                }

                extractMjmlVersion(version)

                try {
                    rendererZip.delete()
                } catch (e: Exception) {
                    logger.warn("Failed to delete render zip $archiveName", e)
                }
            }
            return true
        } catch (e: FileLockFailedException) {
            logger.warn("Failed to get file lock to copy renderer resources for ${version.id}", e)
            MessageBusUtil.showMessage(
                NotificationType.WARNING,
                "Failed to copy resources for builtin mjml ${version.id} rendering",
                "This may be because another instance is already copying the files.<br/>" +
                        "You can also retry it manually under <b>Settings > Tools > MJML-Settings</b> | 'Copy renderer files for mjml preview'"
            )
            return false
        }
    }

    private fun archiveNameFor(version: MjmlVersion): String = when (version) {
        MjmlVersion.V4 -> FilePluginUtil.NODE_RENDERER_V4_ARCHIVE_NAME
        MjmlVersion.V5 -> FilePluginUtil.NODE_RENDERER_V5_ARCHIVE_NAME
    }

    private fun notifyRendererResourcesChanged() {
        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_PREVIEW_FORCE_RENDER_TOPIC)
            .onForcedRender()
    }

    private fun extractMjmlVersion(version: MjmlVersion) {
        try {
            mjmlVersions[version] = parseMjmlVersion(version)
        } catch (e: Exception) {
            mjmlVersions[version] = "N/A"
            logger.warn("Failed to parse mjml version for ${version.id}", e)
        }
    }

    fun extractMrmlVersion() {
        val stream = FilePluginUtil.getResource(Path("wasi/mrml-render.version"))
        val reader = InputStreamReader(stream)
        val version = reader.readText().trim()
        mrmlVersion = version;
    }

    private fun parseMjmlVersion(version: MjmlVersion): String {
        val packageJson = FilePluginUtil.getFile("${version.dirName}/package.json")
        return mapper.readTree(packageJson)["dependencies"]["mjml"].asText()
    }
}
