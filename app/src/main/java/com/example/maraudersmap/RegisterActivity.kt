package com.example.maraudersmap

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.green
import androidx.core.widget.addTextChangedListener

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
            if(validateRegistration()){
                makeToast("Successfully", Toast.LENGTH_LONG)
            }else{
                if(!validateInput(username.text.toString())){
                    makeToast("Username is invalid!", Toast.LENGTH_LONG)
                }else if(!validateInput(password.text.toString())){
                    makeToast("password is invalid", Toast.LENGTH_SHORT)
                }else{
                    makeToast("passwords do not match!", Toast.LENGTH_SHORT)
                }
            }
        }

        passwordConfirmation.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if(p0.toString() == password.text.toString() ){
                    passwordConfirmation.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else{
                    passwordConfirmation.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

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

    private fun validateInput(inputString: String) : Boolean{
        if(inputString.isEmpty() || inputString.isBlank()){
            return false
        }

        return true
    }

    private fun validateRegistration(): Boolean{
       if(validateInput(username.text.toString()) && validateInput(password.text.toString()) && passwordConfirmation.backgroundTintList == ColorStateList.valueOf(Color.GREEN)){
            return true
        }

        return false
    }

}