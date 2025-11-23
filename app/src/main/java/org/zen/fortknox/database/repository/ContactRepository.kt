package org.zen.fortknox.database.repository

import androidx.lifecycle.LiveData
import org.zen.fortknox.database.dao.ContactDAO
import org.zen.fortknox.database.entity.Contact

class ContactRepository(private val contactDAO: ContactDAO) {
    val allContacts : LiveData<List<Contact>> = contactDAO.getAll()

    fun add(contact: Contact) {
        contactDAO.add(contact)
    }

    fun delete(contact: Contact) {
        contactDAO.delete(contact)
    }

    fun get(name: String) : Contact? {
        return contactDAO.get(name)
    }

    fun update(contact: Contact) {
        contactDAO.update(contact)
    }

    fun count() : Int {
        return contactDAO.count()
    }

    fun search(query: String): LiveData<List<Contact>> {
        return contactDAO.search(query)
    }
}