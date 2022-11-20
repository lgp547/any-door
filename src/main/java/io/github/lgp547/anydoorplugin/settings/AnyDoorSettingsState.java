package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@State(name = "AnyDoorSettingsState", storages = @Storage("AnyDoorSettingsState.xml"))
public class AnyDoorSettingsState implements PersistentStateComponent<AnyDoorSettingsState> {

  public Integer port = 8080;
  public Boolean enable = true;
  public String version = "0.0.2";
  public String runModule = "start";
  public Map<String, String> cache = new ConcurrentHashMap<>();

  @Nullable
  @Override
  public AnyDoorSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull AnyDoorSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public void putCache(String key, String value) {
    cache.put(key, value);
  }

  public String getCache(String key) {
    return cache.get(key);
  }
}