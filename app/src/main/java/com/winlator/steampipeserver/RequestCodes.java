package com.winlator.steampipeserver;

public abstract class RequestCodes {
    public static final int MSG_INIT = 1;
    public static final int MSG_SHUTDOWN = 2;
    public static final int MSG_RESTART_APP = 3;
    public static final int MSG_IS_RUNNING = 4;
    public static final int MSG_REGISTER_CALLBACK = 5;
    public static final int MSG_UNREGISTER_CALLBACK = 6;
    public static final int MSG_RUN_CALLBACKS = 7;
}
