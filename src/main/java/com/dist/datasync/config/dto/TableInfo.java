package com.dist.datasync.config.dto;

import com.dist.datasync.config.entity.TableConfig;

/**
 * @author lijy
 */
public class TableInfo extends TableConfig {

    //用于给界面上传递勾选信息
    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
