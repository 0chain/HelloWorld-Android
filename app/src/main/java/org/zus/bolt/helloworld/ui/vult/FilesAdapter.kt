package org.zus.bolt.helloworld.ui.vult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.models.vult.FileModel
import org.zus.bolt.helloworld.utils.Utils.Companion.getConvertedSize

class FilesAdapter(
    var files: List<FileModel>,
    private val onFileClickListener: FileClickListener,
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val fileName: TextView = view.findViewById(R.id.tvFolderName)
        val ivIcon: ImageView = view.findViewById(R.id.ivFileIcon)
        val downloadProgress: ProgressBar = view.findViewById(R.id.uploadsProgressBar)
        val fileSize: TextView = view.findViewById(R.id.textSize)
        val downloadButton: ImageView = view.findViewById(R.id.ivDownloadFile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_view_files, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fileName.text = files[position].name
        holder.fileSize.text = files[position].size.getConvertedSize()
        if (files[position].mimetype.contains("image"))
            holder.ivIcon.setImageResource(R.drawable.ic_upload_image)
        else
            holder.ivIcon.setImageResource(R.drawable.ic_upload_document)
        holder.downloadButton.setOnClickListener {
            onFileClickListener.onDownloadFileClick(position)
        }
        holder.itemView.setOnClickListener {
            onFileClickListener.onFileClick(position)
        }
    }

    override fun getItemCount(): Int {
        return files.size
    }
}

interface FileClickListener {
    fun onDownloadFileClick(filePosition: Int)
    fun onFileClick(filePosition: Int)
}
