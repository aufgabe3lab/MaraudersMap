package com.example.maraudersmap

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.maraudersmap.LoginActivity.Companion.description
import com.example.maraudersmap.LoginActivity.Companion.privacyRadius
import com.example.maraudersmap.LoginActivity.Companion.userID
import kotlinx.coroutines.*
import okhttp3.Response
import org.simpleframework.xml.strategy.Value

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
    private lateinit var changePasswordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var descriptionEditText: EditText
    private lateinit var visibilityRadiusEditText: EditText

    private lateinit var userController: UserController
    private lateinit var response: Response

    private var toastMessage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initSettings()

        autoSendPosSwitch.setOnCheckedChangeListener { _, isChecked ->

            intervalEditText.isEnabled = isChecked

        }


        descriptionEditText.setOnClickListener {
            val dialogChangeDescriptionEditText = EditText(this@SettingsActivity)
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
                .setPositiveButton(getString(R.string.yes_dialogText)) { dialog,_ ->

                    changePassword(changePasswordEditText.text.toString(), userID)
                    changeDescription(descriptionEditText.text.toString(), userID)
                    changePrivacyRadius(privacyRadiusEditText.text.toString().toLong(), userID)

                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no_dialogText)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

    }



    private fun updateEditTextContent(editTextToUpdate: EditText, editText: EditText){
        editTextToUpdate.text = editText.text
    }


    private fun deleteUser(userID: String?){
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            response = userController.deleteUser(userID)

            toastMessage = when (response.code){
                // Response codes:
                // 200 = deleted user,
                // 403 = permission denied (forbidden, json token invalid),
                // else = other unknown error codes possible
                200 -> getString(R.string.deletedUser_text)
                403 -> getString(R.string.permissionDenied_text)
                else -> getString(R.string.unknownError_text)
            }

            withContext(Job() + Dispatchers.Main){
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun changeDescription(description: String?, userID: String?){
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {

            response = userController.changeUserDescription(description, userID)

            toastMessage = when(response.code){
                // Response codes:
                // 200 = description changed,
                // 304 = no changes were made (not-modified),
                // 403 = permission denied (forbidden, json token invalid),
                // else = other unknown error codes possible
                200 -> getString(R.string.descriptionChanged_text)
                304 -> getString(R.string.notModified_text)
                403 -> getString(R.string.permissionDenied_text)


                else -> getString(R.string.unknownError_text)

            }

            withContext(Job() + Dispatchers.Main){
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }

    }

    private fun changePrivacyRadius(privacyRadius: Long?, userID: String?){
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch{
            response = userController.changeUserPrivacyRadius(privacyRadius, userID)

            toastMessage = when(response.code){
                // Response codes:
                // 200 = privacy radius changed,
                // 304 = no changes were made (not-modified),
                // 403 = permission denied (forbidden, json token invalid),
                // else = other unknown error codes possible
                200 -> getString(R.string.privacyRadiusChanged_text)
                304 -> getString(R.string.notModified_text)
                403 -> getString(R.string.permissionDenied_text)


                else -> getString(R.string.unknownError_text)
            }

            withContext(Job() + Dispatchers.Main) {
                makeToast(toastMessage, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun changePassword(newPassword: String?, userID: String?){
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch{
            response = userController.changeUserPassword(newPassword, userID)

            toastMessage = when(response.code){
                // Response codes:
                // 200 = Password changed,
                // 304 = no changes were made (not-modified),
                // 403 = permission denied (forbidden, json token invalid),
                // else = other unknown error codes possible
                200 -> getString(R.string.passwordChanged_text)
                304 -> getString(R.string.notModified_text)
                403 -> getString(R.string.permissionDenied_text)


                else -> getString(R.string.unknownError_text)
            }

            withContext(Job() + Dispatchers.Main) {
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
        changePasswordEditText = findViewById(R.id.changePassword_editText)
        descriptionEditText = findViewById(R.id.changeDescription_editText)
        saveButton = findViewById(R.id.saveSettings_button)
        userController = UserController()
        visibilityRadiusEditText = findViewById(R.id.radiusVisibilty_editTextNumber)

        descriptionEditText.setText(description, TextView.BufferType.EDITABLE)
        privacyRadiusEditText.setText(privacyRadius.toString(), TextView.BufferType.EDITABLE)


    }
}