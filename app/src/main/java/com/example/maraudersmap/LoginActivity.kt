package com.example.maraudersmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

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
            Toast.makeText(this@LoginActivity, "${username.text}, ${password.text}", Toast.LENGTH_LONG).show()
        }

        registerLink.setOnClickListener {
            Toast.makeText(this@LoginActivity, "Register Link clicked", Toast.LENGTH_LONG).show()
        }


    }
}