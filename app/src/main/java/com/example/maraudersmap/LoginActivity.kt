package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ContentInfoCompat.Flags
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import okhttp3.Response
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister


/**
 * provides function to login a user with its credentials
 * @author Felix Kuhbier & Julian Ertle
 * @since 2022.12.02
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var toastMessage: String

    companion object UserInformation {
        var userID: String? = null                                   //todo after logging out this field needs to be set to null again to avoid a bad server request after logging in again
        var jsonWebToken: String? = null //todo after logging out this field needs to be set to null again to avoid a bad server request after logging in again
        var description: String? = null
        var privacyRadius: Long? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username = findViewById(R.id.loginUsername_editText)
        password = findViewById(R.id.loginPassword_editText)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink_textView)


        loginButton.setOnClickListener {
            if (validateLogin(username.text.toString(), password.text.toString())) {
                loginUser(username.text.toString(), password.text.toString())
            } else if (!validateInput(username.text.toString())) {
                makeToast(getString(R.string.invalidUsername_text), Toast.LENGTH_LONG)
            } else {
                makeToast(getString(R.string.invalidPassword_text), Toast.LENGTH_LONG)
            }
        }

        registerLink.setOnClickListener {
            Thread.sleep(1000L)
            switchActivity(SettingsActivity::class.java)
        }

    }


    /**
     * Sends a login request to the server and returns a response. The important
     * information get extracted out of the response and are saved in variables
     *
     * @param username Username of the user
     * @param password Password of the username
     */
    private fun loginUser(username: String, password: String) {

        try {
            val scope = CoroutineScope(Job() + Dispatchers.IO)
            scope.launch {

                val serializer: Serializer = Persister()
                val userController = UserController()
                val response : Response = userController.loginUser(username, password)
                val xmlBody = response.body!!.string()

                when(response.code){      // Response codes: 200 = Login successful, 403 = Forbidden (Login failed), ? = Other unknown error codes possible
                    200 ->{
                        userID = serializer.read(ExtractUserID::class.java, xmlBody).id
                        description = serializer.read(ExtractDescription::class.java, xmlBody).description
                        privacyRadius = serializer.read(ExtractPrivacyRadius::class.java, xmlBody).radius?.toLong()

                        jsonWebToken = response.headers.last().second
                        toastMessage = getString(R.string.successfulLogin)

                    }

                    403 -> toastMessage = getString(R.string.failedLogin_text)

                    else -> toastMessage = getString(R.string.unknownError_text)
                }

                withContext(Dispatchers.Main){
                    makeToast(toastMessage, Toast.LENGTH_SHORT)
                }
            }
        }catch (e: CancellationException){
            e.printStackTrace()
        }
    }



    /**
     * Extracts the user ID out of the response body
     */
    @Root(name = "userXTO", strict = false)
    data class ExtractUserID @JvmOverloads constructor(
        @field:Element(name = "id")
        var id: String? = null
    )

    @Root(name = "userXTO", strict = false)
    data class ExtractDescription @JvmOverloads constructor(
        @field:Element(name = "description")
        var description: String? = null
    )

    @Root(name = "userXTO", strict = false)
    data class ExtractPrivacyRadius @JvmOverloads constructor(
        @field:Element(name = "privacyRadius")
        var radius: String? = null
    )

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
     * @return True if valid
     */
    private fun validateLogin(username: String, password: String): Boolean {
        if (validateInput(username) && validateInput(password)) {
            return true
        }

        return false
    }

    /**
     * makes Toast
     * @param msg message to show
     * @param duration display time
     */
    private fun makeToast(msg: String, duration: Int) {
        Toast.makeText(this@LoginActivity, msg, duration).show()
    }

    /**
     * Switch to activity
     * @param destinationClass destination activity
     */
    private fun switchActivity(destinationClass: Class<*>) {

        val intent = Intent(this@LoginActivity, destinationClass)
        startActivity(intent)

    }
}

