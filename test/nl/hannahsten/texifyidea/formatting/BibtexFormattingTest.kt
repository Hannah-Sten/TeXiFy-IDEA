package nl.hannahsten.texifyidea.formatting

import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.testutils.writeCommand

class BibtexFormattingTest : BasePlatformTestCase() {

    fun testSpacesAroundBraces() {
        """
            @inproceedings {pose:alp2018densepose, 
              title={Densepose: Dense human pose estimation in the wild},
                author = {G{\"u}ler, R{\i}za Alp and Neverova, Natalia and Kokkinos, Iasonas},
                  booktitle = {Proceedings of the IEEE Conference on Computer Vision and Pattern Recognition},
                pages = {7297--7306},
                year = {2018}
            }
        """.trimIndent() `should be reformatted to` """
            @inproceedings{pose:alp2018densepose,
                title = {Densepose: Dense human pose estimation in the wild},
                author = {G{\"u}ler, R{\i}za Alp and Neverova, Natalia and Kokkinos, Iasonas},
                booktitle = {Proceedings of the IEEE Conference on Computer Vision and Pattern Recognition},
                pages = {7297--7306},
                year = {2018}
            }
        """.trimIndent()
    }

    private infix fun String.`should be reformatted to`(expected: String) {
        myFixture.configureByText(BibtexFileType, this)
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        myFixture.checkResult(expected)
    }
}