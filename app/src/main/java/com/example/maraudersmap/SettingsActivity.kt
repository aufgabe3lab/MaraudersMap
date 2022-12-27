package com.example.maraudersmap

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.maraudersmap.LoginActivity.UserInformation.description
import com.example.maraudersmap.LoginActivity.UserInformation.privacyRadius
import com.example.maraudersmap.LoginActivity.UserInformation.userID
import kotlinx.coroutines.*
import okhttp3.Response


/**
 * Provides the user with a way to change their user settings, such as their password, description, and privacy radius.
 * @author Felix Kuhbier
 * @since 2022.12.15
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var intervalEditText: EditText
    private lateinit var privacyRadiusEditText: EditText
    private lateinit var deleteButton: Button
    private lateinit var changePasswordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var descriptionEditText: EditText
    private lateinit var visibilityRadiusEditText: EditText
    private lateinit var descriptionTextView: TextView

    private lateinit var userControllerAPI: UserControllerAPI
    private lateinit var response: Response

    private var toastMessage: String = ""

    companion object SettingsCompanion {
        var interval: Long = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initSettings()

        descriptionEditText.setOnClickListener {
            val dialogChangeDescriptionEditText = EditText(this@SettingsActivity)
            dialogChangeDescriptionEditText.filters =
                arrayOf<InputFilter>(InputFilter.LengthFilter(255))
            dialogChangeDescriptionEditText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            dialogChangeDescriptionEditText.height = 250
            dialogChangeDescriptionEditText.isSingleLine = false
            dialogChangeDescriptionEditText.gravity = Gravity.TOP

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(getString(R.string.descriptionSetting_headerText))
                .setView(dialogChangeDescriptionEditText)
                .setPositiveButton(getString(R.string.saveSetting_text)) { dialog, _ ->
                    updateEditTextContent(descriptionEditText, dialogChangeDescriptionEditText)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancelSetting_text)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        deleteButton.setOnClickListener {
            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(getString(R.string.deleteAccount_headerText))
                .setMessage(getString(R.string.deleteAccount_messageText))
                .setPositiveButton(getString(R.string.yes_dialogText)) { dialog, _ ->
                    deleteUser(userID!!)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no_dialogText)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }


        saveButton.setOnClickListener {

            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(getString(R.string.saveChanges_headerText))
                .setMessage(getString(R.string.saveChanges_messageText))
                .setPositiveButton(getString(R.string.yes_dialogText)) { dialog, _ ->

                    changePrivacyRadius(privacyRadiusEditText.text.toString().toLong(), userID)
                    changePassword(changePasswordEditText.text.toString(), userID)
                    changeDescription(descriptionEditText.text.toString(), userID)
                    if (intervalEditText.text.isNotEmpty()) {

                        interval = intervalEditText.text.toString().toLong()
                    } else {
                        interval = 0L
                    }
                    makeToast(getString(R.string.saved_messageText), Toast.LENGTH_SHORT)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no_dialogText)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }


        descriptionTextView.setOnClickListener {
            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(getString(R.string.currentDescription_headerText))
                .setMessage(description)
                .setNegativeButton(getString(R.string.close_dialogText)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

    }


    /**
     * Updates the content of an edit text with the content of another edit text.
     *
     * @param editTextToUpdate The edit text to be updated.
     * @param editText The edit text whose content will be used to update the other edit text.
     */
    private fun updateEditTextContent(editTextToUpdate: EditText, editText: EditText) {
        editTextToUpdate.text = editText.text
    }

    /**
     * Deletes a user with the specified ID.
     *
     * @param userID The ID of the user to be deleted.
     *
     */
    private fun deleteUser(userID: String?) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            response = userControllerAPI.deleteUser(userID)

            toastMessage = when (response.code) {
                // Response codes:
                // 200 = deleted user,
                // 403 = permission denied (forbidden, json token invalid),
                // else = other unknown error codes possible
                200 -> getString(R.string.deletedUser_text)
                403 -> getString(R.string.permissionDenied_text)
                else -> getString(R.string.unknownError_text)
            }

            withContext(Job() + Dispatchers.Main) {
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    /**
     * Changes the description of the user with the specified ID.
     *
     * @param description The new description for the user.
     * @param userID The ID of the user whose description is being changed.
     *
     */
    private fun changeDescription(description: String?, userID: String?) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {

            response = userControllerAPI.changeUserDescription(description, userID)

            when (response.code) {
                // Response codes:
                // 200 = description changed,
                // 304 = no changes were made (not-modified),
                // 403 = permission denied (forbidden, json token invalid),
                // else = other unknown error codes possible
                200 -> { Log.i(SettingsActivity::class.java.simpleName, getString(R.string.descriptionChanged_text)).toString()
                    LoginActivity.description = description
                }
                304 -> {
                    withContext(Job() + Dispatchers.Main) {
                        makeToast(
                            getString(R.string.notModifiedDescription_text),
                            Toast.LENGTH_SHORT
                        )
                    }
                }
                403 -> {
                    withContext(Job() + Dispatchers.Main) {
                        makeToast(getString(R.string.permissionDenied_text), Toast.LENGTH_SHORT)
                    }
                }


                else -> {
                    withContext(Job() + Dispatchers.Main) {
                        makeToast(getString(R.string.unknownError_text), Toast.LENGTH_SHORT)
                    }
                }

            }

        }

    }


    /**
     * Changes the privacy radius of the user with the specified ID.
     *
     * @param privacyRadius The new privacy radius for the user.
     * @param userID The ID of the user whose privacy radius is being changed.
     *
     */
    private fun changePrivacyRadius(privacyRadius: Long?, userID: String?) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            response = userControllerAPI.changeUserPrivacyRadius(privacyRadius, userID)

            when (response.code) {
                // Response codes:
                // 200 = privacy radius changed,
                // 304 = no changes were made (not-modified),
                // 403 = permission denied (forbidden, json token invalid),
                // else = other unknown error codes possible
                200 -> {
                    Log.i(SettingsActivity::class.java.simpleName, getString(R.string.privacyRadiusChanged_text)).toString()
                    LoginActivity.privacyRadius = privacyRadius
                }
                304 -> {

                    withContext(Job() + Dispatchers.Main) {
                        makeToast(
                            getString(R.string.notModifiedPrivacyRadius_text),
                            Toast.LENGTH_SHORT
                        )
                    }
                }
                403 -> {
                    withContext(Job() + Dispatchers.Main) {
                        makeToast(getString(R.string.permissionDenied_text), Toast.LENGTH_SHORT)
                    }
                }


                else -> {
                    withContext(Job() + Dispatchers.Main) {
                        makeToast(getString(R.string.unknownError_text), Toast.LENGTH_SHORT)
                    }
                }
            }


        }
    }

    /**
     * Changes the password of the user with the specified ID.
     *
     * @param newPassword The new password for the user.
     * @param userID The ID of the user whose password is being changed.
     *
     */
    private fun changePassword(newPassword: String?, userID: String?) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            response = userControllerAPI.changeUserPassword(newPassword, userID)

            when (response.code) {
                // Response codes:
                // 200 = Password changed,
                // 304 = no changes were made (not-modified),
                // 403 = permission denied (forbidden, json token invalid),
                // else = other unknown error codes possible
                200 -> Log.i(
                    SettingsActivity::class.java.simpleName,
                    getString(R.string.passwordChanged_text)
                )
                    .toString()
                304 -> {
                    withContext(Job() + Dispatchers.Main) {
                        makeToast(getString(R.string.notModifiedPassword_text), Toast.LENGTH_SHORT)
                    }
                }
                403 -> {
                    withContext(Job() + Dispatchers.Main) {
                        makeToast(getString(R.string.permissionDenied_text), Toast.LENGTH_SHORT)
                    }
                }


                else -> {
                    withContext(Job() + Dispatchers.Main) {
                        makeToast(getString(R.string.unknownError_text), Toast.LENGTH_SHORT)
                    }
                }
            }

        }
    }

    /**
     * Handles the selection of an item in the options menu.
     *
     * @param item The selected item in the options menu.
     *
     * @return A boolean indicating whether the selection was handled successfully.
     * Possible return values are:
     * - `true`: The selection was handled successfully and the activity should close the options menu.
     * - `false`: The selection was not handled successfully and the options menu should remain open.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                switchActivity(MapActivity::class.java)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Displays a toast message with the specified text and duration.
     *
     * @param msg The text to be displayed in the toast message.
     * @param duration The duration for which the toast message should be displayed.
     */
    private fun makeToast(msg: String, duration: Int) {
        Toast.makeText(this@SettingsActivity, msg, duration).show()
    }

    /**
     * Switches the current activity to the specified destination activity.
     *
     * @param destinationClass The destination activity to switch to.
     */
    private fun switchActivity(destinationClass: Class<*>) {
        val intent = Intent(this@SettingsActivity, destinationClass)
        startActivity(intent)
    }

    /**
     * Initializes the settings in the activity
     */
    private fun initSettings() {
        intervalEditText = findViewById(R.id.interval_editTextNumber)
        privacyRadiusEditText = findViewById(R.id.privacyRadius_editTextNumber)
        deleteButton = findViewById(R.id.deleteAccount_button)
        changePasswordEditText = findViewById(R.id.changePassword_editText)
        descriptionEditText = findViewById(R.id.changeDescription_editText)
        saveButton = findViewById(R.id.saveSettings_button)
        userControllerAPI = UserControllerAPI()
        visibilityRadiusEditText = findViewById(R.id.radiusVisibilty_editTextNumber)
        descriptionTextView = findViewById(R.id.changeDescription_textView)

        descriptionEditText.isFocusable = false
        privacyRadiusEditText.setText(privacyRadius.toString(), TextView.BufferType.EDITABLE)
        descriptionEditText.setText(description, TextView.BufferType.EDITABLE)

    }
}
