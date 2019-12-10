package life.nekos.bot.utils

fun String.toReactionString(): String {
    if (!this.contains(':')) {
        throw IllegalStateException("String does not appear to be a valid emote!")
    }

    return this.substringAfter(':').substringBefore('>')
}
