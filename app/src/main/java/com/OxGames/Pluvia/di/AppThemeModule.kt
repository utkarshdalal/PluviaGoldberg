package com.OxGames.Pluvia.di

import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.enums.AppTheme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Referenced from https://github.com/fvilarino/App-Theme-Compose-Sample
 */

interface IAppTheme {
    val themeFlow: StateFlow<AppTheme>
    var currentTheme: AppTheme
}

class AppThemeImpl : IAppTheme {

    override val themeFlow: MutableStateFlow<AppTheme> = MutableStateFlow(PrefManager.appTheme)

    override var currentTheme: AppTheme by AppThemePreferenceDelegate()

    inner class AppThemePreferenceDelegate : ReadWriteProperty<Any, AppTheme> {

        override fun getValue(thisRef: Any, property: KProperty<*>): AppTheme = PrefManager.appTheme

        override fun setValue(thisRef: Any, property: KProperty<*>, value: AppTheme) {
            themeFlow.value = value
            PrefManager.appTheme = value
        }
    }
}

@InstallIn(SingletonComponent::class)
@Module
class AppThemeModule {
    @Provides
    @Singleton
    fun provideAppTheme(): IAppTheme = AppThemeImpl()
}
