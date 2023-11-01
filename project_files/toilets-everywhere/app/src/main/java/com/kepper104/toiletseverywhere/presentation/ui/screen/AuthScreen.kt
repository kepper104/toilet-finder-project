package com.kepper104.toiletseverywhere.presentation.ui.screen

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.data.AuthUiStatus
import com.kepper104.toiletseverywhere.isNavStackReady
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


/**
 * TODO
 *
 * @param navigator
 * @param mainViewModel
 */
@Destination
@Composable
fun AuthScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)

) {

    isNavStackReady = true

    BackHandler(
        onBack = { mainViewModel.clearAuthState() }
    )


    when (mainViewModel.authState.status){
        AuthUiStatus.MAIN -> { AuthScreenMain(navigator, mainViewModel) }
        AuthUiStatus.REGISTER -> { AuthScreenRegister(navigator, mainViewModel) }
        AuthUiStatus.LOGIN -> { AuthScreenLogin(navigator, mainViewModel) }
    }
}

/**
 * TODO
 *
 * @param navigator
 * @param vm
 */
@Composable
fun AuthScreenMain(navigator: DestinationsNavigator, vm: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier,

            ) {
            Text(
                text = "Welcome to",
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Toilets Everywhere!",
                modifier = Modifier.padding(bottom = 30.dp),
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
            Text(
                text = "If you don't have an account",
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 25.sp
                )
            Button(
                onClick = {
                    vm.setAuthStatus(AuthUiStatus.REGISTER)
                }
            ) {
                Text(
                    text = "Register",
                    fontSize = 25.sp

                )
            }
            Text(
                text = "If have an account",
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 25.sp

            )
            Button(
                onClick = {
                    vm.setAuthStatus(AuthUiStatus.LOGIN)
                }
            ) {
                Text(
                    text = "Login",
                    fontSize = 25.sp

                )
            }
            Text(
                text = "If you don't want an account",
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 25.sp

            )
            Button(
                onClick = {
                    vm.continueWithoutAccount()
                }
            ) {
                Text(
                    text = "Continue without account",
                    fontSize = 25.sp

                )
            }
        }

    }
}

/**
 * TODO
 *
 * @param navigator
 * @param vm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenLogin(navigator: DestinationsNavigator, vm: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            Text(
                text = "Login",
                fontSize = 50.sp,
                modifier = Modifier.padding(vertical = 10.dp)

            )

            // Login
            TextField(
                value = vm.authState.loginLogin,
                onValueChange = { vm.authState = vm.authState.copy(loginLogin = it)},
                placeholder = {
                    Text(text = "Login")
                }
            )

            // Password
            TextField(
                value = vm.authState.loginPassword,
                onValueChange = { vm.authState = vm.authState.copy(loginPassword = it)},
                modifier = Modifier.padding(vertical = 10.dp),
                placeholder = {
                    Text(text = "Password")
                }
            )

            Button(
                onClick = {
                    vm.login()
                }
            ) {
                Text(
                    text = "Login",
                    fontSize = 30.sp
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = if (vm.authState.showInvalidPasswordPrompt){
                    "Invalid login or password!"
                } else {""},

                color = Color.Red
            )


            Spacer(modifier = Modifier.height(300.dp))
        }


    }

}

/**
 * TODO
 *
 * @param navigator
 * @param vm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenRegister(navigator: DestinationsNavigator, vm: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "Register",
                fontSize = 40.sp
            )

            // Login
            TextField(
                value = vm.authState.registerLogin,
                onValueChange = { vm.authState = vm.authState.copy(registerLogin = it)},
                modifier = Modifier.padding(vertical = 10.dp),
                placeholder = {
                    Text(text = "Login")
                },
                singleLine = true
            )

            // Password
            TextField(
                value = vm.authState.registerPassword,
                onValueChange = { vm.authState = vm.authState.copy(registerPassword = it)},
                placeholder = {
                    Text(text = "Password")
                },
                singleLine = true
            )

            // Password Confirmation
            TextField(
                value = vm.authState.registerPasswordConfirmation,
                onValueChange = { vm.authState = vm.authState.copy(registerPasswordConfirmation = it)},
                modifier = Modifier.padding(vertical = 10.dp),
                placeholder = {
                    Text(text = "Confirm Password")
                },
                singleLine = true
            )

            // Display Name
            TextField(
                value = vm.authState.registerName,
                onValueChange = { vm.authState = vm.authState.copy(registerName = it)},
                placeholder = {
                    Text(text = "Your Display Name")
                },
                modifier = Modifier.padding(bottom = 10.dp),
                singleLine = true
            )

            // Registration Error Label
            if (vm.authState.registrationError != null){
                Text(
                    text = vm.authState.registrationError!!.errorMessage,
                    color = Color.Red,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Register Button
            Button(
                onClick = {
                    vm.startRegister()
                },
                modifier = Modifier.padding(10.dp),

            ) {
                Text(
                    text = "Register",
                    fontSize = 30.sp,
                )
            }
        }
    }
}