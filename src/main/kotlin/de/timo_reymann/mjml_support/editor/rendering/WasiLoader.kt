package de.timo_reymann.mjml_support.editor.rendering

import com.dylibso.chicory.compiler.InterpreterFallback
import com.dylibso.chicory.compiler.MachineFactoryCompiler
import com.dylibso.chicory.log.SystemLogger
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.wasi.WasiOptions
import com.dylibso.chicory.wasi.WasiPreview1
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.types.MemoryLimits
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.absolute

data class WasiLoaderOptions(
    val rootHostPath: Path,
    val stdout: OutputStream = System.out,
    val stderr: OutputStream = System.err,
)

class WasiModuleBuilder(private val builder: Instance.Builder) {
    fun build(): Instance = builder.build()
}

class WasiLoader(private val options: WasiLoaderOptions) {
    private fun createStore(): Store {
        val options = WasiOptions.builder()
            .withStdout(options.stdout)
            .withStderr(options.stderr)
            .withEnvironment("RUST_BACKTRACE", "full")
            .withDirectory("/", options.rootHostPath.absolute())
            .build()

        val wasi = WasiPreview1.builder()
            .withOptions(options)
            .withLogger(SystemLogger())
            .build()

        return Store()
            .addFunction(*wasi.toHostFunctions())
    }

    fun createModuleBuilder(raw: ByteArray): WasiModuleBuilder {
        val store = createStore()
        val module = Parser.parse(raw)
        val builder = Instance.builder(module)
            .withImportValues(store.toImportValues())
            .withMachineFactory(
                MachineFactoryCompiler
                    .builder(module)
                    .withInterpreterFallback(InterpreterFallback.WARN)
                    .compile()
            )
        return WasiModuleBuilder(builder)
    }
}
