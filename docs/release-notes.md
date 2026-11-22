# TwitchPro 1.1.0

First TwitchPro release, based on Xtra TV 1.4.6.4 (Xtra by [crackededed](https://github.com/crackededed/Xtra), Xtra TV by razeN).

**TwitchPro** is a Twitch player and browser made for **Android TV**: built-in ad blocker (Proxy & VAFT), streams up to **1440p60 / 4K** (Enhanced Broadcasting), multi-stream (up to 4 channels), live DVR timeline, TV chat and voice search.

### New in TwitchPro (vs Xtra TV 1.4.6.4)
- **New app identity**: package `com.endlessbum.twitchpro`, app name **TwitchPro** (launcher card stays "Twitch" with the original Twitch banner).
- **In-app updates now work**: the update checker points at this repository — every next release will be offered automatically.
- **Ad-block engine fixes**: correct source resolution ladder, atomic master playlist write, LRU VOD cache, captured engine references.
- **Data-layer hardening**: HTTP status checks (Helix/Auth), null-safety on empty GraphQL responses, OkHttp timeouts, non-blocking progress, `goAsync` receiver fix.
- **UI fixes**: binding null-guards, LruCache seek preview sprites, PiP close on destroy, prefetch check+insert race, bookmark saved after downloads, focus/navigation fixes.
- **Security**: release APK is signed with a dedicated release key (no more shared debug key), explicit `specialUse` foreground-service subtype declarations, update endpoint centralized.

### Install
**Updating TwitchPro → TwitchPro:** install over any previous TwitchPro version — your settings and account are kept.

**Switching from Xtra TV:** TwitchPro installs **next to** Xtra TV as a separate app; settings and login are **not** carried over automatically. In Xtra TV make a backup (TV settings → Backup), then in TwitchPro restore that zip — the restore file picker accepts the old `XtraTV-backup.zip` name. Deleting Xtra TV afterwards does not affect TwitchPro.
