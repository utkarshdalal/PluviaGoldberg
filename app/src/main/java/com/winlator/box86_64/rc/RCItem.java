package com.winlator.box86_64.rc;

import java.util.Map;
import java.util.TreeMap;

public class RCItem implements Comparable<RCItem> {
    private String processName;
    private String itemDesc;
    private Map<String, String> varMap;

    public RCItem() {
        this("", "", null);
    }

    public RCItem(String name, String desc, Map<String, String> vars) {
        processName = name;
        itemDesc = desc;
        varMap = vars == null ? new TreeMap<>() : vars;
    }

    public static RCItem copy(RCItem item) {
        return new RCItem(item.processName, item.getItemDesc(), new TreeMap<>(item.varMap));
    }

    public Map<String, String> getVarMap() {
        return varMap;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String name) {
        processName = name;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String desc) {
        itemDesc = desc;
    }

    @Override
    public int compareTo(RCItem o) {
        return getProcessName().compareTo(o.getProcessName());
    }
}
