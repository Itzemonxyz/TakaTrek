package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import java.util.concurrent.TimeUnit

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Auth Mode: "EMAIL", "PHONE", "OTP"
    var authMode by remember { mutableStateOf("EMAIL") }
    
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val auth = FirebaseAuth.getInstance()
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    onLoginSuccess()
                } else {
                    errorMessage = authTask.exception?.message
                    isLoading = false
                }
            }
        } catch (e: ApiException) {
            errorMessage = "Google sign in failed: ${e.message}"
            isLoading = false
        }
    }

    val phoneAuthCallback = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        onLoginSuccess()
                    } else {
                        errorMessage = task.exception?.message
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isLoading = false
                errorMessage = e.message
            }

            override fun onCodeSent(
                verId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                isLoading = false
                verificationId = verId
                authMode = "OTP"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TakaTrek",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Welcome to your minimal savings tracker",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        if (authMode == "EMAIL") {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (email.isBlank() || password.isBlank()) return@Button
                        isLoading = true; errorMessage = null
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) onLoginSuccess()
                                else errorMessage = task.exception?.message
                                isLoading = false
                            }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text("Login")
                }
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (email.isBlank() || password.isBlank()) return@OutlinedButton
                        isLoading = true; errorMessage = null
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) onLoginSuccess()
                                else errorMessage = task.exception?.message
                                isLoading = false
                            }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text("Sign Up")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                authMode = "PHONE"; errorMessage = null 
            }) {
                Text("Login with Phone Number instead")
            }
        } 
        else if (authMode == "PHONE") {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (phoneNumber.isBlank()) return@Button
                    isLoading = true; errorMessage = null
                    
                    var formattedPhone = phoneNumber.trim()
                    if (!formattedPhone.startsWith("+")) {
                        if (formattedPhone.startsWith("880")) {
                            formattedPhone = "+$formattedPhone"
                        } else {
                            if (formattedPhone.startsWith("0")) {
                                formattedPhone = formattedPhone.substring(1)
                            }
                            formattedPhone = "+880$formattedPhone"
                        }
                    }

                    val activity = context.findActivity()
                    if (activity == null) {
                        errorMessage = "Cannot find activity context"
                        isLoading = false
                        return@Button
                    }
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(formattedPhone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(phoneAuthCallback)
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Send OTP")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                authMode = "EMAIL"; errorMessage = null 
            }) {
                Text("Back to Email Login")
            }
        } 
        else if (authMode == "OTP") {
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("Enter OTP Code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (otpCode.isBlank() || verificationId == null) return@Button
                    isLoading = true; errorMessage = null
                    val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) onLoginSuccess()
                        else errorMessage = task.exception?.message
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Verify OTP")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                authMode = "PHONE"; errorMessage = null 
            }) {
                Text("Change Phone Number")
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isLoading = true; errorMessage = null
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    // You must configure strings.xml with default_web_client_id
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp),
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Continue with Google")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isLoading = true; errorMessage = null
                auth.signInAnonymously().addOnCompleteListener { task ->
                    if (task.isSuccessful) onLoginSuccess()
                    else errorMessage = task.exception?.message
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text("Continue as Guest")
        }
        
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        }
    }
}
