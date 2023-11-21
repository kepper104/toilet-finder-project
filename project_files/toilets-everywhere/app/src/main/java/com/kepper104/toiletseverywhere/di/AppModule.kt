package com.kepper104.toiletseverywhere.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.kepper104.toiletseverywhere.data.api.MainApi
import com.kepper104.toiletseverywhere.domain.repository.Repository
import com.kepper104.toiletseverywhere.domain.repository.RepositoryImplementation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


// Currently used backend IP
const val SERVER_IP = "http://185.155.18.225:5010"

/**
 * Dagger Hilt managed DI class
 * containing injection rules for [Repository], [MainApi] and [DataStore]
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRepository(api: MainApi, dataStore: DataStore<Preferences>): Repository {
        return RepositoryImplementation(api, dataStore)
    }

    @Singleton
    @Provides
    fun provideMainApi(): MainApi {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(SERVER_IP)
            .build()
            .create(MainApi::class.java)
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = {
                appContext.preferencesDataStoreFile("toilets_v1")
            }
        )
}
