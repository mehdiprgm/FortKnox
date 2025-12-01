package org.zen.fortknox.activity.user.register

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.zen.fortknox.R
import org.zen.fortknox.adapter.viewpager.RegisterProfileAdapter
import org.zen.fortknox.api.entity.ApiUser
import org.zen.fortknox.databinding.ActivityRegisterProfileBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.fragment.register.RegisterProfileBasicInformationFragment
import org.zen.fortknox.fragment.register.RegisterProfileCompleteSetupFragment
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.isInternetConnected
import org.zen.fortknox.tools.uriToFile
import org.zen.fortknox.viewmodel.ApiViewModel
import org.zen.fortknox.viewmodel.DatabaseViewModel
import java.util.UUID

class RegisterProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityRegisterProfileBinding
    private lateinit var viewPagerAdapter: RegisterProfileAdapter

    private var newUser: ApiUser? = null

    private lateinit var apiViewModel: ApiViewModel
    private lateinit var databaseViewModel: DatabaseViewModel

    var currentFragmentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityRegisterProfileBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        initViewModels()
        setupViewPager()

        b.btnNext.setOnClickListener(this)
        b.btnBack.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnNext -> {
                val fragment = viewPagerAdapter.getFragment(this, currentFragmentIndex)

                if (fragment is RegisterProfileBasicInformationFragment) {
                    if (fragment.isFormInformationValid()) {
                        newUser = fragment.getUser() /* Initialize new user */
                        goToNextFragment()
                    }
                } else if (fragment is RegisterProfileCompleteSetupFragment) {
                    if (fragment.isFormInformationValid()) {/* Complete the user information */

                        if (isInternetConnected(this)) {
                            // بررسی کنید که imageUri قابل Null نیست، زیرا قبلاً isFormInformationValid بررسی شده است
                            fragment.imageUri?.let { uri ->
                                createNewUser(fragment.getUser(), uri)
                            }
                        } else {
                            lifecycleScope.launch {
                                Dialogs.showNoInternetConnection(this@RegisterProfileActivity)
                            }
                        }
                    }
                }
            }

            R.id.btnBack -> {
                goToPreviousFragment()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        /* Save the current fragment index, so when activity re-create itself, we can read it and show the right fragment to the user */
        outState.putInt("CurrentFragmentIndex", currentFragmentIndex)
        outState.putParcelable("ApiUser", newUser)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        /* Get the index and update the UI */
        currentFragmentIndex = savedInstanceState.getInt("CurrentFragmentIndex")
        newUser = savedInstanceState.getParcelable("ApiUser")

        updateScreenInformation()
    }

    private fun initViewModels() {
        apiViewModel = ViewModelProvider(this)[ApiViewModel::class.java]
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private fun setupViewPager() {
        viewPagerAdapter = RegisterProfileAdapter(this)
        b.vpFragments.apply {
            adapter = viewPagerAdapter
            isUserInputEnabled = false /* Disable swipe gestures */
        }
    }

    private fun goToPreviousFragment() {
        if (currentFragmentIndex == 1) {
            b.vpFragments.currentItem = 0
            currentFragmentIndex = 0
            updateScreenInformation()
        }
    }

    private fun goToNextFragment() {
        if (currentFragmentIndex == 0) {
            b.vpFragments.currentItem = 1
            currentFragmentIndex = 1
            updateScreenInformation()
        }
    }

    private fun updateScreenInformation() {
        b.btnBack.isEnabled = currentFragmentIndex != 0

        if (currentFragmentIndex == 0) {
            b.btnBack.setTextColor(getColor(R.color.gray))
            b.btnNext.text = "Next"

            b.viewSep2.setBackgroundColor(getColor(R.color.background))
            b.tvCompleteSetup.setTextColor(getColor(R.color.foreground_less))
        } else if (currentFragmentIndex == 1) {
            b.btnBack.setTextColor(getColor(R.color.theme))
            b.btnNext.text = "Finish"

            b.viewSep2.setBackgroundColor(getColor(R.color.theme))
            b.tvCompleteSetup.setTextColor(getColor(R.color.foreground))
        }
    }

    /**
     * Attempts to create the user and returns the ApiUser object received from the server (which includes the ID).
     * This function calls the server endpoint /users/add.
     */
    private suspend fun addNewUserToDatabase(user: ApiUser): Result<ApiUser> {
        return try {
            apiViewModel.addUser(user)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Result.failure(ex)
        }
    }

    private fun assembleUser(apiUser: ApiUser): ApiUser {/* Assemble the new user object */
        val user = newUser?.copy(
            securityCode = apiUser.securityCode,
            emailAddress = apiUser.emailAddress,
            is2FAActivated = apiUser.is2FAActivated,
            imagePath = "null"
        )

        return user!!
    }

    private fun createNewUser(apiUser: ApiUser, imageUri: Uri) {
        val loadingDialog = Dialogs.load(
            this, "Completing setup", "Please wait until we create new account"
        )

        loadingDialog.show()
        lifecycleScope.launch {
            try {/* Assemble the new user object */
                val userToCreate = assembleUser(apiUser)

                /* Check to see if user exists */
                val checkUserResult = apiViewModel.getUser(userToCreate.username)

                checkUserResult.onSuccess { existingUser ->
                    if (existingUser.username == userToCreate.username) {
                        loadingDialog.dismiss()

                        Dialogs.showMessage(
                            this@RegisterProfileActivity,
                            "Username exists",
                            "This username is already taken by another person",
                            DialogType.Error
                        )
                    } else {/* Add user to the database */
                        val createUserResult = addNewUserToDatabase(userToCreate)
                        createUserResult.onSuccess { createdUser ->
                            /* Get the new user (userId) */
                            val newUserId = createdUser.id

                            /* Let function makes sure user created and value is not null */
                            val imageFinalName = newUserId?.let {
                                if (it > 0) {/* User image file name */
                                    "$newUserId.png"
                                } else {
                                    loadingDialog.dismiss()

                                    Dialogs.showMessage(
                                        this@RegisterProfileActivity,
                                        "Setup Failed",
                                        "Failed to receive a valid User ID after creation.",
                                        DialogType.Error
                                    )

                                    return@onSuccess
                                }
                            }

                            /* Convert uri to the file */
                            val imageFile = withContext(Dispatchers.IO) {
                                /* Create temp image file */
                                uriToFile(
                                    this@RegisterProfileActivity,
                                    imageUri,
                                    UUID.randomUUID().toString() + ".png"
                                )
                            }
                            if (imageFile != null) {
                                try {/* Upload file into server using user (userId) */
                                    val uploadImageResult = apiViewModel.uploadImage(
                                        imageFile, imageFinalName.toString()
                                    )

                                    uploadImageResult.onSuccess { result ->
                                        /* Get the image url from server */
                                        createdUser.imagePath = result.data

                                        /* Update user imagePath */
                                        val updateUserResult = apiViewModel.updateUser(
                                            createdUser.username, createdUser
                                        )

                                        updateUserResult.onSuccess {
                                            withContext(Dispatchers.Main) {
                                                /* Everything is ok, close activity */
                                                loadingDialog.dismiss()

                                                Dialogs.showNewProfileCreated(
                                                    this@RegisterProfileActivity,
                                                    userToCreate.username
                                                )

                                                finish()
                                            }
                                        }

                                        updateUserResult.onFailure { ex ->
                                            loadingDialog.dismiss()
                                            Dialogs.showException(
                                                this@RegisterProfileActivity, ex as Exception
                                            )
                                        }
                                    }

                                    uploadImageResult.onFailure { ex ->
                                        loadingDialog.dismiss()

                                        Dialogs.showException(
                                            this@RegisterProfileActivity, ex as Exception
                                        )
                                    }
                                } finally {/* Delete temp file */
                                    imageFile.delete()
                                }
                            } else {
                                loadingDialog.dismiss()

                                Dialogs.showMessage(
                                    this@RegisterProfileActivity,
                                    "Setup Failed",
                                    "Could not process image file.",
                                    DialogType.Error
                                )
                            }
                        }

                        createUserResult.onFailure { ex ->
                            loadingDialog.dismiss()
                            Dialogs.showException(this@RegisterProfileActivity, ex as Exception)
                        }
                    }
                }

                /* Show errors for Check User (Step 1 failure) */
                checkUserResult.onFailure { ex ->
                    loadingDialog.dismiss()
                    Dialogs.showException(this@RegisterProfileActivity, ex as Exception)
                }
            } catch (ex: Exception) {
                loadingDialog.dismiss()
                Dialogs.showException(this@RegisterProfileActivity, ex)
            }
        }
    }
}
