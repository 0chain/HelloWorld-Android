package org.zus.helloworld.ui.vult

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.zus.helloworld.R
import org.zus.helloworld.data.Files
import org.zus.helloworld.utils.Utils.Companion.getConvertedSize
import java.io.File
import java.util.*

class FilesAdapter(
    var files: MutableList<Files>,
    private val onFileClickListener: FileClickListener,
    private val context: Context?,
    private var thumbnailDownloadCallback: ThumbnailDownloadCallback? = null,
    private var selectedFiles: Set<String> = HashSet(),
    private var isMultiSelectEnabled: Boolean = false


) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {
    fun setThumbnailDownloadCallback(thumbnailDownloadCallback: ThumbnailDownloadCallback?) {
        this.thumbnailDownloadCallback = thumbnailDownloadCallback
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val fileName: TextView = view.findViewById(R.id.tvFolderName)
        val ivIcon: ImageView = view.findViewById(R.id.ivFileIcon)
        val downloadProgress: ProgressBar = view.findViewById(R.id.uploadsProgressBar)
        val fileSize: TextView = view.findViewById(R.id.textSize)
        val downloadButton: ImageView = view.findViewById(R.id.ivDownloadFile)
        val checkBox: AppCompatImageButton = view.findViewById(R.id.check_box)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_view_files, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentFile = files[position]
        holder.fileName.text = currentFile.name
        if (currentFile.getUploadStatus() == Files.STATUS_COMPLETED) {
            if (currentFile.actualFileSize == 0L)
                holder.fileSize.text = currentFile.totalSize.getConvertedSize()
            else holder.fileSize.text = currentFile.actualFileSize.getConvertedSize()
        } else {
            val uploadPercentage: Double =
                if (currentFile.totalSize == 0L) 0.0 else currentFile.uploadedBytes * 100.0 / currentFile.totalSize
            holder.fileSize.text = String.format(
                Locale.getDefault(),
                "%.0f%%",
                uploadPercentage
            )
        }
        if (currentFile.mimeType?.contains("image") == true || currentFile.mimeType?.contains("video") == true) {
            if (currentFile.thumbnailPath != null && File(currentFile.thumbnailPath).exists()) {
                val fileUri = context?.let {
                    FileProvider.getUriForFile(
                        it,
                        context.getPackageName() + ".files.provider",
                        File(currentFile.thumbnailPath)
                    )
                }
                context?.let {
                    Glide.with(it)
                        .load(fileUri)
                        .apply(
                            RequestOptions().placeholder(R.drawable.ic_upload_image)
                                .error(R.drawable.ic_upload_image)
                        )
                        .into(holder.ivIcon)
                }
            } else {
                thumbnailDownloadCallback!!.downloadThumbnail(currentFile, position)
            }
        } else
            holder.ivIcon.setImageResource(R.drawable.ic_upload_document)
        holder.downloadButton.setOnClickListener {
            onFileClickListener.onDownloadFileClickListener(position)
        }
        holder.itemView.setOnClickListener {
            onFileClickListener.onFileClick(position)
        }
        holder.checkBox.setOnClickListener {
            onFileClickListener.onFileMultiSelectClick(position)
        }
        if(isMultiSelectEnabled){
            if (selectedFiles.contains(currentFile.name)) {
                holder.checkBox.setImageResource(R.drawable.check_box_selected)
            } else {
                holder.checkBox.setImageResource(R.drawable.check_box_unselected)
            }
            holder.checkBox.visibility = View.VISIBLE
        } else{
            holder.checkBox.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return files.size
    }
    fun setMultiSelect(isMultiSelectEnabled: Boolean) {
        this.isMultiSelectEnabled = isMultiSelectEnabled
    }

    fun setSelectedFiles(files: Set<String>) {
        this.selectedFiles = files
    }
}

interface ThumbnailDownloadCallback {
    fun downloadThumbnail(file: Files, position: Int)
}

interface FileClickListener {
    fun onShareLongPressFileClickListener(position: Int)
    fun onDownloadToOpenFileClickListener(filePosition: Int)
    fun onFileClick(filePosition: Int)
    fun onDownloadFileClickListener(position: Int)
    fun onFileMultiSelectClick(position: Int)
}
