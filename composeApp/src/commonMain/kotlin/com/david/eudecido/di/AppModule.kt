package com.david.eudecido.di

import com.david.eudecido.screens.ai.AIAssistantScreenModel
import com.david.eudecido.screens.ai.AIInsightsScreenModel
import com.david.eudecido.screens.ai.AIProposalHelperScreenModel
import com.david.eudecido.screens.ai.GlobalAIScreenModel
import com.david.eudecido.screens.auth.LoginScreenModel
import com.david.eudecido.screens.auth.RegisterScreenModel
import com.david.eudecido.screens.auth.WelcomeScreenModel
import com.david.eudecido.screens.candidates.CandidateDetailScreenModel
import com.david.eudecido.screens.candidates.CandidateListScreenModel
import com.david.eudecido.screens.community.CommunityScreenModel
import com.david.eudecido.screens.discussion.DiscussionScreenModel
import com.david.eudecido.screens.elections.ApplyCandidateScreenModel
import com.david.eudecido.screens.elections.ElectionDetailScreenModel
import com.david.eudecido.screens.elections.ElectionListScreenModel
import com.david.eudecido.screens.home.HomeScreenModel
import com.david.eudecido.screens.notifications.NotificationsScreenModel
import com.david.eudecido.screens.profile.DelegationScreenModel
import com.david.eudecido.screens.profile.ProfileScreenModel
import com.david.eudecido.screens.proposals.CreateProposalScreenModel
import com.david.eudecido.screens.proposals.ProposalDetailScreenModel
import com.david.eudecido.screens.proposals.ProposalListScreenModel
import com.david.eudecido.screens.report.ReportScreenModel
import com.david.eudecido.screens.representatives.RepresentativesScreenModel
import com.david.eudecido.screens.results.ResultsScreenModel
import com.david.eudecido.screens.search.SearchScreenModel
import com.david.eudecido.screens.settings.SettingsScreenModel
import com.david.eudecido.screens.territory.TerritoryOverviewScreenModel
import com.david.eudecido.screens.territory.TerritorySelectionScreenModel
import com.david.eudecido.screens.voting.VotingScreenModel
import org.koin.dsl.module

val appModule = module {
    single { GlobalAIScreenModel() }

    // ── ScreenModels sem parâmetros de navegação ──────────────────────────
    factory { WelcomeScreenModel() }
    factory { HomeScreenModel(get(), get()) }
    factory { RegisterScreenModel(get(), get()) }
    factory { LoginScreenModel(get()) }
    factory { CreateProposalScreenModel(get(), get()) }
    factory { RepresentativesScreenModel(get()) }
    factory { NotificationsScreenModel(get()) }
    factory { SettingsScreenModel(get()) }
    factory { ProfileScreenModel(get()) }
    factory { DelegationScreenModel() }
    factory { ElectionListScreenModel(get()) }
    factory { TerritorySelectionScreenModel() }
    factory { ProposalListScreenModel(get()) }
    factory { SearchScreenModel() }
    factory { AIProposalHelperScreenModel() }
    factory { AIAssistantScreenModel() }

    // ── ScreenModels com parâmetros de navegação ──────────────────────────
    factory { (proposalId: String) ->
        ProposalDetailScreenModel(proposalId, get())
    }

    factory { (proposalId: String, proposalTitle: String) ->
        VotingScreenModel(proposalId, proposalTitle, get(), get())
    }

    factory { (proposalId: String) ->
        DiscussionScreenModel(proposalId, get())
    }

    factory { (proposalId: String) ->
        ResultsScreenModel(proposalId, get())
    }

    factory { (electionId: String) ->
        ElectionDetailScreenModel(electionId, get())
    }

    factory { (electionId: String) ->
        ApplyCandidateScreenModel(electionId, get())
    }

    factory { (electionId: String) ->
        CandidateListScreenModel(electionId, get())
    }

    factory { (candidateId: String) ->
        CandidateDetailScreenModel(candidateId)
    }

    factory { (locationName: String) ->
        CommunityScreenModel(locationName)
    }

    factory { (f: String, m: String, r: String) ->
        TerritoryOverviewScreenModel(f, m, r)
    }

    factory { (summary: String, consensus: String, impact: String) ->
        AIInsightsScreenModel(summary, consensus, impact)
    }

    factory { (title: String, description: String, count: Int, args: List<String>) ->
        ReportScreenModel(title, description, count, args)
    }
}
