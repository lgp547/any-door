package io.github.lgp547.anydoorplugin;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.PropertyKey;

/**
 * 插件新增提示的本地化入口。
 *
 * @author luoweiyu
 * @date 2026-10-20
 */
public final class AnyDoorBundle extends DynamicBundle {

    private static final String BUNDLE = "messages.AnyDoorBundle";
    private static final AnyDoorBundle INSTANCE = new AnyDoorBundle();

    private AnyDoorBundle() {
        super(BUNDLE);
    }

    /** 根据当前 IDE 语言读取提示，并保留默认英文回退。 */
    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... parameters) {
        return INSTANCE.getMessage(key, parameters);
    }
}
