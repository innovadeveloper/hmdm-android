package com.hmdm.launcher.ui;

import java.util.function.Consumer;

public class DynamicButton {
    private String title;
    private String action;
    private boolean enabled;
    private int iconResId;

    private boolean isLongClick;

    public boolean isLongClick() {
        return isLongClick;
    }

    private Consumer<Void> closure;

    public Consumer<Void> getClosure() {
        return closure;
    }

    public void setClosure(Consumer<Void> closure) {
        this.closure = closure;
    }

    public DynamicButton(String title, String action, Consumer<Void> closure, boolean isLongClick) {
        this.title = title;
        this.action = action;
        this.enabled = true;
        this.iconResId = 0;
        this.closure = closure;
        this.isLongClick = isLongClick;
    }



    public DynamicButton(String title, String action, Consumer<Void> closure) {
        this.title = title;
        this.action = action;
        this.enabled = true;
        this.iconResId = 0;
        this.closure = closure;
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