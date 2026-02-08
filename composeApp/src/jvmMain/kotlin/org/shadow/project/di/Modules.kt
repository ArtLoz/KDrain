package org.shadow.project.di

import com.l2bot.bridge.api.TransportProvider
import com.l2bot.bridge.core.L2Adrenaline
import com.l2bot.bridge.transport.jvm.JvmTransportProvider
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.shadow.project.WindowController
import org.shadow.project.logging.LogController
import org.shadow.project.plugin.PluginManager
import org.shadow.project.ui.main.MainViewModel

val controllerModules = module {
    singleOf(::WindowController)
    singleOf(::LogController)
    singleOf(::PluginManager)
    viewModelOf(::MainViewModel)
}
val botModules = module {
    single<TransportProvider> { JvmTransportProvider() }
    single { L2Adrenaline(get()) }
}