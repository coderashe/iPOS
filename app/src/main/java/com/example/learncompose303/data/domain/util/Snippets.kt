package com.example.learncompose303.data.domain.util

//
//
////                    DefaultScreen()
////                    FacebookScaffold(navController)
////                    SelectImage()
////                    val itemViewModel: ItemViewModel = viewModel()
////                    ItemListScreen(viewModel = itemViewModel)
//
////                    FingerprintAuthenticationScreen()
//
//
//fun isBiometricAuthenticationSupported(context: Context): Boolean {
//    val biometricManager = BiometricManager.from(context)
//    return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
//}
//
//class BiometricAuthentication(activity: FragmentActivity) {
//    private val biometricPrompt: BiometricPrompt by lazy {
//        val executor = ContextCompat.getMainExecutor(activity)
//        BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
//            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                super.onAuthenticationError(errorCode, errString)
//                // Handle error
//            }
//
//            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                super.onAuthenticationSucceeded(result)
//                // Authentication succeeded
//            }
//
//            override fun onAuthenticationFailed() {
//                super.onAuthenticationFailed()
//                // Authentication failed
//            }
//        })
//    }
//
//    fun authenticate(cryptoObject: BiometricPrompt.CryptoObject? = null) {
//        val promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Authentication")
//            .setSubtitle("Scan your fingerprint to proceed")
//            .setNegativeButtonText("Cancel")
//            .build()
//        if (cryptoObject != null) {
//            biometricPrompt.authenticate(promptInfo, cryptoObject)
//        }
//    }
//}
//
//@Composable
//fun FingerprintAuthenticationScreen() {
//    val context = LocalContext.current
//    val activity = context as FragmentActivity
//    val biometricAuthentication = remember { BiometricAuthentication(activity) }
//
//    Column {
//        Text("Fingerprint Authentication")
//        if (isBiometricAuthenticationSupported(context)) {
//            Button(onClick = { biometricAuthentication.authenticate() }) {
//                Text("Authenticate with Fingerprint")
//            }
//        } else {
//            Text("Biometric authentication is not supported or configured on this device.")
//        }
//    }
//}


