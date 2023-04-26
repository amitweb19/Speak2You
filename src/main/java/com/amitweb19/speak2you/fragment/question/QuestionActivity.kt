package com.amitweb19.speak2you.fragment.question

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
import com.amitweb19.speak2you.*
import com.amitweb19.speak2you.data.Question
import com.amitweb19.speak2you.data.Questionaire
import com.amitweb19.speak2you.db.AppDatabase
import com.amitweb19.speak2you.model.repository
import com.amitweb19.speak2you.model.viewmodel
import com.amitweb19.speak2you.model.viewmodelfactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_question.*
import kotlinx.android.synthetic.main.bottom_sheet_q.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


class QuestionActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var records : ArrayList<Question>
    private lateinit var mAdapter : Adapter
    private lateinit var db : AppDatabase

    private var allChecked = false

    private lateinit var toolbar: MaterialToolbar

    private lateinit var editBar: View
    private lateinit var btnClose: ImageButton
    private lateinit var btnSelectAll: ImageButton
    private var qn_ID: Int = 0
    private var qn_Name: String = ""
    private lateinit var searchInput : TextInputEditText
    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var bottomSheetQ: LinearLayout
    private lateinit var bottomSheetBehaviorQ: BottomSheetBehavior<LinearLayout>

    private lateinit var btnRename : ImageButton
    private lateinit var btnDelete : ImageButton
    private lateinit var tvRename : TextView
    private lateinit var tvDelete : TextView

    private var READ_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        qn_ID = intent.getIntExtra("qnID", 0)
        qn_Name = intent.getStringExtra("qnName").toString()


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

        bottomSheet = findViewById(R.id.bottomSheetItemQ)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetQ = findViewById(R.id.bottomSheetQ)
        bottomSheetBehaviorQ = BottomSheetBehavior.from(bottomSheetQ)
        bottomSheetBehaviorQ.peekHeight = 0
        bottomSheetBehaviorQ.state = BottomSheetBehavior.STATE_HIDDEN

        records = ArrayList()

        db = AppDatabase.getDatabase(this)

        mAdapter = QuestionAdapter(records, this)

        recyclerview.apply {
            adapter = mAdapter as QuestionAdapter
            layoutManager = LinearLayoutManager(context)
        }

        fetchAll(qn_ID)

        searchInput = findViewById(R.id.search_question)
        searchInput.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val query = p0.toString()
                searchDatabase(query, qn_ID)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        toolbar.title = qn_Name

        btnClose.setOnClickListener {
            leaveEditMode()
        }

        btnSelectAll.setOnClickListener {
            allChecked = !allChecked
            records.map { it.isChecked = allChecked }
            (mAdapter as QuestionAdapter).notifyDataSetChanged()

            if(allChecked){
                disableRename()
                enableDelete()
            }else {
                disableRename()
                disableDelete()
            }
        }

        addQ.setOnClickListener {
            bottomSheetBehaviorQ.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBGQ.visibility = View.VISIBLE
            addQ.hide()
        }

        addQ.setOnLongClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/*"
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(intent, READ_REQUEST_CODE)
            true
        }

        btnCancelQ.setOnClickListener {
            dismiss()
            addQ.show()
        }

        btnOkQ.setOnClickListener {
            dismiss()
            save(qn_ID)
            addQ.show()
        }

        bottomSheetBGQ.setOnClickListener {
            dismiss()
            addQ.show()
        }

        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete record?")
            val nbRecords = records.count{it.isChecked}

            builder.setMessage("Are you sure you want to delete $nbRecords record(s) ?")

            builder.setPositiveButton("Delete") {_, _ ->
                val toDelete = records.filter { it.isChecked }.toTypedArray()
                var rCount = 0
                var eCount = 0
                for(rec in toDelete) {
                    val fl = rec.filePath
                    if(fl != "")
                    {
                        rCount += 1
                    } else {
                        eCount += 1
                    }
                }

                GlobalScope.launch {
                    val qn = db.questionaireDao().selectQn(qn_ID)

                    qn.tcount = qn.tcount - nbRecords
                    qn.ecount = qn.ecount - eCount
                    qn.rcount = qn.rcount - rCount

                    GlobalScope.launch {
                        db.questionaireDao().updateQn(qn)
                    }
                }

                GlobalScope.launch {
                    db.questionDao().deleteQ(toDelete)
                    runOnUiThread {
                        records.removeAll(toDelete)
                        (mAdapter as QuestionAdapter).notifyDataSetChanged()
                        //leaveEditMode()
                    }
                }

                for(record in toDelete) {
                    val fl = File(record.filePath)
                    if (fl.isFile) {
                        fl.delete()
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
            textInput.setText(record.question)

            dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
                val input = textInput.text.toString()
                if(input.isEmpty()){
                    Toast.makeText(this, "A name is required", Toast.LENGTH_LONG).show()
                }else{
                    record.question = input
                    GlobalScope.launch {
                        db.questionDao().updateQ(record)
                        runOnUiThread {
                            (mAdapter as QuestionAdapter).notifyItemChanged(records.indexOf(record))
                            dialog.dismiss()
                            //leaveEditMode()
                        }
                    }
                }
            }
            runOnUiThread {
                leaveEditMode()
            }
            dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
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
                    readTextQ(newUrl)
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


    private fun readTextQ(uriPath: String) {
        val inputStream: InputStream = File(uriPath).inputStream()
        val ctr = Files.readAllLines(Paths.get(uriPath)).size
        inputStream.bufferedReader().forEachLine {
            save(qn_ID, it)
        }
        increase_qn_count(ctr)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun save(qn_ID: Int, addQTxt: String = ""){
        var qName = ""
        if((addQInput.text.toString() != "" || addQInput.text.toString().isNotEmpty()) && (addQTxt == "" || addQTxt.isEmpty()))
        {
            qName = addQInput.text.toString()
            increase_qn_count(1)
        } else if(addQTxt != "" && addQTxt.isNotEmpty())
        {
            qName = addQTxt
        }
        val filepath = ""
        val timeStamp = Date().time
        val duration = ""
        val ampsPath = ""
        val sToT = ""

        if(qName != "" && qName.isNotEmpty()) {
            val record = Question(0, qName, filepath, timeStamp, duration, ampsPath, sToT, qn_ID)
            GlobalScope.launch {
                db.questionDao().insertQ(record)
            }
            runOnUiThread {
                (mAdapter as QuestionAdapter).notifyItemChanged(records.indexOf(record))
            }

        } else {
            Toast.makeText(this, "Question name is required", Toast.LENGTH_LONG).show()
        }

    }

    private fun increase_qn_count(ctr: Int)
    {   GlobalScope.launch {
            val qn: Questionaire = db.questionaireDao().selectQn(qn_ID)
            qn.tcount = qn.tcount + ctr
            qn.ecount = qn.ecount + ctr
            db.questionaireDao().updateQn(qn)
        }
    }

    private fun dismiss(){
        bottomSheetBGQ.visibility = View.GONE
        hideKeyboard(addQInput)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehaviorQ.state = BottomSheetBehavior.STATE_COLLAPSED
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
        (mAdapter as QuestionAdapter).setEditMode(false)
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

    private fun searchDatabase(query: String, qnID: Int) {
        GlobalScope.launch {
            records.clear()
            var queryResult = db.questionDao().searchQDB("%$query%", qnID)
            records.addAll(queryResult)

            runOnUiThread {
                (mAdapter as QuestionAdapter).notifyDataSetChanged()
            }

        }
    }


    private fun fetchAll(qnID: Int){

        val repo = repository(db)
        val fact = viewmodelfactory(repo)
        val model = ViewModelProvider(this,fact)[viewmodel::class.java]

        GlobalScope.launch {
            Handler(Looper.getMainLooper()).post {

                model.getQuestion(qnID).observe(this@QuestionActivity, Observer {
                    runOnUiThread {
                        records.clear()
                        ArrayAdapter(
                            this@QuestionActivity,
                            android.R.layout.activity_list_item,
                            it
                        )
                        records.addAll(it)
                        (mAdapter as QuestionAdapter).notifyDataSetChanged()
                    }
                })
            }
        }
    }



    override fun onItemClickListener(position: Int) {
        var questionRecord = records[position]

        if((mAdapter as QuestionAdapter).isEditMode()){
            records[position].isChecked = !records[position].isChecked
            (mAdapter as QuestionAdapter).notifyItemChanged(position)

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
        } else if(questionRecord.isRecord) {
            var intent = Intent(this, QuestionPlayerActivity::class.java)
            intent.putExtra("filepath", questionRecord.filePath)
            intent.putExtra("filename", questionRecord.question)
            startActivity(intent)
        }else{
            //Toast.makeText(this, "Question has no recording available yet!", Toast.LENGTH_LONG).show()
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("qn_name", qn_Name)
            intent.putExtra("q_name", questionRecord.question)
            startActivity(intent)
        }
    }

    override fun onItemLongClickListener(position: Int) {
        (mAdapter as QuestionAdapter).setEditMode(true)
        records[position].isChecked = !records[position].isChecked
        (mAdapter as QuestionAdapter).notifyItemChanged(position)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if((mAdapter as QuestionAdapter).isEditMode() && editBar.visibility == View.GONE){
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)

            editBar.visibility = View.VISIBLE

            enableDelete()
            enableRename()
        }
    }

}