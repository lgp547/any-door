package io.github.lgp547.anydoorplugin.dialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiParameterList;
import com.intellij.ui.EditorTextField;
import com.intellij.util.LocalTimeCounter;
import io.github.lgp547.anydoorplugin.util.JsonElementUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public class JSONEditor extends EditorTextField {
    private final FileType fileType = JsonFileType.INSTANCE;

    private final PsiParameterList psiParameterList;

    private String defaultContent;
    private String cacheContent;

    public JSONEditor(String cacheText, @Nullable PsiParameterList psiParameterList, Project project) {
        super("", project, JsonFileType.INSTANCE);

        this.cacheContent = cacheText;
        this.psiParameterList = psiParameterList;
        setDocument(createDocument(StringUtils.isBlank(cacheContent) ? defaultContent : cacheContent));

        addSettingsProvider(editor -> {
            editor.setHorizontalScrollbarVisible(true);
            editor.setVerticalScrollbarVisible(true);
        });
    }

    public String getDefaultContent() {
        if (defaultContent == null) {
            defaultContent = JsonElementUtil.getJsonText(psiParameterList);
        }
        return defaultContent;
    }

    public void resetDefaultContent() {
        setText(getDefaultContent());
    }

    public String getCacheContent() {
        return cacheContent;
    }

    public PsiParameterList getPsiParameterList() {
        return psiParameterList;
    }

    public String parseQueryParam(String text) {
        String str = text.contains("?") ? text : "?" + text;
        try {
            URI uri = new URI(str);
            String query = uri.getRawQuery();
            Map<String, Object> queryParams = Arrays.stream(query.split("&"))
                    .map(param -> param.split("="))
                    .filter(param -> param[0].length() > 0)
                    .collect(Collectors.toMap(param -> param[0],
                            param -> {
                                if (param.length > 1) {
                                    return URLDecoder.decode(param[1], Charset.defaultCharset());
                                }
                                return "";
                            }));
            // wrap if it is a DTO
            if (psiParameterList.getParametersCount() > 0
                    && psiParameterList.getParameter(0).getType() instanceof PsiClassType) {
                String name = psiParameterList.getParameter(0).getName();
                queryParams = Map.of(name, queryParams);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
            return gson.toJson(queryParams);
        } catch (Exception ignored) {

        }
        setText(text);
        return text;
    }

    public String genQueryParam(String text) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> map = gson.fromJson(text, type);
            // unwrap if it is a DTO
            if (psiParameterList.getParametersCount() > 0
                    && psiParameterList.getParameter(0).getType() instanceof PsiClassType) {
                String name = psiParameterList.getParameter(0).getName();
                Object obj = map.get(name);
                String json = gson.toJson(obj);
                map = gson.fromJson(json, type);
            }
            String queryParam = map.entrySet().stream()
                    .map(entry -> entry.getKey()
                            + "="
                            + URLEncoder.encode(entry.getValue().toString(), Charset.defaultCharset()))
                    .collect(Collectors.joining("&"));
            return "?" + queryParam;
        } catch (Exception ignored) {

        }
        setText(text);
        return text;
    }

    protected Document createDocument(String initText) {
        final PsiFileFactory factory = PsiFileFactory.getInstance(getProject());
        final long stamp = LocalTimeCounter.currentTime();
        final PsiFile psiFile = factory.createFileFromText("Dummy." + fileType.getDefaultExtension(), fileType, initText, stamp, true, false);

        //TextCompletionUtil.installProvider(psiFile, new MyTextFieldCompletionProvider(), true);
        return PsiDocumentManager.getInstance(getProject()).getDocument(psiFile);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        final EditorEx ex = super.createEditor();

        ex.setHighlighter(HighlighterFactory.createHighlighter(getProject(), JsonFileType.INSTANCE));
        ex.setEmbeddedIntoDialogWrapper(true);
        ex.setOneLineMode(false);

        return ex;
    }

//    private static class MyTextFieldCompletionProvider extends TextFieldCompletionProvider implements DumbAware {
//        @Override
//        protected void addCompletionVariants(@NotNull String text, int offset, @NotNull String prefix, @NotNull CompletionResultSet result) {
//            result.addElement(LookupElementBuilder.create("123456"));
//            result.addElement(LookupElementBuilder.create("234567"));
//        }
//    }

}