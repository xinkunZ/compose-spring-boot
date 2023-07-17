package com.kc.phoenix.studio.cef

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import com.google.common.collect.Lists
import com.jetbrains.cef.JCefAppConfig
import com.kc.phoenix.studio.*
import org.apache.commons.lang3.StringUtils
import org.cef.*
import org.cef.browser.*
import org.cef.callback.*
import org.cef.handler.*
import org.jsoup.Jsoup
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.*
import org.springframework.http.RequestEntity
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.awt.*
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.*
import javax.swing.*

object MyCefBrowser : ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    private val app = AtomicReference<CefApp>()
    private val cefClient = AtomicReference<CefClient>()

    private lateinit var applicationContext: ApplicationContext
    val pages = mutableStateListOf<BrowserTab>()

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        applicationContext = event.applicationContext
        loadCefBrowser(event.args)
    }

    private fun loadCefBrowser(args: Array<String>) {
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
            val app = CefApp.getInstance(array, instance.cefSettings)
            val cefClient = app.createClient()
            this.app.set(app)
            this.cefClient.set(cefClient)

            cefClient.addDownloadHandler(DownloadHandlerAdapter)
            cefClient.addLifeSpanHandler(LifeSpanHandlerAdapter)
            cefClient.addKeyboardHandler(KeyboardHandlerAdapter)
            cefClient.addLoadHandler(LoadHandlerAdapter)
            val url = "https://www.baidu.com" // or use systemUrl show default page
            val createBrowser = cefClient.createBrowser(url, CefRendering.DEFAULT, false)
            pages.add(BrowserTab(WebPageTab(true, url), createBrowser))
        } catch (e: Throwable) {
            MAIN_LOGGER.error("error: ", e)
            MAIN_LOGGER.warn("start server with no GUI!")
            throw e
        }
    }

    object LoadHandlerAdapter : CefLoadHandlerAdapter() {
        override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
            pages.find { it.cefBrowser == browser }?.webPageTab?.updateTitle()
        }
    }

    object DownloadHandlerAdapter : CefDownloadHandlerAdapter() {

        override fun onBeforeDownload(
            browser: CefBrowser,
            downloadItem: CefDownloadItem,
            suggestedName: String,
            callback: CefBeforeDownloadCallback
        ) {
            callback.Continue(suggestedName, true)
        }

        override fun onDownloadUpdated(
            browser: CefBrowser,
            downloadItem: CefDownloadItem,
            callback: CefDownloadItemCallback
        ) {
            if (downloadItem.percentComplete > 1) {
                // remove the download tab after 1%.
                pages.removeIf { it.cefBrowser == browser }
            }
            println("download: ${downloadItem.percentComplete}%")
            if (downloadItem.isComplete) {
                browser.doClose()
                Desktop.getDesktop().browseFileDirectory(File(downloadItem.fullPath))
            }
        }
    }

    object LifeSpanHandlerAdapter : CefLifeSpanHandlerAdapter() {
        override fun onBeforePopup(
            browser: CefBrowser,
            frame: CefFrame,
            targetUrl: String,
            targetFrameName: String
        ): Boolean {
            // or just use browser.loadURL() to load new url in the current tab without multi tabs support
            val newBrowser = cefClient.get().createBrowser(targetUrl, CefRendering.DEFAULT, false)
            newBrowser.createImmediately()
            pages.add(
                BrowserTab(
                    WebPageTab(false, targetUrl), newBrowser
                )
            )
            return true
        }

    }

    override fun destroy() {
        pages.forEach { runCatching { it.cefBrowser.doClose() } }
        runCatching { cefClient.get().dispose() }
        runCatching { app.get().dispose() }
    }


}

data class WebPageTab(val home: Boolean = false, val url: String) {
    var title: String = ""
    fun updateTitle() {
        title = title(url)
    }

}

val restTemplate: RestTemplate by lazy {
    val restTemplate = RestTemplate()
    restTemplate.messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))
    restTemplate
}

private fun title(url: String): String {
    return runCatching {
        val request = RequestEntity.get(url).build()
        val response = restTemplate.exchange(request, String::class.java)
        if (response.statusCode.is2xxSuccessful) {
            response.body?.let {
                Jsoup.parse(it).title()
            } ?: ""
        } else ""
    }.getOrElse { "" }
}

class BrowserTab(
    val webPageTab: WebPageTab,
    val cefBrowser: CefBrowser
)


@OptIn(ExperimentalComposeUiApi::class)
object KeyboardHandlerAdapter : CefKeyboardHandlerAdapter() {

    private fun KeyEvent.ctrl(): Boolean {
        return (OS.isMacintosh() && this.isMetaPressed) || this.isCtrlPressed
    }

    data class Action(
        val shortcut: String,
        val key: Predicate<KeyEvent>,
        val action: Consumer<in CefBrowser>
    )

    private val keyMapping = arrayListOf<Action>()

    init {
        keyMapping.add(Action("\$Cut", { it.ctrl() && it.key == Key.X }) { it.focusedFrame.cut() })
        keyMapping.add(Action("\$Copy", { it.ctrl() && it.key == Key.C }) { it.focusedFrame.copy() })
        keyMapping.add(Action("\$Paste", { it.ctrl() && it.key == Key.V }) { it.focusedFrame.paste() })
        keyMapping.add(Action("\$SelectAll", { it.ctrl() && it.key == Key.A }) { it.focusedFrame.selectAll() })
        keyMapping.add(Action("\$Undo", { it.ctrl() && it.key == Key.Z }) { it.focusedFrame.undo() })
        keyMapping.add(Action("\$Redo", { it.ctrl() && it.key == Key.Y }) { it.focusedFrame.redo() })
        keyMapping.add(Action("\$Dev", { it.key == Key.F12 }) {
            val devTools = JFrame()
            val panel = JPanel(BorderLayout())
            panel.add(it.devTools.uiComponent)
            devTools.contentPane = panel
            devTools.size = Dimension(800, 600)
            devTools.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            devTools.isVisible = true
        })
    }

    override fun onKeyEvent(browser: CefBrowser, cefKeyEvent: CefKeyboardHandler.CefKeyEvent): Boolean {
        val composeEvent = KeyEvent(
            CefEvent.convertCefKeyEvent(cefKeyEvent, browser.uiComponent)
        )
        val mapping = keyMapping.firstOrNull {
            it.key.test(composeEvent)
        }
        mapping?.action?.accept(browser) ?: return false
        return true
    }
}
