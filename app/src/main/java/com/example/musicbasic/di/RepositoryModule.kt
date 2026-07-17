package com.example.musicbasic.di

import com.example.musicbasic.repository.MusicRepository
import com.example.musicbasic.repository.MusicRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindMusicRepository(
        impl: MusicRepositoryImpl,
    ): MusicRepository
}