package zdrowy.senior.io.di

import org.koin.dsl.module

val domainModule = module {
    // Use cases will be added here.
}

val dataModule = module {
    // Repository bindings will be added here.
}

val uiModule = module {
    // ViewModel bindings will be added here.
}

val appModules = listOf(domainModule, dataModule, uiModule)
