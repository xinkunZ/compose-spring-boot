package com.kc.phoenix.studio.app.swing

import com.google.common.collect.Lists
import com.jetbrains.cef.JCefAppConfig
import com.kc.phoenix.studio.app.systemUrl
import org.apache.commons.lang3.StringUtils
import org.cef.CefApp
import org.cef.browser.CefBrowser
import org.cef.browser.CefRendering
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * jcef内嵌浏览器
 *
 * @author zhangxinkun
 */
object PlayFrame {


    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val frame = JFrame()
    private val app = AtomicReference<CefApp>()
    private var contentPane: JPanel? = null

    init {
        contentPane = JPanel(BorderLayout())
        try {
            frame.title = "回放工具"
            frame.contentPane = contentPane
            frame.defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
//            frame.jMenuBar = createMenu()
//            frame.addWindowListener(object : WindowAdapter() {
//                override fun windowClosing(e: WindowEvent) {
//
//                }
//            })
        } catch (e: Throwable) {
            logger.error("init GUI fail:{}", e.message)
        }
    }

    lateinit var mainBrowser: CefBrowser
        private set

    fun initUI(args: Array<String>) {
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
            logger.info("start with args: {}", allArgs)
            val array = allArgs.toTypedArray()
            CefApp.startup(array)
            app.set(CefApp.getInstance(array, instance.cefSettings))
            mainBrowser = app.get()
                .createClient()
                .createBrowser(systemUrl, CefRendering.DEFAULT, false)
            mainBrowser.uiComponent?.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_F12) {
                        val devTools = JFrame()
                        val panel = JPanel(BorderLayout())
                        panel.add(mainBrowser.devTools.uiComponent)
                        devTools.contentPane = panel
                        devTools.setSize(
                            Math.min(frame.size.getWidth() - 100, 800.0).toInt(),
                            Math.min(frame.size.getHeight() - 100, 600.0).toInt()
                        )
                        devTools.isVisible = true
                        frame.defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
                    }
                }
            })
            LoadingFrame.stopLoading()
//            SwingUtilities.invokeLater {
//                contentPane!!.add(mainBrowser.uiComponent, BorderLayout.CENTER)
//                contentPane!!.updateUI()
//                frame.pack()
//                frame.setSize(1000, 600)
//                frame.setLocationRelativeTo(null)
//                frame.isVisible = true
//            }
        } catch (e: Throwable) {
            logger.error("error: ", e)
            logger.warn("start server with no GUI!")
        }
    }


}