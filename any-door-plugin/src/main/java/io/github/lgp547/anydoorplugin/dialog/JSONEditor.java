package io.github.lgp547.anydoorplugin.dialog;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiParameterList;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.util.LocalTimeCounter;
import io.github.lgp547.anydoorplugin.AnyDoorInfo;
import io.github.lgp547.anydoorplugin.util.JsonElementUtil;
import io.github.lgp547.anydoorplugin.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class JSONEditor extends EditorTextField {
    private final FileType fileType = JsonFileType.INSTANCE;

    private final PsiParameterList psiParameterList;

    private String cacheContent;
    private String simpleContent;
    private String jsonContent;

    public JSONEditor(String cacheText, @Nullable PsiParameterList psiParameterList, Project project) {
        super("", project, JsonFileType.INSTANCE);

        this.cacheContent = cacheText;
        this.psiParameterList = psiParameterList;

        if (StringUtils.isBlank(cacheContent)) {
            jsonContent = JsonElementUtil.getJsonText(psiParameterList);
            setDocument(createDocument(jsonContent));
        } else {
            setDocument(createDocument(cacheContent));
        }

        addSettingsProvider(editor -> {
            editor.setHorizontalScrollbarVisible(true);
            editor.setVerticalScrollbarVisible(true);
        });
    }

    /**
     * 将当前query内容放到json的第二层
     */
    public void queryToJson() {
        String text = getText();
        try {
            URI uri = new URI(text.contains("?") ? text : "?" + text);
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
            if (psiParameterList.getParametersCount() > 0) {
                String name = Objects.requireNonNull(psiParameterList.getParameter(0)).getName();
                queryParams = Map.of(name, queryParams);
            }
            text = io.github.lgp547.anydoorplugin.util.JsonUtil.toStr(queryParams);
        } catch (Exception ignored) {
        }
        setText(text);
    }

    /**
     * 将当前json内容的第二层转成query参数
     * {
     *     "dto":{
     *         "name":"1",
     *         "phone": 1
     *     }
     * }
     */
    public void jsonToQuery() {
        String text = getText();
        try {
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> contentMap = JsonUtil.toMap(text);
            for (int i = 0; i < psiParameterList.getParametersCount(); i++) {
                Object obj = contentMap.get(Objects.requireNonNull(psiParameterList.getParameter(i)).getName());
                map.putAll(JsonUtil.toMap(JsonUtil.toStrNotExc(obj)));
            }

            String queryParam = map.entrySet().stream()
                    .map(entry -> entry.getKey()
                            + "="
                            + URLEncoder.encode(entry.getValue().toString(), Charset.defaultCharset()))
                    .collect(Collectors.joining("&"));
            text = "?" + queryParam;
        } catch (Exception ignored) {
        }
        setText(text);
    }

    protected Document createDocument(String initText) {
//        final PsiFileFactory factory = PsiFileFactory.getInstance(getProject());
//        final long stamp = ;
//        final PsiFile psiFile = factory.createFileFromText("Dummy." + fileType.getDefaultExtension(), fileType, initText, stamp, true, false);
//
//        //TextCompletionUtil.installProvider(psiFile, new MyTextFieldCompletionProvider(), true);
//        return PsiDocumentManager.getInstance(getProject()).getDocument(psiFile);

        LightVirtualFile virtualFile = new LightVirtualFile(AnyDoorInfo.ANY_DOOR_PARAM_FILE_NAME, fileType, initText, LocalTimeCounter.currentTime());

//        MyBranchedVirtualFile myBranchedVirtualFile = new MyBranchedVirtualFile(virtualFile, AnyDoorInfo.ANY_DOOR_PARAM_FILE_NAME, MyBranchedVirtualFile.createBranchImpl(getProject()));

        return FileDocumentManager.getInstance().getDocument(virtualFile);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        final EditorEx ex = super.createEditor();

        ex.setHighlighter(HighlighterFactory.createHighlighter(getProject(), JsonFileType.INSTANCE));
        ex.setEmbeddedIntoDialogWrapper(true);
        ex.setOneLineMode(false);

        return ex;
    }

    public void genCacheContent() {
        setText(cacheContent);
    }

    public void genSimpleContent() {
        if (simpleContent == null) {
            simpleContent = JsonElementUtil.getSimpleText(psiParameterList);
        }
        setText(simpleContent);
    }

    public void genJsonContent() {
        if (jsonContent == null) {
            jsonContent = JsonElementUtil.getJsonText(psiParameterList);
        }
        setText(jsonContent);
    }

//    private static class MyTextFieldCompletionProvider extends TextFieldCompletionProvider implements DumbAware {
//        @Override
//        protected void addCompletionVariants(@NotNull String text, int offset, @NotNull String prefix, @NotNull CompletionResultSet result) {
//            result.addElement(LookupElementBuilder.create("123456"));
//            result.addElement(LookupElementBuilder.create("234567"));
//        }
//    }

}