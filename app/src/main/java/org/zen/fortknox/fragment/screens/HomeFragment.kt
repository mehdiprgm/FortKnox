package org.zen.fortknox.fragment.screens

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.activity.MainActivity
import org.zen.fortknox.adapter.interfaces.OnAccountClickListener
import org.zen.fortknox.adapter.interfaces.OnBankCardClickListener
import org.zen.fortknox.adapter.interfaces.OnContactClickListener
import org.zen.fortknox.adapter.interfaces.OnNoteClickListener
import org.zen.fortknox.adapter.recyclerview.AccountAdapter
import org.zen.fortknox.adapter.recyclerview.BankCardAdapter
import org.zen.fortknox.adapter.recyclerview.ContactAdapter
import org.zen.fortknox.adapter.recyclerview.NoteAdapter
import org.zen.fortknox.database.entity.Account
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.database.entity.Contact
import org.zen.fortknox.database.entity.Note
import org.zen.fortknox.databinding.FragmentHomeBinding
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.dialog.bottom.BottomDialogNewItem
import org.zen.fortknox.dialog.bottom.details.AccountDetailsDialog
import org.zen.fortknox.dialog.bottom.details.BankCardDetailsDialog
import org.zen.fortknox.dialog.bottom.details.ContactDetailsDialog
import org.zen.fortknox.dialog.bottom.details.NoteDetailsDialog
import org.zen.fortknox.tools.copyTextToClipboard
import org.zen.fortknox.tools.getResourceColor
import org.zen.fortknox.tools.selectedItems
import org.zen.fortknox.tools.selectedViews
import org.zen.fortknox.tools.shareText
import org.zen.fortknox.viewmodel.DatabaseViewModel

class HomeFragment : Fragment(), View.OnClickListener {
    private lateinit var b: FragmentHomeBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    private lateinit var accountAdapter: AccountAdapter
    private lateinit var bankCardAdapter: BankCardAdapter
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var noteAdapter: NoteAdapter

    /* If selection mode is activated */
    private var isSelectionModeActivated = false

    /* Seconds before user click back button */
    private var backPressedTime = 0L

    /* This variable used to detect witch table is loaded from database*//*
            0   -> Accounts
            1   -> Bank Cards
            2   -> Contacts
            3   -> Notes
     */
    private var selectedTableIndex = 0

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = FragmentHomeBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModels()
        setupBackPressListener()
        setupMenuHeader()
        setupSearchBar()

        b.btnAdd.setOnClickListener(this)
        b.btnAccounts.setOnClickListener(this)
        b.btnBankCards.setOnClickListener(this)
        b.btnContacts.setOnClickListener(this)
        b.btnNotes.setOnClickListener(this)

        b.tvShare.setOnClickListener(this)
        b.tvCopy.setOnClickListener(this)
        b.tvDelete.setOnClickListener(this)

        loadTableContent()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAdd -> {/* Load rotation animation */
                val rotate180Reverse =
                    AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_180_reverse)
                rotate180Reverse.duration = 300

                /* Load rotation animation in add button */
                b.btnAdd.startAnimation(rotate180Reverse)

