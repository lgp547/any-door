// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnyDoorSettingsState implements PersistentStateComponent<AnyDoorSettingsState> {

  public Integer port = 8080;
  public Boolean enable = true;
  public String version = "0.0.2";
  public String runModule = "";

  @Nullable
  @Override
  public AnyDoorSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull AnyDoorSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

}