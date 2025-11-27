package org.zen.fortknox.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.zen.fortknox.R
import org.zen.fortknox.activity.application.SettingsActivity
import org.zen.fortknox.adapter.interfaces.OnAccountClickListener
import org.zen.fortknox.adapter.interfaces.OnBankCardClickListener
import org.zen.fortknox.adapter.interfaces.OnContactClickListener
import org.zen.fortknox.adapter.interfaces.OnNoteClickListener
import org.zen.fortknox.adapter.recyclerview.AccountAdapter
import org.zen.fortknox.adapter.recyclerview.BankCardAdapter
import org.zen.fortknox.adapter.recyclerview.ContactAdapter
import org.zen.fortknox.adapter.recyclerview.NoteAdapter
import org.zen.fortknox.api.entity.ApiUser
import org.zen.fortknox.database.entity.Account
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.database.entity.Contact
import org.zen.fortknox.database.entity.Note
import org.zen.fortknox.databinding.ActivityMainBinding
import org.zen.fortknox.dialog.Dialogs
import org.zen.fortknox.dialog.bottom.BottomDialogNewItem
import org.zen.fortknox.dialog.bottom.details.AccountDetailsDialog
import org.zen.fortknox.dialog.bottom.details.BankCardDetailsDialog
import org.zen.fortknox.dialog.bottom.details.ContactDetailsDialog
import org.zen.fortknox.dialog.bottom.details.NoteDetailsDialog
import org.zen.fortknox.tools.applySettings
import org.zen.fortknox.tools.changeThemeButtonIcon
import org.zen.fortknox.tools.disableScreenPadding
import org.zen.fortknox.tools.getSettings
import org.zen.fortknox.tools.preferencesName
import org.zen.fortknox.tools.selectedItems
import org.zen.fortknox.tools.selectedViews
import org.zen.fortknox.tools.theme.changeTheme
import org.zen.fortknox.viewmodel.DatabaseViewModel

