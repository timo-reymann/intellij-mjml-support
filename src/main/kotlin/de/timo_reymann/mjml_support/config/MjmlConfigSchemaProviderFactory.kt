package de.timo_reymann.mjml_support.config

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory

class MjmlConfigSchemaProviderFactory : JsonSchemaProviderFactory {
    companion object {
        const val SCHEMA_FILE_NAME = "mjml-config-schema.json"
    }

    override fun getProviders(project: Project): MutableList<JsonSchemaFileProvider> {
        val provider = object : EmbeddedJsonSchemaFileProvider(
            SCHEMA_FILE_NAME, "MJML configuration",
            "http://json.schemastore.org/prettierrc",
            MjmlConfigSchemaProviderFactory::class.java, "/"
        ) {
            override fun isAvailable(file: VirtualFile): Boolean = file.name == ".mjmlconfig"
        }
        return mutableListOf(provider)
    }
}
