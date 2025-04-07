package com.example.mynews.di

import android.content.Context
import com.example.mynews.domain.model.authentication.LoginModel
import com.example.mynews.domain.model.authentication.RegisterModel
import com.example.mynews.domain.model.goals.GoalsModel
import com.example.mynews.domain.model.home.CondensedNewsArticleModel
import com.example.mynews.domain.model.home.HomeModel
import com.example.mynews.domain.model.home.NewsModel
import com.example.mynews.domain.model.home.SavedArticlesModel
import com.example.mynews.domain.model.settings.SettingsModel
import com.example.mynews.domain.model.social.FriendsModel
import com.example.mynews.domain.model.social.SocialModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.domain.repositories.authentication.AuthRepository
import com.example.mynews.domain.repositories.goals.GoalsRepository
import com.example.mynews.domain.repositories.home.CondensedNewsArticleRepository
import com.example.mynews.domain.repositories.home.HomeRepository
import com.example.mynews.domain.repositories.home.NewsRepository
import com.example.mynews.domain.repositories.home.SavedArticlesRepository
import com.example.mynews.domain.repositories.settings.SettingsRepository
import com.example.mynews.domain.repositories.social.FriendsRepository
import com.example.mynews.domain.repositories.social.SocialRepository
import com.example.mynews.domain.repositories.user.UserRepository
import com.example.mynews.domain.use_cases.ValidateLoginInputUseCase
import com.example.mynews.domain.use_cases.ValidateRegisterInputUseCase
import com.example.mynews.model.authentication.LoginModelImpl
import com.example.mynews.model.authentication.RegisterModelImpl
import com.example.mynews.model.goals.GoalsModelImpl
import com.example.mynews.model.home.CondensedNewsArticleModelImpl
import com.example.mynews.model.home.HomeModelImpl
import com.example.mynews.model.home.NewsModelImpl
import com.example.mynews.model.home.SavedArticlesModelImpl
import com.example.mynews.model.settings.SettingsModelImpl
import com.example.mynews.model.social.FriendsModelImpl
import com.example.mynews.model.social.SocialModelImpl
import com.example.mynews.model.user.UserModelImpl
import com.example.mynews.service.condensednewsarticle.CondensedNewsArticleApiClient
import com.example.mynews.service.news.NewsApiClient
import com.example.mynews.service.newsbias.NewsBiasProvider
import com.example.mynews.service.repositories.authentication.AuthRepositoryImpl
import com.example.mynews.service.repositories.goals.GoalsRepositoryImpl
import com.example.mynews.service.repositories.home.CondensedNewsArticleRepositoryImpl
import com.example.mynews.service.repositories.home.HomeRepositoryImpl
import com.example.mynews.service.repositories.home.NewsRepositoryImpl
import com.example.mynews.service.repositories.home.SavedArticlesRepositoryImpl
import com.example.mynews.service.repositories.settings.SettingsRepositoryImpl
import com.example.mynews.service.repositories.social.FriendsRepositoryImpl
import com.example.mynews.service.repositories.social.SocialRepositoryImpl
import com.example.mynews.service.repositories.user.UserRepositoryImpl
import com.example.mynews.utils.logger.AndroidLogger
import com.example.mynews.utils.logger.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNewsApiClient(): NewsApiClient = NewsApiClient()

    @Provides
    @Singleton
    fun provideNewsBiasProvider(@ApplicationContext context: Context): NewsBiasProvider =
        NewsBiasProvider(context)

    @Provides
    @Singleton
    fun provideCondensedNewsArticleApiClient(): CondensedNewsArticleApiClient {
        return CondensedNewsArticleApiClient()
    }

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
    ): AuthRepository {
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
        @ApplicationContext context: Context,
        newsApiClient: NewsApiClient,
        newsBiasProvider: NewsBiasProvider,
        logger: Logger
    ): NewsRepository {
        return NewsRepositoryImpl(
            context = context,
            newsApiClient = newsApiClient,
            newsBiasProvider = newsBiasProvider,
            logger = logger
        )
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
    fun provideCondensedNewsArticleRepository(
        condensedNewsArticleApiClient: CondensedNewsArticleApiClient,
        logger: Logger
    ): CondensedNewsArticleRepository {
        return CondensedNewsArticleRepositoryImpl(condensedNewsArticleApiClient, logger)
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

    @Provides
    @Singleton
    fun provideSavedArticlesModel(
        savedArticlesRepository: SavedArticlesRepository
    ): SavedArticlesModel {
        return SavedArticlesModelImpl(savedArticlesRepository)
    }

    @Provides
    @Singleton
    fun provideCondensedNewsArticleModel(
        condensedNewsArticleRepository: CondensedNewsArticleRepository,
        settingsRepository: SettingsRepository,
    ): CondensedNewsArticleModel {
        return CondensedNewsArticleModelImpl(condensedNewsArticleRepository, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideHomeModel(
        homeRepository: HomeRepository,
        goalsRepository: GoalsRepository
    ): HomeModel {
        return HomeModelImpl(homeRepository, goalsRepository)
    }

    @Provides
    @Singleton
    fun provideNewsModel(
        newsRepository: NewsRepository
    ): NewsModel {
        return NewsModelImpl(newsRepository)
    }

    @Provides
    @Singleton
    fun provideSettingsModel(
        authRepository: AuthRepository,
        userRepository: UserRepository,
        settingsRepository: SettingsRepository
    ): SettingsModel {
        return SettingsModelImpl(authRepository, userRepository, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideFriendsModel(
        friendsRepository: FriendsRepository,
        goalsRepository: GoalsRepository,
    ): FriendsModel {
        return FriendsModelImpl(friendsRepository, goalsRepository)
    }

    @Provides
    @Singleton
    fun provideSocialModel(
        socialRepository: SocialRepository
    ): SocialModel {
        return SocialModelImpl(socialRepository)
    }

    @Provides
    @Singleton
    fun provideGoalsModel(
        goalsRepository: GoalsRepository
    ): GoalsModel {
        return GoalsModelImpl(goalsRepository)
    }

    @Provides
    @Singleton
    fun provideLoginModel(
        authRepository: AuthRepository
    ): LoginModel {
        return LoginModelImpl(authRepository)
    }

    @Provides
    @Singleton
    fun provideRegisterModel(
        authRepository: AuthRepository
    ): RegisterModel {
        return RegisterModelImpl(authRepository)
    }

    @Provides
    @Singleton
    fun provideUserModel(
        userRepository: UserRepository
    ): UserModel {
        return UserModelImpl(userRepository)
    }


}