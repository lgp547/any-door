package io.github.lgp547.anydoorplugin.dialog.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class JavaFileInfoUtil {

    public static final String METHOD_NAME = "preRun";

    public static final String TEMPLATE = """
                public class AnyDoorInjectedClass {
                    /** AnyDoorIsUpdatePreRun:true */
                    public void preRun() {
                %s
                    }
                }
                """;


    public static class JavaFileInfo {
        private final String content;
        private final List<String> importStrs;
        private final String filePath;

        public JavaFileInfo(String content, List<String> importStrs, String filePath) {
            this.content = content;
            this.importStrs = importStrs;
            this.filePath = filePath;
        }

        public String getContent() {
            return content;
        }

        public List<String> getImportStrs() {
            return importStrs;
        }

        public String getFilePath() {
            return filePath;
        }

        public static JavaFileInfo emptyContent(String javaFilePath) {
            return new JavaFileInfo("", new ArrayList<>(), javaFilePath);
        }
    }



    public static String toAnyDoorInjectedClassStr(List<String> importStrs, String content) {
        StringBuilder sb = new StringBuilder();
        for (String importStr : importStrs) {
            sb.append("import ").append(importStr).append(";\n");
        }
        return sb.append(String.format(TEMPLATE, content)).toString();
    }

    public static JavaFileInfo readFile2(String javaFilePath) {
        try {
            File javaFile = new File(javaFilePath);
            if (!javaFile.exists()) {
                return JavaFileInfo.emptyContent(javaFilePath);
            }
            byte[] javaFileBytes = Files.readAllBytes(javaFile.toPath());
            String fileContent = new String(javaFileBytes, StandardCharsets.UTF_8);

            // 切割出导包的全类名
            List<String> importStrs = new ArrayList<>();
            int importIndex = fileContent.indexOf("import ");
            int importIndexEnd = fileContent.indexOf("public class AnyDoorInjectedClass {");
            while (importIndex != -1 && importIndex < importIndexEnd) {
                int endIndex = fileContent.indexOf(";", importIndex);
                String substring = fileContent.substring(importIndex + 7, endIndex);
                if (!substring.isBlank()) {
                    importStrs.add(substring);
                }
                importIndex = fileContent.indexOf("import ", endIndex);
            }

            // 切割出 preRun 方法 的方法体
            String content = extractMethodBody(fileContent);
            return new JavaFileInfo(content, importStrs, javaFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JavaFileInfo.emptyContent(javaFilePath);
    }

    private static String extractMethodBody(String classString) {
        String searchPattern = "public void preRun() {";
        int methodIndex = classString.indexOf(searchPattern);

        if (methodIndex == -1) {
            return ""; // 方法未找到
        }

        // 找到方法的开始位置
        int startIndex = methodIndex + searchPattern.length();
        int braceCount = 1; // 计数大括号
        int endIndex = startIndex;

        // 遍历找到配对的结束大括号
        while (endIndex < classString.length() && braceCount > 0) {
            char currentChar = classString.charAt(endIndex);
            if (currentChar == '{') {
                braceCount++;
            } else if (currentChar == '}') {
                braceCount--;
            }
            endIndex++;
        }

        // 返回方法体内容
        if (braceCount == 0) {
            return classString.substring(startIndex, endIndex - 1).trim(); // 返回方法体内容
        } else {
            return ""; // 如果没有匹配的闭括号
        }
    }

//    public static JavaFileInfoUtil readFile(Project project, String javaFilePath) {
//        VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(new File(javaFilePath));
//        if (fileByIoFile == null) {
//            return JavaFileInfoUtil.empty();
//        }
//
//        PsiFile psiFile = PsiManager.getInstance(project).findFile(fileByIoFile); // 这里慢
//        // AnyDoorInjectedClass
//        if (psiFile instanceof PsiJavaFile psiJavaFile) {
//            List<String> imports = new ArrayList<>();
//            if (psiJavaFile.getImportList() != null) { // 这里慢
//                for (PsiImportStatementBase importStatement : psiJavaFile.getImportList().getAllImportStatements()) {
//                    if (importStatement.getImportReference() != null) { // 这里慢
//                        imports.add(importStatement.getImportReference().getQualifiedName());
//                    }
//                }
//            }
//
//            String content = null;
//            for (PsiElement child : psiFile.getChildren()) {
//                if (content != null) {
//                    break;
//                }
//                if (child instanceof PsiClass psiClass) {
//                    PsiMethod[] methods = psiClass.getMethods();
//                    for (PsiMethod method : methods) {
//                        if (content != null) {
//                            break;
//                        }
//                        if (method.getName().equals(JavaFileInfoUtil.METHOD_NAME)) {
//                            if (method.getBody() != null) {
//                                String text = method.getBody().getText();
//                                // 移除前后的 { }
//                                text = text.substring(1, text.length() - 1);
//                                // 移除前后的空格
//                                text = text.trim();
//                                content = text;
//                            }
//                        }
//                    }
//                }
//            }
//            return new JavaFileInfoUtil(Optional.ofNullable(content).orElse(""), imports);
//        }
//        return JavaFileInfoUtil.empty();
//    }
}
