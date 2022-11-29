package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


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

        // the part blow is for test purpose of Julian

        val user = User()
        user.description = "Empty description"
        user.username = "EpixxxHD"
        user.password = "1234"

        /*val userXml = """
            <userXTO>
                <username>${user.username}</username>
                <password>${user.password}</password>
                <description>${user.description}</description>
            </userXTO>
        """.trimIndent()
        println(userXml)*/

        val generator = XmlGenerator()
        val xmlString = generator.generateXml(user)
        println(xmlString)

    }
}