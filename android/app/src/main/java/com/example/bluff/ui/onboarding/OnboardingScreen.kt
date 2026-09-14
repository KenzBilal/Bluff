package com.example.bluff.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffTextField

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)
) {
    val name by viewModel.name.collectAsState()
    val phone by viewModel.phone.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to Bluff", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Let's get started", color = TextSecondary)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        BluffTextField(
            value = name,
            onValueChange = viewModel::updateName,
            label = "Your Name",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        BluffTextField(
            value = phone,
            onValueChange = viewModel::updatePhone,
            label = "Phone Number (Optional)",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        BluffButton(
            text = "Continue",
            onClick = {
                viewModel.saveProfile()
                onComplete()
            },
            enabled = name.isNotBlank()
        )
    }
}
