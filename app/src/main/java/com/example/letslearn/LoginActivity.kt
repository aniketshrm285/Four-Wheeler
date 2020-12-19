package com.example.letslearn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var countryCode:String
    private lateinit var phoneNumber : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneNumberEt.addTextChangedListener {
            if(it!=null){
                val text = it.toString()
                nextBtn.isEnabled = text.length>=10
            }
        }

        nextBtn.setOnClickListener {
            notifyUser()
        }
    }

    private fun notifyUser() {
        countryCode = ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode+ phoneNumberEt.text.toString()
        MaterialAlertDialogBuilder(this).apply {
            setMessage("We will be verifying the phone number $phoneNumber \nIs this OK or would you like to edit it?")
            setPositiveButton("OK"){ _,_ ->
                showOtpActivity()
            }
            setNegativeButton("Edit"){dialogInterface, i ->
                dialogInterface.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this,OtpActivity::class.java).putExtra(PHONE_NUMBER,phoneNumber))
        finish()
    }
}