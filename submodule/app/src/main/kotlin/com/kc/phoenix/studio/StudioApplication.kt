package com.kc.phoenix.studio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.kanro.compose.jetbrains.expui.theme.*
import io.kanro.compose.jetbrains.expui.window.*
import kotlinx.coroutines.*
import org.slf4j.*
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.RestController
import java.awt.Toolkit
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
                .listeners(MyCefBrowser)
                .run(*withPort(args).toTypedArray());
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
                        SwingPanel(factory = {
                            MyCefBrowser.mainBrowser.uiComponent
                        })
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
                                    SpringApplication.exit(applicationContext, { 0 })
                                    runCatching { MyCefBrowser.mainBrowser.doClose() }
                                    runCatching { MyCefBrowser.app.get().dispose() }
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






