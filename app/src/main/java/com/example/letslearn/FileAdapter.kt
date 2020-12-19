package com.example.letslearn

import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_file.view.*

class FileAdapter(
    private val files : ArrayList<File>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>(){




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_file,
            parent,
            false
        )
        return FileViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.itemViewFileName.text = files[position].name
        holder.itemViewUploadedBy.text = files[position].uploadedBy
    }

    inner class FileViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView),View.OnClickListener{
        val itemViewFileName :TextView = itemView.itemViewFileName
        val itemViewUploadedBy :TextView = itemView.itemViewUploadedBy

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION){
                listener.onItemClick(pos)
            }

        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

}