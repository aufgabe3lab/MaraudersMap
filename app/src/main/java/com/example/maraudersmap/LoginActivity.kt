package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.maraudersmap.backend.UserControllerAPI
import kotlinx.coroutines.*
import okhttp3.Response
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.net.SocketTimeoutException
import java.net.UnknownHostException


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
        var userID: String? = null        //todo after logging out this field needs to be set to null again to avoid a bad server request after logging in again
        var jsonWebToken: String? = null //todo after logging out this field needs to be set to null again to avoid a bad server request after logging in again
        var description: String? = null
        var privacyRadius: Long? = null
        const val baseURL : String = "https://maraudersmap-ext.hhn.dev/api/v0.2/"
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
            switchActivity(RegisterActivity::class.java)
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
                val userControllerAPI = UserControllerAPI()
                val response : Response

                try { // todo implement try/catch blocks in other classes when using UserControllerAPI methods to catch SocketTimeoutException or UnknownHostException
                    response = userControllerAPI.loginUser(username, password)
                    val xmlBody = response.body!!.string()

                    when(response.code){      // Response codes: 200 = Login successful, 403 = Forbidden (Login failed), ? = Other unknown error codes possible
                        200 ->{

                            val userData = serializer.read(ExtractData::class.java, xmlBody)
                            userID = userData.id
                            description = userData.description
                            privacyRadius = userData.radius?.toDouble()?.toLong() //converts the double value to a  long value

                            jsonWebToken = response.headers.last().second
                            toastMessage = getString(R.string.successfulLogin)
                            switchActivity(MapActivity::class.java)
                        }

                        403 -> toastMessage = getString(R.string.failedLogin_text)

                        else -> toastMessage = getString(R.string.unknownError_text)
                    }

                    withContext(Dispatchers.Main){
                        makeToast(toastMessage, Toast.LENGTH_SHORT)
                    }
                }
                catch (e : SocketTimeoutException){
                    toastMessage = getString(R.string.noServerConnection_text)                  // server not reachable
                    withContext(Dispatchers.Main){
                        makeToast(toastMessage, Toast.LENGTH_SHORT)
                    }
                }
                catch (e : UnknownHostException){                                               // no internet connection
                    toastMessage = getString(R.string.noInternetConnection_text)
                    withContext(Dispatchers.Main){
                        makeToast(toastMessage, Toast.LENGTH_SHORT)
                    }
                }
            }
        }catch (e: CancellationException){
            e.printStackTrace()
        }
    }

    /**
     * Data class representing a user with an ID, description, and privacy radius.
     *
     * @property id The user's ID.
     * @property description The user's description.
     * @property radius The user's privacy radius.
     */
    @Root(name = "userXTO", strict = false)
    data class ExtractData(
        @field:Element(name = "id")
        var id: String? = null,

        @field:Element(name = "description")
        var description: String? = null,

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
     * Displays a toast message with the specified text and duration.
     *
     * @param msg The text to be displayed in the toast message.
     * @param duration The duration for which the toast message should be displayed.
     */
    private fun makeToast(msg: String, duration: Int) {
        Toast.makeText(this@LoginActivity, msg, duration).show()
    }

    /**
     * Switches the current activity to the specified destination activity.
     *
     * @param destinationClass The destination activity to switch to.
     */
    private fun switchActivity(destinationClass: Class<*>) {

        val intent = Intent(this@LoginActivity, destinationClass)
        startActivity(intent)

    }
}

