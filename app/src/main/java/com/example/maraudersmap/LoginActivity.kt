package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.IOException


/**
 * provides function to login a user with its credentials
 * @author Felix Kuhbier & Julian Ertle
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

        loginUser("Username114","password")
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

            val userController1 = UserController()
            val response : Response = userController1.loginUser(username,password)      // sends a login request to the server and returns a response

            val xmlBody = response.body!!.string()
            val responseCode : Int = response.code      // Response codes: 200 = Login successful, 403 = Forbidden (Login failed), ? = Other unknown error codes possible

            if(responseCode==200){
                val userID: String = serializer.read(ExtractUserID::class.java, xmlBody).id.toString()
                val jsonWebToken = response.headers.last().second
            }

            //todo maybe change local variables to instance variables to be able to use them outside of this method
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
}