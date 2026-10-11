package com.pianoquest.app.data

import com.pianoquest.app.data.songs.contemporarySongs
import com.pianoquest.app.data.songs.exerciseSongs
import com.pianoquest.app.data.songs.folkSongs
import com.pianoquest.app.data.songs.holidaySongs
import com.pianoquest.app.data.songs.jazzBluesSongs
import com.pianoquest.app.data.songs.midiSongs
import com.pianoquest.app.data.songs.romanticSongs

object SongLibrary {
    val all: List<Song> by lazy {
        folkSongs() + holidaySongs() + midiSongs() + romanticSongs() + jazzBluesSongs() +
            contemporarySongs() + exerciseSongs()
    }

    private val byId: Map<String, Song> by lazy { all.associateBy { it.id } }

    fun get(id: String): Song? = byId[id] ?: ImportedSongs.find(id)

    fun byGenre(genre: Genre) = all.filter { it.genre == genre }
}
