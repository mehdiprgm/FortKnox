package org.zen.fortknox.fragment.register

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.api.entity.ApiUser
import org.zen.fortknox.databinding.FragmentRegisterProfileCompleteSetupBinding
import org.zen.fortknox.dialog.DialogType
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.tools.validateData
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterProfileCompleteSetupFragment : Fragment(), View.OnClickListener {
    private lateinit var b: FragmentRegisterProfileCompleteSetupBinding

    /* This will store the URI of the image taken by camera or gallery*/
    private var imageUri: Uri? = null

    companion object {
        fun newInstance() = RegisterProfileCompleteSetupFragment()
    }

    /* Check the permission, if granted open gallery else show the requested permission to the user */
    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            lifecycleScope.launch {
                Dialogs.showMessage(
                    requireContext(),
                    "Permission denied",
                    "The app needs gallery permission to show images.\nPlease give these permission to continue",
                    DialogType.Error
                )
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            lifecycleScope.launch {
                Dialogs.showMessage(
                    requireContext(),
                    "Permission denied",
                    "The app needs camera permission to take pictures.\nPlease give these permission to continue",
                    DialogType.Error
                )
            }
        }
    }

    /* If user selects an image load it into the imageview */
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        /* If user selects an image load it into the imageview */
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                b.imgProfile.setImageURI(uri)
                imageUri = uri
            }
        }
    }

    /* If user takes a picture with camera load it into the imageview */
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        /* If user takes a picture load it into the imageview */
        if (result.resultCode == RESULT_OK) {/* Use the stored URI to display the image */
            imageUri?.let { uri ->
                b.imgProfile.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = FragmentRegisterProfileCompleteSetupBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.imgProfile.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgProfile -> {
                showImagePopupMenu(view)
            }
        }
    }

    /* Check the permission, show the gallery or request the permission */
    private fun checkGalleryPermission() {
        val permission = Manifest.permission.READ_MEDIA_IMAGES

        when {/* If permission granted open the gallery */
            ContextCompat.checkSelfPermission(
                requireContext(), permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                showGalleryPermissionRationaleDialog(permission)
            }

            else -> {
                galleryPermissionLauncher.launch(permission)
            }
        }
    }

    /* Check the permission, show the camera or request the permission */
    private fun checkCameraPermission() {
        val permission = Manifest.permission.CAMERA

        when {/* If permission granted open the camera */
            ContextCompat.checkSelfPermission(
                requireContext(), permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                showCameraPermissionRationaleDialog(permission)
            }

            else -> {
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showGalleryPermissionRationaleDialog(permission: String) {
        lifecycleScope.launch {
            if (Dialogs.ask(
                    context = requireContext(),
                    icon = R.drawable.ic_lock_close,
                    title = "Gallery permission required",
                    message = "The app needs access to your gallery to select images",
                    cancellable = true
                )
            ) {
                galleryPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showCameraPermissionRationaleDialog(permission: String) {
        lifecycleScope.launch {
            if (Dialogs.ask(
                    context = requireContext(),
                    icon = R.drawable.ic_lock_close,
                    title = "Camera permission required",
                    message = "The app needs access to your camera to take pictures",
                    cancellable = true
                )
            ) {
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    /* Open the gallery */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }

        galleryLauncher.launch(intent)
    }

    /* Open the camera */
    private fun openCamera() {
        lifecycleScope.launch {
            // Create a file to store the camera image
            val photoFile = createImageFile()

            // Get a URI for the file using FileProvider (required for Android 7+)
            val photoUri: Uri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", photoFile
            )

            // Store the URI so we can use it later to display the image
            imageUri = photoUri

            /* Create intent to open camera */
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            /* Check if there's a camera app available */
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                cameraLauncher.launch(intent)
            } else {
                Dialogs.showMessage(
                    requireContext(),
                    "No camera app",
                    "No camera application found on your device",
                    DialogType.Error
                )
            }
        }
    }

    /* Create a temporary file to store the camera image */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create unique filename using timestamp
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "JPEG_${timeStamp}_"

        // Get the Pictures directory
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Create temporary file
        return File.createTempFile(
            fileName,  // prefix
            ".jpg",    // suffix
            storageDir // directory
        )
    }

    private fun showImagePopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.setForceShowIcon(true)

        popupMenu.menuInflater.inflate(R.menu.menu_image, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem)
        }

        popupMenu.show()
    }

    private fun handleMenuItemClick(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menuCamera -> {
                checkCameraPermission()
                true
            }

            R.id.menuGallery -> {
                checkGalleryPermission()
                true
            }

            else -> false
        }
    }


    fun isFormInformationValid(): Boolean {
        var score = 2

        score += b.txtLaySecurityCode.validateData(
            b.txtSecurityCode.text.toString().isEmpty(), "Security code is empty"
        )

        score += b.txtLayEmailAddress.validateData(
            b.txtEmailAddress.text.toString().isEmpty(), "Email address is empty"
        )

        return score == 2
    }

    fun getUser(): ApiUser {
        val newUser = ApiUser(
            username = "null",
            password = "null",
            emailAddress = b.txtEmailAddress.text.toString(),
            phoneNumber = "null",
            loginDateTime = "null",
            isLocked = false,
            isRoot = false,
            createDate = "null",
            securityCode = b.txtSecurityCode.text.toString(),
            is2FAActivated = b.switchActive2FA.isChecked,
            imagePath = "null"
        )

        return newUser
    }
}