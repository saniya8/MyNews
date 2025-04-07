package com.example.mynews.model.user

import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.domain.repositories.user.UserRepository
import javax.inject.Inject

class UserModelImpl @Inject constructor(
    private val userRepository: UserRepository
) : UserModel {

    override suspend fun getCurrentUserId(): String? {
        return userRepository.getCurrentUserId()
    }

}