package org.zen.fortknox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import org.zen.fortknox.database.repository.UserRepository
import org.zen.fortknox.database.MainDatabase
import org.zen.fortknox.database.entity.Account
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.database.entity.Contact
import org.zen.fortknox.database.entity.Note
import org.zen.fortknox.database.entity.User
import org.zen.fortknox.database.repository.AccountRepository
import org.zen.fortknox.database.repository.BankCardRepository
import org.zen.fortknox.database.repository.ContactRepository
import org.zen.fortknox.database.repository.NoteRepository

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository
    private val accountRepository: AccountRepository
    private val bankCardRepository: BankCardRepository
    private val contactRepository: ContactRepository
    private val noteRepository: NoteRepository

    private val userSearchQuery = MutableLiveData<String>()
    private val accountSearchQuery = MutableLiveData<String>()
    private val bankCardSearchQuery = MutableLiveData<String>()
    private val contactSearchQuery = MutableLiveData<String>()
    private val noteSearchQuery = MutableLiveData<String>()

    val allUsers: LiveData<List<User>>
    val allAccounts: LiveData<List<Account>>
    val allBankCards: LiveData<List<BankCard>>
    val allContacts: LiveData<List<Contact>>
    val allNotes: LiveData<List<Note>>

    init {
        val userDAO = MainDatabase.getDatabase(application).userDAO()
        val accountDAO = MainDatabase.getDatabase(application).accountDAO()
        val bankCardDAO = MainDatabase.getDatabase(application).bankCardDAO()
        val contactDAO = MainDatabase.getDatabase(application).contactDAO()
        val noteDAO = MainDatabase.getDatabase(application).noteDAO()

        userRepository = UserRepository(userDAO)
        accountRepository = AccountRepository(accountDAO)
        bankCardRepository = BankCardRepository(bankCardDAO)
        contactRepository = ContactRepository(contactDAO)
        noteRepository = NoteRepository(noteDAO)

        allUsers = userRepository.allAccounts
        allAccounts = accountRepository.allAccounts
        allBankCards = bankCardRepository.allBankCards
        allContacts = contactRepository.allContacts
        allNotes = noteRepository.allNotes
    }

    val userSearchResults: LiveData<List<User>> = userSearchQuery.switchMap { query ->
        userRepository.search(query)
    }

    val accountSearchResults: LiveData<List<Account>> = accountSearchQuery.switchMap { query ->
        accountRepository.search(query)
    }

    val bankCardSearchResults: LiveData<List<BankCard>> = bankCardSearchQuery.switchMap { query ->
        bankCardRepository.search(query)
    }

    val contactSearchResults: LiveData<List<Contact>> = contactSearchQuery.switchMap { query ->
        contactRepository.search(query)
    }

    val noteSearchResults: LiveData<List<Note>> = noteSearchQuery.switchMap { query ->
        noteRepository.search(query)
    }

    fun addAccount(account: Account) {
        accountRepository.add(account)
    }

    fun getAccount(name: String): Account? {
        return accountRepository.get(name)
    }

    fun deleteAccount(account: Account) {
        accountRepository.delete(account)
    }

    fun updateAccount(account: Account) {
        accountRepository.update(account)
    }

    fun countAccounts(): Int {
        return accountRepository.count()
    }

    fun addBankCard(bankCard: BankCard) {
        bankCardRepository.add(bankCard)
    }

    fun getBankCard(cardName: String): BankCard? {
        return bankCardRepository.get(cardName)
    }

    fun deleteBankCard(bankCard: BankCard) {
        bankCardRepository.delete(bankCard)
    }

    fun updateBankCard(bankCard: BankCard) {
        bankCardRepository.update(bankCard)
    }

    fun countBankCards(): Int {
        return bankCardRepository.count()
    }

    fun addContact(contact: Contact) {
        contactRepository.add(contact)
    }

    fun getContact(name: String): Contact? {
        return contactRepository.get(name)
    }

    fun deleteContact(contact: Contact) {
        contactRepository.delete(contact)
    }

    fun updateContact(contact: Contact) {
        contactRepository.update(contact)
    }

    fun countContacts(): Int {
        return contactRepository.count()
    }

    fun addNote(note: Note) {
        noteRepository.add(note)
    }

    fun getNote(name: String): Note? {
        return noteRepository.get(name)
    }

    fun deleteNote(note: Note) {
        noteRepository.delete(note)
    }

    fun updateNote(note: Note) {
        noteRepository.update(note)
    }

    fun countNotes(): Int {
        return noteRepository.count()
    }

    fun addUser(user: User) {
        userRepository.add(user)
    }

    fun getUser(username: String): User? {
        return userRepository.get(username)
    }

    fun updateUser(user: User) {
        userRepository.update(user)
    }

    fun countUsers(): Int {
        return userRepository.count()
    }

    fun setUserSearchQuery(query: String) {
        userSearchQuery.value = query
    }

    fun setAccountSearchQuery(query: String) {
        accountSearchQuery.value = query
    }

    fun setBankCardSearchQuery(query: String) {
        bankCardSearchQuery.value = query
    }

    fun setContactSearchQuery(query: String) {
        contactSearchQuery.value = query
    }

    fun setNoteSearchQuery(query: String) {
        noteSearchQuery.value = query
    }
}