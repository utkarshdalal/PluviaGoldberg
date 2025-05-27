package app.gamenative.enums

import java.util.EnumSet
import timber.log.Timber

enum class AppType(val code: Int) {
    invalid(0),
    game(0x01),
    application(0x02),
    tool(0x04),
    demo(0x08),
    deprected(0x10),
    dlc(0x20),
    guide(0x40),
    driver(0x80),
    config(0x100),
    hardware(0x200),
    franchise(0x400),
    video(0x800),
    plugin(0x1000),
    music(0x2000),
    series(0x4000),
    comic(0x8000),
    beta(0x10000),
    shortcut(0x20000),
    ;

    companion object {
        fun from(keyValue: String?): AppType {
            return when (keyValue?.lowercase()) {
                invalid.name -> invalid
                game.name -> game
                application.name -> application
                tool.name -> tool
                demo.name -> demo
                deprected.name -> deprected
                dlc.name -> dlc
                guide.name -> guide
                driver.name -> driver
                config.name -> config
                hardware.name -> hardware
                franchise.name -> franchise
                video.name -> video
                plugin.name -> plugin
                music.name -> music
                series.name -> series
                comic.name -> comic
                beta.name -> beta
                shortcut.name -> shortcut
                else -> {
                    if (keyValue != null) {
                        Timber.e("Could not find proper AppType from $keyValue")
                    }
                    invalid
                }
            }
        }

        fun fromFlags(flags: Int): EnumSet<AppType> {
            val result = EnumSet.noneOf(AppType::class.java)
            AppType.entries.forEach { appType ->
                if (flags and appType.code == appType.code) {
                    result.add(appType)
                }
            }
            return result
        }

        fun toFlags(value: EnumSet<AppType>): Int {
            return value.map { it.code }.reduceOrNull { first, second -> first or second } ?: invalid.code
        }

        fun fromCode(code: Int): AppType {
            return entries.find { it.code == code } ?: invalid
        }
    }
}
