package com.amitweb19.speak2you.fragment.question

import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.amitweb19.speak2you.OnItemClickListener
import com.amitweb19.speak2you.R
import com.amitweb19.speak2you.data.Question
import java.text.SimpleDateFormat
import java.util.*

class QuestionAdapter(var records : ArrayList<Question>, var listener: OnItemClickListener) : RecyclerView.Adapter<QuestionAdapter.ViewHolder>(),
    Adapter {

    private var editMode = false

    fun isEditMode(): Boolean { return editMode}
    fun setEditMode(mode: Boolean) {
        if(editMode != mode){
            editMode = mode
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener{
        var adQus : LinearLayout = itemView.findViewById(R.id.adQus)
        var adQusIcn : ImageView = itemView.findViewById(R.id.adQusIcn)
        var tvFilename : TextView = itemView.findViewById(R.id.tvFilename)
        var tvMeta : TextView = itemView.findViewById(R.id.tvMeta)
        var checkbox : CheckBox = itemView.findViewById(R.id.checkbox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
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
        val view= LayoutInflater.from(parent.context).inflate(R.layout.questionview_layout, parent, false)
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

            holder.tvFilename.text = record.question
            holder.tvMeta.text = "${record.duration } $strDate"

            if(record.isRecord)
            {
                holder.adQus.setBackgroundResource(R.drawable.selected_gradient_bg)
                holder.adQusIcn.setImageResource(R.drawable.ic_play)
                holder.adQusIcn.setBackgroundResource(R.drawable.ic_circle)

            }

            if(editMode){
                holder.checkbox.visibility = View.VISIBLE
                holder.checkbox.isChecked = record.isChecked
            }else{
                holder.checkbox.visibility = View.GONE
                holder.checkbox.isChecked = false
            }
        }
    }

    fun setData(question: ArrayList<Question>){
        this.records = question
        notifyDataSetChanged()
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
        //TODO("Not yet implemented")
        return false
    }
}