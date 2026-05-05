package com.david.eudecido.di

import com.david.eudecido.db.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory(get()).createDriver() }
}
