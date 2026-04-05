package com.renalytics.di

import android.content.Context
import com.renalytics.data.db.AppDatabase
import com.renalytics.data.db.MeasurementDao
import com.renalytics.data.db.UserDao
import com.renalytics.data.repository.MeasurementRepository
import com.renalytics.data.repository.UserRepository
import com.renalytics.data.store.AppDataStore
import com.renalytics.ui.viewmodel.BleViewModel
import com.renalytics.ui.viewmodel.MeasurementViewModel
import com.renalytics.ui.viewmodel.UserViewModel
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        // 实际应用中，应该使用安全的方式获取密码
        val passphrase = "renalytics_password".toByteArray()
        return AppDatabase.getInstance(context, passphrase)
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideMeasurementDao(appDatabase: AppDatabase): MeasurementDao {
        return appDatabase.measurementDao()
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideMeasurementRepository(measurementDao: MeasurementDao): MeasurementRepository {
        return MeasurementRepository(measurementDao)
    }

    @Provides
    @Singleton
    fun provideAppDataStore(@ApplicationContext context: Context): AppDataStore {
        return AppDataStore(context)
    }

    @Provides
    @Singleton
    fun provideUserViewModel(userRepository: UserRepository): UserViewModel {
        return UserViewModel(userRepository)
    }

    @Provides
    @Singleton
    fun provideMeasurementViewModel(measurementRepository: MeasurementRepository): MeasurementViewModel {
        return MeasurementViewModel(measurementRepository)
    }

    @Provides
    @Singleton
    fun provideBleViewModel(appDataStore: AppDataStore): BleViewModel {
        return BleViewModel(appDataStore)
    }
}
