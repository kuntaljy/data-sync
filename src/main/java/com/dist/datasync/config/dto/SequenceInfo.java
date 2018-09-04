package com.dist.datasync.config.dto;

import com.dist.datasync.config.entity.SequenceConfig;

/**
 * 在SequenceConfig对象基础上扩展勾选信息
 * @author lijy
 */
public class SequenceInfo extends SequenceConfig {

    //用于给界面上传递勾选信息
    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
