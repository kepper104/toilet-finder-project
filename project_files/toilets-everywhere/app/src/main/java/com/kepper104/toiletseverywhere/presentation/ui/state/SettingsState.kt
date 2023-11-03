package com.kepper104.toiletseverywhere.presentation.ui.state

data class SettingsState (
    val selectedDarkModeOption: DarkModeStatus = DarkModeStatus.AUTO,

)

enum class DarkModeStatus{
    AUTO, FORCED_ON, FORCED_OFF
}