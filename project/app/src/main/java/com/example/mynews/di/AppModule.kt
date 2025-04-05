package com.example.mynews.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.mynews.model.AuthRepositoryImpl
import com.example.mynews.model.CondensedNewsArticleRepositoryImpl
import com.example.mynews.model.FriendsRepositoryImpl
import com.example.mynews.model.GoalsRepositoryImpl
import com.example.mynews.model.HomeRepositoryImpl
import com.example.mynews.model.NewsRepositoryImpl
import com.example.mynews.model.SavedArticlesRepositoryImpl
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.use_cases.ValidateLoginInputUseCase
import com.example.mynews.domain.use_cases.ValidateRegisterInputUseCase
import com.example.mynews.model.UserRepositoryImpl
import com.example.mynews.domain.repositories.CondensedNewsArticleRepository
import com.example.mynews.domain.repositories.FriendsRepository
import com.example.mynews.domain.repositories.HomeRepository
import com.example.mynews.domain.repositories.NewsRepository
import com.example.mynews.domain.repositories.SavedArticlesRepository
import com.example.mynews.domain.repositories.UserRepository
import android.content.Context
import com.example.mynews.model.SettingsRepositoryImpl
import com.example.mynews.model.SocialRepositoryImpl
import com.example.mynews.utils.logger.AndroidLogger
import com.example.mynews.utils.logger.Logger
import com.example.mynews.domain.repositories.GoalsRepository
import com.example.mynews.domain.repositories.SettingsRepository
import com.example.mynews.domain.repositories.SocialRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideHomeRepository(
        firestore: FirebaseFirestore
    ): HomeRepository {
        return HomeRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideNewsRepository(
        @ApplicationContext context: Context
    ): NewsRepository {
        return NewsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideSavedArticlesRepository(
        firestore: FirebaseFirestore
    ): SavedArticlesRepository {
        return SavedArticlesRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideCondensedNewsArticleRepository(): CondensedNewsArticleRepository {
        return CondensedNewsArticleRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideSocialRepository(
        firestore: FirebaseFirestore
    ): SocialRepository {
        return SocialRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideFriendsRepository(
        firestore: FirebaseFirestore
    ): FriendsRepository {
        return FriendsRepositoryImpl(firestore)
    }

    @Singleton
    @Provides
    fun provideGoalsRepository(
        firestore: FirebaseFirestore,
        friendsRepository: FriendsRepository
    ): GoalsRepository {
        return GoalsRepositoryImpl(firestore, friendsRepository)
    }

    @Singleton
    @Provides
    fun provideSettingsRepository(
        firestore: FirebaseFirestore
    ): SettingsRepository {
        return SettingsRepositoryImpl(firestore)
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

    @Provides
    @Singleton
    fun provideLogger(): Logger = AndroidLogger()

}