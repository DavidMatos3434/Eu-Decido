package com.david.eudecido.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
