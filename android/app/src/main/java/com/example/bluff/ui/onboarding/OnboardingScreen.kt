package com.example.bluff.ui.onboarding

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.theme.*
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffTextField

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val phone by viewModel.phone.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(96.dp))

        // App mark — minimal, single colour
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Primary, shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "B",
                color = Background,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Bluff",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Personal finance, simplified",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(64.dp))

        BluffTextField(
            value = name,
            onValueChange = viewModel::updateName,
            label = "Your name",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        BluffTextField(
            value = phone,
            onValueChange = viewModel::updatePhone,
            label = "Phone number (optional)",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        BluffButton(
            text = "Continue",
            onClick = {
                viewModel.saveProfile()
                onComplete()
            },
            enabled = name.isNotBlank()
        )

        Spacer(Modifier.height(20.dp))
    }
}
