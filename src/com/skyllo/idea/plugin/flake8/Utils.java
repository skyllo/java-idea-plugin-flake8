package com.skyllo.idea.plugin.flake8;

import com.intellij.psi.PsiFile;

public class Utils {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    public static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    public static boolean canRunFlake8() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-cl", "type -P flake8 &>/dev/null && exit 0 || exit 1");
            processBuilder.redirectErrorStream(true);
            final Process p = processBuilder.start();
            p.waitFor();
            if (p.exitValue() == 0) {
                return true;
            }
        } catch (Exception e) {
            // do nothing
        }
        return false;
    }

    public static boolean isValidPsiFile(PsiFile file) {
        return file != null && file.getVirtualFile() != null && file.getVirtualFile().isValid();
    }

}
