package com.avijitsamanta.noteapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.avijitsamanta.noteapp.R
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.FROM_QUICK_ACTIONS
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.IMAGE_PATH
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.MY_NOTE
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.NOTE_VIEW_TYPE
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.QUICK_ACTION_TYPE
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.REQUEST_CODE_PERMISSION
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.REQUEST_CODE_SELECT_IMAGE
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.IMAGE_TYPE_VALUE
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.WEB_LINK
import com.avijitsamanta.noteapp.activities.MainActivity.Companion.WEB_TYPE_VALUE
import com.avijitsamanta.noteapp.entities.Note
import com.avijitsamanta.noteapp.viewmodel.NoteViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_create_note.*
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteActivity : AppCompatActivity() {
    private lateinit var viewModel: NoteViewModel
    private lateinit var selectNoteColor: String
    private lateinit var imageNote: ImageView
    private lateinit var textWebURL: TextView
    private lateinit var linearLayoutURL: LinearLayout
    private lateinit var layoutMiscellaneous: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var selectedImagePath: String? = null
    private var dialogAddURL: AlertDialog? = null
    private var dialogDeleteNote: AlertDialog? = null
    private var availableNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        imageBack.setOnClickListener {
            onBackPressed()
        }
        imageNote = findViewById(R.id.imageNote)
        textWebURL = findViewById(R.id.textWebURL)
        linearLayoutURL = findViewById(R.id.layoutWebURL)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)

        // Friday, 20 November 2020 03:21 PM
        textDateTime.text =
            SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.getDefault()).format(
                Date()
            )

        imageSave.setOnClickListener {
            saveNote()
        }
        imageRemoveURL.setOnClickListener {
            textWebURL.text = null
            layoutWebURL.visibility = View.GONE
        }
        imageRemoveImage.setOnClickListener {
            imageNote.setImageBitmap(null)
            imageNote.visibility = View.GONE
            imageRemoveImage.visibility = View.GONE
            selectedImagePath = null
        }

        val myIntent = intent
        if (myIntent != null) {
            if (myIntent.getBooleanExtra(NOTE_VIEW_TYPE, false)) {
                availableNote = myIntent.getSerializableExtra(MY_NOTE) as Note?
                setViewOrUpdateNote()
            }
            if (myIntent.getBooleanExtra(FROM_QUICK_ACTIONS, false)) {
                val type = myIntent.getStringExtra(QUICK_ACTION_TYPE)
                if (type != null) {
                    if (type == IMAGE_TYPE_VALUE) {
                        selectedImagePath = myIntent.getStringExtra(IMAGE_PATH)
                        if (selectedImagePath != null) {
                            imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath))
                            imageNote.visibility = View.VISIBLE
                            imageRemoveImage.visibility = View.VISIBLE
                        }
                    } else if (type == WEB_TYPE_VALUE) {
                        val url = myIntent.getStringExtra(WEB_LINK)
                        if (url != null) {
                            textWebURL.text = url
                            layoutWebURL.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        selectNoteColor = ContextCompat.getColor(this, R.color.colorDefaultNoteColor).toString()
        initMiscellaneous()
    }

    private fun setViewOrUpdateNote() {
        inputNoteTitle.setText(availableNote?.title)
        inputNoteText.setText(availableNote?.noteText)
        textDateTime.text = availableNote?.dateTime
        if (availableNote?.imagePath != null && availableNote?.imagePath!!.trim().isNotEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(availableNote?.imagePath))
            imageNote.visibility = View.VISIBLE
            imageRemoveImage.visibility = View.VISIBLE
            selectedImagePath = availableNote?.imagePath
        }

        if (availableNote?.webLink != null && availableNote?.webLink!!.trim().isNotEmpty()) {
            textWebURL.text = availableNote?.webLink
            layoutWebURL.visibility = View.VISIBLE
        }
    }

    /***
     * It save the note and
     * return to the previous activity
     */
    private fun saveNote() {
        val title = inputNoteTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show()
            return
        }
        val noteContent = inputNoteText.text.toString().trim()

        if (noteContent.isEmpty()) {
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show()
            return
        }
        val note = Note()
        note.title = title
        note.noteText = noteContent
        note.dateTime = textDateTime.text.toString().trim()
        note.myColor = selectNoteColor
        note.imagePath = selectedImagePath
        note.timeStamp = Date()
        if (layoutWebURL.visibility == View.VISIBLE) {
            note.webLink = textWebURL.text.toString().trim()
        }

        /**
         * it replace the note
         * because we set the note id
         */
        if (availableNote != null) {
            note.id = availableNote?.id!!
        }

        viewModel.insertNote(note)
        Toast.makeText(this, "$title Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    /**
     * It show Bottom sheet dialog
     * and select color
     * add image and url
     */
    private fun initMiscellaneous() {
        layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous)
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous)
        layoutMiscellaneous.findViewById<TextView>(R.id.textMiscellaneous).setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        val imageColor1: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor1)
        val imageColor2: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor2)
        val imageColor3: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor3)
        val imageColor4: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor4)
        val imageColor5: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor5)

        layoutMiscellaneous.findViewById<View>(R.id.viewColor1).setOnClickListener {
            selectNoteColor =
                ContextCompat.getColor(this, R.color.colorDefaultNoteColor).toString()
            imageColor1.setImageResource(R.drawable.ic_done)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor2).setOnClickListener {
            selectNoteColor = ContextCompat.getColor(this, R.color.colorNoteColor2).toString()
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(R.drawable.ic_done)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor3).setOnClickListener {
            selectNoteColor = ContextCompat.getColor(this, R.color.colorNoteColor3).toString()
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(R.drawable.ic_done)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor4).setOnClickListener {
            selectNoteColor = ContextCompat.getColor(this, R.color.colorNoteColor4).toString()
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(R.drawable.ic_done)
            imageColor5.setImageResource(0)
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor5).setOnClickListener {
            selectNoteColor = ContextCompat.getColor(this, R.color.colorNoteColor5).toString()
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(R.drawable.ic_done)
        }

        /**
         * It set Note color
         * if it is updated note
         */
        if (availableNote != null && availableNote?.myColor != null && availableNote?.myColor!!.trim()
                .isNotEmpty()
        ) {
            when (availableNote?.myColor!!.toInt()) {
                ContextCompat.getColor(
                    this,
                    R.color.colorNoteColor2
                ) -> layoutMiscellaneous.findViewById<View>(R.id.viewColor2).performClick()
                ContextCompat.getColor(
                    this,
                    R.color.colorNoteColor3
                ) -> layoutMiscellaneous.findViewById<View>(R.id.viewColor3).performClick()
                ContextCompat.getColor(
                    this,
                    R.color.colorNoteColor4
                ) -> layoutMiscellaneous.findViewById<View>(R.id.viewColor4).performClick()
                ContextCompat.getColor(
                    this,
                    R.color.colorNoteColor5
                ) -> layoutMiscellaneous.findViewById<View>(R.id.viewColor5).performClick()
            }
        }

        layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutAddImage).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
        layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutAddURL).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showAddURLDialog()
        }

        if (availableNote != null) {
            layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutDeleteNote).visibility =
                View.VISIBLE
            layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutDeleteNote)
                .setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    showDeleteNoteDialog()
                }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
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
                        val inputStream = contentResolver.openInputStream(imageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imageNote.setImageBitmap(bitmap)
                        imageNote.visibility = View.VISIBLE
                        imageRemoveImage.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(imageUri)
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else
            super.onBackPressed()
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
                    textWebURL.text = url
                    layoutWebURL.visibility = View.VISIBLE
                    dialogAddURL!!.dismiss()
                }
            }
            view.findViewById<TextView>(R.id.textCancel).setOnClickListener {
                dialogAddURL!!.dismiss()
            }
        }
        dialogAddURL!!.show()
    }

    private fun showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            val builder = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(
                R.layout.layout_delete_note,
                findViewById(R.id.layoutAddUrlContainer)
            )
            builder.setView(view)
            dialogDeleteNote = builder.create()
            if (dialogDeleteNote!!.window != null) {
                dialogDeleteNote!!.window?.setBackgroundDrawable(ColorDrawable(0))
            }
            view.findViewById<TextView>(R.id.textDeleteNote).setOnClickListener {
                availableNote?.let { it1 -> viewModel.deleteNote(it1) }
                Toast.makeText(
                    applicationContext,
                    "$availableNote?.title Deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            view.findViewById<TextView>(R.id.textCancel).setOnClickListener {
                dialogDeleteNote!!.dismiss()
            }
        }
        dialogDeleteNote!!.show()
    }
}

