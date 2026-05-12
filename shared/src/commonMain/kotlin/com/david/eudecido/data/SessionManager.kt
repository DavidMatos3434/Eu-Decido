package com.david.eudecido.data

object SessionManager {
    var currentUserId: String = ""
        private set
    var currentUsername: String = "Cidadão"
        private set
    var currentEmail: String = ""
        private set
    var token: String? = null
        private set

    val isLoggedIn: Boolean
        get() = token != null

    fun login(userId: String, username: String, email: String, token: String) {
        this.currentUserId = userId
        this.currentUsername = username
        this.currentEmail = email
        this.token = token
    }

    fun logout() {
        currentUserId = ""
        currentUsername = "Cidadão"
        currentEmail = ""
        token = null
    }
}
