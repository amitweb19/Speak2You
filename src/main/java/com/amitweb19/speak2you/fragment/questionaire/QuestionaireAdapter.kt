package com.amitweb19.speak2you.fragment.questionaire

import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter

import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amitweb19.speak2you.OnItemClickListener
import com.amitweb19.speak2you.R
import com.amitweb19.speak2you.data.Questionaire
import java.text.SimpleDateFormat
import java.util.*

class QuestionaireAdapter(var records : ArrayList<Questionaire>, var listener: OnItemClickListener) : RecyclerView.Adapter<QuestionaireAdapter.ViewHolder>(),
    Adapter {

    private var editMode = false

    fun isEditMode(): Boolean { return editMode}
    fun setEditMode(mode: Boolean) {
        if(editMode != mode){
            editMode = mode
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(questionaireview: View) : RecyclerView.ViewHolder(questionaireview), View.OnClickListener, View.OnLongClickListener{
        var tvFilename : TextView = questionaireview.findViewById(R.id.tvFilename)
        var tvMeta : TextView = questionaireview.findViewById(R.id.tvMeta)
        var checkbox : CheckBox = questionaireview.findViewById(R.id.checkbox)

        init {
            questionaireview.setOnClickListener(this)
            questionaireview.setOnLongClickListener(this)
        }
        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION)
                listener.onItemClickListener(position)
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION)
                listener.onItemLongClickListener(position)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.questionaireview_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if(position != RecyclerView.NO_POSITION){
            var record = records[position]

            var sdf = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timestamp)
            var strDate = sdf.format(date)

            holder.tvFilename.text = record.questionaire
            holder.tvMeta.text = "$strDate | T: ${record.tcount } | R: ${record.rcount } | E: ${record.ecount } "

            if(editMode){
                holder.checkbox.visibility = View.VISIBLE
                holder.checkbox.isChecked = record.isChecked
            }else{
                holder.checkbox.visibility = View.GONE
                holder.checkbox.isChecked = false
            }
        }
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        //TODO("Not yet implemented")
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        //TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        //TODO("Not yet implemented")
        return 0
    }

    override fun getItem(position: Int): Any {
        //TODO("Not yet implemented")
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): ViewGroup? {
        //TODO("Not yet implemented")
        return parent
    }

    override fun getViewTypeCount(): Int {
        //TODO("Not yet implemented")
        return 0
    }

    override fun isEmpty(): Boolean {
        //TODO
        return false
    }
}