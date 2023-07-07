package io.github.lgp547.anydoorplugin.data.domain;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-28 19:55
 **/
public abstract class DataItem {
    protected Long id;
    protected long updateTime;
    protected Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }


}
