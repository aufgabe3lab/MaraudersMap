package com.example.maraudersmap

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
            Toast.makeText(this@LoginActivity, "${username.text}, ${password.text}", Toast.LENGTH_LONG).show()
        }

        registerLink.setOnClickListener {
           val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        val button = findViewById<Button>(R.id.button)

        button.setOnClickListener {
            val intent = Intent(this@LoginActivity, MapActivity::class.java)
            startActivity(intent)
        }
    }
}