package org.zen.fortknox.fragment.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import org.zen.fortknox.api.entity.ApiUser
import org.zen.fortknox.databinding.FragmentRegisterProfileBasicInformationBinding
import org.zen.fortknox.tools.formatting.PhoneNumberFormattingTextWatcher
import org.zen.fortknox.tools.getDate
import org.zen.fortknox.tools.showLimiter
import org.zen.fortknox.tools.validateData

class RegisterProfileBasicInformationFragment : Fragment() {
    private lateinit var b: FragmentRegisterProfileBasicInformationBinding

    companion object {
        fun newInstance() = RegisterProfileBasicInformationFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentRegisterProfileBasicInformationBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextFormatters()
        setupTextLimiters()
    }

    private fun setupTextFormatters() {
        b.txtPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    private fun setupTextLimiters() {
        b.txtPhoneNumber.showLimiter(textView = b.tvPhoneNumberLimiter, maxChars = 15)
    }

    fun isFormInformationValid(): Boolean {
        var score = 3

        score += b.txtLayUsername.validateData(
            b.txtUsername.text.toString().isEmpty(), "Username is empty"
        )

        score += b.txtLayPhoneNumber.validateData(
            b.txtPhoneNumber.text.toString().length != 15, "Phone number is not valid"
        )

        score += b.txtLayPassword.validateData(
            b.txtPassword.text.toString().isEmpty(), "Password is empty"
        )

        return score == 3
    }

    fun getUser() : ApiUser {
        val newUser = ApiUser(
            username = b.txtUsername.text.toString(),
            password = b.txtPassword.text.toString(),
            emailAddress = "null",
            phoneNumber = b.txtPhoneNumber.text.toString(),
            loginDateTime = "null",
            isLocked = false,
            isRoot = false,
            createDate = getDate(),
            securityCode = "null",
            is2FAActivated = false,
            imagePath = "null"
        )

        return newUser
    }
}