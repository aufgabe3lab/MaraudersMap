package com.example.maraudersmap

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

/**
 * provides function to login a user with its credentials
 * @author Felix Kuhbier
 * @since 2022.11.23
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username = findViewById(R.id.loginUsername_editText)
        password = findViewById(R.id.loginPassword_editText)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink_textView)

        loginButton.setOnClickListener {
            makeToast("${username.text}", Toast.LENGTH_LONG)
        }

        registerLink.setOnClickListener {
           switchActivity(RegisterActivity::class.java)
        }


    }

    /**
     * Switches to activity
     * @param destinationClass component class that is to be used for the intent
     */
    private fun switchActivity(destinationClass: Class<*>?){
        val intent = Intent(this@LoginActivity, destinationClass)
        startActivity(intent)
    }

    /**
     * show toast with given msg and duration
     * @param msg message of toast
     * @param length duration of toast
     */
    private fun makeToast(msg: String, length: Int){
        Toast.makeText(this@LoginActivity, msg, length).show()
    }

}