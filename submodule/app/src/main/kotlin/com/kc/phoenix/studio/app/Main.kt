package com.kc.phoenix.studio.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme
import com.kc.phoenix.studio.app.swing.ExitDialog
import com.kc.phoenix.studio.app.swing.LoadingFrame
import com.kc.phoenix.studio.app.swing.PlayFrame
import org.springframework.boot.SpringApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import java.awt.Toolkit
import kotlin.concurrent.thread

val primary = Color(233, 97, 0)

fun startWithUI(args: Array<String>) {
    FlatArcOrangeIJTheme.setup()
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

        Window(title = "Map Studio", onCloseRequest = {
            openDialog.value = true
        }, resizable = true, state = windowState, visible = frameVisible.value, onKeyEvent = {
            println("key event: ${it.key}")
            true
        }) {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.matchParentSize()) {
                        SwingPanel(factory = {
                            PlayFrame.mainBrowser.uiComponent
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
                                        log.info("do exit...")
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
    log.info("load spring boot")
    LoadingFrame.startLoading()
    return SpringApplicationBuilder(StudioApplication::class.java)
        .headless(false)
        .listeners(ApplicationReadyListener)
        .run(*withPort(args).toTypedArray());
}


object ApplicationReadyListener : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        log.info("load ui")
        LoadingFrame.stopLoading()
        PlayFrame.initUI(event.args)
    }

}
