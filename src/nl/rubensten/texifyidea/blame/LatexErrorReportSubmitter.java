package nl.rubensten.texifyidea.blame;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Send error report to GitHub issue tracker.
 *
 * @author Sten Wessel
 */
public class LatexErrorReportSubmitter extends ErrorReportSubmitter {

    private static final String URL = "https://github.com/Ruben-Sten/TeXiFy-IDEA/issues/new?labels[]=crash-report&title=Crash%20Report:%20";
    private static final String ENCODING = "UTF-8";

    @NotNull
    @Override
    public String getReportActionText() {
        return "Report to TeXiFy-IDEA issue tracker";
    }

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo,
                          @NotNull Component parentComponent, @NotNull
                                      Consumer<SubmittedReportInfo> consumer) {
        IdeaLoggingEvent event = events[0];

        StringBuilder builder = new StringBuilder(URL);
        try {
            builder.append(URLEncoder.encode(event.getMessage(), ENCODING));
            builder.append("&body=");
            builder.append(URLEncoder.encode("### Description\n", ENCODING));
            builder.append(URLEncoder.encode(additionalInfo == null ? "\n" : additionalInfo, ENCODING));
            builder.append(URLEncoder.encode("\n### Stacktrace\n```\nPlease paste the full stacktrace from the IDEA error popup\n```", ENCODING));
        }
        catch (UnsupportedEncodingException e) {
            consumer.consume(new SubmittedReportInfo(null, null, SubmittedReportInfo
                    .SubmissionStatus.FAILED));
            return false;
        }

        BrowserUtil.browse(builder.toString());
        consumer.consume(new SubmittedReportInfo(null, "GitHub issue",
                                                 SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));
        return true;
    }
}
