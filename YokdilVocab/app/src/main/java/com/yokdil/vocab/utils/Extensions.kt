package com.yokdil.vocab.utils

import com.google.gson.Gson
import com.yokdil.vocab.data.db.entities.Word

private val gson = Gson()

fun Word.parseSynonyms(): List<String> {
    return try {
        gson.fromJson(synonyms, Array<String>::class.java).toList()
    } catch (e: Exception) {
        listOf(synonyms)
    }
}

fun Word.parseAntonyms(): List<String> {
    return try {
        gson.fromJson(antonyms, Array<String>::class.java).toList()
    } catch (e: Exception) {
        listOf(antonyms)
    }
}
