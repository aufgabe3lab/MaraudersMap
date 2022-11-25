package com.example.maraudersmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

/**
 * provides function to register a new user
 * @author Felix Kuhbier
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
                makeToast("${username.text}, ${password.text}", Toast.LENGTH_LONG)

            }else{
                makeToast("Passwords do not match!", Toast.LENGTH_LONG)
            }
        }

        loginLink.setOnClickListener {
            switchActivity(LoginActivity::class.java)
        }


    }

    /**
     * show toast with given msg and duration
     * @param msg message of toast
     * @param length duration of toast
     */
    private fun makeToast(msg: String, length: Int){
        Toast.makeText(this@RegisterActivity, msg, length).show()
    }

    /**
     * Switches to activity
     * @param destinationClass component class that is to be used for the intent
     */
    private fun switchActivity(destinationClass: Class<*>?){
        val intent = Intent(this@RegisterActivity, destinationClass)
        startActivity(intent)
    }
}