                /* Open bottom dialog for creating new item */
                val newItemDialog = BottomDialogNewItem()
                newItemDialog.show(requireActivity().supportFragmentManager, "New Item")
            }

            R.id.btnAccounts -> {
                selectedTableIndex = 0
                loadTableContent()
            }

            R.id.btnBankCards -> {
                selectedTableIndex = 1
                loadTableContent()
            }

            R.id.btnContacts -> {
                selectedTableIndex = 2
                loadTableContent()
            }

            R.id.btnNotes -> {
                selectedTableIndex = 3
                loadTableContent()
            }

            R.id.tvShare -> {
                shareSelectedItems()
            }

            R.id.tvCopy -> {
                copySelectedItems()
            }

            R.id.tvDelete -> {
                deleteSelectedItems()
            }
        }
    }

    private fun setupMenuHeader() {
//        try {/* Ge the menu view */
//            val view: View = b.navMenu.getHeaderView(0)
//            val pref = getSharedPreferences(preferencesName, MODE_PRIVATE)
//            val settings = getSettings()
//
//            /* Read username from preferences *//* Get user from database using username from preferences */
//            val username = pref.getString("Username", "")
//            val user = databaseViewModel.getUser(username!!)
//
//            imgProfile = view.findViewById(R.id.imgProfile)
//
//            val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
//            val tvEmailAddress = view.findViewById<TextView>(R.id.tvEmailAddress)
//            val btnTheme = view.findViewById<FloatingActionButton>(R.id.btnTheme)
//
//            tvUsername.text = user!!.username
//            tvEmailAddress.text = user.emailAddress
//            changeThemeButtonIcon(btnTheme, settings.theme)
//
//            btnTheme.setOnClickListener {
//                /* Set the next theme */
//                val newTheme = settings.theme.next()
//
//                /* Change button icon */
//                changeThemeButtonIcon(btnTheme, newTheme)
//                changeTheme(newTheme)
//
//                /* Apply new theme to the settings */
//                settings.theme = newTheme
//                applySettings(this)
//            }
//        } catch (ex: Exception) {
//            lifecycleScope.launch {
//                Dialogs.showException(requireContext(), ex)
//            }
//        }
    }

    private fun setupBackPressListener() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSelectionModeActivated) {
                    disableSelectionMode()
                } else {
                    val currentTime = System.currentTimeMillis()

                    /* If user press back button again under 3 seconds */
                    if (currentTime - backPressedTime < 3000) {
                        requireActivity().finish()
                    } else {
                        Toast.makeText(
                            requireContext(), "Press back button again to exit", Toast.LENGTH_SHORT
                        ).show()

                        /* Close screen and update pressed time to current time */
                        backPressedTime = currentTime
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun setupSearchBar() {
        b.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {/* In the selection mode, we don't want edittext to work */
                if (!isSelectionModeActivated) {
                    loadTableContent(s.toString())
                }
            }
        })
    }

    private fun initViewModels() {
        databaseViewModel = ViewModelProvider(requireActivity())[DatabaseViewModel::class.java]
    }

    private fun loadTableContent(searchQuery: String = "") {
        executeActionByTableIndex(
            listOf(
                { loadAccounts(searchQuery) },
                { loadBankCards(searchQuery) },
                { loadContacts(searchQuery) },
                { loadNotes(searchQuery) },
            )
        )

        disableSelectionMode()
        changeCategorySelection()
    }

    private fun loadAccounts(searchQuery: String = "") {
        try {
            accountAdapter = AccountAdapter(requireContext())

            /* Setup adapter */
            b.rcTableContent.adapter = accountAdapter
            b.rcTableContent.layoutManager = LinearLayoutManager(requireContext())

            if (searchQuery.isEmpty()) {/* Load all items */
                databaseViewModel.allAccounts.observe(viewLifecycleOwner) { accounts ->
                    accountAdapter.accounts = accounts
                    showEmptyList(accounts.isEmpty())
                }
            } else {/* Show only found items */
                databaseViewModel.setAccountSearchQuery(searchQuery)
                databaseViewModel.accountSearchResults.observe(viewLifecycleOwner) { accounts ->
                    accountAdapter.accounts = accounts
                    showEmptyList(accounts.isEmpty())
                }
            }

            accountAdapter.setOnItemClickListener(object : OnAccountClickListener {
                override fun onItemClick(
                    checkBox: MaterialCheckBox, account: Account
                ) {
                    if (isSelectionModeActivated) {
                        if (removeSelectedItem(checkBox, account)) {
                            checkBox.isChecked = false
                        } else {
                            addNewSelectedItem(checkBox, account)
                            checkBox.isChecked = true
                        }

                        if (selectedItems.isEmpty()) {
                            disableSelectionMode()
                        }
                    } else {
                        val accountDetailsDialog = AccountDetailsDialog(requireContext(), account)
                        accountDetailsDialog.show(
                            requireActivity().supportFragmentManager, "Account Details"
                        )
                    }
                }

                override fun onItemLongClick(checkBox: MaterialCheckBox, account: Account) {
                    if (!isSelectionModeActivated) {
                        enableSelectionMode()

                        addNewSelectedItem(checkBox, account)
                        checkBox.isChecked = true
                    }
                }
            })
        } catch (ex: Exception) {
            lifecycleScope.launch {
                Dialogs.showException(requireContext(), ex)
            }
        }
    }

    private fun loadBankCards(searchQuery: String = "") {
        try {
            bankCardAdapter = BankCardAdapter(requireContext())

            /* Setup adapter */
            b.rcTableContent.adapter = bankCardAdapter
            b.rcTableContent.layoutManager = LinearLayoutManager(requireContext())

            if (searchQuery.isEmpty()) {/* Load all items */
                databaseViewModel.allBankCards.observe(viewLifecycleOwner) { bankCards ->
                    bankCardAdapter.bankCards = bankCards
                    showEmptyList(bankCards.isEmpty())
                }
            } else {/* Show only found items */
                databaseViewModel.setBankCardSearchQuery(searchQuery)
                databaseViewModel.bankCardSearchResults.observe(viewLifecycleOwner) { bankCards ->
                    bankCardAdapter.bankCards = bankCards
                    showEmptyList(bankCards.isEmpty())
                }
            }

            bankCardAdapter.setOnItemClickListener(object : OnBankCardClickListener {
                override fun onItemClick(
                    checkBox: MaterialCheckBox, bankCard: BankCard
                ) {
                    if (isSelectionModeActivated) {
                        if (removeSelectedItem(checkBox, bankCard)) {
                            checkBox.isChecked = false
                        } else {
                            addNewSelectedItem(checkBox, bankCard)
                            checkBox.isChecked = true
                        }

                        if (selectedItems.isEmpty()) {
                            disableSelectionMode()
                        }
                    } else {
                        val bankCardDetailsDialog =
                            BankCardDetailsDialog(requireContext(), bankCard)
                        bankCardDetailsDialog.show(
                            requireActivity().supportFragmentManager, "Bank card Details"
                        )
                    }
                }

                override fun onItemLongClick(checkBox: MaterialCheckBox, bankCard: BankCard) {
                    if (!isSelectionModeActivated) {
                        enableSelectionMode()

                        addNewSelectedItem(checkBox, bankCard)
                        checkBox.isChecked = true
                    }
                }
            })
        } catch (ex: Exception) {
            lifecycleScope.launch {
                Dialogs.showException(requireContext(), ex)
            }
        }
    }

    private fun loadContacts(searchQuery: String = "") {
        try {
            contactAdapter = ContactAdapter(requireContext())

            /* Setup adapter */
            b.rcTableContent.adapter = contactAdapter
            b.rcTableContent.layoutManager = LinearLayoutManager(requireContext())

            if (searchQuery.isEmpty()) {/* Load all items */
                databaseViewModel.allContacts.observe(viewLifecycleOwner) { contacts ->
                    contactAdapter.contacts = contacts
                    showEmptyList(contacts.isEmpty())
                }
            } else {/* Show only found items */
                databaseViewModel.setContactSearchQuery(searchQuery)
                databaseViewModel.contactSearchResults.observe(viewLifecycleOwner) { contacts ->
                    contactAdapter.contacts = contacts
                    showEmptyList(contacts.isEmpty())
                }
            }

            contactAdapter.setOnItemClickListener(object : OnContactClickListener {
                override fun onItemClick(
                    checkBox: MaterialCheckBox, contact: Contact
                ) {
                    if (isSelectionModeActivated) {
                        if (removeSelectedItem(checkBox, contact)) {
                            checkBox.isChecked = false
                        } else {
                            addNewSelectedItem(checkBox, contact)
                            checkBox.isChecked = true
                        }

                        if (selectedItems.isEmpty()) {
                            disableSelectionMode()
                        }
                    } else {
                        val contactDetailsDialog = ContactDetailsDialog(requireContext(), contact)
                        contactDetailsDialog.show(
                            requireActivity().supportFragmentManager, "Bank card Details"
                        )
                    }
                }

                override fun onItemLongClick(checkBox: MaterialCheckBox, contact: Contact) {
                    if (!isSelectionModeActivated) {
                        enableSelectionMode()

                        addNewSelectedItem(checkBox, contact)
                        checkBox.isChecked = true
                    }
                }
            })
        } catch (ex: Exception) {
            lifecycleScope.launch {
                Dialogs.showException(requireContext(), ex)
            }
        }
    }

    private fun loadNotes(searchQuery: String = "") {
        try {
            noteAdapter = NoteAdapter(requireContext())

            /* Setup adapter */
            b.rcTableContent.adapter = noteAdapter
            b.rcTableContent.layoutManager = LinearLayoutManager(requireContext())

            if (searchQuery.isEmpty()) {/* Load all items */
                databaseViewModel.allNotes.observe(viewLifecycleOwner) { notes ->
                    noteAdapter.notes = notes
                    showEmptyList(notes.isEmpty())
                }
            } else {/* Show only found items */
                databaseViewModel.setNoteSearchQuery(searchQuery)
                databaseViewModel.noteSearchResults.observe(viewLifecycleOwner) { notes ->
                    noteAdapter.notes = notes
                    showEmptyList(notes.isEmpty())
                }
            }

            noteAdapter.setOnItemClickListener(object : OnNoteClickListener {
                override fun onItemClick(
                    checkBox: MaterialCheckBox, note: Note
                ) {
                    if (isSelectionModeActivated) {
                        if (removeSelectedItem(checkBox, note)) {
                            checkBox.isChecked = false
                        } else {
                            addNewSelectedItem(checkBox, note)
                            checkBox.isChecked = true
                        }

                        if (selectedItems.isEmpty()) {
                            disableSelectionMode()
                        }
                    } else {
                        val noteDetailsDialog = NoteDetailsDialog(requireContext(), note)
                        noteDetailsDialog.show(
                            requireActivity().supportFragmentManager, "Note Details"
                        )
                    }
                }

                override fun onItemLongClick(checkBox: MaterialCheckBox, note: Note) {
                    if (!isSelectionModeActivated) {
                        enableSelectionMode()

                        addNewSelectedItem(checkBox, note)
                        checkBox.isChecked = true
                    }
                }
            })
        } catch (ex: Exception) {
            lifecycleScope.launch {
                Dialogs.showException(requireContext(), ex)
            }
        }
    }

