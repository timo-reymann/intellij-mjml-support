package de.timo_reymann.mjml_support.config

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import de.timo_reymann.mjml_support.bundle.MjmlBundle

class MjmlConfigSchemaProviderFactory : JsonSchemaProviderFactory {
    companion object {
        const val SCHEMA_FILE_NAME = "mjml-config-schema.json"
        val SCHEMA_NAME = MjmlBundle.message("config_schema.name")
    }

    override fun getProviders(project: Project): MutableList<JsonSchemaFileProvider> {
        val provider = object : EmbeddedJsonSchemaFileProvider(
            SCHEMA_FILE_NAME,
            SCHEMA_NAME,
            "https://raw.githubusercontent.com/timo-reymann/intellij-mjml-support/main/src/main/resources/mjml-config-schema.json",
            MjmlConfigSchemaProviderFactory::class.java, "/"
        ) {
            override fun isAvailable(file: VirtualFile): Boolean = file.name == ".mjmlconfig"
        }
        return mutableListOf(provider)
    }
}
