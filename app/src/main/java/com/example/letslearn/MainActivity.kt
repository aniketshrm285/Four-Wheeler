package com.example.letslearn

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList
const val TAG = "FILEPATH"
class MainActivity : AppCompatActivity(),FileAdapter.OnItemClickListener {
    private val storage by lazy {
        Firebase.storage
    }
    private val storageRef by lazy{
        storage.reference
    }
    private val auth by lazy{
        Firebase.auth
    }
    private val firestore by lazy {
        Firebase.firestore
    }

    private val db by lazy {
        Firebase.database.reference
    }

    private val files by lazy{
        arrayListOf<File>()
    }

    private lateinit var name:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar as Toolbar?)
        title = "All Files"
        val fileAdapter = FileAdapter(files,this)
        filesRV.layoutManager = LinearLayoutManager(this)
        filesRV.adapter =fileAdapter

        uploadBtn.setOnClickListener {
            checkPermissionForFile()
        }
        findNameOfCurrentUser()

        db.child("allPublicFiles")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    files.clear()
                    for(snap in snapshot.children){
                        val nameOfFile = snap.child("name").value.toString()
                        val uploadedBy:String = snap.child("uploadedBy").value.toString()
                        val url:String = snap.child("url").value.toString()
                        val file = File(nameOfFile,url,uploadedBy)
                        files.add(file)

                    }
                    fileAdapter.notifyDataSetChanged()
                }

            })

    }

    private fun findNameOfCurrentUser() {
        name = "UNKNOWN"
        val docRef = firestore.collection("users").document(auth.uid!!)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val currentUser = documentSnapshot.toObject<User>()
                name = currentUser!!.name
                //Log.d(TAG, "findNameOfCurrentUser: "+name)

            }
            .addOnFailureListener {
                name= "UNKNOWN"
            }
        //Log.d(TAG, "findNameOfCurrentUser: $name")
    }

    private fun checkPermissionForFile() {
        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED))
         {
            val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

            requestPermissions(
                permission,
                1001
            ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
        } else {
            pickFile()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1001){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickFile()
            }
            else{
                Toast.makeText(this, "Please grant permissions.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(
            intent,
            1000
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            data?.data?.let {
                MaterialAlertDialogBuilder(this).apply {
                    setMessage("How would you like to upload this file?\nPrivate or Public?")
                    setPositiveButton("Public"){ _,_ ->
                        //Toast.makeText(this@MainActivity, "Public", Toast.LENGTH_SHORT).show()
                        uploadFile(it,false)
                    }
                    setNegativeButton("Private"){dialogInterface, i ->
                        //Toast.makeText(this@MainActivity, "Private", Toast.LENGTH_SHORT).show()
                        uploadFile(it,true)
                    }
                    setCancelable(false)
                    create()
                    show()
                }
            }
        }
    }

    private fun uploadFile(uri:Uri,isPrivate : Boolean) {
        uploadBtn.isEnabled = false
        val ref = storageRef.child("uploads/" + uri.lastPathSegment + " by " + name + " at " + System.currentTimeMillis())
        val uploadTask = ref.putFile(uri)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result.toString()

                val file = File(uri.lastPathSegment!!,downloadUrl,name)
                uploadFileToRealtimeDatabase(file,isPrivate)
            } else {
                Toast.makeText(this, "File upload failed.", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, "File upload failed.", Toast.LENGTH_SHORT).show()
        }
        uploadBtn.isEnabled = true
    }

    private fun uploadFileToRealtimeDatabase(file: File, isPrivate: Boolean) {
        if(isPrivate){
            db.child("privateFiles").child(auth.uid!!).child(System.currentTimeMillis().toString()).setValue(file)
        }
        else{
            val currTime = System.currentTimeMillis().toString()
            db.child("publicFiles").child(auth.uid!!).child(currTime).setValue(file)
            db.child("allPublicFiles").child(currTime+auth.uid!!).setValue(file)
        }
        Toast.makeText(this, "File uploaded.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.myPublicFiles){
            val i = Intent(this,PersonalFilesActivity::class.java)
            i.putExtra("isPrivate",false)
            startActivity(i)
        }
        else if(item.itemId == R.id.myPrivateFiles){
            val i = Intent(this,PersonalFilesActivity::class.java)
            i.putExtra("isPrivate",true)
            startActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(position: Int) {
       // Toast.makeText(this, "Item Clicked.${files[position]}", Toast.LENGTH_SHORT).show()
        val i = Intent(Intent.ACTION_VIEW,Uri.parse(files[position].url))
        startActivity(i)
        //Log.d(TAG, files[position].url)
    }

}