package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import okio.Path.Companion.toPath
import java.nio.charset.StandardCharsets
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.io.path.relativeTo

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
            "/" + virtualFile.toNioPath().parent.relativeTo(Path(project.basePath!!)).toString(),
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
        memory.write(rawPtr, rawBytes)

        // Call render
        val resultPtr = renderMjml.apply(rawPtr.toLong())

        // Read characters until termination
        var character = -1
        val characters: MutableList<Byte?> = ArrayList<Byte?>()
        var offset = resultPtr[0].toInt()
        while (character != 0) {
            character = memory.read(offset).toInt()
            offset++
            if (character != 0) {
                characters.add(character.toByte())
            }
        }

        // Free memory for the return pointer
        freeString.apply(resultPtr[0])

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
