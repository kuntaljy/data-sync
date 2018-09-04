package com.dist.datasync.base;

/**
 * 带有消息布尔类
 * @author lijy
 */
public class BooleanWithMessage {
    private Boolean state;
    private String message;

    public BooleanWithMessage() {
    }

    public BooleanWithMessage(Boolean state, String message) {
        this.state = state;
        this.message = message;
    }

    public static BooleanWithMessage of(Boolean state, String message){
        return new BooleanWithMessage(state, message);
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
