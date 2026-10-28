package com.example.romposter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class Profile(
    val token: String = "",
    val chatId: String = "",
    val deviceName: String = "Redmi Note 13 / Redmi Note 13 NFC",
    val codenames: String = "sapphire,sapphiren",
    val sfProject: String = "hisiudzromupload",
    val qpr: String = "QPR0",
    val channelUrl: String = "https://t.me/noobbuildxyz",
    val supportUrl: String = "https://t.me/chatnoobbuild",
    val donateUrl: String = "https://t.me/noobbuildxyz/11",
    val defaultNotes: String = "Clean flash is recommended.",
    val credits: String = "@Sarojtaj77 & @Angelpro09_Devv for base device tree.",
    val by: String = "@HiSiuDz",
    val follow: String = "@RedmiNote134GUpdates",
    val join: String = "@RedmiNote134GCommunity"
)

data class ZipInfo(
    val rom: String, val android: String, val buildDate: String, // yyyyMMdd
    val codename: String, val version: String,
    val vanilla: Boolean, val type: String
) {
    // 20261009 -> 9-10-2026 (thư mục SourceForge)
    val sfFolder: String
        get() = "${buildDate.substring(6).toInt()}-${buildDate.substring(4, 6).toInt()}-${buildDate.substring(0, 4)}"
}

private val ZIP_REGEX =
    Regex("""^(.+?)-(\d+)\.\d+-(\d{8})-(\w+)-([\d.]+)-(?:(Vanilla)-)?([A-Za-z]+)\.zip$""")

fun parseZip(name: String): ZipInfo? =
    ZIP_REGEX.matchEntire(name.trim())?.destructured?.let { (rom, a, d, code, ver, van, type) ->
        ZipInfo(rom, a, d, code, ver, van.isNotEmpty(), type)
    }

private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
private fun a(label: String, url: String) = "<a href=\"${esc(url.trim())}\">${esc(label)}</a>"

private fun drive(id: String) = "https://drive.google.com/uc?id=$id&export=download"
private fun sf(p: Profile, z: ZipInfo, file: String) =
    "https://sourceforge.net/projects/${p.sfProject}/files/${z.rom}/${z.sfFolder}/$file/download"

data class PostInput(
    val gappsZip: String, val gappsDriveId: String,
    val vanillaZip: String, val vanillaDriveId: String,
    val releaseDate: String, val screenshotsUrl: String,
    val notes: String, val changelog: String, val bugs: String
)

fun buildPost(p: Profile, i: PostInput): String {
    val g = if (i.gappsZip.isBlank()) null else parseZip(i.gappsZip)
    val v = if (i.vanillaZip.isBlank()) null else parseZip(i.vanillaZip)
    if (i.gappsZip.isNotBlank() && g == null) error("Tên file Gapps không đúng định dạng")
    if (i.vanillaZip.isNotBlank() && v == null) error("Tên file Vanilla không đúng định dạng")
    val z = g ?: v ?: error("Chưa nhập tên file zip")

    val codes = p.codenames.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val tags = listOf(z.rom, "A${z.android}", p.qpr) +
            codes.map { it.replaceFirstChar(Char::uppercase) } + z.type
    val hashtags = tags.joinToString(" ") { "#$it" }

    fun group(name: String, zip: String, info: ZipInfo?, driveId: String): String? {
        if (info == null) return null
        val links = mutableListOf<String>()
        if (driveId.isNotBlank()) links += a("Drive", drive(driveId.trim()))
        links += a("Sourceforge", sf(p, info, zip.trim()))
        return "- $name: ${links.joinToString(" | ")}"
    }

    return buildString {
        appendLine(hashtags)
        appendLine("<b>${esc(z.rom)} ${z.version} | ${z.type.uppercase()} | Android ${z.android} ${p.qpr}</b>")
        appendLine("Released: ${esc(i.releaseDate)}")
        appendLine("Device: ${esc(p.deviceName)} (${codes.joinToString("/")})")
        appendLine()
        appendLine("▪️Download:")
        group("Gapps", i.gappsZip, g, i.gappsDriveId)?.let { appendLine(it) }
        group("Vanilla", i.vanillaZip, v, i.vanillaDriveId)?.let { appendLine(it) }
        if (i.screenshotsUrl.isNotBlank()) appendLine("▪️${a("Screenshots", i.screenshotsUrl)}")
        appendLine("▪️${a("Official Channel", p.channelUrl)}")
        appendLine("▪️${a("Support Group", p.supportUrl)}")
        appendLine("▪️${a("Donate", p.donateUrl)}")
        appendLine()
        appendLine("• Notes:")
        appendLine(esc(i.notes.ifBlank { p.defaultNotes }))
        appendLine()
        appendLine("• Changelogs:")
        appendLine(esc(i.changelog.trim()))
        appendLine()
        appendLine("• Bugs:")
        appendLine(esc(i.bugs.ifBlank { "Tell us!" }))
        appendLine()
        appendLine("• Credits:")
        appendLine(esc(p.credits))
        appendLine()
        appendLine("By: ${esc(p.by)}")
        appendLine("Follow ${esc(p.follow)}")
        appendLine("Join ${esc(p.join)}")
    }.trim()
}

suspend fun sendMessage(token: String, chatId: String, html: String): String =
    withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("chat_id", chatId)
            .put("text", html)
            .put("parse_mode", "HTML")
            .put("link_preview_options", JSONObject().put("is_disabled", true))
            .toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        OkHttpClient().newCall(
            Request.Builder()
                .url("https://api.telegram.org/bot$token/sendMessage")
                .post(body)
                .build()
        ).execute().use { it.body?.string().orEmpty() }
    }