class MainActivity : AppCompatActivity(), View.OnClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var b: ActivityMainBinding
    private lateinit var databaseViewModel: DatabaseViewModel

    private lateinit var accountAdapter: AccountAdapter
    private lateinit var bankCardAdapter: BankCardAdapter
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var noteAdapter: NoteAdapter

    private var isSelectionModeActivated = false
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        disableScreenPadding(b.root)

        initViewModels()
        setupBackPressListener()
        setupMenuHeader()
        setupSearchBar()

        b.imgAppIcon.setOnClickListener(this)
        b.btnAdd.setOnClickListener(this)

        /* Set menu item change listener */
        b.navMenu.setNavigationItemSelectedListener(this)

        /* Default load all accounts */
        onNavigationItemSelected(b.navMenu.menu.findItem(R.id.menuAccounts))
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgAppIcon -> {
                if (!b.main.isOpen) {
                    b.main.openDrawer(GravityCompat.START)
                }
            }

            R.id.btnAdd -> {/* Load rotation animation */
                val rotate180Reverse = AnimationUtils.loadAnimation(this, R.anim.rotate_180_reverse)
                rotate180Reverse.duration = 300

                /* Load rotation animation in add button */
                b.btnAdd.startAnimation(rotate180Reverse)

                /* Open bottom dialog for creating new item */
                val newItemDialog = BottomDialogNewItem()
                newItemDialog.show(supportFragmentManager, "New Item")
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menuAccounts -> {
                menuItem.isChecked = true
                loadAccounts()
            }

            R.id.menuBankCards -> {
                menuItem.isChecked = true
                loadBankCards()
            }

            R.id.menuContacts -> {
                menuItem.isChecked = true
                loadContacts()
            }

            R.id.menuNotes -> {
                menuItem.isChecked = true
                loadNotes()
            }

            R.id.menuSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            R.id.menuAboutUs -> {
                Dialogs.showAboutUs(this)
            }
        }

        /* Prevent multiselection in the menu *//* Otherwise it create a bug */
        b.navMenu.setCheckedItem(menuItem)
        b.root.closeDrawer(GravityCompat.START)

        return true
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
                        finish()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Press back button again to exit",
                            Toast.LENGTH_SHORT
                        ).show()

                        /* Close screen and update pressed time to current time */
                        backPressedTime = currentTime
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupMenuHeader() {
        /* Ge the menu view */
        val view: View = b.navMenu.getHeaderView(0)
        val pref = getSharedPreferences(preferencesName, MODE_PRIVATE)
        val settings = getSettings()

        /* Read json from preferences */
        /* We saved entire user object into shared preferences */
        val userJson = pref.getString("User", "")

        /* Convert user json into user object, so we can use it in the application */
        val userObject = ApiUser.convertJsonToUser(userJson!!)

        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvEmailAddress = view.findViewById<TextView>(R.id.tvEmailAddress)
        val btnTheme = view.findViewById<FloatingActionButton>(R.id.btnTheme)

        tvUsername.text = userObject.username
        tvEmailAddress.text = userObject.emailAddress
        changeThemeButtonIcon(btnTheme, settings.theme)

        btnTheme.setOnClickListener {
            /* Set the next theme */
            val newTheme = settings.theme.next()

            /* Change button icon */
            changeThemeButtonIcon(btnTheme, newTheme)
            changeTheme(newTheme)

            /* Apply new theme to the settings */
            settings.theme = newTheme
            applySettings(this)
        }
    }

    private fun setupSearchBar() {
        b.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (b.navMenu.menu.findItem(R.id.menuAccounts).isChecked) {
                    loadAccounts(s.toString())
                } else if (b.navMenu.menu.findItem(R.id.menuBankCards).isChecked) {
                    loadBankCards(s.toString())
                } else if (b.navMenu.menu.findItem(R.id.menuContacts).isChecked) {
                    loadContacts(s.toString())
                } else if (b.navMenu.menu.findItem(R.id.menuNotes).isChecked) {
                    loadNotes(s.toString())
                }
            }
        })
    }

    private fun initViewModels() {
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    private fun loadAccounts(searchQuery: String = "") {
        try {
            accountAdapter = AccountAdapter(this)
            disableSelectionMode()

            /* Setup adapter */
            b.rcTableContent.adapter = accountAdapter
            b.rcTableContent.layoutManager = LinearLayoutManager(this)

            if (searchQuery.isEmpty()) {/* Load all items */
                databaseViewModel.allAccounts.observe(this) { accounts ->
                    accountAdapter.accounts = accounts
                    showEmptyList(accounts.isEmpty())
                }
            } else {/* Show only found items */
                databaseViewModel.setAccountSearchQuery(searchQuery)
                databaseViewModel.accountSearchResults.observe(this) { accounts ->
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
                        val accountDetailsDialog = AccountDetailsDialog(this@MainActivity, account)
                        accountDetailsDialog.show(supportFragmentManager, "Account Details")
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
                Dialogs.showException(this@MainActivity, ex)
            }
        }
    }

    private fun loadBankCards(searchQuery: String = "") {
        try {
            bankCardAdapter = BankCardAdapter(this)
            disableSelectionMode()

            /* Setup adapter */
            b.rcTableContent.adapter = bankCardAdapter
            b.rcTableContent.layoutManager = LinearLayoutManager(this)

            if (searchQuery.isEmpty()) {/* Load all items */
                databaseViewModel.allBankCards.observe(this) { bankCards ->
                    bankCardAdapter.bankCards = bankCards
                    showEmptyList(bankCards.isEmpty())
                }
            } else {/* Show only found items */
                databaseViewModel.setBankCardSearchQuery(searchQuery)
                databaseViewModel.bankCardSearchResults.observe(this) { bankCards ->
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
                            BankCardDetailsDialog(this@MainActivity, bankCard)
                        bankCardDetailsDialog.show(supportFragmentManager, "Bank card Details")
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
                Dialogs.showException(this@MainActivity, ex)
            }
        }
    }

    private fun loadContacts(searchQuery: String = "") {
        try {
            contactAdapter = ContactAdapter(this)
            disableSelectionMode()

            /* Setup adapter */
            b.rcTableContent.adapter = contactAdapter
            b.rcTableContent.layoutManager = LinearLayoutManager(this)

            if (searchQuery.isEmpty()) {/* Load all items */
                databaseViewModel.allContacts.observe(this) { contacts ->
                    contactAdapter.contacts = contacts
                    showEmptyList(contacts.isEmpty())
                }
            } else {/* Show only found items */
                databaseViewModel.setContactSearchQuery(searchQuery)
                databaseViewModel.contactSearchResults.observe(this) { contacts ->
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
                        val contactDetailsDialog = ContactDetailsDialog(this@MainActivity, contact)
                        contactDetailsDialog.show(supportFragmentManager, "Bank card Details")
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
                Dialogs.showException(this@MainActivity, ex)
            }
        }
    }

    private fun loadNotes(searchQuery: String = "") {
        try {
            noteAdapter = NoteAdapter(this)
            disableSelectionMode()

            /* Setup adapter */
            b.rcTableContent.adapter = noteAdapter
            b.rcTableContent.layoutManager = LinearLayoutManager(this)

            if (searchQuery.isEmpty()) {/* Load all items */
                databaseViewModel.allNotes.observe(this) { notes ->
                    noteAdapter.notes = notes
                    showEmptyList(notes.isEmpty())
                }
            } else {/* Show only found items */
                databaseViewModel.setNoteSearchQuery(searchQuery)
                databaseViewModel.noteSearchResults.observe(this) { notes ->
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
                        val noteDetailsDialog = NoteDetailsDialog(this@MainActivity, note)
                        noteDetailsDialog.show(supportFragmentManager, "Note Details")
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
                Dialogs.showException(this@MainActivity, ex)
            }
        }
    }

    private fun showEmptyList(visible: Boolean) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        animation.duration = 500

        if (visible) {
            b.lottieEmpty.visibility = View.VISIBLE
            b.tvEmpty.visibility = View.VISIBLE

            b.lottieEmpty.animation = animation
            b.tvEmpty.animation = animation
        } else {
            b.lottieEmpty.animation = null
            b.tvEmpty.animation = null

            b.lottieEmpty.visibility = View.GONE
            b.tvEmpty.visibility = View.GONE
        }
    }

    private fun disableSelectionMode() {
        isSelectionModeActivated = false

        if (b.navMenu.menu.findItem(R.id.menuAccounts).isChecked) {
            accountAdapter.setShowCheckboxes(false)
        } else if (b.navMenu.menu.findItem(R.id.menuBankCards).isChecked) {
            bankCardAdapter.setShowCheckboxes(false)
        } else if (b.navMenu.menu.findItem(R.id.menuContacts).isChecked) {
            contactAdapter.setShowCheckboxes(false)
        } else if (b.navMenu.menu.findItem(R.id.menuNotes).isChecked) {
            noteAdapter.setShowCheckboxes(false)
        }

        selectedItems.clear()
        for (item in selectedViews) {
            item.isChecked = false
        }

        selectedViews.clear()
//        if (snackBar.isShown) {
//            snackBar.dismiss()
//        }

        b.btnAdd.setImageResource(R.drawable.ic_add)
        b.txtSearch.isEnabled = true
    }

    private fun enableSelectionMode() {
        isSelectionModeActivated = true
        if (b.navMenu.menu.findItem(R.id.menuAccounts).isChecked) {
            accountAdapter.setShowCheckboxes(true)
        } else if (b.navMenu.menu.findItem(R.id.menuBankCards).isChecked) {
            bankCardAdapter.setShowCheckboxes(true)
        } else if (b.navMenu.menu.findItem(R.id.menuContacts).isChecked) {
            contactAdapter.setShowCheckboxes(true)
        } else if (b.navMenu.menu.findItem(R.id.menuNotes).isChecked) {
            noteAdapter.setShowCheckboxes(true)
        }

        b.txtSearch.isEnabled = false
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
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
            selectedViews.remove(checkBox)

            return true
        }

        return false
    }
}