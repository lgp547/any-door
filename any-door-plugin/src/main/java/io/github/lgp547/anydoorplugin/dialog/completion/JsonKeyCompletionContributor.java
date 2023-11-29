package io.github.lgp547.anydoorplugin.dialog.completion;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.dialog.JSONEditor;
import io.github.lgp547.anydoorplugin.util.JsonElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 代码完成
 */
public class JsonKeyCompletionContributor extends CompletionContributor {
    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);

        // 只填充anyDoor的编辑器
        if (!parameters.getOriginalFile().getName().equals(JSONEditor.ANY_DOOR_PARAM_FILE_NAME)) {
            return;
        }

        // 非key不做处理
        if (!JsonElementUtil.isJsonKey(parameters.getPosition())) {
            return;
        }

        PsiParameterList psiParameterList = parameters.getEditor().getUserData(JSONEditor.ANY_DOOR_EDIT_PARAMS);
        if (psiParameterList != null && psiParameterList.getParameters().length > 0) {
            JsonObject jsonObject = JsonElementUtil.toParamNameListNew(psiParameterList);
            Set<String> keys = JsonElementUtil.getJsonObjectKey(jsonObject);
            keys.forEach(key -> result.addElement(LookupElementBuilder.create(key)));
        }
    }


}
