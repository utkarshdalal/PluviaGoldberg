package com.winlator.box86_64.rc;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;

public enum RCField {
    BOX64_LOG("BOX64_LOG", false),
    BOX64_ROLLING_LOG("BOX64_ROLLING_LOG", false),
    BOX64_NOBANNER("BOX64_NOBANNER", false),
    BOX64_LD_LIBRARY_PATH("BOX64_LD_LIBRARY_PATH", false),
    BOX64_PATH("BOX64_PATH", false),
    BOX64_DLSYM_ERROR("BOX64_DLSYM_ERROR", false),
    BOX64_TRACE_FILE("BOX64_TRACE_FILE", false),
    BOX64_TRACE("BOX64_TRACE", false),
    BOX64_TRACE_INIT("BOX64_TRACE_INIT", false),
    BOX64_TRACE_START("BOX64_TRACE_START", false),
    BOX64_TRACE_XMM("BOX64_TRACE_XMM", false),
    BOX64_TRACE_EMM("BOX64_TRACE_EMM", false),
    BOX64_TRACE_COLOR("BOX64_TRACE_COLOR", false),
    BOX64_LOAD_ADDR("BOX64_LOAD_ADDR", false),
    BOX64_NOSIGSEGV("BOX64_NOSIGSEGV", false),
    BOX64_NOSIGILL("BOX64_NOSIGILL", false),
    BOX64_SHOWSEGV("BOX64_SHOWSEGV", false),
    BOX64_SHOWBT("BOX64_SHOWBT", false),
    BOX64_X11THREADS("BOX64_X11THREADS", false),
    BOX64_MMAP32("BOX64_MMAP32", false),
    BOX64_IGNOREINT3("BOX64_IGNOREINT3", false),
    BOX64_X11GLX("BOX64_X11GLX", false),
    BOX64_DYNAREC_DUMP("BOX64_DYNAREC_DUMP", false),
    BOX64_DYNAREC_LOG("BOX64_DYNAREC_LOG", false),
    BOX64_DYNAREC("BOX64_DYNAREC", true),
    BOX64_DYNAREC_TRACE("BOX64_DYNAREC_TRACE", false),
    BOX64_NODYNAREC("BOX64_NODYNAREC", false),
    BOX64_DYNAREC_TEST("BOX64_DYNAREC_TEST", false),
    BOX64_DYNAREC_BIGBLOCK("BOX64_DYNAREC_BIGBLOCK", true, S.S4),
    BOX64_DYNAREC_FORWARD("BOX64_DYNAREC_FORWARD", true),
    BOX64_DYNAREC_STRONGMEM("BOX64_DYNAREC_STRONGMEM", true, S.S4),
    BOX64_DYNAREC_X87DOUBLE("BOX64_DYNAREC_X87DOUBLE", true),
    BOX64_DYNAREC_FASTNAN("BOX64_DYNAREC_FASTNAN", true),
    BOX64_DYNAREC_FASTROUND("BOX64_DYNAREC_FASTROUND", true),
    BOX64_DYNAREC_SAFEFLAGS("BOX64_DYNAREC_SAFEFLAGS", true, S.S3),
    BOX64_DYNAREC_CALLRET("BOX64_DYNAREC_CALLRET", true),
    BOX64_DYNAREC_ALIGNED_ATOMICS("BOX64_DYNAREC_ALIGNED_ATOMICS", false),
    BOX64_DYNAREC_BLEEDING_EDGE("BOX64_DYNAREC_BLEEDING_EDGE", false),
    BOX64_DYNAREC_JVM("BOX64_DYNAREC_JVM", false),
    BOX64_DYNAREC_WAIT("BOX64_DYNAREC_WAIT", true),
    BOX64_DYNAREC_MISSING("BOX64_DYNAREC_MISSING", false),
    BOX64_SSE_FLUSHTO0("BOX64_SSE_FLUSHTO0", false),
    BOX64_X87_NO80BITS("BOX64_X87_NO80BITS", false),
    BOX64_MAXCPU("BOX64_MAXCPU", false),
    BOX64_SYNC_ROUNDING("BOX64_SYNC_ROUNDING", false),
    BOX64_LIBCEF("BOX64_LIBCEF", false),
    BOX64_JVM("BOX64_JVM", false),
    BOX64_UNITYPLAYER("BOX64_UNITYPLAYER", false),
    BOX64_SDL2_JGUID("BOX64_SDL2_JGUID", false),
    BOX64_LIBGL("BOX64_LIBGL", false),
    BOX64_LD_PRELOAD("BOX64_LD_PRELOAD", false),
    BOX64_EMULATED_LIBS("BOX64_EMULATED_LIBS", false),
    BOX64_ALLOWMISSINGLIBS("BOX64_ALLOWMISSINGLIBS", false),
    BOX64_PREFER_WRAPPED("BOX64_PREFER_WRAPPED", false),
    BOX64_PREFER_EMULATED("BOX64_PREFER_EMULATED", false),
    BOX64_CRASHHANDLER("BOX64_CRASHHANDLER", false),
    BOX64_MALLOC_HACK("BOX64_MALLOC_HACK", false),
    BOX64_NOPULSE("BOX64_NOPULSE", false),
    BOX64_NOGTK("BOX64_NOGTK", false),
    BOX64_NOVULKAN("BOX64_NOVULKAN", false),
    BOX64_SHAEXT("BOX64_SHAEXT", false),
    BOX64_SSE42("BOX64_SSE42", false),
    BOX64_FUTEX_WAITV("BOX64_FUTEX_WAITV", false),
    BOX64_BASH("BOX64_BASH", false),
    BOX64_ENV("BOX64_ENV", false),
    BOX64_ENV1("BOX64_ENV1", false),
    BOX64_RESERVE_HIGH("BOX64_RESERVE_HIGH", false),
    BOX64_JITGDB("BOX64_JITGDB", false),
    BOX64_NORCFILES("BOX64_NORCFILES", false),
    BOX64_RCFILE("BOX64_RCFILE", false),
    BOX64_NOSANDBOX("BOX64_NOSANDBOX", true),
    BOX64_INPROCESSGPU("BOX64_INPROCESSGPU", true),
    BOX64_CEFDISABLEGPU("BOX64_CEFDISABLEGPU", true),
    BOX64_CEFDISABLEGPUCOMPOSITOR("BOX64_CEFDISABLEGPUCOMPOSITOR", true),
    BOX64_AVX("BOX64_AVX", true),
    BOX64_ARGS("BOX64_ARGS", true, S.S0);


    private final String fieldName;
    private final boolean enabled;
    private final String[] selections;

    RCField(String name, boolean enabled) {
        this(name, enabled, S.S2);
    }

    RCField(String name, boolean enabled, String[] selections) {
        this.fieldName = name;
        this.enabled = enabled;
        this.selections = selections;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String[] getSelections() {
        return selections;
    }

    @NonNull
    @Override
    public String toString() {
        return fieldName;
    }

    public static String[] getEnabledField() {
        LinkedList<String> list = new LinkedList<>();
        for (RCField field : RCField.values())
            if (field.enabled)
                list.add(field.toString());
        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static class S {
        public static final String[] S0 = {};
        public static final String[] S2 = {"0", "1"};
        public static final String[] S3 = {"0", "1", "2"};
        public static final String[] S4 = {"0", "1", "2", "3"};
    }
}


