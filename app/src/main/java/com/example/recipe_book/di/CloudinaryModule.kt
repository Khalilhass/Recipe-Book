package com.example.recipe_book.di

import android.content.Context
import com.example.recipe_book.util.Constants
import com.cloudinary.android.MediaManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    @Provides
    @Singleton
    fun provideCloudinaryConfig(@ApplicationContext context: Context): MediaManager {
        val config = mapOf("cloud_name" to Constants.CLOUDINARY_CLOUD_NAME)
        MediaManager.init(context, config)
        return MediaManager.get()
    }
}
