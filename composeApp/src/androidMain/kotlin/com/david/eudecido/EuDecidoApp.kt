package com.david.eudecido

import android.app.Application
import com.david.eudecido.data.DataSeeder
import com.david.eudecido.di.appModule
import com.david.eudecido.di.initKoin
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class EuDecidoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger(Level.ERROR)
            androidContext(this@EuDecidoApp)
            modules(appModule)
        }

        // Executa o seeder para popular a base de dados na primeira execução
        val seeder: DataSeeder = get()
        seeder.seedIfNeeded()
    }
}
