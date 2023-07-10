package com.kc.phoenix.studio.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.google.common.collect.Lists
import com.jetbrains.cef.JCefAppConfig
import io.kanro.compose.jetbrains.expui.theme.*
import io.kanro.compose.jetbrains.expui.window.*
import org.apache.commons.lang3.StringUtils
import org.cef.CefApp
import org.cef.browser.*
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.*
import java.awt.*
import java.awt.event.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread


object ApplicationReadyListener : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        MAIN_LOGGER.info("load ui")
        loadCefBrowser(event.args)
        mainUI(event.applicationContext)
    }

}

fun mainUI(applicationContext: ConfigurableApplicationContext) {
    val size = Toolkit.getDefaultToolkit().screenSize.size
    application {
        val frameVisible = remember { mutableStateOf(true) }
        val windowState = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(size.width.dp, size.height.dp) - DpSize(150.dp, 150.dp)
        )
        val openDialog = remember { mutableStateOf(false) }

        val dialogState = rememberDialogState(
            windowState.position,
            size = DpSize(400.dp, 200.dp)
        )

        val color = LightTheme.MainToolBarColors.copy(
            normalAreaColors = LightTheme.MainToolBarColors.normalAreaColors
                .copy(startBackground = Color.Blue, endBackground = Color.Blue),
        )
        val theme = object : Theme by LightTheme {
            override fun provideValues(): Array<ProvidedValue<*>> {
                val value = LightTheme.provideValues().toMutableSet()
                value.removeIf { it.compositionLocal == LocalMainToolBarColors }
                value.add(LocalMainToolBarColors provides color)
                return value.toTypedArray()
            }
        }
        val state = rememberWindowState(size = DpSize(1200.dp, 700.dp))
        JBWindow(
            title = "Spring Boot",
            theme = theme,
            state = state,
            onCloseRequest = {
                openDialog.value = true
            },
            mainToolBar = {
            }
        ) {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.matchParentSize()) {
                        SwingPanel(factory = {
                            mainBrowser.uiComponent
                        })
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (openDialog.value) {
                            ExitDialog(
                                state = dialogState,
                                doExit = {
                                    openDialog.value = false
                                    frameVisible.value = false
                                    thread {
                                        MAIN_LOGGER.info("do exit...")
                                        SpringApplication.exit(applicationContext, { 0 })
                                        exitApplication()
                                    }
                                },
                                dismiss = { openDialog.value = false }
                            )
                        }
                    }
                }
            }
        }
    }
}


lateinit var mainBrowser: CefBrowser
    private set

private val app = AtomicReference<CefApp>()

fun loadCefBrowser(args: Array<String>): Component {
    try {
        val instance = JCefAppConfig.getInstance()
        val locale: MutableList<String?> = Lists.newArrayList()
        val defaultLocale = Locale.getDefault()
        if (StringUtils.isNotBlank(defaultLocale.language)) {
            locale.add(defaultLocale.language)
        }
        if (StringUtils.isNotBlank(defaultLocale.country)) {
            locale.add(defaultLocale.country)
        }
        instance.cefSettings.locale = StringUtils.join(locale, "-")
        val appArgs = instance.appArgs
        val allArgs = arrayListOf<String>()
        allArgs.addAll(appArgs)
        allArgs.addAll(args)
        MAIN_LOGGER.info("start with args: {}", allArgs)
        val array = allArgs.toTypedArray()
        CefApp.startup(array)
        app.set(CefApp.getInstance(array, instance.cefSettings))
        mainBrowser = app.get()
            .createClient()
            .createBrowser(systemUrl, CefRendering.DEFAULT, false)
        mainBrowser.uiComponent?.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_F12) {
                }
            }
        })
        return mainBrowser.uiComponent
    } catch (e: Throwable) {
        MAIN_LOGGER.error("error: ", e)
        MAIN_LOGGER.warn("start server with no GUI!")
        throw e
    }

}
