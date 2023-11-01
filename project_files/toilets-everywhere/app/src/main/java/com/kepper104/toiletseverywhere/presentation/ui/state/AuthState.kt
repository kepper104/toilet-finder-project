package com.kepper104.toiletseverywhere.presentation.ui.state

import com.kepper104.toiletseverywhere.data.AuthUiStatus
import com.kepper104.toiletseverywhere.data.RegistrationError

/**
 * TODO
 *
 */
data class AuthState(
    val status: AuthUiStatus = AuthUiStatus.MAIN,
    val registerLogin: String = "",
    val registerPassword: String = "",
    val registerPasswordConfirmation: String = "",
    val registerName: String = "",
    val loginLogin: String = "",
    val loginPassword: String = "",
    val showInvalidPasswordPrompt: Boolean = false,
    val registrationError: RegistrationError? = null

)

