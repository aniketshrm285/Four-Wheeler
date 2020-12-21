package com.example.letslearn

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.view.Menu
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_view_file_image.*
import kotlinx.android.synthetic.main.activity_view_file_video.*
import java.net.URLEncoder

class ViewFileActivity : AppCompatActivity() {
    private val fileUrl by lazy {
        intent.getStringExtra("fileUrl")
    }

    private val fileName by lazy {
        intent.getStringExtra("fileName")
    }

    private val mimeType by lazy {
        intent.getStringExtra("mimeType")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isImage = intent.getBooleanExtra("isImage",true)
        if(isImage){
            setContentView(R.layout.activity_view_file_image)
            setSupportActionBar(toolbarViewFileActivity as Toolbar?)

        }
        else {
            setContentView(R.layout.activity_view_file_video)
            setSupportActionBar(toolbarViewFileActivityVideo as Toolbar?)
            //title = fileName
        }
        title = fileName



        if(isImage){
            Picasso.get().load(fileUrl).into(fileImageView);
        }
        else{

            val mediaController = MediaController(this)
            videoView.setMediaController(mediaController)
            mediaController.setAnchorView(videoView)

            videoView.setVideoURI(Uri.parse(fileUrl))
            videoView.setOnPreparedListener {
                videoView.start()
            }
        }
    }

    private fun downloadFile(context: Context, destinationDirectory:String){
        val downloadManager : DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(fileUrl)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val mime = MimeTypeMap.getSingleton()
        val extType = mime.getExtensionFromMimeType(mimeType)

        request.setDestinationInExternalFilesDir(context,destinationDirectory, "$fileName.$extType")
        downloadManager.enqueue(request)
        Toast.makeText(context, "File Downloading..", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.download_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.downloadFile){
            downloadFile(this@ViewFileActivity,DIRECTORY_DOWNLOADS)
        }
        return super.onOptionsItemSelected(item)
    }
}