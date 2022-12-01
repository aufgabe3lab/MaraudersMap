package com.example.maraudersmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response

/**
 * provides function to register a new user
 * @author Felix Kuhbier & Julian Ertle
 * @since 2022.11.23
 */
class RegisterActivity : AppCompatActivity() {

    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var passwordConfirmation: EditText
    lateinit var registerButton: Button
    lateinit var loginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        username = findViewById(R.id.registerUsername_editText)
        password = findViewById(R.id.registerPassword_editText)
        passwordConfirmation = findViewById(R.id.confirmPassword_editText)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink_textView)

        registerButton.setOnClickListener {
            if(passwordConfirmation.text.toString() == password.text.toString()){
                Toast.makeText(this@RegisterActivity, "${username.text}, ${password.text}", Toast.LENGTH_LONG).show()

            }else{
                Toast.makeText(this@RegisterActivity, "Passwords do not match!", Toast.LENGTH_LONG).show()
            }
        }

        loginLink.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        registerUser("Username114","password","description")
    }

    /**
     * Sends a register request to the server and returns a response.
     * The response code is saved in a variable
     *
     * @param username Username of the user
     * @param password Password of the username
     * @param description small description, needed but not sure why
     */
    private fun registerUser(username : String, password : String, description : String){

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response : Response = userController.createNewUser(username,password,description)

            val responseCode : Int = response.code         // Response codes: 200 = User was added, 409 = User already exists, ? = other unknown error codes possible

            //todo maybe change local variables to instance variables to be able to use them outside of this method
        }
    }
}