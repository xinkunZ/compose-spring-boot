package com.kc.phoenix.studio

import com.google.common.collect.Lists
import com.jetbrains.cef.JCefAppConfig
import org.apache.commons.lang3.StringUtils
import org.cef.CefApp
import org.cef.browser.*
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.*
import java.awt.Component
import java.awt.event.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference

object MyCefBrowser : ApplicationListener<ApplicationReadyEvent> {
    lateinit var mainBrowser: CefBrowser
        private set

    val app = AtomicReference<CefApp>()

    private lateinit var applicationContext: ApplicationContext

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        this.applicationContext = event.applicationContext
        loadCefBrowser(event.args)
    }

    private fun loadCefBrowser(args: Array<String>): Component {
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

}

