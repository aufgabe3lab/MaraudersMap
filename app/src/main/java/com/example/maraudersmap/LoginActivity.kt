package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


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

        // the part below is for test purpose of Julian


        // callback methods are called after server responds to the request below (userController.createNewUser(...))
        val callback = object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                println("Error, the server is probably not responding")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseCode : Int = response.code
                println(responseCode)

                //todo please post your code here to handle the responseCode
            }
        }

        val userController = UserController()
        userController.createNewUser("Username112","password","description",callback)
    }
}