//    private fun loadProfilePicture(imageView: ImageView) {
//        lifecycleScope.launch {
//            try {
//                val pref = requireContext().getSharedPreferences(preferencesName, MODE_PRIVATE)
//                val username = pref.getString("Username", "")
//
//                val user = databaseViewModel.getUser(username!!)
//
//                Glide.with(requireContext()).load(user!!.imagePath).skipMemoryCache(true)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.ic_avatar)
//                    .error(R.drawable.ic_avatar).into(imageView)
//            } catch (ex: Exception) {
//                Dialogs.showException(requireContext(), ex)
//            } finally {
//            }
//        }
//    }

    private fun showEmptyList(visible: Boolean) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        animation.duration = 600

        b.lottieEmpty.isVisible = visible
        b.tvEmpty.isVisible = visible
        b.tvEmptyDescription.isVisible = visible

        if (visible) {
            b.lottieEmpty.animation = animation
            b.tvEmpty.animation = animation
            b.tvEmptyDescription.animation = animation
        } else {
            b.lottieEmpty.animation = null
            b.tvEmpty.animation = null
            b.tvEmptyDescription.animation = null
        }
    }

    private fun disableSelectionMode() {
        isSelectionModeActivated = false
        b.txtSearch.isEnabled = true
        (requireActivity() as? MainActivity)?.showBottomNavigation(true)

        executeActionByTableIndex(
            listOf(
                { accountAdapter.setShowCheckboxes(false) },
                { bankCardAdapter.setShowCheckboxes(false) },
                { contactAdapter.setShowCheckboxes(false) },
                { noteAdapter.setShowCheckboxes(false) },
            )
        )

        /* Clear all selected items */
        selectedItems.clear()
        for (item in selectedViews) {
            item.isChecked = false
        }

        /* Clear all selected views */
        selectedViews.clear()

        b.btnAdd.isVisible = true
        b.layBottom.isVisible = false
    }

    private fun enableSelectionMode() {
        isSelectionModeActivated = true
        b.txtSearch.isEnabled = false
        (requireActivity() as? MainActivity)?.showBottomNavigation(false)

        executeActionByTableIndex(
            listOf(
                { accountAdapter.setShowCheckboxes(true) },
                { bankCardAdapter.setShowCheckboxes(true) },
                { contactAdapter.setShowCheckboxes(true) },
                { noteAdapter.setShowCheckboxes(true) },
            )
        )

        b.btnAdd.isVisible = false
        b.layBottom.isVisible = true
    }

    private fun changeCategorySelection() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        animation.duration = 300

        /* List of all category buttons */
        val buttons = listOf(
            b.btnAccounts, b.btnBankCards, b.btnContacts, b.btnNotes
        )

        /* Default selection */
        var currentSelectedButton = b.btnAccounts

        for (button in buttons) {/* Change text style to normal */
            button.typeface = Typeface.create(button.typeface, Typeface.NORMAL)

            /* Set background to transparent, without any border */
            button.setBackgroundResource(android.R.color.transparent)

            /* Set text color to less important */
            button.setTextColor(getResourceColor(requireContext(), R.color.foreground_less))
        }

        executeActionByTableIndex(
            listOf(
                { currentSelectedButton = b.btnAccounts },
                { currentSelectedButton = b.btnBankCards },
                { currentSelectedButton = b.btnContacts },
                { currentSelectedButton = b.btnNotes })
        )

        currentSelectedButton.typeface =
            Typeface.create(currentSelectedButton.typeface, Typeface.BOLD)
        currentSelectedButton.setBackgroundResource(R.drawable.button_category_background)
        currentSelectedButton.setTextColor(getResourceColor(requireContext(), R.color.foreground))

        currentSelectedButton.startAnimation(animation)
    }

    private fun executeActionByTableIndex(actions: List<() -> Unit>) {
        if (selectedTableIndex in actions.indices) {
            actions[selectedTableIndex].invoke()
        }
    }

    private fun addNewSelectedItem(
        checkBox: MaterialCheckBox, item: Any
    ) {/* we don't want duplicate items in the list, so first check the list */
        selectedItems.add(item)
        selectedViews.add(checkBox)
    }

    private fun removeSelectedItem(
        checkBox: MaterialCheckBox, item: Any
    ): Boolean {/* Removes the selected item, if item exist in the list return true else returns false */
        var result: Boolean

        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
            selectedViews.remove(checkBox)

            result = true
        } else {
            result = false
        }

        return result
    }

    private fun convertSelectedItemsToText(): String {
        val sb = StringBuilder()
        val firstItem = selectedItems.first()

        /* create text to share text */
        when (firstItem) {
            is Account -> {
                for (account in selectedItems) {
                    sb.append(account).append(System.lineSeparator()).append(System.lineSeparator())
                }
            }

            is BankCard -> {
                for (bankCard in selectedItems) {
                    sb.append(bankCard).append(System.lineSeparator())
                        .append(System.lineSeparator())
                }
            }

            is Contact -> {
                for (contact in selectedItems) {
                    sb.append(contact).append(System.lineSeparator()).append(System.lineSeparator())
                }
            }

            is Note -> {
                for (note in selectedItems) {
                    sb.append(note).append(System.lineSeparator()).append(System.lineSeparator())
                }
            }
        }

        return sb.toString().trim()
    }

    private fun shareSelectedItems() {
        if (selectedItems.isNotEmpty()) {
            shareText(requireContext(), "Share information", convertSelectedItemsToText())
            disableSelectionMode()
        }
    }

    private fun copySelectedItems() {
        if (selectedItems.isNotEmpty()) {
            copyTextToClipboard(requireContext(), "Copy details", convertSelectedItemsToText())
            disableSelectionMode()
        }
    }

    private fun deleteSelectedItems() {
        if (selectedItems.isNotEmpty()) {
            val firstItem = selectedItems.first()

            lifecycleScope.launch {
                try {
                    if (Dialogs.ask(
                            context = requireContext(),
                            icon = R.drawable.ic_delete,
                            title = "Delete selected items",
                            message = "You have selected ${selectedItems.size} item(s) from the list.\nAre you sure you want to delete these items?",
                            yesText = "Delete",
                            noText = "Cancel"
                        )
                    ) {
                        when (firstItem) {
                            is Account -> {
                                for (account in selectedItems) {
                                    databaseViewModel.deleteAccount(account as Account)
                                }
                            }

                            is BankCard -> {
                                for (bankCard in selectedItems) {
                                    databaseViewModel.deleteBankCard(bankCard as BankCard)
                                }
                            }

                            is Contact -> {
                                for (contact in selectedItems) {
                                    databaseViewModel.deleteContact(contact as Contact)
                                }
                            }

                            is Note -> {
                                for (note in selectedItems) {
                                    databaseViewModel.deleteNote(note as Note)
                                }
                            }
                        }

                        disableSelectionMode()
                    }
                } catch (ex: Exception) {
                    Dialogs.showException(requireContext(), ex)
                }
            }
        }
    }

}