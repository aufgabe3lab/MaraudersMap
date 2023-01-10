package com.example.maraudersmap

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.maraudersmap.backend.UserControllerAPI
import kotlinx.coroutines.*
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
            if (validateRegister(
                    username.text.toString(),
                    password.text.toString(),
                    passwordConfirmation.text.toString()
                )
            ) {
                registerUser(username.text.toString(), password.text.toString(), "description")
            } else if (!validateInput(username.text.toString())) {
                makeToast(getString(R.string.invalidUsername_text), Toast.LENGTH_SHORT)
            } else if (!validateInput(password.text.toString())) {
                makeToast(getString(R.string.invalidPassword_text), Toast.LENGTH_SHORT)
            } else if (!validateInput(passwordConfirmation.text.toString())) {
                makeToast(getString(R.string.confirmPassword_text), Toast.LENGTH_SHORT)
            } else {
                makeToast(getString(R.string.passwordsDoNotMatch_text), Toast.LENGTH_SHORT)
            }
        }

        loginLink.setOnClickListener {
            switchActivity(LoginActivity::class.java)
        }

        passwordConfirmation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() == password.text.toString()) {
                    passwordConfirmation.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    password.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                } else if (p0.toString() != password.text.toString()) {
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
    private fun registerUser(username: String, password: String, description: String) {

        try {
            val scope = CoroutineScope(Job() + Dispatchers.IO)
            scope.launch {


                val userControllerAPI = UserControllerAPI()
                val response: Response =
                    userControllerAPI.createNewUser(username, password, description)

                when (response.code) {         // Response codes: 200 = User was added, 409 = User already exists, ? = other unknown error codes possible
                    200 -> {
                        toastMessage = getString(R.string.successfulRegistration_text)
                        switchActivity(LoginActivity::class.java)
                    }

                    409 -> toastMessage = getString(R.string.userAlreadyExists_text)


                    else -> toastMessage = getString(R.string.unknownError_text)
                }



                withContext(Dispatchers.Main) {
                    makeToast(toastMessage, Toast.LENGTH_SHORT)
                }

            }
        } catch (e: CancellationException) {
            e.printStackTrace()
        }

    }

    /**
     * validates input string
     * @param inputString String to validate
     * @return True if input is valid
     */
    private fun validateInput(inputString: String): Boolean {

        if (inputString.isEmpty() || inputString.isBlank()) {
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
    private fun validateRegister(
        username: String,
        password: String,
        passwordConfirmation: String
    ): Boolean {
        if (validateInput(username) && validateInput(password) && validateInput(passwordConfirmation) && passwordConfirmation == password) {
            return true
        }

        return false
    }

    /**
     * Displays a toast message with the specified text and duration.
     *
     * @param msg The text to be displayed in the toast message.
     * @param duration The duration for which the toast message should be displayed.
     */
    private fun makeToast(msg: String, duration: Int) {
        Toast.makeText(this@RegisterActivity, msg, duration).show()
    }

    /**
     * Switches the current activity to the specified destination activity.
     *
     * @param destinationClass The destination activity to switch to.
     */
    private fun switchActivity(destinationClass: Class<*>) {
        val intent = Intent(this@RegisterActivity, destinationClass)
        startActivity(intent)
    }
}