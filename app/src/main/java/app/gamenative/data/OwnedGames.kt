package app.gamenative.data

data class OwnedGames(
    val appId: Int = 0,
    val name: String = "",
    val playtimeTwoWeeks: Int = 0,
    val playtimeForever: Int = 0,
    val imgIconUrl: String = "",
    val sortAs: String? = null,
    val rtimeLastPlayed: Int = 0,
)
