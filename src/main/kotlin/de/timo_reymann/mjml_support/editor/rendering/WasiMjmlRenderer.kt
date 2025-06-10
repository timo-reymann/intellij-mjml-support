package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import okio.Path.Companion.toPath
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.io.path.relativeTo

class WasiMjmlRendererException(message: String): Exception(message)

class WasiMjmlRenderer(project: Project) : BaseMjmlRenderer(project) {
    private val stringTerminator = "\u0000"
    private val wasiInstance by lazy {
        val loader = WasiLoader(
            WasiLoaderOptions(
                rootHostPath = Path(project.basePath!!)
            )
        )
        val moduleBuilder = loader.createModuleBuilder(getRendererWASI())
        moduleBuilder.build()
    }

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
        val mjmlRenderParameters = MjmlRenderParameters(
            "/" + Paths.get(virtualFile.parent.path).relativeTo(Path(project.basePath!!)).toString(),
            text,
            MjmlRenderParametersOptions(mjmlSettings.mjmlConfigFile),
            virtualFile.path
        )

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
        memory.write(rawPtr, rawBytes)

        // Call render
        val resultPtrs = renderMjml.apply(rawPtr.toLong())
        val resultPtr = resultPtrs[0].toInt()
        if(resultPtr == 0) {
            throw WasiMjmlRendererException("Received null pointer for render result")
        }

        // Read characters until termination
        var character = -1
        val characters: MutableList<Byte?> = ArrayList()
        var offset = resultPtr
        while (character != 0) {
            character = memory.read(offset).toInt()
            offset++
            if (character != 0) {
                characters.add(character.toByte())
            }
        }

        // Free memory for the return pointer
        freeString.apply(resultPtr.toLong())

        // Free memory for the input pointer
        freeString.apply(rawAllocPtr[0])

        // Convert into the byte array
        val byteArray = ByteArray(characters.size)
        for (j in characters.indices) {
            byteArray[j] = characters.get(j)!!
        }
        return String(byteArray, StandardCharsets.UTF_8)
    }
}
