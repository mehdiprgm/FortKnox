package org.zen.fortknox.database.repository

import androidx.lifecycle.LiveData
import org.zen.fortknox.database.dao.AccountDAO
import org.zen.fortknox.database.entity.Account

class AccountRepository(private val accountDAO: AccountDAO) {
    val allAccounts : LiveData<List<Account>> = accountDAO.getAll()

    fun add(account: Account) {
        accountDAO.add(account)
    }

    fun delete(account: Account) {
        accountDAO.delete(account)
    }

    fun get(name: String) : Account? {
        return accountDAO.get(name)
    }

    fun update(account: Account) {
        accountDAO.update(account)
    }

    fun count() : Int {
        return accountDAO.count()
    }

    fun search(query: String): LiveData<List<Account>> {
        return accountDAO.search(query)
    }
}