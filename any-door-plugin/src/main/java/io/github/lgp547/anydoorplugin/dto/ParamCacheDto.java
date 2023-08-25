package io.github.lgp547.anydoorplugin.dto;

import io.github.lgp547.anydoorplugin.util.JsonUtil;

public class ParamCacheDto {

    private Integer runNum;

    private Boolean isConcurrent;

    private String content;

    public ParamCacheDto() {
    }

    public ParamCacheDto(String content) {
        this.content = content;
    }

    public ParamCacheDto(Integer runNum, Boolean isConcurrent, String content) {
        this.runNum = runNum;
        this.isConcurrent = isConcurrent;
        this.content = content;
    }

    public Integer getRunNum() {
        return runNum == null ? 1 : runNum;
    }

    public void setRunNum(Integer runNum) {
        this.runNum = runNum;
    }

    public Boolean getConcurrent() {
        return isConcurrent == null || isConcurrent;
    }

    public void setConcurrent(Boolean concurrent) {
        isConcurrent = concurrent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String content() {
        if (null == content) {
            return null;
        }
        return JsonUtil.formatterJson(content);
    }
}
