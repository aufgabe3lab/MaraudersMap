package com.example.maraudersmap

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.FocusFinder
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NavUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import com.example.maraudersmap.LoginActivity.Companion.userID

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
    private lateinit var changePassword: EditText
    private lateinit var confirmPasswordChange: Button
    private lateinit var toastMessage: String

    companion object{
        var interval: Long? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        autoSendPosSwitch = findViewById(R.id.autoSendPos_switch)
        intervalEditText = findViewById(R.id.interval_editTextNumber)
        privacyRadiusEditText = findViewById(R.id.privacyRadius_editTextNumber)
        deleteButton =  findViewById(R.id.deleteAccount_button)
        changePassword = findViewById(R.id.changePassword_editText)
        confirmPasswordChange = findViewById(R.id.changePassword_button)

        intervalEditText.isEnabled = false

        autoSendPosSwitch.setOnCheckedChangeListener{_, isChecked->

            intervalEditText.isEnabled = isChecked

        }

        intervalEditText.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                intervalEditText.addTextChangedListener(object : TextWatcher{
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        //TODO("Not yet implemented")
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        //TODO("Not yet implemented")
                    }

                    override fun afterTextChanged(s: Editable?) {
                        //TODO("Not yet implemented")
                        if(intervalEditText.text.toString().isEmpty() || intervalEditText.text.toString().isBlank()){
                            makeToast("Interval invalid", Toast.LENGTH_SHORT)
                        }
                    }

                })
            }else{
                if(intervalEditText.text.toString().isEmpty() || intervalEditText.text.toString().isBlank()){
                    intervalEditText.setText("5")
                    makeToast("Set Interval to default", Toast.LENGTH_SHORT)
                }

                interval = intervalEditText.text.toString().toLong()

            }
        }

        privacyRadiusEditText.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                privacyRadiusEditText.addTextChangedListener(object : TextWatcher{
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        //TODO("Not yet implemented")
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        //TODO("Not yet implemented")

                    }

                    override fun afterTextChanged(s: Editable?) {
                        if(privacyRadiusEditText.text.toString().isEmpty() || privacyRadiusEditText.text.toString().isBlank()){
                            makeToast("Radius invalid", Toast.LENGTH_SHORT)
                        }
                    }

                })
            }else{
                if(privacyRadiusEditText.text.toString().isEmpty() || privacyRadiusEditText.text.toString().isBlank()){
                    privacyRadiusEditText.setText("-1")
                    makeToast("Set Privacy Radius to default", Toast.LENGTH_SHORT)
                }

                changePrivacyRadius(privacyRadiusEditText.text.toString().toLong(), userID!!)


            }
        }

        deleteButton.setOnClickListener {
            deleteUser(userID!!)
        }

       confirmPasswordChange.setOnClickListener {
           if(changePassword.text.toString().isEmpty() || changePassword.text.toString().isBlank()){
               makeToast(getString(R.string.invalidPassword_text), Toast.LENGTH_SHORT)
           }else {
               changePassword(changePassword.text.toString(), userID!!)
           }
       }




    }

    private fun changePrivacyRadius(privacyRadius: Long, userID: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response = userController.changeUserPrivacyRadius(privacyRadius, userID)

            toastMessage = when (response.code) {         // Response codes: 200 = privacy radius changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> {
                    getString(R.string.privacyRadiusChanged_text)
                }

                304 -> getString(R.string.notModified_text)
                403 -> getString(R.string.permissionDenied_text)


                else -> getString(R.string.unknownError_text)
            }

            withContext(Dispatchers.Main) {
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun deleteUser(userID: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response = userController.deleteUser(userID)

            toastMessage = when (response.code) {         // Response codes: 200 = deleted user, 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> {
                    getString(R.string.deletedUser_text)

                }

                403 -> getString(R.string.permissionDenied_text)


                else -> getString(R.string.unknownError_text)
            }

            withContext(Dispatchers.Main) {
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun changePassword(newPassword: String, userID: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response = userController.changeUserPassword(newPassword, userID)

            when (response.code) {         // Response codes: 200 = Password changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> {
                    toastMessage = getString(R.string.passwordChanged_text)
                }

                304 -> toastMessage = getString(R.string.notModified_text)
                403 -> toastMessage = getString(R.string.permissionDenied_text)


                else -> toastMessage = getString(R.string.unknownError_text)
            }

            withContext(Dispatchers.Main) {
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun changeDescription(description: String, userID: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response = userController.changeUserDescription(description, userID)

            toastMessage = when (response.code) {         // Response codes: 200 = description changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> {
                    getString(R.string.descriptionChanged_text)

                }

                304 -> getString(R.string.notModified_text)
                403 -> getString(R.string.permissionDenied_text)


                else -> getString(R.string.unknownError_text)
            }

            withContext(Dispatchers.Main) {
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun updateUserPosition(
        latitude: Long,
        longitude: Long,
        userID: String,
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response =
                userController.updateUserGpsPosition(latitude, longitude, userID)

            when (response.code) {         // Response codes: 200 = deleted user, 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> {
                    toastMessage = getString(R.string.updatedPosition_text)
                }

                403 -> toastMessage = getString(R.string.permissionDenied_text)


                else -> toastMessage = getString(R.string.unknownError_text)
            }

            withContext(Dispatchers.Main) {
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }


    /**
     * this event will enable the back function to the button on press
     * @param item MenuItem
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                switchActivity(LoginActivity::class.java)
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