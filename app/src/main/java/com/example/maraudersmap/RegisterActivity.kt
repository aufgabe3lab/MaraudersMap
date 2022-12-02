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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response

/**
 * provides function to register a new user
 * @author Felix Kuhbier & Julian Ertle
 * @since 2022.12.02
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var passwordConfirmation: EditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var toastMessage: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        username = findViewById(R.id.registerUsername_editText)
        password = findViewById(R.id.registerPassword_editText)
        passwordConfirmation = findViewById(R.id.confirmPassword_editText)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink_textView)

        registerButton.setOnClickListener {
           if(validateRegister(username.text.toString(),password.text.toString(), passwordConfirmation.text.toString())){
               registerUser(username.text.toString(),password.text.toString(),"description")
           }else if(!validateInput(username.text.toString())){
               makeToast("Username is invalid", Toast.LENGTH_SHORT)
           }else if(!validateInput(password.text.toString())){
               makeToast("Password is invalid", Toast.LENGTH_SHORT)
           }else if(!validateInput(passwordConfirmation.text.toString())){
               makeToast("Confirm password", Toast.LENGTH_SHORT)
           }else{
               makeToast("Passwords do not match!", Toast.LENGTH_SHORT)
           }
        }

        loginLink.setOnClickListener {
           switchActivity(LoginActivity::class.java)
        }

        passwordConfirmation.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString() == password.text.toString()){
                    passwordConfirmation.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    password.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if (p0.toString() != password.text.toString()){
                    passwordConfirmation.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    password.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.isNullOrEmpty() || password.text.toString().isEmpty()) {
                    passwordConfirmation.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                    password.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                }
            }

        })


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

            when(response.code){         // Response codes: 200 = User was added, 409 = User already exists, ? = other unknown error codes possible
                200 -> {
                    toastMessage = "Registration successful"
                    switchActivity(LoginActivity::class.java)
                }

                409 -> toastMessage = "User already exists"


                else -> toastMessage = "Unknown error"
            }

            withContext(Dispatchers.Main){
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }

        }
    }

    /**
     * validates input string
     * @param inputString String to validate
     * @return True if input is valid
     */
    private fun validateInput(inputString: String): Boolean{

        if(inputString.isBlank() || inputString.isBlank()){
            return false
        }

        return true
    }

    /**
     * validates registration
     * @param username username to validate
     * @param password password to validate
     * @param passwordConfirmation confirmation password to validate
     * @return True if valid
     */
    private fun validateRegister(username: String, password: String, passwordConfirmation: String): Boolean{
        if(validateInput(username) && validateInput(password) && validateInput(passwordConfirmation) && passwordConfirmation == password){
            return true
        }

        return false
    }

    /**
     * makes Toast
     * @param msg message to show
     * @param duration display time
     */
    private fun makeToast(msg: String, duration: Int){
        Toast.makeText(this@RegisterActivity, msg, duration).show()
    }

    /**
     * Switch to activity
     * @param destinationClass destination activity
     */
    private fun switchActivity(destinationClass: Class<*>){
        val intent = Intent(this@RegisterActivity, destinationClass)
        startActivity(intent)
    }
}