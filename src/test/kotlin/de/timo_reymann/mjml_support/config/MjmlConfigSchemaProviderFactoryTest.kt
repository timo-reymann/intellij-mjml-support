package de.timo_reymann.mjml_support.config

import com.intellij.json.JsonFileType
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.jetbrains.jsonSchema.impl.JsonSchemaServiceImpl
import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase


class MjmlConfigSchemaProviderFactoryTest : MjmlPluginBaseTestCase() {
    fun testFileGetsDetected() {
        myFixture.configureByFiles(".mjmlconfig")
        assertTrue(myFixture.file.fileType == JsonFileType.INSTANCE)
        val providers =
            (JsonSchemaService.Impl.get(myFixture.project) as JsonSchemaServiceImpl).getProvidersForFile(myFixture.file.virtualFile)
        assertEquals(1, providers.size)
        assertEquals(providers[0].presentableName, MjmlConfigSchemaProviderFactory.SCHEMA_NAME)
    }

    override fun getTestDataPath(): String = super.getTestDataPath() + "file"
}
