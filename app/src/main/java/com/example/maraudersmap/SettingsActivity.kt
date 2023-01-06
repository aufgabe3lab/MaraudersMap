package com.example.maraudersmap

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.maraudersmap.LoginActivity.UserInformation.description
import com.example.maraudersmap.LoginActivity.UserInformation.privacyRadius
import com.example.maraudersmap.LoginActivity.UserInformation.userID
import kotlinx.coroutines.*
import okhttp3.Response


/**
 * Provides the user with a way to change their user settings, such as their password, description, and privacy radius.
 * @author Felix Kuhbier & Julian Ertle
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
        var visibilityRadius: Int = 5               // Standard search radius set to 5
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

                    var collectedPrivacyRadius: Long? = null
                    if(privacyRadiusEditText.text.isNotEmpty()){
                        collectedPrivacyRadius = privacyRadiusEditText.text.toString().toLong()
                    }
                    val collectedNewPassword = changePasswordEditText.text.toString()
                    val collectedDescription = descriptionEditText.text.toString()
                    val collectedInterval = intervalEditText.text.toString()
                    val collectedVisibleRadius = visibilityRadiusEditText.text.toString()

                    changeUserStoredServerData(collectedNewPassword,collectedPrivacyRadius,collectedDescription, userID)
                    changeUserInterval(collectedInterval)
                    changeUserVisibilityRadius(collectedVisibleRadius)

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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.settings_menu,menu)
        return super.onCreateOptionsMenu(menu)
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
                403  -> ({
                    getString(R.string.permissionDenied_text)
                    switchActivity(LoginActivity::class.java)
                }).toString()
                else -> getString(R.string.unknownError_text)
            }

            withContext(Job() + Dispatchers.Main) {
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    /**
     * Changes the information of a user that are stored on the backend database and informs the user
     * if the request was successful or not via toast messages.
     *
     * @param newPassword The new password for the user.
     * @param privacyRadius The new privacy radius for the user.
     * @param description The new description for the user.
     * @param userID The ID of the user whose data is being changed.
     */
    private fun changeUserStoredServerData(newPassword: String?, privacyRadius: Long?, description: String?, userID: String?){

        if(privacyRadius!=null || newPassword != "" || description != ""){
            val scope = CoroutineScope(Job() + Dispatchers.IO)
            scope.launch {
                response = userControllerAPI.changeUserStoredServerData(newPassword,privacyRadius,description,userID)

                when (response.code) {

                    200 -> {
                        toastMessage = getString(R.string.successfulModification_text)

                        if(changePasswordEditText.text.isNotEmpty()){
                            changePassword()
                        }
                        if(descriptionEditText.text.isNotEmpty()){
                            changeDescription(description)
                        }
                        if(privacyRadiusEditText.text.isNotEmpty()){ // needs to be checked after the changePassword() and changeDescription method got called to avoid bugs
                            changePrivacyRadius(privacyRadius)
                        }
                    }

                    303  -> toastMessage = getString(R.string.notModified_text)
                    403  -> {
                        toastMessage = getString(R.string.permissionDenied_text)
                        switchActivity(LoginActivity::class.java)
                    }
                    else -> toastMessage = getString(R.string.unknownError_text)
                }

                withContext(Job() + Dispatchers.Main) {
                    makeToast(toastMessage, Toast.LENGTH_SHORT)
                }
            }
        }
    }

    /**
     * Saves the new interval and informs the user about it
     * and clears the EditText field.
     *
     * @param newInterval The new interval for the user.
     */
    private fun changeUserInterval(newInterval: String ){
        if (newInterval != ""){
            interval = newInterval.toLong()
            intervalEditText.hint = interval.toString() + " seconds"
            makeToast(getString(R.string.savedInterval_text), Toast.LENGTH_SHORT)
            intervalEditText.text.clear()
        }
    }

    /**
     * Saves the new radius and informs the user about it
     * and clears the EditText field.
     *
     * @param newRadius The new interval for the user.
     */
    private fun changeUserVisibilityRadius(newRadius: String) {
        if (newRadius != "") {
            val newRadiusInt: Int = newRadius.toInt()
            visibilityRadius = newRadiusInt
            visibilityRadiusEditText.hint = newRadiusInt.toString() + " km"
            makeToast(getString(R.string.savedRadius_text), Toast.LENGTH_SHORT)
            visibilityRadiusEditText.text.clear()
        }
    }

    /**
     * Saves the new description and converts it into a displayed hint for the user
     * and clears the EditText field.
     *
     * @param description The new description for the user.
     */
    private fun changeDescription(description: String?) {

        LoginActivity.description = description
        descriptionEditText.hint = description  // convert text of EditText into a hint
        descriptionEditText.text.clear()        // clearing so text is empty and when using the save button a 2nd time the device doesn't send it again
        if(privacyRadiusEditText.text.isEmpty()){
            privacyRadius = 0                   // server sets privacy radius to 0 after the description got changed, seems to be a bug on the backend
            privacyRadiusEditText.hint = privacyRadius.toString() + " km "
        }
    }

    /**
     * Saves the new privacy Radius and converts it into a displayed hint for the user
     * and clears the EditText field.
     *
     * @param privacyRadius The new privacy radius for the user.
    */
    private fun changePrivacyRadius(privacyRadius: Long?) {
        LoginActivity.privacyRadius = privacyRadius
        privacyRadiusEditText.hint = privacyRadius.toString() + " km"
        privacyRadiusEditText.text.clear()
    }


    /**
     * Clears the EditText field
     */
    private fun changePassword() {
        changePasswordEditText.text.clear()     // clearing so text is empty and when using the save button a 2nd time the device doesn't have to send it again
            if(privacyRadiusEditText.text.isEmpty()){
                privacyRadius = 0                   // server sets privacy radius to 0 after the password got changed, seems to be a bug on the backend
                privacyRadiusEditText.hint = privacyRadius.toString() + " km "
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

            R.id.menu_logOut -> {
                userID = null
                switchActivity(LoginActivity::class.java)
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
        //privacyRadiusEditText.setText(privacyRadius.toString(), TextView.BufferType.EDITABLE)
        privacyRadiusEditText.hint = privacyRadius.toString() + " km"
        //descriptionEditText.setText(description, TextView.BufferType.EDITABLE)
        descriptionEditText.hint = description
        //visibilityRadiusEditText.setText(visibilityRadius.toString(), TextView.BufferType.EDITABLE)
        visibilityRadiusEditText.hint = visibilityRadius.toString() + " km"

        intervalEditText.hint = interval.toString() + " seconds"

    }
}
