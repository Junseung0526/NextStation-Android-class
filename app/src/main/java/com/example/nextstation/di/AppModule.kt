package com.example.nextstation.di

import android.content.Context
import androidx.room.Room
import com.example.nextstation.data.local.ArrivalDao
import com.example.nextstation.data.local.ArrivalDatabase
import com.example.nextstation.data.remote.BusArrivalApi
import com.example.nextstation.data.repository.ArrivalRepositoryImpl
import com.example.nextstation.domain.repository.ArrivalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideArrivalDatabase(@ApplicationContext context: Context): ArrivalDatabase {
        return Room.databaseBuilder(
            context,
            ArrivalDatabase::class.java,
            "arrival_db"
        ).build()
    }

    @Provides
    fun provideArrivalDao(database: ArrivalDatabase): ArrivalDao {
        return database.dao
    }

    @Provides
    @Singleton
    fun provideBusArrivalApi(): BusArrivalApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://ws.bus.go.kr/api/rest/stationinfo/") // Placeholder base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(BusArrivalApi::class.java)
    }

    @Provides
    @Singleton
    fun provideArrivalRepository(
        dao: ArrivalDao,
        api: BusArrivalApi
    ): ArrivalRepository {
        return ArrivalRepositoryImpl(dao, api)
    }
}