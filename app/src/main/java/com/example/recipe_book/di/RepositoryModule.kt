package com.example.recipe_book.di

import com.example.recipe_book.data.repository.AuthRepositoryImpl
import com.example.recipe_book.data.repository.ImageRepositoryImpl
import com.example.recipe_book.data.repository.RecipeRepositoryImpl
import com.example.recipe_book.domain.repository.AuthRepository
import com.example.recipe_book.domain.repository.ImageRepository
import com.example.recipe_book.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository
}
