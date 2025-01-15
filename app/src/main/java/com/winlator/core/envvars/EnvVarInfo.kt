package com.winlator.core.envvars

data class EnvVarInfo(
    val identifier: String,
    val selectionType: EnvVarSelectionType = EnvVarSelectionType.NONE,
    val possibleValues: List<String> = emptyList(),
) {
    companion object {
        val KNOWN_ENV_VARS = mapOf(
            Pair("ZINK_DESCRIPTORS",
                EnvVarInfo(
                    identifier = "ZINK_DESCRIPTORS",
                    possibleValues = listOf("auto", "lazy", "cached", "notemplates")
                )
            ),
            Pair("ZINK_DEBUG",
                EnvVarInfo(
                    identifier = "ZINK_DEBUG",
                    selectionType = EnvVarSelectionType.MULTI_SELECT,
                    possibleValues = listOf("nir", "spirv", "tgsi", "validation", "sync", "compact", "noreorder")
                )
            ),
            Pair("MESA_SHADER_CACHE_DISABLE",
                EnvVarInfo(
                    identifier = "MESA_SHADER_CACHE_DISABLE",
                    selectionType = EnvVarSelectionType.TOGGLE,
                    possibleValues = listOf("false", "true")
                )
            ),
            Pair("mesa_glthread",
                EnvVarInfo(
                    identifier = "mesa_glthread",
                    selectionType = EnvVarSelectionType.TOGGLE,
                    possibleValues = listOf("false", "true")
                )
            ),
            Pair("WINEESYNC",
                EnvVarInfo(
                    identifier = "WINEESYNC",
                    selectionType = EnvVarSelectionType.TOGGLE,
                    possibleValues = listOf("0", "1")
                )
            ),
            Pair("TU_DEBUG",
                EnvVarInfo(
                    identifier = "TU_DEBUG",
                    selectionType = EnvVarSelectionType.MULTI_SELECT,
                    possibleValues = listOf("startup", "nir", "nobin", "sysmem", "gmem", "forcebin", "layout", "noubwc", "nomultipos",
                        "nolrz", "nolrzfc", "perf", "perfc", "flushall", "syncdraw", "push_consts_per_stage", "rast_order",
                        "unaligned_store", "log_skip_gmem_ops", "dynamic", "bos", "3d_load", "fdm", "noconform", "rd"
                    )
                )
            ),
            Pair("DXVK_HUD",
                EnvVarInfo(
                    identifier = "DXVK_HUD",
                    selectionType = EnvVarSelectionType.MULTI_SELECT,
                    possibleValues = listOf("devinfo", "fps", "frametimes", "submissions", "drawcalls", "pipelines", "descriptors",
                        "memory", "gpuload", "version", "api", "cs", "compiler", "samplers"
                    )
                )
            ),
            Pair("MESA_EXTENSION_MAX_YEAR",
                EnvVarInfo(
                    identifier = "MESA_EXTENSION_MAX_YEAR",
                )
            ),
            Pair("PULSE_LATENCY_MSEC",
                EnvVarInfo(
                    identifier = "PULSE_LATENCY_MSEC",
                )
            ),
            Pair("MESA_VK_WSI_PRESENT_MODE",
                EnvVarInfo(
                    identifier = "MESA_VK_WSI_PRESENT_MODE",
                    possibleValues = listOf("immediate", "mailbox", "fifo", "relaxed")
                )
            ),
        )
    }
}
