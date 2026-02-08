package org.shadow.project.di

import org.koin.core.context.GlobalContext.startKoin

fun initKoin() {
    startKoin {
        printLogger()
        modules(controllerModules, botModules)
    }
}