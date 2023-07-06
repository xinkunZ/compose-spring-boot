package com.kc.phoenix.studio.app.swing

import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingUtilities

/**
 * 丑丑的启动进度条
 *
 * @author zhangxinkun
 */
object LoadingFrame {

    private val progressBar = JProgressBar()

    private val loadingFrame = JFrame()

    fun startLoading() {
        val loading = JPanel(BorderLayout())
        loading.add(progressBar, BorderLayout.CENTER)
        progressBar.isIndeterminate = true
        progressBar.setBounds(10, 10, 600, 30)
        SwingUtilities.invokeLater {
            loadingFrame.contentPane = progressBar
            loadingFrame.isUndecorated = true
            loadingFrame.pack()
            loadingFrame.setSize(600, 30)
            loadingFrame.setLocationRelativeTo(null)
            loadingFrame.isVisible = true
        }
    }

    fun stopLoading() {
        SwingUtilities.invokeLater {
            loadingFrame.isVisible = false
            loadingFrame.dispose()
        }
    }
}