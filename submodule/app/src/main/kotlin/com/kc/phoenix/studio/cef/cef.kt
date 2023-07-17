package com.kc.phoenix.studio.cef

import org.cef.handler.CefKeyboardHandler
import org.cef.misc.EventFlags
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

/**
 * copy from intellij-community
 *
 * @author zhangxinkun
 */

object CefEvent {
    private val CEF_2_JAVA_KEYCODES = hashMapOf<Int, Int>()
    private val CEF_2_JAVA_MODIFIERS = hashMapOf<Int, Int>()

    init {


        // CEF uses Windows VK: https://github.com/adobe/webkit/blob/master/Source/WebCore/platform/chromium/KeyboardCodes.h
        // [tav] todo: check if there's more VK's to manually convert
        CEF_2_JAVA_KEYCODES[0x0d] = KeyEvent.VK_ENTER;
        CEF_2_JAVA_KEYCODES[0x08] = KeyEvent.VK_BACK_SPACE;
        CEF_2_JAVA_KEYCODES[0x09] = KeyEvent.VK_TAB;

        CEF_2_JAVA_MODIFIERS[EventFlags.EVENTFLAG_COMMAND_DOWN] = InputEvent.META_DOWN_MASK;
        CEF_2_JAVA_MODIFIERS[EventFlags.EVENTFLAG_CONTROL_DOWN] = InputEvent.CTRL_DOWN_MASK;
        CEF_2_JAVA_MODIFIERS[EventFlags.EVENTFLAG_SHIFT_DOWN] = InputEvent.SHIFT_DOWN_MASK;
        CEF_2_JAVA_MODIFIERS[EventFlags.EVENTFLAG_ALT_DOWN] = InputEvent.ALT_DOWN_MASK;
        CEF_2_JAVA_MODIFIERS[EventFlags.EVENTFLAG_LEFT_MOUSE_BUTTON] = InputEvent.BUTTON1_DOWN_MASK;
        CEF_2_JAVA_MODIFIERS[EventFlags.EVENTFLAG_MIDDLE_MOUSE_BUTTON] = InputEvent.BUTTON2_DOWN_MASK;
        CEF_2_JAVA_MODIFIERS[EventFlags.EVENTFLAG_RIGHT_MOUSE_BUTTON] = InputEvent.BUTTON3_DOWN_MASK;
    }


    fun convertCefKeyEvent(cefKeyEvent: CefKeyboardHandler.CefKeyEvent, source: Component?): KeyEvent {
        return KeyEvent(
            source,
            convertCefKeyEventType(cefKeyEvent),
            System.currentTimeMillis(),
            convertCefKeyEventModifiers(cefKeyEvent),
            convertCefKeyEventKeyCode(cefKeyEvent),
            cefKeyEvent.character,
            KeyEvent.KEY_LOCATION_UNKNOWN
        )
    }

    fun convertCefKeyEventType(cefKeyEvent: CefKeyboardHandler.CefKeyEvent): Int {
        return when (cefKeyEvent.type) {
            CefKeyboardHandler.CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN, CefKeyboardHandler.CefKeyEvent.EventType.KEYEVENT_KEYDOWN -> KeyEvent.KEY_PRESSED
            CefKeyboardHandler.CefKeyEvent.EventType.KEYEVENT_KEYUP -> KeyEvent.KEY_RELEASED
            CefKeyboardHandler.CefKeyEvent.EventType.KEYEVENT_CHAR -> KeyEvent.KEY_TYPED
        }
    }

    fun convertCefKeyEventKeyCode(cefKeyEvent: CefKeyboardHandler.CefKeyEvent): Int {
        val value = CEF_2_JAVA_KEYCODES[cefKeyEvent.windows_key_code]
        return value ?: cefKeyEvent.windows_key_code
    }

    fun convertCefKeyEventModifiers(cefKeyEvent: CefKeyboardHandler.CefKeyEvent): Int {
        var javaModifiers = 0
        for ((key, value) in CEF_2_JAVA_MODIFIERS) {
            if (cefKeyEvent.modifiers and key != 0) {
                javaModifiers = javaModifiers or value
            }
        }
        return javaModifiers
    }
}

