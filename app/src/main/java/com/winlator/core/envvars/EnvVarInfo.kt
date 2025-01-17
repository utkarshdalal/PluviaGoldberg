package com.winlator.core.envvars

data class EnvVarInfo(
    val identifier: String,
    val selectionType: EnvVarSelectionType = EnvVarSelectionType.NONE,
    val possibleValues: List<String> = emptyList(),
) {
    companion object {
        val KNOWN_BOX64_VARS = mapOf(
            "BOX64_DYNAREC_SAFEFLAGS" to EnvVarInfo(
                identifier = "BOX64_DYNAREC_SAFEFLAGS",
                possibleValues = listOf("0", "1", "2"),
            ),
            "BOX64_DYNAREC_FASTNAN" to EnvVarInfo(
                identifier = "BOX64_DYNAREC_FASTNAN",
                selectionType = EnvVarSelectionType.TOGGLE,
                possibleValues = listOf("0", "1"),
            ),
            "BOX64_DYNAREC_FASTROUND" to EnvVarInfo(
                identifier = "BOX64_DYNAREC_FASTROUND",
                selectionType = EnvVarSelectionType.TOGGLE,
                possibleValues = listOf("0", "1"),
            ),
            "BOX64_DYNAREC_X87DOUBLE" to EnvVarInfo(
                identifier = "BOX64_DYNAREC_X87DOUBLE",
                selectionType = EnvVarSelectionType.TOGGLE,
                possibleValues = listOf("0", "1"),
            ),
            "BOX64_DYNAREC_BIGBLOCK" to EnvVarInfo(
                identifier = "BOX64_DYNAREC_BIGBLOCK",
                possibleValues = listOf("0", "1", "2", "3"),
            ),
            "BOX64_DYNAREC_STRONGMEM" to EnvVarInfo(
                identifier = "BOX64_DYNAREC_STRONGMEM",
                possibleValues = listOf("0", "1", "2", "3"),
            ),
            "BOX64_DYNAREC_FORWARD" to EnvVarInfo(
                identifier = "BOX64_DYNAREC_FORWARD",
                possibleValues = listOf("0", "128", "256", "512", "1024"),
            ),
            "BOX64_DYNAREC_CALLRET" to EnvVarInfo(
                identifier = "BOX64_DYNAREC_CALLRET",
                selectionType = EnvVarSelectionType.TOGGLE,
                possibleValues = listOf("0", "1"),
            ),
            "BOX64_DYNAREC_WAIT" to EnvVarInfo(
                identifier = "BOX64_DYNAREC_WAIT",
                selectionType = EnvVarSelectionType.TOGGLE,
                possibleValues = listOf("0", "1"),
            ),
        )
        val KNOWN_ENV_VARS = mapOf(
            "ZINK_DESCRIPTORS" to EnvVarInfo(
                identifier = "ZINK_DESCRIPTORS",
                possibleValues = listOf("auto", "lazy", "cached", "notemplates"),
            ),
            "ZINK_DEBUG" to EnvVarInfo(
                identifier = "ZINK_DEBUG",
                selectionType = EnvVarSelectionType.MULTI_SELECT,
                possibleValues = listOf("nir", "spirv", "tgsi", "validation", "sync", "compact", "noreorder"),
            ),
            "MESA_SHADER_CACHE_DISABLE" to EnvVarInfo(
                identifier = "MESA_SHADER_CACHE_DISABLE",
                selectionType = EnvVarSelectionType.TOGGLE,
                possibleValues = listOf("false", "true"),
            ),
            "mesa_glthread" to EnvVarInfo(
                identifier = "mesa_glthread",
                selectionType = EnvVarSelectionType.TOGGLE,
                possibleValues = listOf("false", "true"),
            ),
            "WINEESYNC" to EnvVarInfo(
                identifier = "WINEESYNC",
                selectionType = EnvVarSelectionType.TOGGLE,
                possibleValues = listOf("0", "1"),
            ),
            "TU_DEBUG" to EnvVarInfo(
                identifier = "TU_DEBUG",
                selectionType = EnvVarSelectionType.MULTI_SELECT,
                possibleValues = listOf(
                    "startup", "nir", "nobin", "sysmem", "gmem", "forcebin", "layout", "noubwc", "nomultipos",
                    "nolrz", "nolrzfc", "perf", "perfc", "flushall", "syncdraw", "push_consts_per_stage", "rast_order",
                    "unaligned_store", "log_skip_gmem_ops", "dynamic", "bos", "3d_load", "fdm", "noconform", "rd",
                ),
            ),
            "DXVK_HUD" to EnvVarInfo(
                identifier = "DXVK_HUD",
                selectionType = EnvVarSelectionType.MULTI_SELECT,
                possibleValues = listOf(
                    "devinfo", "fps", "frametimes", "submissions", "drawcalls", "pipelines", "descriptors",
                    "memory", "gpuload", "version", "api", "cs", "compiler", "samplers",
                ),
            ),
            "MESA_EXTENSION_MAX_YEAR" to EnvVarInfo(
                identifier = "MESA_EXTENSION_MAX_YEAR",
            ),
            "PULSE_LATENCY_MSEC" to EnvVarInfo(
                identifier = "PULSE_LATENCY_MSEC",
            ),
            "MESA_VK_WSI_PRESENT_MODE" to EnvVarInfo(
                identifier = "MESA_VK_WSI_PRESENT_MODE",
                possibleValues = listOf("immediate", "mailbox", "fifo", "relaxed"),
            ),
        )
    }
}
