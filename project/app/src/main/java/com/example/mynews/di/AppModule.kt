package com.example.mynews.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.mynews.data.AuthRepositoryImpl
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.use_cases.ValidateLoginInputUseCase
import com.example.mynews.domain.use_cases.ValidateRegisterInputUseCase
import com.example.mynews.data.UserRepositoryImpl
import com.example.mynews.domain.repositories.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideValidateLoginInputUseCase():ValidateLoginInputUseCase{
        return ValidateLoginInputUseCase()
    }

    @Provides
    @Singleton
    fun provideValidateRegisterInputUseCase():ValidateRegisterInputUseCase{
        return ValidateRegisterInputUseCase()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore
    ) : UserRepository {
        return UserRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        userRepository: UserRepository,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ):AuthRepository{
        return AuthRepositoryImpl(userRepository, firestore, auth)
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

}