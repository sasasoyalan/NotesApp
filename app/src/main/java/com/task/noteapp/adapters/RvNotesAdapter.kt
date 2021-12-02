package com.task.noteapp.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.task.noteapp.R
import com.task.noteapp.databinding.NoteItemLayoutBinding
import com.task.noteapp.model.Note
import com.task.noteapp.ui.fragments.NoteFragmentDirections
import com.task.noteapp.utils.hideKeyboard
import com.task.noteapp.utils.loadHiRezThumbnail
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class RvNotesAdapter : androidx.recyclerview.widget.ListAdapter<
        Note,
        RvNotesAdapter.NotesViewHolder>(
    DiffUtilCallback()
) {

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentBinding = NoteItemLayoutBinding.bind(itemView)
        val title: MaterialTextView = contentBinding.noteItemTitle
        val content: TextView = contentBinding.noteContentItemTitle
        val date: MaterialTextView = contentBinding.noteDate
        val isEdited: MaterialTextView = contentBinding.itemIsEditedImage
        val image: ImageView = contentBinding.itemNoteImage
        val parent: MaterialCardView = contentBinding.noteItemLayoutParent
        val markWon = Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(itemView.context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ) { visitor, _ -> visitor.forceNewLine() }
                }
            })
            .build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.note_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {

        getItem(position).let { note ->

            holder.apply {
                parent.transitionName = "recyclerView_${note.id}"

                if (note.isEdited){
                    isEdited.text =
                        parent.context.getString(R.string.edited_on, SimpleDateFormat.getDateInstance().format(
                            Date()
                        ))
                    isEdited.visibility = View.VISIBLE
                }

                title.text = note.title
                markWon.setMarkdown(content, note.content)
                date.text = note.date
                if (note.imagePath != null) {
                    image.visibility = View.VISIBLE
                    val uri = Uri.fromFile(File(note.imagePath))
                    if (File(note.imagePath).exists())
                        itemView.context.loadHiRezThumbnail(uri, image)
                } else {
                    Glide.with(itemView).clear(image)
                    image.isVisible = false
                }

                parent.setCardBackgroundColor(note.color)

                itemView.setOnClickListener {
                    val action = NoteFragmentDirections.actionNoteFragmentToNoteContentFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action, extras)
                }
                content.setOnClickListener {
                    val action = NoteFragmentDirections.actionNoteFragmentToNoteContentFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action, extras)
                }
            }
        }
    }
}