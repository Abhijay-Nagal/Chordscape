package com.pianoquest.app.data

/**
 * Orders songs by how famous they are, most popular first. Hand-curated for the best-known pieces; everything else
 * follows by its composer's fame (the order of [Composers.all]) and then by difficulty.
 */
object Popularity {
    private val ranked = listOf(
        // The world's most famous piano pieces
        "fur-elise", "moonlight", "turkish-march", "clair-de-lune", "canon-d", "nocturne-9-2", "entertainer", "gymnopedie",
        "prelude-c", "ode-to-joy", "minute-waltz", "fantaisie-impromptu", "sonata-facile", "maple-leaf", "mountain-king",
        "beethoven-5th", "moonlight-3", "revolutionary", "raindrop", "traumerei", "pathetique-2", "rach-prelude",
        "minuet-g", "chopin-ballade-1", "impromptu-899-3", "brahms-waltz-15", "brahms-lullaby", "pathetique-1", "pathetique-3",
        "moment-musical-3", "prelude-e-minor", "wild-rider", "happy-farmer", "invention-1", "arabesque-1", "nocturne-72",
        "carmen-prelude", "marche-militaire", "impromptu-899-2", "impromptu-899-4", "fantasy-d", "black-key", "moonlight-2",
        "rach-23-5", "etude-10-1", "nocturne-27-2", "gnossienne-1", "consolation-3", "january", "thunder-lightning",
        "tempest-1", "appassionata-3", "op49-2-2", "k331-theme", "sonata-facile-2", "sonata-facile-3", "clementi-36-1",
        "rage-penny", "solace", "bethena", "elite-syncopations", "easy-winners", "pineapple-rag", "aeolian-harp",
        "waltz-b-minor", "chopin-funeral", "prelude-c-minor", "prelude-a", "prelude-b-minor", "chopin-prelude-1",
        "etude-10-2", "etude-25-2", "nocturne-9-1", "chopin-ballade-4", "invention-13", "invention-4", "invention-8",
        "prelude-c-minor-847", "fugue-c", "fugue-c-minor-847", "musette-d", "troldhaugen", "ases-death", "march-dwarfs",
        "brahms-intermezzo", "gymnopedie-2", "gymnopedie-3", "gnossienne-2", "gnossienne-3", "arabesque-2",
        // Favourite tunes for beginners
        "twinkle", "happy-birthday", "jingle-bells", "mary-lamb", "hot-cross-buns", "silent-night", "greensleeves",
        "london-bridge", "frere-jacques", "saints", "auld-lang-syne", "deck-the-halls", "merry-christmas", "row-boat",
        "old-macdonald", "yankee-doodle", "au-clair",
    ).withIndex().associate { (i, id) -> id to i }

    private val composerRank: Map<String, Int> by lazy {
        Composers.all.withIndex().flatMap { (i, c) -> c.match.map { it to i } }.toMap()
    }

    fun rank(song: Song): Int {
        ranked[song.id]?.let { return it }
        val c = composerRank.entries.firstOrNull { song.composer.contains(it.key) }?.value ?: 60
        return 1000 + c * 10 + song.difficulty
    }

    fun sort(songs: List<Song>): List<Song> = songs.sortedBy { rank(it) }
}
