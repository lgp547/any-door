package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.github.lgp547.anydoorplugin.AnyDoorInfo;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@State(name = "AnyDoorSettingsState", storages = @Storage("AnyDoorSettingsState.xml"))
public class AnyDoorSettingsState implements PersistentStateComponent<AnyDoorSettingsState> {

    public Boolean enableAsyncExecute = true;

    public String dependenceName = AnyDoorInfo.ANY_DOOR_NAME;

    public String dependenceVersion = AnyDoorInfo.ANY_DOOR_JAR_MIN_VERSION;

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
        updateDependence(dependenceName, dependenceVersion);
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

    public boolean updateDependence(String newName, String newVersion) {
        dependenceName = newName;
        if (!Objects.equals(newName, AnyDoorInfo.ANY_DOOR_NAME)) {
            dependenceVersion = newVersion;
            return true;
        }

        if (StringUtils.contains(newVersion, "SNAPSHOT")) {
            dependenceVersion = newVersion;
            return true;
        }

        // min version check
        int newVersionNum = NumberUtils.toInt(StringUtils.replace(newVersion, ".", ""));
        int minVersionNum = NumberUtils.toInt(StringUtils.replace(AnyDoorInfo.ANY_DOOR_JAR_MIN_VERSION, ".", ""));
        boolean b = newVersionNum > minVersionNum;
        dependenceVersion = b ? newVersion : AnyDoorInfo.ANY_DOOR_JAR_MIN_VERSION;
        return b;
    }
}