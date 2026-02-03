package org.shadow.project.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.shadow.project.WindowController
import org.shadow.project.logging.LogController
import org.shadow.project.ui.main.MainViewModel

val controllerModules = module {
    singleOf(::WindowController)
    singleOf(::LogController)
    viewModelOf(::MainViewModel)
}