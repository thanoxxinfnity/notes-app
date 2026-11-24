package com.thanoxxinfnity.notesapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel(private val repo: NotesRepository) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _editing = MutableStateFlow<Note?>(null)
    val editing: StateFlow<Note?> = _editing.asStateFlow()

    private val _deleteTarget = MutableStateFlow<Note?>(null)
    val deleteTarget: StateFlow<Note?> = _deleteTarget.asStateFlow()

    init {
        viewModelScope.launch {
            repo.notes.collect { _notes.value = it }
        }
    }

    fun startNew() { _editing.value = Note() }
    fun startEdit(note: Note) { _editing.value = note }
    fun cancelEdit() { _editing.value = null }

    fun updateDraft(title: String, content: String) {
        _editing.update { it?.copy(title = title, content = content) }
    }

    fun saveDraft() {
        val draft = _editing.value ?: return
        val list = _notes.value.toMutableList()
        val idx = list.indexOfFirst { it.id == draft.id }
        if (idx >= 0) list[idx] = draft else list.add(0, draft)
        viewModelScope.launch {
            repo.saveAll(list)
        }
        _editing.value = null
    }

    fun requestDelete(note: Note) { _deleteTarget.value = note }
    fun cancelDelete() { _deleteTarget.value = null }
    fun confirmDelete() {
        val target = _deleteTarget.value ?: return
        val list = _notes.value.filterNot { it.id == target.id }
        viewModelScope.launch {
            repo.saveAll(list)
        }
        _deleteTarget.value = null
    }

    class Factory(private val repo: NotesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotesViewModel(repo) as T
    }
}
