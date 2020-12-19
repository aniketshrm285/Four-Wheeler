package com.example.letslearn

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"
class OtpActivity : AppCompatActivity(){

    private lateinit var phoneNumber:String
    private var mCountDownTimer:CountDownTimer?=null


    var mVerificationId : String ?= null
    var mResendToken: PhoneAuthProvider.ForceResendingToken?=null
    lateinit var callbacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var auth: FirebaseAuth

    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        initViews()
        startVerify()


    }

    private fun startVerify() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        progressDialog = createProgressDialog("Sending a verification code",false)
        progressDialog.show()

        showTimer()
    }

    private fun resendVerificationCode(toString: String, mResendToken: PhoneAuthProvider.ForceResendingToken) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(mResendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        progressDialog = createProgressDialog("Sending a verification code",false)
        progressDialog.show()

        showTimer()
    }


    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)!!

        auth = Firebase.auth

        verifyTv.text = "Verify ${phoneNumber}"
        setSpannableString()


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                //Log.d(TAG, "onVerificationCompleted:$credential")

                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }

                val smsCode = credential.smsCode
                if(!smsCode.isNullOrEmpty()){
                    sentcodeEt.setText(smsCode)
                }
                Log.d(TAG, "onVerificationCompleted: verification completed")
                signInWithPhoneAuthCredential(credential)
                //Toast.makeText(this@OtpActivity, "Done!!", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                //Log.w(TAG, "onVerificationFailed", e)

                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@OtpActivity, "Too many requests. Please try again later.", Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, "onVerificationFailed: verification failed")

                // Show a message and update the UI
                // ...
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                Log.d(TAG, "onCodeSent: bhej diya")

                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                //Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

                // ...
            }
        }

        verificationBtn.setOnClickListener {

            val code = sentcodeEt.text.toString()
            if(code.isNotEmpty() && !mVerificationId.isNullOrBlank()){
                val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            }


        }

        resendBtn.setOnClickListener {
            if (mResendToken != null) {
                resendVerificationCode(phoneNumber, mResendToken!!)
            } else {
                Toast.makeText(this, "Sorry, You Can't request new code now, Please wait ...", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this@OtpActivity, "Success.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@OtpActivity,SignUpActivity::class.java).putExtra(
                        PHONE_NUMBER,phoneNumber))
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    //Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        notifyUserAndRetry("The verification code entered was invalid")
                    }
                }
            }
    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            setCancelable(false)
            create()
            show()
        }
    }

    private fun showTimer() {
        resendBtn.isEnabled = false
        mCountDownTimer = object : CountDownTimer(60000,1000){
            override fun onFinish() {
                counterTv.isVisible = true
                resendBtn.isEnabled = true
            }

            override fun onTick(p0: Long) {
                counterTv.isVisible = true
                counterTv.text = "Seconds Remaining: " + (p0 / 1000)
            }

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mCountDownTimer!=null){
            mCountDownTimer!!.cancel()
        }
    }


    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text, phoneNumber))
        val clickSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = ds.linkColor // you can use custom color
                ds.isUnderlineText = false // this remove the underline
            }

            override fun onClick(textView: View) { // handle click event
                showLoginActivity()
                //send back
            }
        }

        span.setSpan(clickSpan, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span
    }

    private fun showLoginActivity() {
        startActivity(
            Intent(this,LoginActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }
}

fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
    return ProgressDialog(this).apply {
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(false)
        setMessage(message)
    }
}