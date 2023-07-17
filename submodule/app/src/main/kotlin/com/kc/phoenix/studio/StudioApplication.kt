package com.kc.phoenix.studio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.window.rememberWindowState
import com.kc.phoenix.studio.cef.MyCefBrowser
import com.kc.phoenix.studio.cef.TabbedBrowser
import io.kanro.compose.jetbrains.expui.theme.LightTheme
import io.kanro.compose.jetbrains.expui.theme.Theme
import io.kanro.compose.jetbrains.expui.window.JBWindow
import io.kanro.compose.jetbrains.expui.window.LocalMainToolBarColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.core.env.MapPropertySource
import org.springframework.web.bind.annotation.RestController
import java.awt.Toolkit
import java.io.IOException
import java.net.Socket
import kotlin.concurrent.thread

/**
 * @author zhangxinkun
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@RestController
class StudioApplication

val primary = Color(33, 41, 54)

val MAIN_LOGGER: Logger = LoggerFactory.getLogger(StudioApplication::class.java)

val mainToolBarColors = LightTheme.MainToolBarColors.copy(
    normalAreaColors = LightTheme.MainToolBarColors.normalAreaColors
        .copy(startBackground = primary, endBackground = primary),
)
val theme = object : Theme by LightTheme {
    override fun provideValues(): Array<ProvidedValue<*>> {
        val value = LightTheme.provideValues().toMutableSet()
        value.removeIf { it.compositionLocal == LocalMainToolBarColors }
        value.add(LocalMainToolBarColors provides mainToolBarColors)
        return value.toTypedArray()
    }
}

lateinit var applicationContext: ApplicationContext

fun main(args: Array<String>) = application {
    val size = Toolkit.getDefaultToolkit().screenSize.size

    val browserReady = remember { mutableStateOf(false) }

    LaunchedEffect("", block = {
        withContext(Dispatchers.IO) {
            // run spring boot in another thread.
            // note: the `Dispatchers.Main` thread is swing awt, not `main` thread
            MAIN_LOGGER.info("load spring boot")
            applicationContext = SpringApplicationBuilder(StudioApplication::class.java)
                .headless(false)
                .listeners(RandomPortPrepare, MyCefBrowser)
                .run(*args)
            browserReady.value = true
        }
    })

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

    JBWindow(
        title = "Compose Boot",
        theme = theme,
        state = windowState,
        visible = frameVisible.value,
        onCloseRequest = {
            openDialog.value = true
        },
        mainToolBar = {
        }
    ) {
        MaterialTheme {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.matchParentSize()) {
                    if (browserReady.value) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TabbedBrowser()
                        }
                    } else {
                        Column(modifier = Modifier.align(Alignment.Center)) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(150.dp, 150.dp).align(Alignment.CenterHorizontally),
                                strokeWidth = 10.dp
                            )
                            Spacer(modifier = Modifier.height(30.dp))
                            ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    text = "loading..."
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (openDialog.value) {
                        // a simple exist confirm dialog
                        ExitDialog(
                            state = dialogState,
                            doExit = {
                                openDialog.value = false
                                frameVisible.value = false
                                thread {
                                    MAIN_LOGGER.info("do exit...")
                                    applicationContext.let { SpringApplication.exit(it, { 0 }) }
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

object RandomPortPrepare : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val range = 9045..9999
        val randomPort = randomPort(range)
        if (randomPort < range.first) {
            throw IllegalStateException("can not find available port in [$range]")
        }
        MAIN_LOGGER.info("service start with port: $randomPort")
        event.environment.propertySources.addFirst(MapPropertySource("random-port", mapOf("server.port" to randomPort)))
    }

    private fun isLocalPortInUse(port: Int): Boolean {
        try {
            Socket("localhost", port).use { ignored -> return true }
        } catch (ignored: IOException) {
            return false
        }
    }

    private fun randomPort(range: IntRange): Int {
        val port = range.find { !isLocalPortInUse(it) }
        if (port == null) {
            MAIN_LOGGER.warn("can not find available port in [$range]")
            return -1
        }
        return port
    }
}
