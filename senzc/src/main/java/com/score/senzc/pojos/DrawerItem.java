package com.score.senzc.pojos;

/**
 * POJO class to keep Drawer item attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class DrawerItem {
    String name;
    int resourceId;
    int selectedResourceId;
    boolean isSelected;

    public DrawerItem(String name, int resourceId, int selectedResourceId, boolean isSelected) {
        this.name = name;
        this.resourceId = resourceId;
        this.selectedResourceId = selectedResourceId;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getSelectedResourceId() {
        return selectedResourceId;
    }

    public void setSelectedResourceId(int selectedResourceId) {
        this.selectedResourceId = selectedResourceId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
