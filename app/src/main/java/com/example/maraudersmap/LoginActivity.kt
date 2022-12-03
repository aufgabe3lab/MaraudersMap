package com.example.maraudersmap

import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private lateinit var changePasswordButton: Button                //todo needs to be deleted in future

    companion object{
        var userID: String? = null
        var jsonWebToken: String? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username = findViewById(R.id.loginUsername_editText)
        password = findViewById(R.id.loginPassword_editText)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink_textView)

        changePasswordButton = findViewById(R.id.password_button)     //todo needs to be deleted in future

        loginButton.setOnClickListener {
            if(validateLogin(username.text.toString(), password.text.toString())){
                loginUser(username.text.toString(), password.text.toString())
            }else if(!validateInput(username.text.toString())){
                makeToast(getString(R.string.invalidUsername_text), Toast.LENGTH_LONG)
            }else{
                makeToast(getString(R.string.invalidPassword_text), Toast.LENGTH_LONG)
            }
        }

        registerLink.setOnClickListener {
           switchActivity(RegisterActivity::class.java)
        }

        changePasswordButton.setOnClickListener {                     //todo needs to be deleted in future
            if(userID!=null){
                //changePassword("123", userID!!, jsonWebToken!!)
                //changePassword("123", userID!!, jsonWebToken!!)
                deleteUser(userID!!, jsonWebToken!!)
            }
        }
    }

    private fun changePassword(newPassword: String, userID: String, jsonWebToken: String){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response : Response = userController.changeUserPassword(newPassword,userID,jsonWebToken)
            println(response)

            when(response.code){         // Response codes: 200 = Password changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid) ? = other unknown error codes possible
                200 -> {
                    toastMessage = getString(R.string.passwordChanged_text)
                    switchActivity(LoginActivity::class.java)
                }

                304 -> toastMessage = getString(R.string.notModified_text)
                403 -> toastMessage = getString(R.string.permissionDenied_text)


                else -> toastMessage = getString(R.string.unknownError_text)
            }

            withContext(Dispatchers.Main){
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun changePrivacyRadius(privacyRadius: Long, userID: String, jsonWebToken: String){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response : Response = userController.changeUserPrivacyRadius(privacyRadius,userID,jsonWebToken)
            println(response)

            when(response.code){         // Response codes: 200 = privacy radius changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid) ? = other unknown error codes possible
                200 -> {
                    toastMessage = getString(R.string.privacyRadiusChanged_text)
                    switchActivity(LoginActivity::class.java)
                }

                304 -> toastMessage = getString(R.string.notModified_text)
                403 -> toastMessage = getString(R.string.permissionDenied_text)


                else -> toastMessage = getString(R.string.unknownError_text)
            }

            withContext(Dispatchers.Main){
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun changeDescription(description: String, userID: String, jsonWebToken: String){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response : Response = userController.changeUserDescription(description,userID,jsonWebToken)
            println(response)

            when(response.code){         // Response codes: 200 = description changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid) ? = other unknown error codes possible
                200 -> {
                    toastMessage = getString(R.string.descriptionChanged_text)
                    switchActivity(LoginActivity::class.java)
                }

                304 -> toastMessage = getString(R.string.notModified_text)
                403 -> toastMessage = getString(R.string.permissionDenied_text)


                else -> toastMessage = getString(R.string.unknownError_text)
            }

            withContext(Dispatchers.Main){
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun deleteUser(userID: String, jsonWebToken: String){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response : Response = userController.deleteUser(userID,jsonWebToken)
            println(response)

            when(response.code){         // Response codes: 200 = Password changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid) ? = other unknown error codes possible
                200 -> {
                    toastMessage = getString(R.string.deletedUser_text)
                    switchActivity(LoginActivity::class.java)
                }

                403 -> toastMessage = getString(R.string.permissionDenied_text)


                else -> toastMessage = getString(R.string.unknownError_text)
            }

            withContext(Dispatchers.Main){
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    /**
     * Sends a login request to the server and returns a response. The important
     * information get extracted out of the response and are saved in variables
     *
     * @param username Username of the user
     * @param password Password of the username
     */
    private fun loginUser(username : String, password : String){

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val serializer: Serializer = Persister()

            val userController = UserController()
            val response : Response = userController.loginUser(username,password)      // sends a login request to the server and returns a response

            val xmlBody = response.body!!.string()

            when(response.code){      // Response codes: 200 = Login successful, 403 = Forbidden (Login failed), ? = Other unknown error codes possible
                200 ->{
                    userID = serializer.read(ExtractUserID::class.java, xmlBody).id.toString()
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
    }

    /**
     * Extracts the user ID out of the response body
     */
    @Root(name = "userXTO", strict = false)
    data class ExtractUserID @JvmOverloads constructor(
        @field:Element(name = "id")
        var id: String? = null
    )

    /**
     * validates input string
     * @param inputString String to validate
     * @return True if input is valid
     */
    private fun validateInput(inputString: String): Boolean{

        if(inputString.isEmpty() || inputString.isBlank()){
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
    private fun validateLogin(username: String, password: String): Boolean{
        if(validateInput(username) && validateInput(password)){
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
        Toast.makeText(this@LoginActivity, msg, duration).show()
    }

    /**
     * Switch to activity
     * @param destinationClass destination activity
     */
    private fun switchActivity(destinationClass: Class<*>){
        val intent = Intent(this@LoginActivity, destinationClass)
        startActivity(intent)
    }
}