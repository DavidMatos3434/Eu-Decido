package com.david.eudecido.di

import com.david.eudecido.data.*
import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.sync.SyncManager
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
    
    // Repositories
    single<ProposalRepository> { ProposalRepositoryImpl(get()) }
    single<CandidateRepository> { CandidateRepositoryImpl(get()) }
    single<RepresentativeRepository> { RepresentativeRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<SyncRepository> { SyncRepositoryImpl(get()) }
    single<IdentityRepository> { IdentityRepositoryImpl() }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<ElectionRepository> { ElectionRepositoryImpl(get()) }

    // Sync Manager (O motor de sincronização)
    single { SyncManager(get()) }

    // Data Seeder
    single { DataSeeder(get(), get(), get(), get(),get(),get()) }
}

expect fun platformModule(): Module
