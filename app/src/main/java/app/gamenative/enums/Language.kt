package app.gamenative.enums

import timber.log.Timber

enum class Language {
    english,
    german,
    french,
    italian,
    koreana,
    spanish,
    schinese,
    sc_schinese,
    tchinese,
    russian,
    japanese,
    polish,
    brazilian,
    latam,
    vietnamese,
    portuguese,
    danish,
    dutch,
    swedish,
    norwegian,
    finnish,
    turkish,
    thai,
    czech,
    unknown,
    ;

    companion object {
        fun from(keyValue: String?): Language {
            return when (keyValue?.lowercase()) {
                english.name -> english
                german.name -> german
                french.name -> french
                italian.name -> italian
                koreana.name -> koreana
                spanish.name -> spanish
                schinese.name -> schinese
                sc_schinese.name -> sc_schinese
                tchinese.name -> tchinese
                russian.name -> russian
                japanese.name -> japanese
                polish.name -> polish
                brazilian.name -> brazilian
                latam.name -> latam
                vietnamese.name -> vietnamese
                portuguese.name -> portuguese
                danish.name -> danish
                dutch.name -> dutch
                swedish.name -> swedish
                norwegian.name -> norwegian
                finnish.name -> finnish
                turkish.name -> turkish
                thai.name -> thai
                czech.name -> czech
                unknown.name -> unknown
                else -> {
                    if (keyValue != null) {
                        Timber.e("Could not find proper Language from $keyValue")
                    }
                    unknown
                }
            }
        }
    }
}
