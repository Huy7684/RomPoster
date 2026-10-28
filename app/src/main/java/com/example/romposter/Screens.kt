package com.example.romposter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun Field(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PostScreen(profile: Profile) {
    val scope = rememberCoroutineScope()
    var gappsZip by remember { mutableStateOf("") }
    var gappsId by remember { mutableStateOf("") }
    var vanillaZip by remember { mutableStateOf("") }
    var vanillaId by remember { mutableStateOf("") }
    var date by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date()))
    }
    var shots by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var changelog by remember { mutableStateOf("") }
    var bugs by remember { mutableStateOf("") }
    var preview by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    fun input() = PostInput(
        gappsZip, gappsId, vanillaZip, vanillaId, date, shots, notes, changelog, bugs
    )

    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Field("File zip Gapps", gappsZip) { gappsZip = it }
        Field("ID Drive Gapps (tuỳ chọn)", gappsId) { gappsId = it }
        Field("File zip Vanilla", vanillaZip) { vanillaZip = it }
        Field("ID Drive Vanilla (tuỳ chọn)", vanillaId) { vanillaId = it }
        Field("Released", date) { date = it }
        Field("Link Screenshots", shots) { shots = it }
        Field("Notes (để trống = mặc định)", notes) { notes = it }
        Field("Changelogs (dán cả khối)", changelog, Modifier.heightIn(min = 160.dp)) { changelog = it }
        Field("Bugs (để trống = Tell us!)", bugs) { bugs = it }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                preview = runCatching { buildPost(profile, input()) }
                    .getOrElse { "Lỗi: ${it.message}" }
            }) { Text("Preview") }

            Button(onClick = {
                scope.launch {
                    status = "Đang gửi..."
                    status = runCatching {
                        sendMessage(profile.token, profile.chatId, buildPost(profile, input()))
                    }.getOrElse { "Lỗi: ${it.message}" }
                }
            }) { Text("Đăng") }
        }

        if (preview.isNotEmpty()) {
            Text(AnnotatedString.fromHtml(preview.replace("\n", "<br>")))
        }
        if (status.isNotEmpty()) {
            Text(status, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SettingsScreen(profile: Profile, onSave: (Profile) -> Unit) {
    var p by remember { mutableStateOf(profile) }

    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Field("Bot token", p.token) { p = p.copy(token = it) }
        Field("Chat ID (@channel hoặc -100...)", p.chatId) { p = p.copy(chatId = it) }
        Field("Tên máy", p.deviceName) { p = p.copy(deviceName = it) }
        Field("Codename (cách nhau dấu phẩy)", p.codenames) { p = p.copy(codenames = it) }
        Field("SourceForge project", p.sfProject) { p = p.copy(sfProject = it) }
        Field("QPR", p.qpr) { p = p.copy(qpr = it) }
        Field("Link Channel", p.channelUrl) { p = p.copy(channelUrl = it) }
        Field("Link Support", p.supportUrl) { p = p.copy(supportUrl = it) }
        Field("Link Donate", p.donateUrl) { p = p.copy(donateUrl = it) }
        Field("Notes mặc định", p.defaultNotes) { p = p.copy(defaultNotes = it) }
        Field("Credits", p.credits) { p = p.copy(credits = it) }
        Field("By", p.by) { p = p.copy(by = it) }
        Field("Follow", p.follow) { p = p.copy(follow = it) }
        Field("Join", p.join) { p = p.copy(join = it) }
        Button(onClick = { onSave(p) }) { Text("Lưu") }
    }
}
