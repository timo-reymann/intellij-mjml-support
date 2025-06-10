package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.mock.MockVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class WasiMjmlRendererTest : MjmlPluginBaseTestCase() {
    fun addFileToProject(fileName: String, fileContent: String): VirtualFile {
        val psiFile = PsiFileFactory.getInstance(project).createFileFromText(fileName, fileContent)
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
        val file = addFileToProject("test.mjml","<mjml></mjml>")
        val result = renderer.render(file,"<mjml>\n" +
                "    <mj-body>\n" +
                "        <mj-section>\n" +
                "            <mj-column>\n" +
                "                <mj-text>foo</mj-text>\n" +
                "            </mj-column>\n" +
                "        </mj-section>\n" +
                "    </mj-body>\n" +
                "</mjml>")

        assertTrue(result.contains("foo"))
    }

    public fun `test rendering template with special chars works`() {
        val renderer = WasiMjmlRenderer(project)
        val file = addFileToProject("test.mjml","<mjml></mjml>")
        val result = renderer.render(file,"<mjml>\n" +
                "    <mj-body>\n" +
                "        <mj-section>\n" +
                "            <mj-column>\n" +
                "                <mj-text>fóéoö</mj-text>\n" +
                "            </mj-column>\n" +
                "        </mj-section>\n" +
                "    </mj-body>\n" +
                "</mjml>")

        assertTrue(result.contains("fóéoö"))
    }

}
