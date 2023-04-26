package com.amitweb19.speak2you.fragment.questionaire

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.amitweb19.speak2you.OnItemClickListener
import com.amitweb19.speak2you.R
import com.amitweb19.speak2you.data.Questionaire
import com.amitweb19.speak2you.db.AppDatabase
import com.amitweb19.speak2you.fragment.question.QuestionActivity
import com.amitweb19.speak2you.model.repository
import com.amitweb19.speak2you.model.viewmodel
import com.amitweb19.speak2you.model.viewmodelfactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_question.*
import kotlinx.android.synthetic.main.activity_questionaire.*
import kotlinx.android.synthetic.main.activity_questionaire.recyclerview
import kotlinx.android.synthetic.main.activity_questionaire.tvEdit
import kotlinx.android.synthetic.main.bottom_sheet_q.*
import kotlinx.android.synthetic.main.bottom_sheet_qn.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*


class QuestionaireActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var records : ArrayList<Questionaire>
    private lateinit var mAdapter : Adapter
    private lateinit var db : AppDatabase

    private var allChecked = false

    private lateinit var toolbar: MaterialToolbar

    private lateinit var editBar: View
    private lateinit var btnClose: ImageButton
    private lateinit var btnSelectAll: ImageButton

    private lateinit var searchInput : TextInputEditText
    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var bottomSheetQn: LinearLayout
    private lateinit var bottomSheetBehaviorQn: BottomSheetBehavior<LinearLayout>

    private lateinit var btnRename : ImageButton
    private lateinit var btnDelete : ImageButton
    private lateinit var tvRename : TextView
    private lateinit var tvDelete : TextView

    private var READ_REQUEST_CODE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionaire)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        btnRename = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        tvRename = findViewById(R.id.tvEdit)
        tvDelete = findViewById(R.id.tvDelete)

        editBar = findViewById(R.id.editBar)
        btnClose = findViewById(R.id.btnClose)
        btnSelectAll = findViewById(R.id.btnSelectAll)

        bottomSheet = findViewById(R.id.bottomSheetItemQn)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetQn = findViewById(R.id.bottomSheetQn)
        bottomSheetBehaviorQn = BottomSheetBehavior.from(bottomSheetQn)
        bottomSheetBehaviorQn.peekHeight = 0
        bottomSheetBehaviorQn.state = BottomSheetBehavior.STATE_HIDDEN

        records = ArrayList()

        db = AppDatabase.getDatabase(this)

        mAdapter = QuestionaireAdapter(records, this)

        recyclerview.apply {
            adapter = mAdapter as QuestionaireAdapter
            layoutManager = LinearLayoutManager(context)
        }

        fetchAll()

        searchInput = findViewById(R.id.search_questionaire)
        searchInput.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var qn = p0.toString()
                searchDatabase(qn)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        btnClose.setOnClickListener {
            leaveEditMode()
            //bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        btnSelectAll.setOnClickListener {
            allChecked = !allChecked
            records.map { it.isChecked = allChecked }
            (mAdapter as QuestionaireAdapter).notifyDataSetChanged()

            if(allChecked){
                disableRename()
                enableDelete()
            }else {
                disableRename()
                disableDelete()
            }
        }

        addQn.setOnClickListener {
            //startActivity(Intent(this, AddQuestionaire::class.java))
            //dismiss()
            //save()
            bottomSheetBehaviorQn.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBGQn.visibility = View.VISIBLE
            //filenameInput.setText(qName)
            addQn.hide()
        }

        addQn.setOnLongClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/*"
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(intent, READ_REQUEST_CODE)
            true
        }

        btnCancelQn.setOnClickListener {
            //File("$qnName$qName.mp3").delete()
            dismiss()
            addQn.show()
        }

        btnOkQn.setOnClickListener {
            dismiss()
            save()
            addQn.show()
        }

        bottomSheetBGQn.setOnClickListener {
            //File("$qnName$qName.mp3").delete
            dismiss()
            addQn.show()
        }

        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete record?")
            val nbRecords = records.count{it.isChecked}
            builder.setMessage("Are you sure you want to delete $nbRecords record(s) ?")

            builder.setPositiveButton("Delete") {_, _ ->
                val toDelete = records.filter { it.isChecked }.toTypedArray()
                GlobalScope.launch {
                    db.questionaireDao().deleteQn(toDelete)
                    runOnUiThread {
                        records.removeAll(toDelete)
                        (mAdapter as QuestionaireAdapter).notifyDataSetChanged()
                        //dismiss()
                    }

                }

                for(record in toDelete){
                    val dir = File("${externalCacheDir?.absolutePath}/" + record.questionaire)
                    if (dir.isDirectory) {
                        val children: Array<String> = dir.list() as Array<String>
                        for (i in children.indices) {
                            File(dir, children[i]).delete()
                        }
                        dir.delete()
                    }

                    GlobalScope.launch {
                        var q_list = db.questionDao().getAllQwL(record.id)

                        db.questionDao().deleteQ(q_list)
                        runOnUiThread {
                            records.removeAll(toDelete)
                            (mAdapter as QuestionaireAdapter).notifyDataSetChanged()
                            //dismiss()
                        }

                    }
                }
                runOnUiThread {
                    leaveEditMode()
                }
            }

            builder.setNegativeButton("Cancel") {_, _ ->
                // it does nothing
            }

            val dialog = builder.create()
            dialog.show()
        }

        btnRename.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = this.layoutInflater.inflate(R.layout.rename_layout, null)
            builder.setView(dialogView)
            val dialog = builder.create()

            val record = records.filter { it.isChecked }.get(0)
            val textInput = dialogView.findViewById<TextInputEditText>(R.id.filenameInput)
            textInput.setText(record.questionaire)

            dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
                val input = textInput.text.toString()
                if(input.isEmpty()){
                    Toast.makeText(this, "A name is required", Toast.LENGTH_LONG).show()
                }else{
                    record.questionaire = input
                    GlobalScope.launch {
                        db.questionaireDao().updateQn(record)
                        runOnUiThread {
                            (mAdapter as QuestionaireAdapter).notifyItemChanged(records.indexOf(record))
                            dialog.dismiss()
                        }
                    }
                }
            }

            dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            runOnUiThread {
                leaveEditMode()
            }

            dialog.show()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data?.data != null) {
                val newUrl = copyToInternal(data.data!!)
                if (newUrl != null) {
                    readText(newUrl)
                }
            }
        }

    }

    @SuppressLint("Range")
    private fun copyToInternal(fileUri: Uri): String? {
        val theCursor: Cursor? = contentResolver.query(
            fileUri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )
        theCursor?.moveToFirst()

        val theFile = File("$filesDir/temp_data.txt")
        try {
            val fileOutputStream = FileOutputStream(theFile)
            val inputStream = contentResolver.openInputStream(fileUri)
            val buffers = ByteArray(1024)
            var read: Int
            while (inputStream!!.read(buffers).also { read = it } != -1) {
                fileOutputStream.write(buffers, 0, read)
            }
            inputStream.close()
            fileOutputStream.close()
            return theFile.path
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    private fun readText(uriPath: String) {
        val inputStream: InputStream = File(uriPath).inputStream()

        inputStream.bufferedReader().forEachLine {
            save(it)
        }
    }

    private fun save(addQnTxt: String = ""){
        var qnName = ""
        if((addQnInput.text.toString() != "" && addQnInput.text.toString().isNotEmpty()) && (addQnTxt == "" && addQnTxt.isEmpty()))
        {
            qnName = addQnInput.text.toString()
        } else if(addQnTxt != "" && addQnTxt.isNotEmpty())
        {
            qnName = addQnTxt
        }

        var timeStamp = Date().time

        if(qnName != "" && qnName.isNotEmpty()) {
            var record = Questionaire(qnName, timeStamp, 0, 0, 0)

            GlobalScope.launch {
                db.questionaireDao().insertQn(record)
            }
            try{
                val f = File("${externalCacheDir?.absolutePath}/", qnName)
                f.mkdir()
            }catch(e: IOException){}


        } else {
            Toast.makeText(this, "Questionaire name is required", Toast.LENGTH_LONG).show()
        }

    }

    private fun dismiss(){
        bottomSheetBGQn.visibility = View.GONE
        hideKeyboard(addQnInput)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehaviorQn.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 100)
    }
    private fun hideKeyboard(view: View){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun leaveEditMode () {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        editBar.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        //bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        records.map { it.isChecked = false }
        (mAdapter as QuestionaireAdapter).setEditMode(false)
    }

    private fun disableRename () {
        btnRename.isClickable = false
        btnRename.backgroundTintList = ResourcesCompat.getColorStateList(resources,
            R.color.grayDarkDisabled, theme)
        tvEdit.setTextColor(ResourcesCompat.getColorStateList(resources,
            R.color.grayDarkDisabled, theme))
    }
    private fun disableDelete () {
        btnDelete.isClickable = false
        btnDelete.backgroundTintList = ResourcesCompat.getColorStateList(resources,
            R.color.grayDarkDisabled, theme)
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources,
            R.color.grayDarkDisabled, theme))
    }

    private fun enableRename () {
        btnRename.isClickable = true
        btnRename.backgroundTintList = ResourcesCompat.getColorStateList(resources,
            R.color.grayDark, theme)
        tvEdit.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDark, theme))
    }
    private fun enableDelete () {
        btnDelete.isClickable = true
        btnDelete.backgroundTintList = ResourcesCompat.getColorStateList(resources,
            R.color.grayDark, theme)
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDark, theme))
    }

    private fun searchDatabase(q: String) {
        GlobalScope.launch {
            records.clear()
            val queryResult = db.questionaireDao().searchQnDB("%$q%")
            records.addAll(queryResult)

            runOnUiThread {
                (mAdapter as QuestionaireAdapter).notifyDataSetChanged()
            }

        }
    }


    private fun fetchAll(){

        /*
        GlobalScope.launch {
            records.clear()
            //var queryResult = db.questionaireDao().getAllQn()
            //records.addAll(queryResult)

            (mAdapter as QuestionaireAdapter).notifyDataSetChanged()
        }

        */

        val repo = repository(db)
        val fact = viewmodelfactory(repo)
        val model = ViewModelProvider(this,fact)[viewmodel::class.java]

        GlobalScope.launch {
            Handler(Looper.getMainLooper()).post {

                model.getQuestionaire().observe(this@QuestionaireActivity, Observer {
                        runOnUiThread {
                            records.clear()
                            ArrayAdapter(
                                this@QuestionaireActivity,
                                android.R.layout.activity_list_item,
                                it
                            )
                            records.addAll(it)
                            (mAdapter as QuestionaireAdapter).notifyDataSetChanged()
                        }
                })
            }
        }
    }

    override fun onItemClickListener(position: Int) {
        var qnRecord = records[position]

        if((mAdapter as QuestionaireAdapter).isEditMode()){
            records[position].isChecked = !records[position].isChecked
            (mAdapter as QuestionaireAdapter).notifyItemChanged(position)

            var nbSelected = records.count{it.isChecked}
            when(nbSelected){
                0 -> {
                    disableRename()
                    disableDelete()
                }
                1 -> {
                    enableDelete()
                    enableRename()
                }
                else -> {
                    disableRename()
                    enableDelete()
                }
            }
        }else{
            var intent = Intent(this, QuestionActivity::class.java)

            //intent.putExtra("filepath", audioRecord.filePath)
            //intent.putExtra("filename", audioRecord.questionaire)
            intent.putExtra("qnID", qnRecord.id)
            intent.putExtra("qnName", qnRecord.questionaire)
            startActivity(intent)
        }
    }

    override fun onItemLongClickListener(position: Int) {
        (mAdapter as QuestionaireAdapter).setEditMode(true)
        records[position].isChecked = !records[position].isChecked
        (mAdapter as QuestionaireAdapter).notifyItemChanged(position)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if((mAdapter as QuestionaireAdapter).isEditMode() && editBar.visibility == View.GONE){
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)

            editBar.visibility = View.VISIBLE

            enableDelete()
            enableRename()
        }
    }

}