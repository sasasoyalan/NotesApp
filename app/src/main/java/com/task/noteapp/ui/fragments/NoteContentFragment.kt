package com.task.noteapp.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import com.task.noteapp.R
import com.task.noteapp.databinding.BottomSheetDialogBinding
import com.task.noteapp.databinding.FragmentNoteContentBinding
import com.task.noteapp.model.Note
import com.task.noteapp.ui.activity.NoteActivity
import com.task.noteapp.utils.*
import com.task.noteapp.viewmodel.NoteActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NoteContentFragment : Fragment(R.layout.fragment_note_content) {

    private lateinit var navController: NavController
    private lateinit var contentBinding: FragmentNoteContentBinding
    private lateinit var result: String
    private lateinit var photoFile: File
    private var note: Note? = null
    private var color = -1
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat.getDateInstance().format(Date())
    private val REQUEST_IMAGE_CAPTURE = 100
    private val SELECT_IMAGE_FROM_STORAGE = 101
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: NoteContentFragmentArgs by navArgs()
    private var lastNoteChanged: Boolean =false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration = 300L
           // setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
        addSharedElementListener()
    }

    @SuppressLint("InflateParams", "QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding = FragmentNoteContentBinding.bind(view)
        /* Sets the unique transition name for the layout that is
         being inflated using SharedElementEnterTransition class */
        ViewCompat.setTransitionName(
            contentBinding.noteContentFragmentParent,
            "recyclerView_${args.note?.id}"
        )

        showInfo()

        navController = Navigation.findNavController(view)
        val activity = activity as NoteActivity
        registerForContextMenu(contentBinding.noteImage)

        contentBinding.backBtn.setOnClickListener {
            requireView().hideKeyboard()
            saveNoteAndGoBack()
        }

        contentBinding.infoImage.setOnClickListener {
            showInfo()
        }

        try {
            contentBinding.etNoteContent.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    contentBinding.bottomBar.visibility = View.VISIBLE
                    contentBinding.etNoteContent.setStylesBar(contentBinding.styleBar)
                } else contentBinding.bottomBar.visibility = View.GONE
            }
        } catch (e: Throwable) {
            Log.d("TAG", e.stackTraceToString())
        }

        try {
            contentBinding.etNoteContent.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (args.note!=null){
                        if (args.note!!.content!=contentBinding.etNoteContent.text.toString() || args.note!!.title!=contentBinding.etTitle.text.toString()){
                            contentBinding.backBtn.setImageResource(R.drawable.ic_baseline_check_24)
                        }else
                            contentBinding.backBtn.setImageResource(R.drawable.ic_round_arrow_back_24)
                    } else {
                        if (contentBinding.etNoteContent.text.toString().isNotEmpty()||contentBinding.etTitle.text.toString().isNotEmpty()){
                            contentBinding.backBtn.setImageResource(R.drawable.ic_baseline_check_24)
                        }else
                            contentBinding.backBtn.setImageResource(R.drawable.ic_round_arrow_back_24)
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    if(contentBinding.etNoteContent.layout!=null){
                        if (contentBinding.etNoteContent.layout.lineCount > 2){
                            contentBinding.etNoteContent.text?.delete(contentBinding.etNoteContent.text!!.length - 1, contentBinding.etNoteContent.text!!.length)
                            Snackbar.make(view, getString(R.string.max_lenght_error), Snackbar.LENGTH_SHORT).apply {
                                animationMode = Snackbar.ANIMATION_MODE_FADE
                            }.show()
                        }
                    }
                }
            })
            contentBinding.etTitle.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (args.note!=null){
                        if (args.note!!.title!=contentBinding.etTitle.text.toString()){
                            contentBinding.backBtn.setImageResource(R.drawable.ic_baseline_check_24)
                        }else
                            contentBinding.backBtn.setImageResource(R.drawable.ic_round_arrow_back_24)
                    } else {
                        if (contentBinding.etTitle.text.toString().isNotEmpty()){
                            contentBinding.backBtn.setImageResource(R.drawable.ic_baseline_check_24)
                        }else
                            contentBinding.backBtn.setImageResource(R.drawable.ic_round_arrow_back_24)
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                }
            })
        }catch (e: Throwable) {
            Log.d("TAG", e.stackTraceToString())
        }

        contentBinding.noteOptionsMenu.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme,
            )
            val bottomSheetView: View = layoutInflater.inflate(
                R.layout.bottom_sheet_dialog,
                null,
            )

            with(bottomSheetDialog) {
                setContentView(bottomSheetView)
                show()
            }
            val bottomSheetBinding = BottomSheetDialogBinding.bind(bottomSheetView)

            bottomSheetBinding.apply {
                colorPicker.apply {
                    setSelectedColor(color)
                    setOnColorSelectedListener { value ->
                        color = value
                        contentBinding.apply {
                            noteContentFragmentParent.setBackgroundColor(color)
                            toolbarFragmentNoteContent.setBackgroundColor(color)
                            bottomBar.setBackgroundColor(color)
                            activity.window.statusBarColor = color
                        }
                        bottomSheetBinding.bottomSheetParent.setCardBackgroundColor(color)
                    }
                }
                bottomSheetParent.setCardBackgroundColor(color)
            }
            bottomSheetView.post {
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            bottomSheetBinding.addImage.setOnClickListener {
                val permission = ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA,
                )
                if (permission != PackageManager.PERMISSION_GRANTED) {

                    val permissionArray = arrayOf(Manifest.permission.CAMERA)
                    ActivityCompat.requestPermissions(
                        activity,
                        permissionArray,
                        REQUEST_IMAGE_CAPTURE
                    )
                    ActivityCompat.OnRequestPermissionsResultCallback { requestCode,
                                                                        permissions,
                                                                        grantResults ->
                        when (requestCode) {
                            REQUEST_IMAGE_CAPTURE -> {
                                if (permissions[0] == Manifest.permission.CAMERA &&
                                    grantResults.isNotEmpty()
                                ) {
                                    Log.d("tag", "this function is called")
                                    takePictureIntent()
                                }
                            }
                        }
                    }
                }
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    takePictureIntent()
                    bottomSheetDialog.dismiss()
                }
            }
            @Suppress("DEPRECATION")
            bottomSheetBinding.selectImage.setOnClickListener {
                Intent(Intent.ACTION_GET_CONTENT).also { chooseIntent ->
                    chooseIntent.type = "image/*"
                    chooseIntent.resolveActivity(activity.packageManager!!.also {
                        startActivityForResult(chooseIntent, SELECT_IMAGE_FROM_STORAGE)
                    })
                }
                bottomSheetDialog.dismiss()
            }
        }

        //opens with existing note item
        setUpNote()

        activity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    saveNoteAndGoBack()
                }
            })
    }

    private fun showInfo() {
        GlobalScope.launch(Dispatchers.Main){
            delay(500L)
            showText()
            delay(2500L)
            hideText()
        }
    }

    suspend fun showText() {
        contentBinding.infoSpeech.animate()
            .translationX(0F)
            .setDuration(300)
            .alpha(1F)
            .start()
        contentBinding.infoSpeech.visibility=View.VISIBLE
    }

    suspend fun hideText() {
        contentBinding.infoSpeech.animate()
            .translationX(contentBinding.infoSpeech.width.toFloat())
            .alpha(0F)
            .setDuration(300)
            .start()
    }

    private fun addSharedElementListener() {
        (sharedElementEnterTransition as Transition).addListener(
            object : TransitionListenerAdapter() {
                override fun onTransitionStart(transition: Transition) {
                    super.onTransitionStart(transition)
                    if (args.note?.imagePath != null) {
                        contentBinding.noteImage.isVisible = true
                        val uri = Uri.fromFile(File(args.note?.imagePath!!))
                        job.launch {
                            requireContext().asyncImageLoader(uri, contentBinding.noteImage, this)
                        }
                    } else contentBinding.noteImage.isVisible = false
                }
            }
        )
    }

    /**
     * This Method handles the save and update operation.
     *
     * Checks if the note arg is null
     * It will save the note with a unique id.
     *
     * If note arg has data it will update
     * note to save any changes. */
    private fun saveNoteAndGoBack() {

        if (contentBinding.etTitle.text.toString().isEmpty() &&
            contentBinding.etNoteContent.text.toString().isEmpty()
        ) {
            result = "Empty Note Discarded"
            setFragmentResult("key", bundleOf("bundleKey" to result))
            navController.navigate(
                NoteContentFragmentDirections
                    .actionNoteContentFragmentToNoteFragment()
            )

        } else {
            if (contentBinding.etTitle.text.toString().isEmpty()){
                result = "Title is required!"
                CoroutineScope(Dispatchers.Main).launch {
                    view?.let {
                        Snackbar.make(it, result, Snackbar.LENGTH_SHORT).apply {
                            animationMode = Snackbar.ANIMATION_MODE_FADE
                        }.show()
                    }
                }
                return
            }
            if (contentBinding.etNoteContent.text.toString().isEmpty()){
                result = "Note is required!"
                CoroutineScope(Dispatchers.Main).launch {
                    view?.let {
                        Snackbar.make(it, result, Snackbar.LENGTH_SHORT).apply {
                            animationMode = Snackbar.ANIMATION_MODE_FADE
                        }.show()
                    }
                }
                return
            }
            note = args.note
            when (note) {
                null -> {
                    noteActivityViewModel.saveNote(
                        Note(
                            0,
                            contentBinding.etTitle.text.toString(),
                            contentBinding.etNoteContent.text.toString(),
                            currentDate,
                            "",
                            color,
                            noteActivityViewModel.setImagePath(),
                            false
                        )
                    )
                    result = "Note Saved"
                    setFragmentResult(
                        "key",
                        bundleOf("bundleKey" to result)
                    )
                    navController.navigate(
                        NoteContentFragmentDirections
                            .actionNoteContentFragmentToNoteFragment()
                    )

                }
                else -> {
                    updateNote()
                    navController.popBackStack()
                }
            }
        }
    }

    private fun updateNote() {
        if (note != null) {
            val edited = if (!note!!.isEdited){
                contentBinding.etNoteContent.text.toString()!=note!!.content || contentBinding.etTitle.text.toString()!=note!!.title
            }else
                true
            noteActivityViewModel.updateNote(
                Note(
                    note!!.id,
                    contentBinding.etTitle.text.toString(),
                    contentBinding.etNoteContent.text.toString(),
                    note!!.date,
                    currentDate,
                    color,
                    noteActivityViewModel.setImagePath(),
                    edited
                )
            )
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    @Suppress("DEPRECATION")
    private fun takePictureIntent() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { captureIntent ->
            photoFile = getPhotoFile(requireActivity())
            val fileProvider = FileProvider.getUriForFile(
                requireContext(),
                getString(R.string.fileAuthority),
                photoFile
            )
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            captureIntent.resolveActivity(activity?.packageManager!!.also {
                startActivityForResult(captureIntent, REQUEST_IMAGE_CAPTURE)
            })
        }
    }

    private fun menuIconWithText(r: Drawable, title: String): CharSequence {
        r.setBounds(0, 0, r.intrinsicWidth, r.intrinsicHeight)
        val sb = SpannableString("   $title")
        val imageSpan = ImageSpan(r, ImageSpan.ALIGN_BOTTOM)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

    @SuppressLint("SetTextI18n")
    private fun setUpNote() {
        val note = args.note
        val title = contentBinding.etTitle
        val content = contentBinding.etNoteContent
        val lastEdited = contentBinding.lastEdited
        val savedImage = noteActivityViewModel.setImagePath()

        if (note == null) {
            lastEdited.text =
                getString(R.string.created_on, SimpleDateFormat.getDateInstance().format(Date()))
            setImage(noteActivityViewModel.setImagePath())
        }

        if (note != null) {
            title.setText(note.title)
            content.setText(note.content)
            if (note.edited!=""){
                lastEdited.text = getString(R.string.created_on, note.date) +"\n"+ getString(R.string.edited_on, SimpleDateFormat.getDateInstance().format(Date()))
            }else {
                lastEdited.text = getString(R.string.created_on, note.date)
            }

            color = note.color
            if (savedImage != null) setImage(savedImage)
            else noteActivityViewModel.saveImagePath(note.imagePath)
            contentBinding.apply {
                job.launch {
                    delay(10)
                    noteContentFragmentParent.setBackgroundColor(color)
                    noteImage.isVisible = true
                }
                toolbarFragmentNoteContent.setBackgroundColor(color)
                bottomBar.setBackgroundColor(color)
            }
            activity?.window?.statusBarColor = note.color
        }
    }

    /**
     * This method gets a filePath as a string and converts it into URI
     * then passes that URI and the target imageView to and extension function
     * loadImage that will the image to its given target*/
    private fun setImage(filePath: String?) {
        if (filePath != null) {
            val uri = Uri.fromFile(File(filePath))
            contentBinding.noteImage.isVisible = true
            try {
                job.launch {
                    requireContext().asyncImageLoader(uri, contentBinding.noteImage, this)
                }
            } catch (e: Exception) {
                context?.shortToast(e.message)
                contentBinding.noteImage.isVisible = false
            }
        } else contentBinding.noteImage.isVisible = false
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            noteActivityViewModel.saveImagePath(photoFile.absolutePath)
            setImage(photoFile.absolutePath)
        }
        if (requestCode == SELECT_IMAGE_FROM_STORAGE && resultCode == RESULT_OK) {
            val uri = data?.data
            Log.d("Tag", uri.toString())
            if (uri != null) {
                val selectedImagePath = getImageUrlWithAuthority(
                    requireContext(),
                    uri,
                    requireActivity()
                )
                noteActivityViewModel.saveImagePath(selectedImagePath)
                setImage(selectedImagePath)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(
            0,
            1,
            1,
            menuIconWithText(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_round_delete_24
                )!!, getString(R.string.delete)
            )
        )
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                if (note?.imagePath != null) {
                    val toDelete = File(note?.imagePath!!)
                    if (toDelete.exists()) {
                        toDelete.delete()
                    }
                }
                if (noteActivityViewModel.setImagePath() != null) {
                    val toDelete = File(noteActivityViewModel.setImagePath()!!)
                    if (toDelete.exists()) {
                        toDelete.delete()
                    }
                    noteActivityViewModel.saveImagePath(null)
                }

                contentBinding.noteImage.isVisible = false
                updateNote()
                context?.shortToast("Deleted")
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (job.isActive) {
            job.cancel()
        }
    }
}