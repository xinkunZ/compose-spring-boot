package com.kc.phoenix.studio.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState

@Composable
@Preview
fun ExitDialog(doExit: () -> Unit = {}, dismiss: () -> Unit = {}, state: DialogState) {
    Dialog(
        onCloseRequest = dismiss, resizable = false, title = "确认", state = state
    ) {
        Column(modifier = Modifier.padding(10.dp).fillMaxHeight()) {
            Row(modifier = Modifier.padding(20.dp)) {

                Icon(
                    Icons.Filled.Warning,
                    "",
                    tint = Color.Yellow, modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.padding(10.dp))

                BoxWithConstraints(modifier = Modifier.align(Alignment.CenterVertically)) {
                    ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                        Text("确定要退出吗？")
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            ConfirmCloseButtons(doExit, dismiss)
        }
    }
}

@Composable
fun ColumnScope.ConfirmCloseButtons(confirm: () -> Unit, dismiss: () -> Unit) {
    Row(
        modifier = Modifier.padding(10.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            border = BorderStroke(1.dp, Color.Gray),
            onClick = dismiss,
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text("取消")
        }

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            onClick = confirm,
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("确定")
        }
    }
}
