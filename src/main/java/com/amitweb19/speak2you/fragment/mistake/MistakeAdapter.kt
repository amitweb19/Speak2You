package com.amitweb19.speak2you.fragment.mistake

import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amitweb19.speak2you.R
import com.amitweb19.speak2you.data.Mistake
import java.util.*

class MistakeAdapter(var records : ArrayList<Mistake>) : RecyclerView.Adapter<MistakeAdapter.ViewHolder>(),
    Adapter {

    inner class ViewHolder(mistakeview: View) : RecyclerView.ViewHolder(mistakeview){
        var tvFilename : TextView = mistakeview.findViewById(R.id.tvFilename)
        var tvMeta : TextView = mistakeview.findViewById(R.id.tvMeta)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.mistakeview_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if(position != RecyclerView.NO_POSITION){
            var record = records[position]


            holder.tvFilename.text = record.mistake
            holder.tvMeta.text = record.mistake
            holder.tvMeta.text = "Count: ${record.mcount } | Correction: ${record.correction }"
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