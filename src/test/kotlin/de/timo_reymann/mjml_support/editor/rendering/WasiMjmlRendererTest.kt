package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType

class WasiMjmlRendererTest : MjmlPluginBaseTestCase() {
    fun addFileToProject(fileName: String, fileContent: String): VirtualFile {
        val psiFile = PsiFileFactory.getInstance(project).createFileFromText(fileName, MjmlHtmlFileType.INSTANCE, fileContent)
        val virtualFile = psiFile.virtualFile
        if (virtualFile == null) {
            // PsiFile might be non-physical, create in-memory virtual file
            val vFile = myFixture.tempDirFixture.createFile(fileName, fileContent)
            return vFile
        }
        return virtualFile
    }

    public fun `test rendering simple template works`() {
        val renderer = WasiMjmlRenderer(project)
        val file = addFileToProject("test.mjml", "<mjml></mjml>")
        val result = renderer.render(
            file, "<mjml>\n" +
                    "    <mj-body>\n" +
                    "        <mj-section>\n" +
                    "            <mj-column>\n" +
                    "                <mj-text>foo</mj-text>\n" +
                    "            </mj-column>\n" +
                    "        </mj-section>\n" +
                    "    </mj-body>\n" +
                    "</mjml>"
        )

        assertTrue(result.contains("foo"))
    }

    public fun `test rendering template with special chars works`() {
        val renderer = WasiMjmlRenderer(project)
        val file = addFileToProject("test.mjml", "<mjml></mjml>")
        val result = renderer.render(
            file, "<mjml>\n" +
                    "    <mj-body>\n" +
                    "        <mj-section>\n" +
                    "            <mj-column>\n" +
                    "                <mj-text>fóéoö</mj-text>\n" +
                    "            </mj-column>\n" +
                    "        </mj-section>\n" +
                    "    </mj-body>\n" +
                    "</mjml>"
        )

        assertTrue(result.contains("fóéoö"))
    }

    public fun `test multiple times rendering works`() {
        val file = addFileToProject("test.mjml", "<mjml></mjml>")

        repeat(50) { idx ->
            val renderer = WasiMjmlRenderer(project)
            val result = renderer.render(
                file, "<mjml>\n" +
                        "    <mj-body>\n" +
                        "        <mj-section>\n" +
                        "            <mj-column>\n" +
                        "                <mj-text>$idx</mj-text>\n" +
                        "            </mj-column>\n" +
                        "        </mj-section>\n" +
                        "    </mj-body>\n" +
                        "</mjml>"
            )

            assertTrue(result.contains(idx.toString()))
        }
    }
}
