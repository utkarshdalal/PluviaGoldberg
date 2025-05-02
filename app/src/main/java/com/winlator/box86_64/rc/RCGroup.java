package com.winlator.box86_64.rc;

import java.util.LinkedList;
import java.util.List;

public class RCGroup implements Comparable<RCGroup> {
    private String groupName;
    private String groupDesc;
    private List<RCItem> items;
    private boolean enabled;

    public RCGroup() {
        this("", "", true, null);
    }

    public RCGroup(String name, String desc, boolean enabled, List<RCItem> items) {
        groupName = name;
        groupDesc = desc;
        this.enabled = enabled;
        this.items = items == null ? new LinkedList<>() : items;
    }

    public static RCGroup copy(RCGroup group) {
        LinkedList<RCItem> items = new LinkedList<>();
        for (RCItem item : group.getItems())
            items.add(RCItem.copy(item));
        return new RCGroup(group.groupName, group.groupDesc, group.isEnabled(), items);
    }

    public RCItem getItem(String name) {
        for (RCItem item : items)
            if (item.getProcessName().equals(name)) return item;
        return null;
    }

    public List<RCItem> getItems() {
        return items;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String name) {
        groupName = name;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public void setGroupDesc(String desc) {
        groupDesc = desc;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean b) {
        enabled = b;
    }

    @Override
    public int compareTo(RCGroup o) {
        return getGroupName().compareTo(o.groupName);
    }
}
