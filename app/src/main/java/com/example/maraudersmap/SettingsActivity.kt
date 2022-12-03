package com.example.maraudersmap

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NavUtils

/**
 * Provides functions to individualize the app
 * @author Felix Kuhbier
 * @since 2022.12.03
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var autoSendPosSwitch: SwitchCompat
    private lateinit var intervalEditText: EditText
    private lateinit var privacyRadiusEditText: EditText
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        autoSendPosSwitch = findViewById(R.id.autoSendPos_switch)
        intervalEditText = findViewById(R.id.interval_editTextNumber)
        privacyRadiusEditText = findViewById(R.id.privacyRadius_editTextNumber)
        deleteButton =  findViewById(R.id.deleteAccount_button)

        autoSendPosSwitch.setOnCheckedChangeListener{_, isChecked->
            if(isChecked){
                makeToast("Checked", Toast.LENGTH_SHORT)
            }else{
                makeToast("Unchecked", Toast.LENGTH_SHORT)
            }
        }

        intervalEditText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("Not yet implemented")
                makeToast("${s}s", Toast.LENGTH_SHORT)
            }

            override fun afterTextChanged(s: Editable?) {
                //TODO("Not yet implemented")
            }

        })

        privacyRadiusEditText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("Not yet implemented")
                makeToast("${s}km", Toast.LENGTH_SHORT)
            }

            override fun afterTextChanged(s: Editable?) {
                //TODO("Not yet implemented")
            }

        })

        deleteButton.setOnClickListener {
            makeToast("Account Deleted", Toast.LENGTH_SHORT)
        }
    }


    /**
     * this event will enable the back function to the button on press
     * @param item MenuItem
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                //switchActivity(MapActivity::class.java)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * makes Toast
     * @param msg message to show
     * @param duration display time
     */
    private fun makeToast(msg: String, duration: Int){
        Toast.makeText(this@SettingsActivity, msg, duration).show()
    }

    /**
     * Switch to activity
     * @param destinationClass destination activity
     */
    private fun switchActivity(destinationClass: Class<*>){
        val intent = Intent(this@SettingsActivity, destinationClass)
        startActivity(intent)
    }
}