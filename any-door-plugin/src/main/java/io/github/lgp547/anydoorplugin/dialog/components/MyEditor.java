package io.github.lgp547.anydoorplugin.dialog.components;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 11:14
 **/

import java.util.Objects;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.JSONEditor;
import io.github.lgp547.anydoorplugin.dialog.MethodDataContext;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.event.impl.DisplayDataChangeEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.SelectItemChangedEvent;
import org.jetbrains.annotations.Nullable;

public class MyEditor extends JSONEditor implements Listener {

    private final MethodDataContext context;

    public MyEditor(MethodDataContext context, String cacheText, @Nullable PsiParameterList psiParameterList, Project project) {
        super(cacheText, psiParameterList, project);
        this.context = context;

//        addFocusListener(new FocusAdapter() {
//            @Override
//            public void focusLost(FocusEvent e) {
//                super.focusLost(e);
//                String text = getText();
//                context.updateCache(text);
//            }
//        });
    }

    @Override
    public void onEvent(Event event) {
        if (Objects.equals(event.getType(), EventType.DISPLAY_DATA_CHANGE)) {
            ParamDataItem selectedItem = ((DisplayDataChangeEvent) event).getSelectedItem();
            if (selectedItem != null) {
                String text = selectedItem.getParam();
                setText(text);
            }
        }
    }
}
