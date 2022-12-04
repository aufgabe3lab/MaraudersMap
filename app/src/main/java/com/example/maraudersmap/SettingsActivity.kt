package com.example.maraudersmap

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.maraudersmap.LoginActivity.Companion.userID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response

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
    private lateinit var toastMessage: String
    private lateinit var description: EditText

    companion object{
        var interval: Long? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initSettings()

        autoSendPosSetting(autoSendPosSwitch)
        descriptionSetting(description)
        intervalSetting(intervalEditText)
        privacyRadiusSetting(privacyRadiusEditText)
        changePasswordSetting(changePassword)
        deleteAccountSetting(deleteButton)

    }

    private fun autoSendPosSetting(autoSendPosSwitchCompat: SwitchCompat){
        autoSendPosSwitchCompat.setOnCheckedChangeListener{_, isChecked->

            intervalEditText.isEnabled = isChecked

        }
    }

    private fun descriptionSetting(descriptionEditText: EditText){
        descriptionEditText.setOnClickListener {
            val editText = EditText(this@SettingsActivity)
            editText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            editText.height = 250
            editText.isSingleLine = false

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle("Description")
                .setView(editText)
                .setPositiveButton("Save") { dialog, id ->

                    changeDescription(editText.text.toString(), userID!!)

                    dialog.dismiss()

                }
                .setNegativeButton("Cancel") { dialog, which ->
                    editText.text.clear()
                    dialog.cancel()
                }
                .show()

        }
    }

    private fun intervalSetting(intervalSettingEditText: EditText){
        intervalSettingEditText.setOnClickListener {
            val editText = EditText(this@SettingsActivity)
            editText.inputType = InputType.TYPE_NUMBER_VARIATION_NORMAL
            editText.isSingleLine = true

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle("Interval")
                .setView(editText)
                .setPositiveButton("Save") { dialog, id ->

                    if (editText.text.toString().isEmpty() || editText.text.toString().isBlank()) {
                        makeToast(getString(R.string.invalidInterval_text), Toast.LENGTH_SHORT)
                    } else {

                        interval = editText.text.toString().toLong()
                        dialog.dismiss()
                    }


                }
                .setNegativeButton("Cancel") { dialog, which ->
                    editText.text.clear()
                    dialog.cancel()
                }
                .show()
        }
    }

    private fun changePasswordSetting(changePasswordEditText: EditText){
        changePasswordEditText.setOnClickListener {
            val editText = EditText(this@SettingsActivity)
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.isSingleLine = true

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle("Change Password")
                .setView(editText)
                .setPositiveButton("Save") { dialog, id ->

                    if(editText.text.toString().isEmpty() || editText.text.toString().isBlank()){
                        makeToast(getString(R.string.invalidPassword_text), Toast.LENGTH_SHORT)
                    }else{

                        changePassword(editText.text.toString(), userID!!)
                        dialog.dismiss()
                    }


                }
                .setNegativeButton("Cancel") { dialog, which ->
                    editText.text.clear()
                    dialog.cancel()
                }
                .show()
        }
    }

    private fun deleteAccountSetting(deleteAccountButton: Button){
        deleteAccountButton.setOnClickListener {
            deleteUser(userID!!)
        }
    }

    private fun privacyRadiusSetting(privacyRadiusEditText: EditText){
        privacyRadiusEditText.setOnClickListener {
            val editText = EditText(this@SettingsActivity)
            editText.inputType = InputType.TYPE_NUMBER_VARIATION_NORMAL
            editText.isSingleLine = true

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle("Privacy Radius")
                .setView(editText)
                .setPositiveButton("Save") { dialog, id ->

                    if (editText.text.toString().isEmpty() || editText.text.toString().isBlank()) {
                        makeToast(getString(R.string.invalidRadius_text), Toast.LENGTH_SHORT)
                    } else {

                        changePrivacyRadius(editText.text.toString().toLong(), userID!!)
                        dialog.dismiss()
                    }


                }
                .setNegativeButton("Cancel") { dialog, which ->
                    editText.text.clear()
                    dialog.cancel()
                }
                .show()
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

            toastMessage = when (response.code) {         // Response codes: 200 = Password changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> {
                    getString(R.string.passwordChanged_text)
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

    private fun updateUserPosition(latitude: Long, longitude: Long, userID: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response =
                userController.updateUserGpsPosition(latitude, longitude, userID)

            toastMessage = when (response.code) {         // Response codes: 200 = deleted user, 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> {
                    getString(R.string.updatedPosition_text)
                }

                403 -> getString(R.string.permissionDenied_text)


                else -> getString(R.string.unknownError_text)
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

    private fun initSettings(){
        autoSendPosSwitch = findViewById(R.id.autoSendPos_switch)
        intervalEditText = findViewById(R.id.interval_editTextNumber)
        privacyRadiusEditText = findViewById(R.id.privacyRadius_editTextNumber)
        deleteButton =  findViewById(R.id.deleteAccount_button)
        changePassword = findViewById(R.id.changePassword_editText)
        description = findViewById(R.id.changeDescription_editText)

        intervalEditText.isEnabled = false
        intervalEditText.isFocusable = false
        intervalEditText.isClickable = true

        privacyRadiusEditText.isFocusable = false
        privacyRadiusEditText.isClickable = true

        changePassword.isFocusable = false
        changePassword.isClickable = true

        description.isFocusable = false
        description.isClickable = true

    }
}