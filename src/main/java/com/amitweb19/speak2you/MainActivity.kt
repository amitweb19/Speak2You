package com.amitweb19.speak2you

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.amitweb19.speak2you.R
import com.amitweb19.speak2you.data.Question
import com.amitweb19.speak2you.db.AppDatabase
import com.amitweb19.speak2you.db.AppDatabase.Companion.getDatabase
import com.amitweb19.speak2you.fragment.mistake.MistakeActivity
import com.amitweb19.speak2you.fragment.questionaire.QuestionaireActivity
import com.amitweb19.speak2you.model.repository
import com.amitweb19.speak2you.model.viewmodel
import com.amitweb19.speak2you.model.viewmodelfactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.util.*


const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener, TextToSpeech.OnInitListener, AdapterView.OnItemSelectedListener {

    private lateinit var amplitudes: ArrayList<Float>
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE, Manifest.permission.INTERNET)
    private var permissionGranted = false

    private lateinit var recorder: MediaRecorder

    private var dirPath = ""

    private var qnName = ""
    private var qName = ""
    private var qn_id: Int = 0
    private var q_id: Int = 0
    private var isRecording = false
    private var isPaused = false

    private var duration = ""

    private lateinit var vibrator: Vibrator

    private lateinit var timer: Timer

    private lateinit var db : AppDatabase

    private var questionaire_count = 0
    private var question_count = 0

    private lateinit var spinerModel: viewmodel

    private var tts: TextToSpeech? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var isSpeak: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionGranted = (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permissions[1]) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permissions[2]) == PackageManager.PERMISSION_GRANTED)

        if(!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        db = getDatabase(this)

        tts = TextToSpeech(this, this)


        GlobalScope.launch {
            questionaire_count = db.questionaireDao().getQnCount()
        }

        val repo = repository(db)
        val fact = viewmodelfactory(repo)
        spinerModel = ViewModelProvider(this,fact)[viewmodel::class.java]

        GlobalScope.launch {
            Handler(Looper.getMainLooper()).post {
                spinerModel.getQuestionaireSn().observe(this@MainActivity, Observer {
                    runOnUiThread {
                        val qnAdapter =
                            ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, it)
                        qnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        qnAdapter.notifyDataSetChanged()
                        qnSpinner.adapter = qnAdapter

                        if(intent.hasExtra("qn_name")){
                            val qniName = intent.getStringExtra("qn_name").toString()
                            qnSpinner.setSelection(qnAdapter.getPosition(qniName), true)
                        }
                    }
                })
            }
        }

        runOnUiThread {
            qnSpinner.onItemSelectedListener = this@MainActivity

        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        btnRecord.setOnClickListener {
            try {
                qnName = qnSpinner.selectedItem.toString()
            } catch (npe: NullPointerException) {
                qnName = ""
                qn_id = 0
            }

            try {
                qName = qSpinner.selectedItem.toString()
            } catch (npe: NullPointerException) {
                qName = ""
                q_id = 0
            }

            if (qn_id != 0 && qnName.isNotEmpty())
            {
                if (q_id != 0 && qName.isNotEmpty())
                {
                    when{
                        isPaused -> resumeRecorder()
                        isRecording -> pauseRecorder()
                        else -> startRecording()
                    }
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    q_id = 0
                    qName = ""
                    //db_q_id = false
                    Toast.makeText(this, "Please Pick a Question!", Toast.LENGTH_SHORT).show()
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                qn_id = 0
                qnName = ""
                //db_qn_id = false
                Toast.makeText(this, "Please Pick a Category!", Toast.LENGTH_SHORT).show()
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }

        btnList.setOnClickListener {
            startActivity(Intent(this, QuestionaireActivity::class.java))
        }

        btnList.setOnLongClickListener {
            startActivity(Intent(this, MistakeActivity::class.java))
            true
        }

        btnDone.setOnClickListener {
            stopRecorder()
            Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBG.visibility = View.VISIBLE
            filenameInput.setText(qName)
        }

        btnCancel.setOnClickListener {
            File("$qnName/$qName"+"_tmp.mp3").delete()
            dismiss()
        }

        btnOk.setOnClickListener {
            dismiss()
            save()
        }

        bottomSheetBG.setOnClickListener {
            File("$qnName/$qName"+"_tmp.mp3").delete()
            dismiss()
        }

        btnDelete.setOnClickListener {
            when{
                isPaused -> deleteRecording()
                isRecording -> deleteRecording()
                else -> randomGenerate(false)
            }
        }

        btnDelete.setOnLongClickListener{
            when{
                isPaused -> deleteRecording()
                isRecording -> deleteRecording()
                else -> randomGenerate(true)
            }
            true
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "The Language not supported!", Toast.LENGTH_SHORT).show()
            } else {
                isSpeak = true
            }
        }
    }

    private fun save(){
        val newFilename = filenameInput.text.toString()
        if(newFilename != qName){
            var newFile = File("$dirPath$qnName/$newFilename.mp3")
            try{
                newFile.mkdirs()
            }catch(e:IOException){}
            File("$dirPath$qnName/$qName.mp3").renameTo(newFile)
        }

        var filePath = "$dirPath$qnName/$newFilename.mp3"

        val from = File("$dirPath$qnName/$qName"+"_tmp.mp3")
        val to = File("$dirPath$qnName/$qName.mp3")
        if (from.exists())
        {
            if(to.exists())
            {
                to.delete()
                from.renameTo(to)
            } else {
                from.renameTo(to)
            }
        }
        var timestamp = Date().time
        var ampsPath = "$dirPath$qnName/$newFilename"

        try{
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        }catch (e :IOException){}

        GlobalScope.launch {
            val isRec:Boolean = db.questionDao().isRecord(q_id)
            if(!isRec){
                GlobalScope.launch {
                    val qn = db.questionaireDao().selectQn(qn_id)

                    qn.ecount = qn.ecount - 1
                    qn.rcount = qn.rcount + 1
                    db.questionaireDao().updateQn(qn)
                }

            }
        }

        var record = Question(q_id, newFilename, filePath, timestamp, duration, ampsPath, "", qn_id, true)

        GlobalScope.launch {
            db.questionDao().updateQ(record)
        }
    }

    private fun dismiss(){
        bottomSheetBG.visibility = View.GONE
        hideKeyboard(filenameInput)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 100)
    }
    private fun hideKeyboard(view: View){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE)
            permissionGranted = (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)
    }

    private fun pauseRecorder(){
        recorder.pause()
        isPaused = true
        btnRecord.setImageResource(R.drawable.ic_play)

        timer.pause()
    }

    private fun deleteRecording() {
        stopRecorder()
        File("$qnName/$qName"+"_tmp.mp3").delete()
        Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()
    }

    private fun randomGenerate(rmode: Boolean) {
        if(questionaire_count != 0) {
            if(rmode){
                qnSpinner.setSelection((0 until questionaire_count).random(), true)
            }
            try {
                val qnS: String = qnSpinner.selectedItem.toString()
                GlobalScope.launch {
                    qn_id = spinerModel.getQnID(qnS)
                }
                qnName = qnS
            } catch (npe: NullPointerException) {
                qn_id = 0
                qnName = ""
            }
            GlobalScope.launch {
                question_count = db.questionDao().getQCount(qn_id)

            }
            if(question_count != 0)
            {
                qSpinner.setSelection((0 until question_count).random(), true)
                try {
                    val qS: String = qSpinner.selectedItem.toString()
                    GlobalScope.launch {
                        q_id = spinerModel.getQID(qS)
                    }
                    qName = qS
                } catch (npe: NullPointerException) {
                    q_id = 0
                    qName = ""
                }
            }
        }

    }

    private fun resumeRecorder(){
        recorder.resume()
        isPaused = false
        btnRecord.setImageResource(R.drawable.ic_pause)
        timer.start()
    }

    private fun startRecording(){
        if(!permissionGranted){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }

        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"


        speakOut(qName)

        recorder.apply{
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$qnName/$qName"+"_tmp.mp3")
            try {
                prepare()
            }catch (e: IOException){ }
            start()
        }

        btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPaused = false

        timer.start()

        btnDelete.isClickable = true
        btnDelete.setImageResource(R.drawable.ic_delete)

        btnList.visibility = View.GONE
        btnDone.visibility = View.VISIBLE
    }

    private fun stopRecorder(){
        timer.stop()

        recorder.apply {
            stop()
            release()
        }

        isPaused = false
        isRecording = false

        btnList.visibility = View.VISIBLE
        btnDone.visibility = View.GONE

        //btnDelete.isClickable = false
        btnDelete.setImageResource(R.drawable.ic_random)

        btnRecord.setImageResource(R.drawable.ic_mic)
        //btnRecord.setBackgroundColor(R.drawable.ic_record)

        tvTimer.text = "00:00.00"
        amplitudes = waveformView.clear()

    }

    override fun onTimerTick(duration: String) {
        tvTimer.text = duration
        this.duration = duration.dropLast(3)
        waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
    }

    public override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()

        //recorder.stop()
        //recorder.release()
    }

    private fun speakOut(txtSpeak: String) {
        tts!!.speak(txtSpeak, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if(view == qnSpinner.selectedView){
            qnName = qnSpinner.selectedItem.toString()

            GlobalScope.launch {
                qn_id = spinerModel.getQnID(qnName)
                Handler(Looper.getMainLooper()).post {

                    spinerModel.getQuestionSn(qn_id).observe(this@MainActivity, Observer {
                        runOnUiThread {
                            val qAdapter =
                                ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, it)
                            qAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            qAdapter.notifyDataSetChanged()
                            qSpinner.adapter = qAdapter
                            if(intent.hasExtra("q_name")){
                                val qiName = intent.getStringExtra("q_name").toString()
                                qSpinner.setSelection(qAdapter.getPosition(qiName), true)
                            }
                        }
                    })
                    runOnUiThread {
                        qSpinner.onItemSelectedListener = this@MainActivity
                    }
                }
                GlobalScope.launch {
                    question_count = db.questionDao().getQCount(qn_id)
                }
            }
        }

        if (view == qnSpinner.selectedView){
            qnName = qnSpinner.selectedItem.toString()

            GlobalScope.launch {
                qn_id = spinerModel.getQnID(qnName)
            }
        }

        if (view == qSpinner.selectedView){
            qName = qSpinner.selectedItem.toString()

            GlobalScope.launch {
                q_id = spinerModel.getQID(qName)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        qn_id = 0
        q_id = 0
        qnName = ""
        qName = ""
    }
}
