package com.winlator.xserver;

/* loaded from: classes.dex */
public enum Decoration {
    ALL,
    BORDER,
    RESIZEH,
    TITLE,
    MENU,
    MINIMIZE,
    MAXIMIZE;

    public int flag() {
        return 1 << ordinal();
    }
}
