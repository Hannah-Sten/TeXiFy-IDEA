package nl.rubensten.texifyidea.templates;

import com.intellij.ide.fileTemplates.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import nl.rubensten.texifyidea.TeXception;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.util.Container;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Ruben Schellekens
 */
public class LatexTemplatesFactory implements FileTemplateGroupDescriptorFactory {

    public static final String DESCRIPTOR = "LaTeX";

    public static final String FILE_TEMPLATE_TEX = "LaTeX Source.tex";
    public static final String FILE_TEMPLATE_STY = "LaTeX Package.sty";
    public static final String FILE_TEMPLATE_CLS = "LaTeX Document class.cls";

    public static PsiFile createFromTemplate(PsiDirectory directory, String fileName,
                                             String templateName, FileType fileType) {
        Project project = directory.getProject();
        String templateText = getTemplateText(project, templateName);

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
        PsiFile file = fileFactory.createFileFromText(fileName, fileType, templateText);

        Container<PsiFile> createdFile = new Container<>();
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> createdFile.setItem((PsiFile)directory.add(file)));

        return createdFile.getItem()
                .orElseThrow(() -> new TeXception("No created file in container."));
    }

    /**
     * Get the text from a certain template.
     *
     * @param project
     *         The IntelliJ project that contains the templates.
     * @param templateName
     *         The name of the template. Use the constants prefixed with {@code FILE_TEMPLATE_} from
     *         {@link LatexTemplatesFactory}.
     * @return The contents of the template with applied properties.
     */
    public static String getTemplateText(Project project, String templateName) {
        FileTemplateManager templateManager = FileTemplateManager.getInstance(project);
        FileTemplate template = templateManager.getInternalTemplate(templateName);
        Properties properties = new Properties(templateManager.getDefaultProperties());

        try {
            return template.getText(properties);
        }
        catch (IOException e) {
            throw new TeXception("Could not load template " + templateName, e);
        }
    }

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor descriptor = new FileTemplateGroupDescriptor(
                DESCRIPTOR,
                TexifyIcons.LATEX_FILE
        );

        descriptor.addTemplate(new FileTemplateDescriptor(FILE_TEMPLATE_TEX, TexifyIcons.LATEX_FILE));
        descriptor.addTemplate(new FileTemplateDescriptor(FILE_TEMPLATE_STY, TexifyIcons.STYLE_FILE));
        descriptor.addTemplate(new FileTemplateDescriptor(FILE_TEMPLATE_CLS, TexifyIcons.CLASS_FILE));

        return descriptor;
    }
}
