package org.zen.fortknox.database.repository

import androidx.lifecycle.LiveData
import org.zen.fortknox.database.dao.UserDAO
import org.zen.fortknox.database.entity.User

class UserRepository(private val userDao: UserDAO) {
    val allAccounts : LiveData<List<User>> = userDao.getAll()

    fun add(user: User) {
        userDao.add(user)
    }

    fun get(username: String) : User? {
        return userDao.get(username)
    }

    fun update(user: User) {
        userDao.update(user)
    }

    fun count() : Int {
        return userDao.count()
    }

    fun search(query: String): LiveData<List<User>> {
        return userDao.search(query)
    }
}