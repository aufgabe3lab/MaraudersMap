package com.example.maraudersmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var autoSendPosSwitch: SwitchCompat
    private lateinit var intervalEditText: EditText
    private lateinit var privacyRadius: EditText
    private lateinit var deleteButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        autoSendPosSwitch = findViewById(R.id.autoSendPos_switch)
        intervalEditText = findViewById(R.id.interval_editTextNumber)
        privacyRadius = findViewById(R.id.privacyRadius_editTextNumber)
        deleteButton =  findViewById(R.id.deleteAccount_button)

        autoSendPosSwitch.setOnCheckedChangeListener{_, isChecked->
            if(isChecked){
                Toast.makeText(this@SettingsActivity, "Checked", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@SettingsActivity, "Unchecked", Toast.LENGTH_SHORT).show()
            }
        }
    }
}