package com.example.letslearn

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        val fileAdapter = FileAdapter(files,this)
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
                    val file = File(nameOfFile, url, uploadedBy)
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
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(files[position].url))
        startActivity(i)
    }
}