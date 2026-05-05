package com.david.eudecido.di

import com.david.eudecido.screens.ai.GlobalAIScreenModel
import com.david.eudecido.screens.home.HomeScreenModel
import com.david.eudecido.screens.auth.RegisterScreenModel
import com.david.eudecido.screens.auth.LoginScreenModel
import com.david.eudecido.screens.voting.VotingScreenModel
import com.david.eudecido.screens.discussion.DiscussionScreenModel
import com.david.eudecido.screens.proposals.CreateProposalScreenModel
import com.david.eudecido.screens.proposals.ProposalDetailScreenModel
import com.david.eudecido.screens.representatives.RepresentativesScreenModel
import com.david.eudecido.screens.results.ResultsScreenModel
import com.david.eudecido.screens.notifications.NotificationsScreenModel
import com.david.eudecido.screens.settings.SettingsScreenModel
import org.koin.dsl.module

val appModule = module {
    single { GlobalAIScreenModel() }
    
    // ViewModels / ScreenModels
    factory { HomeScreenModel(get(), get()) }
    factory { RegisterScreenModel(get(), get()) }
    factory { LoginScreenModel(get(), get()) }
    factory { CreateProposalScreenModel(get(), get()) }
    factory { RepresentativesScreenModel(get()) }
    factory { NotificationsScreenModel(get()) }
    factory { SettingsScreenModel(get()) }
    
    // Factories com parâmetros
    factory { (proposalId: String) -> 
        ProposalDetailScreenModel(proposalId, get()) 
    }
    
    factory { (proposalId: String, proposalTitle: String) -> 
        VotingScreenModel(proposalId, proposalTitle, get()) 
    }
    
    factory { (proposalId: String) -> 
        DiscussionScreenModel(proposalId, get()) 
    }

    factory { (proposalId: String) -> 
        ResultsScreenModel(proposalId, get()) 
    }
}
