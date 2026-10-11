package com.pianoquest.app.data

import com.pianoquest.app.platform.Storage
import com.pianoquest.app.platform.nowMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class ImportedEntry(val id: String, val title: String, val file: String, val bpm: Int, val seconds: Double, val meterNum: Int, val meterDen: Int)

/** MIDI files the user imported from their device. Stored privately in the app's own storage. */
object ImportedSongs {
    private const val INDEX = "imported-index.json"
    private val json = Json { ignoreUnknownKeys = true }
    private val _entries = MutableStateFlow(load())
    val entries: StateFlow<List<ImportedEntry>> = _entries.asStateFlow()

    private fun load(): List<ImportedEntry> = runCatching {
        Storage.read(INDEX)?.let { json.decodeFromString(ListSerializer(ImportedEntry.serializer()), it) }
    }.getOrNull() ?: emptyList()

    private fun save(list: List<ImportedEntry>) {
        _entries.value = list
        Storage.write(INDEX, json.encodeToString(ListSerializer(ImportedEntry.serializer()), list))
    }

    fun toSong(e: ImportedEntry) = Song(
        id = e.id, title = e.title, composer = "Imported MIDI file", genre = Genre.IMPORTED, difficulty = 3, bpm = e.bpm,
        meterNum = e.meterNum, meterDen = e.meterDen, importedFile = e.file, durationHint = e.seconds,
        about = "A MIDI file you imported. Choose Easy, Medium or Hard before you start.", source = "Your imported file",
    )

    fun songs(): List<Song> = _entries.value.map(::toSong)

    /** Files opened with "Play without saving": usable for this session only, not listed under My Songs. */
    private val temporary = HashMap<String, ImportedEntry>()

    fun find(id: String): Song? = (_entries.value.firstOrNull { it.id == id } ?: temporary[id])?.let(::toSong)

    /** Checks that [bytes] is a MIDI file with notes; returns its suggested title. */
    fun validate(name: String, bytes: ByteArray): Result<String> = runCatching {
        val parsed = MidiFileReader.parse(bytes)
        require(parsed.notes.isNotEmpty()) { "This file contains no notes." }
        name.substringBeforeLast('.').replace('_', ' ').trim().ifBlank { "Imported song" }.take(60)
    }

    /**
     * Validates and stores a MIDI file. With [save] it is added to My Songs; otherwise it is kept only until the app
     * closes. Returns the new song or an error message.
     */
    fun import(name: String, bytes: ByteArray, title: String? = null, save: Boolean = true): Result<Song> = runCatching {
        val parsed = MidiFileReader.parse(bytes)
        require(parsed.notes.isNotEmpty()) { "This file contains no notes." }
        val id = "imported-${nowMillis()}"
        val file = if (save) "imported/$id.mid" else "imported/tmp-$id.mid"
        Storage.writeBytes(file, bytes)
        val entry = ImportedEntry(id, (title ?: name.substringBeforeLast('.')).trim().ifBlank { "Imported song" }.take(60), file,
            parsed.initialBpm.toInt().coerceIn(20, 300), parsed.lengthSec, parsed.meterNum, parsed.meterDen)
        if (save) save(_entries.value + entry) else temporary[id] = entry
        toSong(entry)
    }

    fun rename(id: String, title: String) {
        save(_entries.value.map { if (it.id == id) it.copy(title = title.trim().ifBlank { it.title }.take(60)) else it })
    }

    fun remove(id: String) {
        val e = _entries.value.firstOrNull { it.id == id } ?: return
        Storage.delete(e.file)
        save(_entries.value - e)
    }
}
