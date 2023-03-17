package io.github.lgp547.anydoorplugin.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.ui.JBDimension;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TextAreaDialog extends DialogWrapper {
    private final ContentPanel contentPanel;

    private Runnable okAction;

    private final Project project;

    public TextAreaDialog(Project project, String title, PsiParameterList psiParameterList, String cacheContent) {
        super(project, true, IdeModalityType.MODELESS);
        this.project = project;
        setTitle(title);
        contentPanel = new ContentPanel(psiParameterList, cacheContent, project);
        init();
    }

    public void setOkAction(Runnable runnable) {
        okAction = runnable;
    }

    public String getText() {
        return contentPanel.getText();
    }

    @Override
    protected void doOKAction() {
        okAction.run();
        super.doOKAction();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPanel;
    }

    private class ContentPanel extends JBPanel<ContentPanel> {
        final EditorTextField textArea;

        final PsiParameterList psiParameterList;

        public String getText() {
            return textArea.getText();
        }

        public ContentPanel(PsiParameterList psiParameterList, String cacheContent, Project project) {
            super(new GridBagLayout());
            this.psiParameterList = psiParameterList;
            setPreferredSize(new JBDimension(600, 500));


            GridBagConstraints constraints1 = new GridBagConstraints();
            constraints1.anchor = GridBagConstraints.EAST;

            JPanel functionPanel = new JPanel();
            JButton jButton1 = new JButton("gen json param");
            JButton jButton2 = new JButton("parse query param");
            JButton jButton3 = new JButton("gen query param");
            functionPanel.add(jButton1);
            functionPanel.add(jButton2);
            functionPanel.add(jButton3);
            add(functionPanel, constraints1);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.gridx = 0;
            constraints.gridy = 1;

            if (StringUtils.isBlank(cacheContent)) {
                textArea = new JSONEditor(getJsonText(), project);
            } else {
                textArea = new JSONEditor(cacheContent, project);
            }

            textArea.requestFocusInWindow();
            textArea.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    textArea.requestFocusInWindow();
                }
            });

            textArea.addSettingsProvider(editor -> {
                editor.setHorizontalScrollbarVisible(true);
                editor.setVerticalScrollbarVisible(true);
            });

            add(textArea, constraints);

            jButton1.addActionListener(e -> textArea.setText(getJsonText()));

            jButton2.addActionListener(e -> {
                String newText = parseQueryParam(textArea.getText());
                textArea.setText(newText);
            });

            jButton3.addActionListener(e -> {
                String newText = genQueryParam(textArea.getText());
                textArea.setText(newText);
            });

        }

        private String getJsonText() {
            JsonObject jsonObject = toParamNameListNew(psiParameterList);
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            return gson.toJson(jsonObject);
        }

        private String parseQueryParam(String text) {
            String str = text.contains("?") ? text : "?" + text;
            try {
                URI uri = new URI(str);
                String query = uri.getRawQuery();
                Map<String, String> queryParams = Arrays.stream(query.split("&"))
                        .map(param -> param.split("="))
                        .collect(Collectors.toMap(param -> param[0],
                                param -> {
                                    if (param.length > 1) {
                                        return URLDecoder.decode(param[1], Charset.defaultCharset());
                                    }
                                    return "";
                                }));
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
                return gson.toJson(queryParams);
            } catch (Exception ignored) {

            }
            return text;
        }

        private String genQueryParam(String text) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                Map<String, Object> map = gson.fromJson(text, type);
                String queryParam = map.entrySet().stream()
                        .map(entry -> entry.getKey()
                                + "="
                                + URLEncoder.encode(entry.getValue().toString(), Charset.defaultCharset()))
                        .collect(Collectors.joining("&"));
                return "?" + queryParam;
            } catch (Exception ignored) {

            }
            return text;
        }

    }

    private static JsonObject toParamNameListNew(PsiParameterList parameterList) {
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            String key = parameter.getName();

            PsiType type = parameter.getType();
            JsonElement jsonElement = toJson(type, 0);
            if (jsonElement == JsonNull.INSTANCE && type instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) type).resolve();
                if (null != psiClass) {
                    if (isNoSupportType(psiClass)) {
                        jsonElement = new JsonPrimitive("");
                    } else {
                        JsonObject jsonObject1 = new JsonObject();
                        Arrays.stream(psiClass.getFields()).forEach(field -> jsonObject1.add(field.getName(), toJson(field.getType(), 0)));
                        jsonElement = jsonObject1;
                    }
                }
            }
            jsonObject.add(key, jsonElement);
        }
        return jsonObject;
    }

    public static boolean isNoSupportType(PsiClass psiClass) {
        return psiClass.isEnum() || psiClass instanceof PsiClass;
    }

    public static JsonElement toJson(PsiType type, Integer num) {
        if (num > 5) {
            return new JsonPrimitive("");
        }
        if (type.isAssignableFrom(PsiType.INT)) {
            return new JsonPrimitive(0);
        }
        if (type.isAssignableFrom(PsiType.LONG)) {
            return new JsonPrimitive(0);
        }
        if (type.isAssignableFrom(PsiType.BOOLEAN)) {
            return new JsonPrimitive(false);
        }
        if (type.isAssignableFrom(PsiType.BYTE)) {
            return new JsonPrimitive("");
        }
        if (type.isAssignableFrom(PsiType.CHAR)) {
            return new JsonPrimitive("");
        }
        if (type.isAssignableFrom(PsiType.DOUBLE)) {
            return new JsonPrimitive(0.00);
        }
        if (type.isAssignableFrom(PsiType.FLOAT)) {
            return new JsonPrimitive(0.0);
        }
        if (type.isAssignableFrom(PsiType.SHORT)) {
            return new JsonPrimitive("");
        }
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            if (null != psiClass) {
                if (isNoSupportType(psiClass)) {
                    return new JsonPrimitive("");
                } else {
                    try {
                        Class<?> aClass = Class.forName(psiClass.getQualifiedName());
                        if (isSimpleValueType(aClass)) {
                            return new JsonPrimitive("");
                        }
                        if (isCollType(aClass)) {
                            return new JsonArray();
                        }
                        if (isMapType(aClass)) {
                            return new JsonObject();
                        }
                    } catch (Exception ignored) {
                    }
                    JsonObject jsonObject1 = new JsonObject();
                    Arrays.stream(psiClass.getFields()).forEach(field -> {
                        if (!StringUtils.contains(field.getText(), " static ")) {
                            jsonObject1.add(field.getName(), toJson(field.getType(), num + 1));
                        }
                    });
                    return jsonObject1;
                }
            }
        }


        return JsonNull.INSTANCE;
    }

    public static boolean isSimpleValueType(Class<?> type) {
        return (Void.class != type && void.class != type &&
                (ClassUtils.isPrimitiveOrWrapper(type) ||
                        Enum.class.isAssignableFrom(type) ||
                        CharSequence.class.isAssignableFrom(type) ||
                        Number.class.isAssignableFrom(type) ||
                        Date.class.isAssignableFrom(type) ||
                        Temporal.class.isAssignableFrom(type) ||
                        URI.class == type ||
                        URL.class == type ||
                        Locale.class == type ||
                        Class.class == type));
    }

    public static boolean isCollType(Class<?> type) {
        return type.isArray() || Collection.class.isAssignableFrom(type);
    }

    public static boolean isMapType(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }


    private class JSONEditor extends EditorTextField {
        private final FileType fileType = JsonFileType.INSTANCE;

        public JSONEditor(String text, Project project) {
            super(text, project, JsonFileType.INSTANCE);
            super.setDocument(createDocument(text));
        }

        protected Document createDocument(String initText) {
            final PsiFileFactory factory = PsiFileFactory.getInstance(project);
            final long stamp = LocalTimeCounter.currentTime();
            final PsiFile psiFile = factory.createFileFromText("Dummy." + fileType.getDefaultExtension(), fileType, initText, stamp, true, false);

            //TextCompletionUtil.installProvider(psiFile, new MyTextFieldCompletionProvider(), true);
            return PsiDocumentManager.getInstance(project).getDocument(psiFile);
        }

        @Override
        protected @NotNull EditorEx createEditor() {
            final EditorEx ex = super.createEditor();

            ex.setHighlighter(HighlighterFactory.createHighlighter(project, JsonFileType.INSTANCE));
            ex.setEmbeddedIntoDialogWrapper(true);
            ex.setOneLineMode(false);

            return ex;
        }
    }

//    private static class MyTextFieldCompletionProvider extends TextFieldCompletionProvider implements DumbAware {
//        @Override
//        protected void addCompletionVariants(@NotNull String text, int offset, @NotNull String prefix, @NotNull CompletionResultSet result) {
//            result.addElement(LookupElementBuilder.create("123456"));
//            result.addElement(LookupElementBuilder.create("234567"));
//        }
//    }
}
