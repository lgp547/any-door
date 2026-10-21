package io.github.lgp547.anydoorplugin.action;

import io.github.lgp547.anydoorplugin.AnyDoorBundle;

/**
 * 标记尚不支持的协程调用，供界面入口作为能力提示处理，而不是记录为插件故障。
 *
 * @author luoweiyu
 * @date 2026-10-20
 */
final class UnsupportedSuspendMethodException extends IllegalArgumentException {

    UnsupportedSuspendMethodException() {
        super(AnyDoorBundle.message("method.suspend.unsupported"));
    }
}
