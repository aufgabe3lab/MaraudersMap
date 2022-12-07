package com.example.maraudersmap

import android.app.AlertDialog
import android.content.Intent
import android.icu.number.NumberFormatter
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar.DisplayOptions
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.maraudersmap.LoginActivity.Companion.userID
import okhttp3.Response
import com.example.maraudersmap.LoginActivity.Companion.description
import com.example.maraudersmap.LoginActivity.Companion.privacyRadius
import kotlinx.coroutines.*

/**
 * Provides functions to individualize the app
 * @author Felix Kuhbier
 * @since 2022.12.04
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var autoSendPosSwitch: SwitchCompat
    private lateinit var intervalEditText: EditText
    private lateinit var privacyRadiusEditText: EditText
    private lateinit var deleteButton: Button
    private lateinit var changePassword: EditText
    private lateinit var saveButton: Button
    private lateinit var descriptionEditText: EditText

    private lateinit var toastMessage: String

    private var descriptionString: String? = null
    private var privacyRadiusString: Long? = null
    private var newPasswordString: String? = null
    private var interval: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initSettings()

        autoSendPosSetting(autoSendPosSwitch)
        descriptionSetting(descriptionEditText)
        intervalSetting(intervalEditText)
        privacyRadiusSetting(privacyRadiusEditText)
        changePasswordSetting(changePassword)
        deleteAccountSetting(deleteButton)
        saveSettings(userID!!, saveButton)


    }

    private fun autoSendPosSetting(autoSendPosSwitchCompat: SwitchCompat) {
        autoSendPosSwitchCompat.setOnCheckedChangeListener { _, isChecked ->

            intervalEditText.isEnabled = isChecked

        }
    }


    private fun descriptionSetting(descriptionEditText: EditText) {
        descriptionEditText.setOnClickListener {
            val editText = EditText(this@SettingsActivity)
            editText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            editText.height = 250
            editText.isSingleLine = false

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(getString(R.string.descriptionSetting_headerText))
                .setView(editText)
                .setPositiveButton(getString(R.string.saveSetting_text)) { dialog, _ ->

                    descriptionString = editText.text.toString()
                    updateEditText(editText.text.toString(), descriptionEditText)
                    dialog.dismiss()

                }
                .setNegativeButton(getString(R.string.cancelSetting_text)) { dialog, _ ->
                    editText.text.clear()
                    dialog.cancel()
                }
                .show()


        }


    }

    private fun updateEditText(text: String?, editText: EditText) {
        editText.text.clear()
        editText.setText(text)
    }

    private fun intervalSetting(intervalSettingEditText: EditText) {
        intervalSettingEditText.setOnClickListener {
            val editText = EditText(this@SettingsActivity)
            editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            editText.isSingleLine = true

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(getString(R.string.intervalSetting_headerText))
                .setView(editText)
                .setPositiveButton(getString(R.string.saveSetting_text)) { dialog, _ ->

                    if (editText.text.toString().isEmpty() || editText.text.toString().isBlank()) {
                        makeToast(getString(R.string.invalidInterval_text), Toast.LENGTH_SHORT)
                    } else {

                        interval = editText.text.toString().toLong()
                        dialog.dismiss()
                    }


                }
                .setNegativeButton(getString(R.string.cancelSetting_text)) { dialog, _ ->
                    editText.text.clear()
                    dialog.cancel()
                }
                .show()
        }
    }

    private fun changePasswordSetting(changePasswordEditText: EditText) {
        changePasswordEditText.setOnClickListener {
            val editText = EditText(this@SettingsActivity)
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.isSingleLine = true

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(getString(R.string.changePasswordSetting_headerText))
                .setView(editText)
                .setPositiveButton(getString(R.string.saveSetting_text)) { dialog, _ ->

                    if (editText.text.toString().isEmpty() || editText.text.toString().isBlank()) {
                        makeToast(getString(R.string.invalidPassword_text), Toast.LENGTH_SHORT)
                    } else {

                        newPasswordString = editText.text.toString()
                        dialog.dismiss()
                    }


                }
                .setNegativeButton(getString(R.string.cancelSetting_text)) { dialog, _ ->
                    editText.text.clear()
                    dialog.cancel()
                }
                .show()
        }
    }

    private fun deleteAccountSetting(deleteAccountButton: Button) {
        deleteAccountButton.setOnClickListener {
            deleteUser(userID!!)
        }
    }

    private fun privacyRadiusSetting(privacyRadiusEditText: EditText) {
        privacyRadiusEditText.setOnClickListener {
            try {
                val editText = EditText(this@SettingsActivity)
                editText.inputType = InputType.TYPE_TEXT_VARIATION_NORMAL
                editText.setText(privacyRadius)

                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle(getString(R.string.privacyRadiusSetting_headerText))
                    .setView(editText)
                    .setPositiveButton(getString(R.string.saveSetting_text)) { dialog, _ ->

                        if (editText.text.toString().isEmpty() || editText.text.toString()
                                .isBlank()
                        ) {
                            makeToast(getString(R.string.invalidRadius_text), Toast.LENGTH_SHORT)
                        } else {

                            privacyRadiusString = editText.text.toString().toLong()
                            updateEditText(editText.text.toString(), privacyRadiusEditText)
                            dialog.dismiss()
                        }


                    }
                    .setNegativeButton(getString(R.string.cancelSetting_text)) { dialog, _ ->
                        editText.text.clear()
                        dialog.cancel()
                    }
                    .show()

            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

        }
    }


    private fun changePrivacyRadius(privacyRadius: Long?, userID: String) {
        val scope = CoroutineScope(Job() +  Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response = userController.changeUserPrivacyRadius(privacyRadius!!, userID)
            when (response.code) {         // Response codes: 200 = privacy radius changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.privacyRadiusChanged_text)
                ).toString()

                304 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.notModified_text)
                ).toString()
                403 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.permissionDenied_text)
                ).toString()


                else -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.unknownError_text)
                ).toString()
            }

        }
    }

    private fun deleteUser(userID: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response = userController.deleteUser(userID)

            toastMessage =
                when (response.code) {         // Response codes: 200 = deleted user, 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
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

    private fun changePassword(newPassword: String?, userID: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response = userController.changeUserPassword(newPassword!!, userID)

            when (response.code) {         // Response codes: 200 = Password changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.passwordChanged_text)
                ).toString()

                304 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.notModified_text)
                ).toString()
                403 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.permissionDenied_text)
                ).toString()


                else -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.unknownError_text)
                ).toString()
            }


        }
    }

    private fun changeDescription(description: String?, userID: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response = userController.changeUserDescription(description!!, userID)


            when (response.code) {         // Response codes: 200 = description changed, 304 = no changes were made (not-modified), 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.descriptionChanged_text)
                ).toString()

                304 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.notModified_text)
                ).toString()
                403 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.permissionDenied_text)
                ).toString()


                else -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.unknownError_text)
                ).toString()
            }


        }
    }

    private fun updateUserPosition(latitude: Long, longitude: Long, userID: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val userController = UserController()
            val response: Response =
                userController.updateUserGpsPosition(latitude, longitude, userID)


            when (response.code) {         // Response codes: 200 = deleted user, 403 = permission denied (forbidden, json token invalid), ? = other unknown error codes possible
                200 -> Log.i(SettingsActivity::class.java.simpleName, "200").toString()

                403 -> Log.i(SettingsActivity::class.java.simpleName, "403").toString()


                else -> Log.i(SettingsActivity::class.java.simpleName, "?").toString()
            }

        }
    }


    private fun saveSettings(userIDString: String, saveButton: Button) {

        saveButton.setOnClickListener {

            changePrivacyRadius(privacyRadiusString, userIDString)
            changePassword(newPasswordString, userIDString)
            changeDescription(descriptionString, userIDString)

            makeToast("Settings saved!", Toast.LENGTH_SHORT)


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
    private fun makeToast(msg: String, duration: Int) {
        Toast.makeText(this@SettingsActivity, msg, duration).show()
    }

    /**
     * Switch to activity
     * @param destinationClass destination activity
     */
    private fun switchActivity(destinationClass: Class<*>) {
        val intent = Intent(this@SettingsActivity, destinationClass)
        startActivity(intent)
    }

    private fun initSettings() {
        autoSendPosSwitch = findViewById(R.id.autoSendPos_switch)
        intervalEditText = findViewById(R.id.interval_editTextNumber)
        privacyRadiusEditText = findViewById(R.id.privacyRadius_editTextNumber)
        deleteButton = findViewById(R.id.deleteAccount_button)
        changePassword = findViewById(R.id.changePassword_editText)
        descriptionEditText = findViewById(R.id.changeDescription_editText)
        saveButton = findViewById(R.id.saveSettings_button)

        intervalEditText.isEnabled = false
        intervalEditText.isFocusable = false
        intervalEditText.isClickable = true

        privacyRadiusEditText.isFocusable = false
        privacyRadiusEditText.isClickable = true
        privacyRadiusEditText.setText(privacyRadius)

        changePassword.isFocusable = false
        changePassword.isClickable = true

        descriptionEditText.isFocusable = false
        descriptionEditText.isClickable = true
        descriptionEditText.setText(description)

        newPasswordString = ""
        privacyRadiusString = 0L
        descriptionString = ""
        toastMessage = ""

    }
}