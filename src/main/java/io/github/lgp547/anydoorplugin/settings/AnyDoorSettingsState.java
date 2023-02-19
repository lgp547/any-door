package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@State(name = "AnyDoorSettingsState", storages = @Storage("AnyDoorSettingsState.xml"))
public class AnyDoorSettingsState implements PersistentStateComponent<AnyDoorSettingsState> {

  public Integer port = 8080;
  public Integer runProjectMode = 100; // 100 is Java attach, 200 is Spring mvc.
  public Boolean enableAutoFill = true;
  public Boolean enableAsyncExecute = true;
  public String version = "0.0.5";
  public String runModule = "start";
  public String webPathPrefix = "";
  public Long pid = 0L;
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

  public static Optional<AnyDoorSettingsState> getAnyDoorSettingsStateNotExc(@NotNull Project project) {
    try {
      return Optional.of(project.getService(AnyDoorSettingsState.class));
    } catch (Exception e) {
      NotifierUtil.notifyError(project, "get AnyDoorSettings Service error. errMsg:" + e.getMessage());
      return Optional.empty();
    }
  }

  public static AnyDoorSettingsState getAnyDoorSettingsState(@NotNull Project project) throws IllegalStateException {
    AnyDoorSettingsState service = project.getService(AnyDoorSettingsState.class);
    if (service == null) {
      throw new IllegalStateException("get AnyDoorSettings Service error");
    }
    return service;
  }

  public boolean isSelectJavaAttach() {
    return runProjectMode.equals(100);
  }

  public void updateRunProjectEnum(boolean selectJavaAttach) {
    runProjectMode = selectJavaAttach ? 100 : 200;
  }
}