package io.github.lgp547.anydoorplugin.dto;

public class ParamCacheDto {
    private Long runNum;
    private Boolean isConcurrent;
    private String content;

    public ParamCacheDto() {
    }

    public ParamCacheDto(String content) {
        this.content = content;
    }

    public ParamCacheDto(Long runNum, Boolean isConcurrent, String content) {
        this.runNum = runNum;
        this.isConcurrent = isConcurrent;
        this.content = content;
    }

    public Long getRunNum() {
        return runNum == null ? 1 : runNum;
    }

    public void setRunNum(Long runNum) {
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
}
