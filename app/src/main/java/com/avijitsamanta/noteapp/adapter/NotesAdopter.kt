package com.avijitsamanta.noteapp.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.avijitsamanta.noteapp.R
import com.avijitsamanta.noteapp.entities.Note
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.note_items.view.*
import java.util.*
import kotlin.collections.ArrayList

class NodesAdopter(
    private val context: Context,
    private val listener: NoteItemClick,
) :
    RecyclerView.Adapter<NodesAdopter.NoteViewHolder>() {

    private var noteList = ArrayList<Note>()
    private val noteSource = ArrayList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(context).inflate(R.layout.note_items, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.title.text = note.title
        holder.date.text = note.dateTime
        holder.noteContent.text = note.noteText
        val gradientDrawable: GradientDrawable = holder.layoutNote.background as GradientDrawable
        if (note.myColor != null) {
            gradientDrawable.setColor(note.myColor!!.toInt())
        } else {
            gradientDrawable.setColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorDefaultNoteColor
                )
            )
        }
        if (note.imagePath != null) {
            holder.imageNote.setImageBitmap(BitmapFactory.decodeFile(note.imagePath))
            holder.imageNote.visibility = View.VISIBLE
        } else {
            holder.imageNote.visibility = View.GONE
        }
        holder.layoutNote.setOnClickListener {
            listener.onNoteItemClick(note, position)
        }
    }

    fun updateNote(note: List<Note>) {
        noteList.clear()
        noteList.addAll(note)
        noteSource.clear()
        noteSource.addAll(note)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return noteList.size
    }


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.textTitleItem
        val date: TextView = itemView.textDateTimeItem
        val noteContent: TextView = itemView.textNoteContentItem
        val layoutNote: LinearLayout = itemView.layoutNoteItem
        val imageNote: RoundedImageView = itemView.imageNoteItem
    }

}


interface NoteItemClick {
    fun onNoteItemClick(note: Note, position: Int)
}

