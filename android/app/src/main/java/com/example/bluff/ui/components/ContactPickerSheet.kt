package com.example.bluff.ui.components

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bluff.theme.*
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PhoneContact(
    val name: String,
    val phone: String
)

private suspend fun loadContacts(context: Context): List<PhoneContact> =
    withContext(Dispatchers.IO) {
        val contacts = mutableListOf<PhoneContact>()
        val cr: ContentResolver = context.contentResolver
        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: continue
                val phone = it.getString(phoneIdx) ?: ""
                contacts.add(PhoneContact(name = name, phone = phone.trim()))
            }
        }
        contacts.distinctBy { it.name }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerSheet(
    onContactSelected: (PhoneContact) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf<List<PhoneContact>>(emptyList()) }
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        permissionDenied = !granted
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            contacts = loadContacts(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val filtered = remember(query, contacts) {
        if (query.isBlank()) contacts
        else contacts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.phone.contains(query)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(DividerColor, RoundedCornerShape(50.dp))
            )
        }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
            Text(
                text = "Select Contact",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (permissionDenied) {
                Text(
                    text = "Contacts permission denied. Allow it in Settings to pick contacts.",
                    color = ExpenseColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Manual entry fallback
                var manualName by remember { mutableStateOf("") }
                BluffTextField(
                    value = manualName,
                    onValueChange = { manualName = it },
                    label = "Enter name manually",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                BluffButton(
                    text = "Use This Name",
                    onClick = {
                        if (manualName.isNotBlank()) {
                            onContactSelected(PhoneContact(name = manualName, phone = ""))
                        }
                    }
                )
            } else {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search contacts…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (contacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(filtered) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onContactSelected(contact); onDismiss() }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .then(Modifier.padding(0.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Primary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = contact.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    if (contact.phone.isNotBlank()) {
                                        Text(
                                            text = contact.phone,
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
