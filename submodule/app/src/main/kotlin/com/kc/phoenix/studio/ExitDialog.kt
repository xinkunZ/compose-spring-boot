package com.kc.phoenix.studio

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*

@Composable
@Preview
fun ExitDialog(doExit: () -> Unit = {}, dismiss: () -> Unit = {}, state: DialogState) {
    Dialog(
        onCloseRequest = dismiss, resizable = false, title = "confirmation", state = state
    ) {
        Column(modifier = Modifier.padding(10.dp).fillMaxHeight()) {
            Row(modifier = Modifier.padding(20.dp)) {

                Icon(
                    Icons.Filled.Warning,
                    "",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.padding(10.dp))

                BoxWithConstraints(modifier = Modifier.align(Alignment.CenterVertically)) {
                    ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                        Text("Are you sure to close app?")
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
            Text("Cancel")
        }

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            onClick = confirm,
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("Confirm")
        }
    }
}
