package com.kc.phoenix.studio.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.window.rememberWindowState
import com.google.common.collect.Lists
import com.jetbrains.cef.JCefAppConfig
import io.kanro.compose.jetbrains.expui.theme.LightTheme
import io.kanro.compose.jetbrains.expui.theme.Theme
import io.kanro.compose.jetbrains.expui.window.JBWindow
import io.kanro.compose.jetbrains.expui.window.LocalMainToolBarColors
import org.apache.commons.lang3.StringUtils
import org.cef.CefApp
import org.cef.browser.CefBrowser
import org.cef.browser.CefRendering
import org.springframework.boot.SpringApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import java.awt.Component
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

val primary = Color(233, 97, 0)

fun startWithUI(args: Array<String>) {
    val applicationContext = runSpringBoot(args)
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


fun runSpringBoot(args: Array<String>): ApplicationContext {
    MAIN_LOGGER.info("load spring boot")
    return SpringApplicationBuilder(StudioApplication::class.java)
        .headless(false)
        .listeners(ApplicationReadyListener)
        .run(*withPort(args).toTypedArray());
}


object ApplicationReadyListener : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        MAIN_LOGGER.info("load ui")
        loadCefBrowser(event.args)
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
