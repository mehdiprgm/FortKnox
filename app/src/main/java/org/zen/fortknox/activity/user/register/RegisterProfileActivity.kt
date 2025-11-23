package org.zen.fortknox.activity.user.register

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
import org.zen.fortknox.viewmodel.ApiViewModel
import org.zen.fortknox.viewmodel.DatabaseViewModel

class RegisterProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityRegisterProfileBinding
    private lateinit var viewPagerAdapter: RegisterProfileAdapter

    /* Initialize newUser with default values to avoid lateinit issues */
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
                createNewUser()
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

    private suspend fun isUserCreatedSuccessfully(): Boolean {
        return try {
            val response = apiViewModel.addUser(newUser!!)
            response.isSuccess
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    private fun createNewUser() {
        val fragment = viewPagerAdapter.getFragment(this, currentFragmentIndex)

        if (fragment is RegisterProfileBasicInformationFragment) {
            if (fragment.isFormInformationValid()) {
                newUser = fragment.getUser() // This initializes newUser
                goToNextFragment()
            }
        } else if (fragment is RegisterProfileCompleteSetupFragment) {
            if (fragment.isFormInformationValid()) {/* Complete the user information */
                if (isInternetConnected(this)) {
                    val loadingDialog = Dialogs.load(
                        this, "Completing setup", "Please wait until we create new account"
                    )

                    loadingDialog.show()
                    lifecycleScope.launch {
                        try {
                            val tmpUser = fragment.getUser()

                            /* Use safe calls since newUser is nullable and assemble the user */
                            newUser = newUser?.copy(
                                securityCode = tmpUser.securityCode,
                                emailAddress = tmpUser.emailAddress,
                                is2FAActivated = tmpUser.is2FAActivated,
                                imagePath = "null"
                            )

                            /* Send request to check if user exists in database or not */
                            val requestResult = apiViewModel.getUser(newUser!!.username)

                            requestResult.onSuccess { user ->
                                loadingDialog.dismiss()

                                /* User found in the database, so show the message to select another username */
                                if (user.username == newUser!!.username) {
                                    Dialogs.showMessage(
                                        this@RegisterProfileActivity,
                                        "Username exists",
                                        "This username is already taken by another person",
                                        DialogType.Error
                                    )
                                } else {
                                    if (isUserCreatedSuccessfully()) {
                                        withContext(Dispatchers.Main) {
                                            Dialogs.showNewProfileCreated(
                                                this@RegisterProfileActivity, newUser!!.username
                                            )
                                            finish()
                                        }
                                    } else {
                                        Dialogs.showMessage(
                                            this@RegisterProfileActivity,
                                            "Setup Failed",
                                            "Failed to create user profile. Please try again.",
                                            DialogType.Error
                                        )
                                    }
                                }
                            }

                            /* Show errors */
                            requestResult.onFailure { ex ->
                                Dialogs.showException(
                                    this@RegisterProfileActivity, ex as Exception
                                )
                            }
                        } catch (ex: Exception) {
                            loadingDialog.dismiss()
                            Dialogs.showException(this@RegisterProfileActivity, ex)
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        Dialogs.showNoInternetConnection(this@RegisterProfileActivity)
                    }
                }
            }
        }
    }
}