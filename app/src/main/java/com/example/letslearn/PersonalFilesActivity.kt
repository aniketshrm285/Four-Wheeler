package com.example.letslearn

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_personal_files.*
import java.util.ArrayList

class PersonalFilesActivity : AppCompatActivity(),FileAdapter.OnItemClickListener {
    private val auth by lazy{
        Firebase.auth
    }

    private val db by lazy {
        Firebase.database.reference
    }

    private val files by lazy{
        arrayListOf<File>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_files)
        setSupportActionBar(toolbarPersonalActivity as Toolbar?)



        val fileAdapter = FileAdapter(files,this,this)
        rvFiles.layoutManager = LinearLayoutManager(this)
        rvFiles.adapter =fileAdapter


        val isPrivate = intent.getBooleanExtra("isPrivate",false)

        title =if(isPrivate){
            "My Private Files"
        }
        else{
            "My Public Files"
        }

        val ref = if(isPrivate){
            db.child("privateFiles").child(auth.uid!!)
        } else{
            db.child("publicFiles").child(auth.uid!!)
        }
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                files.clear()
                for (snap in snapshot.children) {
                    val nameOfFile = snap.child("name").value.toString()
                    val uploadedBy: String = snap.child("uploadedBy").value.toString()
                    val url: String = snap.child("url").value.toString()
                    val mimeType = snap.child("mimeType").value.toString()
                    val file = File(nameOfFile, url, uploadedBy,mimeType)
                    files.add(file)

                }
                fileAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PersonalFilesActivity, "Please check your internet connection.", Toast.LENGTH_SHORT).show()
            }

        })


    }

    override fun onItemClick(position: Int) {
        val type= files[position].mimeType
        val i = Intent(this,ViewFileActivity::class.java)

        i.putExtra("fileName",files[position].name)
        i.putExtra("fileUrl",files[position].url)
        i.putExtra("mimeType",files[position].mimeType)
        if(type.startsWith("image")){
            i.putExtra("isImage",true)
            startActivity(i)
        }
        else if(type.startsWith("video")){
            i.putExtra("isImage",false)
            startActivity(i)
        }
        else{
            downloadFile(this,files[position], Environment.DIRECTORY_DOWNLOADS)
        }
    }
    private fun downloadFile(context: Context, file : File, destinationDirectory:String){
        val downloadManager : DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(file.url)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val mime = MimeTypeMap.getSingleton()
        val extType = mime.getExtensionFromMimeType(file.mimeType)
        var finalFileName:String = file.name
        if(!finalFileName.endsWith(extType!!)){
            finalFileName+=".$extType"
        }

        request.setDestinationInExternalFilesDir(context,destinationDirectory,finalFileName)
        downloadManager.enqueue(request)
        Toast.makeText(context, "File Downloading..", Toast.LENGTH_SHORT).show()
    }
}