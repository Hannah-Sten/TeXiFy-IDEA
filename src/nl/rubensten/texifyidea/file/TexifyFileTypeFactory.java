package nl.rubensten.texifyidea.file;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class TexifyFileTypeFactory extends FileTypeFactory {

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(LatexFileType.INSTANCE, LatexFileType.INSTANCE.getDefaultExtension());
        consumer.consume(StyleFileType.INSTANCE, StyleFileType.INSTANCE.getDefaultExtension());
        consumer.consume(ClassFileType.INSTANCE, ClassFileType.INSTANCE.getDefaultExtension());
        consumer.consume(BibtexFileType.INSTANCE, BibtexFileType.INSTANCE.getDefaultExtension());
    }
}
