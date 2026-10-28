package com.example.romposter

import android.content.Context

class ProfileStore(ctx: Context) {
    private val sp = ctx.getSharedPreferences("profile", Context.MODE_PRIVATE)

    fun load(): Profile {
        val d = Profile()
        fun g(k: String, def: String) = sp.getString(k, def) ?: def
        return Profile(
            token = g("token", d.token), chatId = g("chatId", d.chatId),
            deviceName = g("deviceName", d.deviceName), codenames = g("codenames", d.codenames),
            sfProject = g("sfProject", d.sfProject), qpr = g("qpr", d.qpr),
            channelUrl = g("channelUrl", d.channelUrl), supportUrl = g("supportUrl", d.supportUrl),
            donateUrl = g("donateUrl", d.donateUrl), defaultNotes = g("defaultNotes", d.defaultNotes),
            credits = g("credits", d.credits), by = g("by", d.by),
            follow = g("follow", d.follow), join = g("join", d.join)
        )
    }

    fun save(p: Profile) = sp.edit()
        .putString("token", p.token).putString("chatId", p.chatId)
        .putString("deviceName", p.deviceName).putString("codenames", p.codenames)
        .putString("sfProject", p.sfProject).putString("qpr", p.qpr)
        .putString("channelUrl", p.channelUrl).putString("supportUrl", p.supportUrl)
        .putString("donateUrl", p.donateUrl).putString("defaultNotes", p.defaultNotes)
        .putString("credits", p.credits).putString("by", p.by)
        .putString("follow", p.follow).putString("join", p.join)
        .apply()
}
