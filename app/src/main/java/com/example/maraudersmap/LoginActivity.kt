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
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
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
                val responseCode : Int = response.code      // response code: 200 = User was added, 409 = User already exists
                println(responseCode)
                deleteMeLater()

                //todo please post your code here to handle the responseCode
            }
        }

        val userController = UserController()
        userController.createNewUser("Username114","password","description",callback)







    }



    @Root(name = "userXTO", strict = false)
    data class Command @JvmOverloads constructor(
        @field:Element(name = "id")
        var id: String? = null
    )

    fun deleteMeLater(){


        val callbackLogin = object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                println("Error, the server is probably not responding")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseCode : Int = response.code      // response code: 200 = Login successful, 403 = Forbidden / Login failed
                println(responseCode)
                val body = response.body
                val xmlBody = body?.string()
                var userID = ""


                val serializer: Serializer = Persister()
                val dataFetch = serializer.read(Command::class.java, xmlBody)
                userID = dataFetch.id.toString()

                println(userID)



                //todo please post your code here to handle the responseCode
            }
        }

        val userController1 = UserController()
        userController1.loginUser("Username114","password",callbackLogin)
    }


}