package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import okio.Path.Companion.toPath
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.io.path.relativeTo

class WasiMjmlRendererException(message: String) : Exception(message)

class WasiMjmlRenderer(project: Project) : BaseMjmlRenderer(project) {
    private val stringTerminator = "\u0000"
    private val loader = WasiLoader(
        WasiLoaderOptions(
            rootHostPath = Path(project.basePath!!)
        )
    )
    private val moduleBuilder = loader.createModuleBuilder(getRendererWASI())

    private fun getRendererWASI(): ByteArray {
        if (mjmlSettings.useBuiltinWASIRenderer) {
            return BuiltinRenderResourceProvider.getBuiltinWasiRenderer()
        }

        return mjmlSettings.rendererWASIPath
            .toPath(true)
            .toNioPath()
            .readBytes()
    }

    override fun render(virtualFile: VirtualFile, text: String): String {
        val wasiInstance = moduleBuilder.build()

        val fileRelativePath = Paths.get(virtualFile.path).relativeTo(Path(project.basePath!!))
        val mjmlRenderParameters = MjmlRenderParameters(
            "/" +  (fileRelativePath.parent ?: "/").toString(),
            text,
            MjmlRenderParametersOptions(mjmlSettings.mjmlConfigFile),
            "/$fileRelativePath"
        )

        wasiInstance.module().functionSection()
        val renderMjml = wasiInstance.export("render_mjml")
        val freeString = wasiInstance.export("free_string")
        val allocString = wasiInstance.export("alloc_string")
        val memory = wasiInstance.memory()

        val raw = objectMapper.writeValueAsString(mjmlRenderParameters) + stringTerminator
        val rawBytes = raw.toByteArray()

        // Allocate memory for raw and write into memory
        val rawAllocPtr = allocString.apply(rawBytes.size.toLong())
        val rawPtr = rawAllocPtr[0].toInt()
        if(rawPtr == 0) {
            throw WasiMjmlRendererException("Received null pointer for alloc string")
        }
        memory.writeCString(rawPtr,raw)

        // Call render
        val resultPtrs = renderMjml.apply(rawPtr.toLong())
        val resultPtr = resultPtrs[0].toInt()
        if(resultPtr == 0) {
            throw WasiMjmlRendererException("Received null pointer for render result")
        }

        // Read cstring from memory
        val str = memory.readCString(resultPtr)

        // Free memory for the return pointer
        freeString.apply(resultPtr.toLong())

        // Free memory for the input pointer
        freeString.apply(rawAllocPtr[0])

        return str
    }
}
