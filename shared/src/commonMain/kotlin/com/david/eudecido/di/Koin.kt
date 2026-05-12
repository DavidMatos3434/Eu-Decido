package com.david.eudecido.di

import com.david.eudecido.data.*
import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.sync.SyncManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule())
    }

// Para iOS
fun initKoin() = initKoin {}

val commonModule = module {
    single { EuDecidoDatabase(get()) }
    
    // HTTP Client
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }
    
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<ProposalRepository> { ProposalRepositoryImpl(get(), get(), get()) }
    single<CandidateRepository> { CandidateRepositoryImpl(get()) }
    single<RepresentativeRepository> { RepresentativeRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<SyncRepository> { SyncRepositoryImpl(get()) }
    single<IdentityRepository> { IdentityRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<ElectionRepository> { ElectionRepositoryImpl(get(), get(), get()) }

    // Sync Manager
    single { SyncManager(get(), get()) }

    // Data Seeder
    single { DataSeeder(get(), get(), get(), get(), get(), get()) }
}

expect fun platformModule(): Module
