package nl.rubensten.texifyidea.templates;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Ruben Schellekens
 */
public class LatexTemplatesFactory {

    public static final String FILE_TEMPLATE_TEX = "LaTeX Source";
    public static final String FILE_TEMPLATE_STY = "LaTeX Package";
    public static final String FILE_TEMPLATE_CLS = "LaTeX Document class";

    public static PsiFile createFromTemplate(PsiDirectory directory, String fileName,
                                             String templateName, FileType fileType) {
        Project project = directory.getProject();
        FileTemplateManager templateManager = FileTemplateManager.getInstance(project);
        FileTemplate template = templateManager.getInternalTemplate(templateName);
        Properties properties = new Properties(templateManager.getDefaultProperties());

        String templateText;
        try {
            templateText = template.getText(properties);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not load tempalte " +
                    templateManager.internalTemplateToSubject(templateName), e);
        }

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
        PsiFile file = fileFactory.createFileFromText(fileName, fileType, templateText);
        return (PsiFile)directory.add(file);
    }

}
