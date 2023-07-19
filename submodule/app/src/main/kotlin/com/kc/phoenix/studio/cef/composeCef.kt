package com.kc.phoenix.studio.cef

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.awt.BorderLayout
import javax.swing.JPanel


/**
 * @author zhangxinkun
 */

@Composable
fun TestAddScreen(add: () -> Unit) {
    Column {
        Button(
            onClick = add,
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("Add")
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TabbedBrowser() {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = remember { MyCefBrowser.pages }
    selectedTabIndex = if (selectedTabIndex >= tabs.size) selectedTabIndex - 1 else selectedTabIndex
    ScrollableTabRow(
        edgePadding = 0.dp,
        selectedTabIndex = selectedTabIndex,
        containerColor = Color(229, 229, 229),
        tabs = {
            tabs.forEachIndexed { index, tab ->
                Tab(selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index }) {
                    when (index) {
                        0 -> TabButton(tab.webPageTab)
                        else -> TabButton(tab.webPageTab) {
                            tabs.removeAt(index)
                            /* TODO
                               if you want to focus right tab, you can set selectedTabIndex = index
                               but the value of selectedTabIndex not change, compose will not refresh
                               swing panel.
                               Have not found resolve way for now.
                            */
                            selectedTabIndex = index - 1
                        }
                    }
                }
            }
        })

    SwingBrowser(selectedTabIndex, tabs)
//    TestAddScreen { }
}

val cefPanel = JPanel(BorderLayout()).apply {
    border = null
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SwingBrowser(selectedTabIndex: Int, tabs: SnapshotStateList<BrowserTab>) {
    if (selectedTabIndex != -1) {
        SwingPanel(modifier = Modifier.fillMaxSize(), factory = {
            cefPanel
        }, update = {
            val cef = tabs[selectedTabIndex].cefBrowser
            cef.setFocus(true)
            it.add(cef.uiComponent, BorderLayout.CENTER)
        })
    }
}

@Preview
@Composable
fun TabButton(tab: WebPageTab, close: () -> Unit = {}) {
    Box(
        modifier = Modifier.size(200.dp, 45.dp),
    ) {
        Text(tab.title, modifier = Modifier.padding(end = 10.dp).align(Alignment.Center))
        if (!tab.home) {
            IconButton(onClick = close, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }
    }
}
