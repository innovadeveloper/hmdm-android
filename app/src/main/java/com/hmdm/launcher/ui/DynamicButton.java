package com.hmdm.launcher.ui;

public class DynamicButton {
    private String title;
    private String action;
    private boolean enabled;
    private int iconResId;

    public DynamicButton(String title, String action) {
        this.title = title;
        this.action = action;
        this.enabled = true;
        this.iconResId = 0;
    }

    public DynamicButton(String title, String action, boolean enabled, int iconResId) {
        this.title = title;
        this.action = action;
        this.enabled = enabled;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}