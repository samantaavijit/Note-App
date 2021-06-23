package com.avijitsamanta.noteapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.avijitsamanta.noteapp.R
import com.avijitsamanta.noteapp.adapter.NodesAdopter
import com.avijitsamanta.noteapp.adapter.NoteItemClick
import com.avijitsamanta.noteapp.entities.Note
import com.avijitsamanta.noteapp.viewmodel.NoteViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), NoteItemClick {
    private lateinit var viewModel: NoteViewModel
    private var dialogAddURL: AlertDialog? = null

    private var adopter = NodesAdopter(this, this)

    private var noteClickedPosition: Int = -1

    // static final field
    companion object {
        const val REQUEST_CODE_PERMISSION = 101
        const val REQUEST_CODE_SELECT_IMAGE = 102
        const val NOTE_VIEW_TYPE = "isViewOrUpdate"
        const val FROM_QUICK_ACTIONS = "isFromQuickActions"
        const val QUICK_ACTION_TYPE = "quickActionType"
        const val MY_NOTE = "note"
        const val IMAGE_PATH = "imagePath"
        const val IMAGE_TYPE_VALUE = "image"
        const val WEB_TYPE_VALUE = "web"
        const val WEB_LINK = "webLink"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageAddNoteMain.setOnClickListener {
            startActivity(
                Intent(applicationContext, CreateNoteActivity::class.java).putExtra(
                    NOTE_VIEW_TYPE,
                    false
                )
            )
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)

        notesRecyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        notesRecyclerView.adapter = adopter

        getNotes()

        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("beforeTextChanged:", s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getSearchNote(s.toString().toLowerCase(Locale.ROOT))
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("afterTextChanged:", s.toString())
            }
        })

        imageAddNote.setOnClickListener {
            startActivity(
                Intent(applicationContext, CreateNoteActivity::class.java).putExtra(
                    NOTE_VIEW_TYPE,
                    false
                )
            )
        }

        imageAddImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSION
                )
            } else {
                selectImage()
            }
        }

        imageAddWebLink.setOnClickListener {
            showAddURLDialog()
        }

    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    @SuppressLint("Recycle")
    private fun getPathFromUri(contentUrl: Uri): String {
        val filePath: String
        val cursor = contentResolver.query(contentUrl, null, null, null, null)
        if (cursor == null) {
            filePath = contentUrl.path.toString()
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    /**
     * it get
     * all search notes
     */
    private fun getSearchNote(key: String) {
        val aa = viewModel.searchNote(key)
        aa.observe(this, { list ->
            list?.let {
                adopter.updateNote(it)
            }
        })
    }

    /**
     * It gets all Notes
     */
    private fun getNotes() {
        viewModel.allNodes.observe(this, { list ->
            list?.let {
                adopter.updateNote(it)
            }
        })
    }

    private fun showAddURLDialog() {
        if (dialogAddURL == null) {
            val builder = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(
                R.layout.layout_add_url,
                findViewById(R.id.layoutAddUrlContainer)
            )
            builder.setView(view)
            dialogAddURL = builder.create()
            if (dialogAddURL!!.window != null) {
                dialogAddURL!!.window?.setBackgroundDrawable(ColorDrawable(0))
            }
            val inputUrl = view.findViewById<EditText>(R.id.inputURL)
            inputUrl.requestFocus()

            view.findViewById<TextView>(R.id.textAdd).setOnClickListener {
                val url = inputUrl.text.toString().trim()
                if (url.isEmpty() || !Patterns.WEB_URL.matcher(url).matches()) {
                    Toast.makeText(applicationContext, "Enter valid URL", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(applicationContext, CreateNoteActivity::class.java)
                    intent.putExtra(FROM_QUICK_ACTIONS, true)
                    intent.putExtra(QUICK_ACTION_TYPE, WEB_TYPE_VALUE)
                    intent.putExtra(WEB_LINK, url)
                    startActivity(intent)
                    dialogAddURL!!.dismiss()
                }
            }
            view.findViewById<TextView>(R.id.textCancel).setOnClickListener {
                dialogAddURL!!.dismiss()
            }
        }
        dialogAddURL!!.show()
    }

    override fun onNoteItemClick(note: Note, position: Int) {
        noteClickedPosition = position
        val intent = Intent(applicationContext, CreateNoteActivity::class.java)
        intent.putExtra(NOTE_VIEW_TYPE, true)
        intent.putExtra(MY_NOTE, note)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) selectImage()
                else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val imageUri = data.data
                if (imageUri != null) {
                    try {
                        val selectedImagePath = getPathFromUri(imageUri)
                        val intent = Intent(applicationContext, CreateNoteActivity::class.java)
                        intent.putExtra(FROM_QUICK_ACTIONS, true)
                        intent.putExtra(QUICK_ACTION_TYPE, IMAGE_TYPE_VALUE)
                        intent.putExtra(IMAGE_PATH, selectedImagePath)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}