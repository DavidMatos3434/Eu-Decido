package com.david.eudecido.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        // Mudamos o nome para V3 para garantir que todas as novas colunas (identity_id, etc) são criadas
        return AndroidSqliteDriver(
            schema = EuDecidoDatabase.Schema,
            context = context,
            name = "EuDecidoV3.db"
        )
    }
}
