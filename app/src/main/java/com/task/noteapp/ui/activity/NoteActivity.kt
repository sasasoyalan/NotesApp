package com.task.noteapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.task.noteapp.R
import com.task.noteapp.databinding.ActivityNoteBinding
import com.task.noteapp.db.NoteDatabase
import com.task.noteapp.repository.NoteRepository
import com.task.noteapp.utils.shortToast
import com.task.noteapp.viewmodel.NoteActivityViewModel
import com.task.noteapp.viewmodel.NoteActivityViewModelFactory

class NoteActivity : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var binding: ActivityNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        try {
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteViewModelProviderFactory = NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(
                this,
                noteViewModelProviderFactory
            )[NoteActivityViewModel::class.java]
        } catch (e: Exception) {
            shortToast("error occurred")
        }
    }
}