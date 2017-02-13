package nl.rubensten.texifyidea.templates;

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import nl.rubensten.texifyidea.TexifyIcons;

/**
 * @author Ruben Schellekens
 */
public class LatexTemplatesFactory implements FileTemplateGroupDescriptorFactory {

    public static final String DEFAULT_TEMPLATE_FILENAME = "latex-source.tex";

    public LatexTemplatesFactory() {
    }

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor templateGroup =
                new FileTemplateGroupDescriptor("LaTeX file templates", TexifyIcons.LATEX_FILE);
        templateGroup.addTemplate(DEFAULT_TEMPLATE_FILENAME);

        return templateGroup;
    }

    public static PsiFile createFromTemplate(PsiDirectory directory, String name,
                                             String fileName, FileType fileType) {
        String content = "% MyFirstTemplate\n\n";

        PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());
        PsiFile file = factory.createFileFromText(fileName, fileType, content);

        return (PsiFile)directory.add(file);
    }
}
