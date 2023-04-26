package com.amitweb19.speak2you.fragment.mistake

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.amitweb19.speak2you.R
import com.amitweb19.speak2you.data.Mistake
import com.amitweb19.speak2you.db.AppDatabase
import com.amitweb19.speak2you.model.repository
import com.amitweb19.speak2you.model.viewmodel
import com.amitweb19.speak2you.model.viewmodelfactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_questionaire.recyclerview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class MistakeActivity : AppCompatActivity() {

    private lateinit var records : ArrayList<Mistake>
    private lateinit var mAdapter : Adapter
    private lateinit var db : AppDatabase

    private lateinit var toolbar: MaterialToolbar


    private lateinit var searchInput : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mistake)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        records = ArrayList()

        db = AppDatabase.getDatabase(this)

        mAdapter = MistakeAdapter(records)

        recyclerview.apply {
            adapter = mAdapter as MistakeAdapter
            layoutManager = LinearLayoutManager(context)
        }

        fetchAll()

        searchInput = findViewById(R.id.search_mistake)
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
    }

    private fun searchDatabase(q: String) {
        GlobalScope.launch {
            records.clear()
            val queryResult = db.mistakeDao().searchMDB("%$q%")
            records.addAll(queryResult)

            runOnUiThread {
                (mAdapter as MistakeAdapter).notifyDataSetChanged()
            }

        }
    }


    private fun fetchAll(){
        val repo = repository(db)
        val fact = viewmodelfactory(repo)
        val model = ViewModelProvider(this,fact)[viewmodel::class.java]

        GlobalScope.launch {
            Handler(Looper.getMainLooper()).post {

                model.getAllMistake().observe(this@MistakeActivity, Observer {
                    runOnUiThread {
                        records.clear()
                        ArrayAdapter(
                            this@MistakeActivity,
                            android.R.layout.activity_list_item,
                            it
                        )
                        records.addAll(it)
                        (mAdapter as MistakeAdapter).notifyDataSetChanged()
                    }
                })
            }
        }
    }

}