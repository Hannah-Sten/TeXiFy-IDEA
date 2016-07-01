package nl.rubensten.texifyidea;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sten Wessel
 */
public class LatexCommenter implements Commenter {

    @Nullable
    @Override
    public String getLineCommentPrefix() {
        return "%";
    }

    @Nullable
    @Override
    public String getBlockCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getBlockCommentSuffix() {
        return null;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }
}
