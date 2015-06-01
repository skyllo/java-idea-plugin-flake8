package com.skyllo.idea.plugin.flake8;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Flake8Annotator extends ExternalAnnotator<PsiFile, List<Flake8Annotator.Issue>> {

    private static Logger LOG = Logger.getInstance(Flake8Annotator.class.getName());
    private static Pattern PATTERN = Pattern.compile(".*:(\\d+):(\\d+): .{4} (.*)");

    private boolean isRunning = false;

    public Flake8Annotator() {
        LOG.info("Flake8Annotator created!");
        if (Utils.canRunFlake8() && (Utils.isMac() || Utils.isUnix())) {
            LOG.info("Flake8Annotator is running");
            isRunning = true;
        } else {
            LOG.info("Flake8Annotator is not running");
        }
    }

    /**
     * Called first; return file
     */
    @Override
    @Nullable
    public PsiFile collectInformation(@NotNull final PsiFile file) {
        return file;
    }

    /**
     * Called second; run flake8 on file
     */
    @Nullable
    @Override
    public List<Issue> doAnnotate(final PsiFile file) {
        if (Utils.isValidPsiFile(file) && isRunning) {
            // Force all files to save so its sync with the local file system
            ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    FileDocumentManager.getInstance().saveAllDocuments();
                }
            }, ModalityState.any());

            return getFlake8Issues(file);
        } else {
            return Collections.emptyList();
        }
    }

    private List<Issue> getFlake8Issues(@NotNull final PsiFile file) {
        final List<Issue> issues = new ArrayList<Issue>();
        String filePath = file.getVirtualFile().getPath();

        BufferedReader reader = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-cl", "flake8 --select=I10 " + filePath);
            processBuilder.redirectErrorStream(true);
            final Process p = processBuilder.start();
            p.waitFor();

            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = PATTERN.matcher(line);
                if (matcher.find()) {
                    int lineNumber = Integer.parseInt(matcher.group(1));
                    int columnNumber = Integer.parseInt(matcher.group(2));
                    String errorMessage = matcher.group(3);
                    LOG.info("Found flake8 issue: " + line);
                    LOG.info("Parsed flake8 issue: " + lineNumber + ":" + columnNumber + ":" + errorMessage);
                    issues.add(new Issue(lineNumber, columnNumber, errorMessage));
                } else {
                    LOG.warn("Found flake8 issue but regex did not match: " + line);
                }
            }

        } catch (Exception e) {
            LOG.error("Exception executing flake8 on: " + filePath, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("Exception closing stream", e);
                }
            }
        }

        return issues;
    }

    /**
     * Called third; applying annotations
     */
    @Override
    public void apply(@NotNull PsiFile file, List<Flake8Annotator.Issue> issues, @NotNull AnnotationHolder holder) {
        LOG.info("Highlighting flake8 issues: " + file.getVirtualFile().getPath());

        Project project = file.getProject();
        final Document document = PsiDocumentManager.getInstance(project).getDocument(file);

        for (Issue issue : issues) {
            int startOffset = document.getLineStartOffset(issue.lineNumber - 1);
            int endOffset = document.getLineEndOffset(issue.lineNumber - 1);
            TextRange textRange = new TextRange(startOffset, endOffset);
            holder.createErrorAnnotation(textRange, issue.errorMessage);
        }

        super.apply(file, issues, holder);
    }

    public class Issue {
        final int lineNumber;
        final int columnNumber;
        final String errorMessage;

        public Issue(int lineNumber, int columnNumber, String errorMessage) {
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
            this.errorMessage = errorMessage;
        }
    }

}
