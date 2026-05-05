package com.david.eudecido.data

object SessionManager {
    var currentUserId: String = "current_user"
        private set
    var currentUsername: String = "Cidadão"
        private set
    var currentEmail: String = ""
        private set

    val isLoggedIn: Boolean
        get() = currentUserId != "current_user" && currentUserId.isNotBlank()

    fun login(userId: String, username: String, email: String) {
        currentUserId = userId
        currentUsername = username
        currentEmail = email
    }

    fun logout() {
        currentUserId = "current_user"
        currentUsername = "Cidadão"
        currentEmail = ""
    }
}
