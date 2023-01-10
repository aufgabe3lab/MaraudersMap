package com.example.maraudersmap.backend

data class UserXTO(
    var username: String? = null,
    var password: String? = null,
    var description: String? = null,
    var privacyRadius: Long? = null
)