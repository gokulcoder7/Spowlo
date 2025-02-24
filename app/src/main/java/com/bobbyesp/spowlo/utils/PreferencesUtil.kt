package com.bobbyesp.spowlo.utils

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.bobbyesp.spowlo.App
import com.bobbyesp.spowlo.App.Companion.applicationScope
import com.bobbyesp.spowlo.App.Companion.context
import com.bobbyesp.spowlo.App.Companion.isFDroidBuild
import com.bobbyesp.spowlo.R
import com.bobbyesp.spowlo.database.CookieProfile
import com.bobbyesp.spowlo.ui.theme.DEFAULT_SEED_COLOR
import com.google.android.material.color.DynamicColors
import com.kyant.monet.PaletteStyle
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val SPOTDL = "spotDL_Init"
const val DEBUG = "debug"
const val CONFIGURE = "configure"
const val DARK_THEME_VALUE = "dark_theme_value"
const val AUDIO_FORMAT = "audio_format"
const val AUDIO_QUALITY = "audio_quality"
const val WELCOME_DIALOG = "welcome_dialog"
const val AUDIO_DIRECTORY = "audio_dir"
const val EXTRA_DIRECTORY = "extra_dir"
const val ORIGINAL_AUDIO = "original_audio"
const val SDCARD_DOWNLOAD = "sdcard_download"
const val SDCARD_URI = "sd_card_uri"
const val PLAYLIST = "playlist"
const val LANGUAGE = "language"
const val NOTIFICATION = "notification"
private const val THEME_COLOR = "theme_color"
const val PALETTE_STYLE = "palette_style"
const val CUSTOM_PATH = "custom_path"

const val USE_YT_METADATA = "use_yt_metadata"
const val USE_SPOTIFY_CREDENTIALS = "use_spotify_credentials"
const val DOWNLOAD_LYRICS = "download_lyrics"
const val LYRIC_PROVIDERS = "lyric_providers"
const val AUDIO_PROVIDERS = "audio_providers"
const val SPONSORBLOCK = "sponsorblock"
const val ONLY_VERIFIED_RESULTS = "only_verified_results"
const val SKIP_EXPLICIT = "skip_explicit"
const val GENERATE_LRC = "generate_lrc"
const val SKIP_ALBUM_ART = "skip_album_art"
const val OUTPUT_FORMAT = "output_format"

const val SKIP_INFO_FETCH = "skip_info_fetch"

const val SPOTIFY_CLIENT_ID = "spotify_client_id"
const val SPOTIFY_CLIENT_SECRET = "spotify_client_secret"

const val USE_CACHING = "use_caching"
const val DONT_FILTER_RESULTS = "dont_filter_results"
const val SPLIT_BY_PLAYLIST = "split_by_playlist"
const val SPLIT_BY_MAIN_ARTIST = "split_by_main_artist"
const val THREADS = "threads"

const val SPOTDL_UPDATE = "spotdl_update"
const val TEMPLATE_ID = "template_id"
const val MAX_FILE_SIZE = "max_file_size"
const val COOKIES = "cookies"
const val AUTO_UPDATE = "auto_update"
const val UPDATE_CHANNEL = "update_channel"
const val INCOGNITO_MODE = "incognito_mode"
private const val DYNAMIC_COLOR = "dynamic_color"
const val CELLULAR_DOWNLOAD = "cellular_download"
private const val HIGH_CONTRAST = "high_contrast"
const val FORMAT_SELECTION = "format_selection"

const val SYSTEM_DEFAULT = 0

//UPDATE CHANNELS
const val STABLE = 0
const val PRE_RELEASE = 1

private val StringPreferenceDefaults =
    mapOf(
        EXTRA_DIRECTORY to "",
        SPOTIFY_CLIENT_ID to "",
        SPOTIFY_CLIENT_SECRET to "",
        LYRIC_PROVIDERS to "",
        AUDIO_PROVIDERS to "",
    )

private val BooleanPreferenceDefaults =
    mapOf(
        FORMAT_SELECTION to true,
        CONFIGURE to true,
        CELLULAR_DOWNLOAD to true,
        ORIGINAL_AUDIO to false,
        USE_SPOTIFY_CREDENTIALS to false,
        USE_YT_METADATA to false,
        DOWNLOAD_LYRICS to false,
        AUTO_UPDATE to true,
        USE_CACHING to false,
        DONT_FILTER_RESULTS to false,
        SPOTDL_UPDATE to true,
        SKIP_INFO_FETCH to false,
        NOTIFICATION to false,
    )

private val IntPreferenceDefaults = mapOf(
    TEMPLATE_ID to 0,
    LANGUAGE to SYSTEM_DEFAULT,
    PALETTE_STYLE to 0,
    DARK_THEME_VALUE to DarkThemePreference.FOLLOW_SYSTEM,
    WELCOME_DIALOG to 1,
    AUDIO_FORMAT to 6,
    AUDIO_QUALITY to 17,
    UPDATE_CHANNEL to STABLE,
    THREADS to 1,
)

val palettesMap = mapOf(
    0 to PaletteStyle.TonalSpot,
    1 to PaletteStyle.Spritz,
    2 to PaletteStyle.FruitSalad,
    3 to PaletteStyle.Vibrant,
    4 to PaletteStyle.Spotify,
    5 to PaletteStyle.Monochrome,
)

val lyricProvidersList = listOf("Genius", "Musixmatch", "AZLyrics", "Synced")
val audioProvidersList =
    listOf("YouTube", "YouTube Music", "Soundcloud", "Bandcamp", "Piped")
val outputFormatList =
    listOf("{artist}", "{album}", "{album-artist}", "{genre}", "{year}", "{list-name}")

object PreferencesUtil {
    private val kv = MMKV.defaultMMKV()

    fun String.getInt(default: Int = IntPreferenceDefaults.getOrElse(this) { 0 }): Int =
        kv.decodeInt(this, default)

    fun String.getString(default: String = StringPreferenceDefaults.getOrElse(this) { "" }): String =
        kv.decodeString(this) ?: default

    fun String.getBoolean(default: Boolean = BooleanPreferenceDefaults.getOrElse(this) { false }): Boolean =
        kv.decodeBool(this, default)

    fun String.updateString(newString: String) = kv.encode(this, newString)

    fun String.updateInt(newInt: Int) = kv.encode(this, newInt)

    fun String.updateBoolean(newValue: Boolean) = kv.encode(this, newValue)
    fun updateValue(key: String, b: Boolean) = key.updateBoolean(b)
    fun encodeInt(key: String, int: Int) = key.updateInt(int)
    fun getValue(key: String): Boolean = key.getBoolean()
    fun encodeString(key: String, string: String) = key.updateString(string)
    fun containsKey(key: String) = kv.containsKey(key)

    fun getExtraDirectory(): String = EXTRA_DIRECTORY.getString()

    fun getAudioFormat(): Int = AUDIO_FORMAT.getInt()

    fun getAudioProvider(): List<String> {
        val audioProviderString = AUDIO_PROVIDERS.getString()
        val providersList = audioProviderString?.split(",") ?: emptyList()
        return providersList
    }

    fun getAudioQuality(): Int = AUDIO_QUALITY.getInt()

    fun getLyricProviders(): List<String> {
        val lyricProviderString = LYRIC_PROVIDERS.getString()
        val providersList = lyricProviderString?.split(",") ?: emptyList()
        return providersList
    }

    fun getOutputFormat(): List<String> {
        val outputFormatString = OUTPUT_FORMAT.getString(default = "{artists} - {title}.{output-ext}")
        val outputFormat = outputFormatString.split("/") ?: emptyList()
        return outputFormat
    }

    fun getAudioFormatDesc(audioQualityStr: Int = getAudioFormat()): String {
        return when (audioQualityStr) {
            0 -> "mp3"
            1 -> "flac"
            2 -> "ogg"
            3 -> "opus"
            4 -> "m4a"
            5 -> "wav"
            6 -> context.getString(R.string.not_specified)
            else -> "mp3"
        }
    }

    fun getAudioQualityDesc(audioQualityStr: Int = getAudioQuality()): String {
        return when (audioQualityStr) {
            0 -> context.getString(R.string.not_specified)
            1 -> "8 kbps"
            2 -> "16 kbps"
            3 -> "24 kbps"
            4 -> "32 kbps"
            5 -> "40 kbps"
            6 -> "48 kbps"
            7 -> "64 kbps"
            8 -> "80 kbps"
            9 -> "96 kbps"
            10 -> "112 kbps"
            11 -> "128 kbps"
            12 -> "160 kbps"
            13 -> "192 kbps"
            14 -> "224 kbps"
            15 -> "256 kbps"
            16 -> "320 kbps"
            17 -> context.getString(R.string.not_convert)
            else -> "auto"
        }
    }

    fun isNetworkAvailableForDownload() =
        !App.connectivityManager.isActiveNetworkMetered //CELLULAR_DOWNLOAD.getBoolean() ||

    //check if the phone is connected to a network source (wifi or mobile data)
    fun isNetworkAvailable() = App.connectivityManager.activeNetworkInfo?.isConnected == true

    fun isAutoUpdateEnabled() = AUTO_UPDATE.getBoolean(!isFDroidBuild())


    fun getLanguageConfiguration(languageNumber: Int = kv.decodeInt(LANGUAGE)) =
        languageMap.getOrElse(languageNumber) { "" }


    private fun getLanguageNumberByCode(languageCode: String): Int =
        languageMap.entries.find { it.value == languageCode }?.key ?: SYSTEM_DEFAULT


    fun getLanguageNumber(): Int {
        return if (Build.VERSION.SDK_INT >= 33)
            getLanguageNumberByCode(
                LocaleListCompat.getAdjustedDefault()[0]?.toLanguageTag().toString()
            )
        else LANGUAGE.getInt()
    }

    data class AppSettings(
        val darkTheme: DarkThemePreference = DarkThemePreference(),
        val isDynamicColorEnabled: Boolean = false,
        val seedColor: Int = DEFAULT_SEED_COLOR,
        val paletteStyleIndex: Int = 0
    )

    private val mutableAppSettingsStateFlow = MutableStateFlow(
        AppSettings(
            DarkThemePreference(
                darkThemeValue = kv.decodeInt(
                    DARK_THEME_VALUE,
                    DarkThemePreference.FOLLOW_SYSTEM
                ), isHighContrastModeEnabled = kv.decodeBool(HIGH_CONTRAST, false)
            ),
            isDynamicColorEnabled = kv.decodeBool(
                DYNAMIC_COLOR,
                DynamicColors.isDynamicColorAvailable()
            ),
            seedColor = kv.decodeInt(THEME_COLOR, DEFAULT_SEED_COLOR),
            paletteStyleIndex = kv.decodeInt(PALETTE_STYLE, 0)
        )
    )
    val AppSettingsStateFlow = mutableAppSettingsStateFlow.asStateFlow()

    fun modifyDarkThemePreference(
        darkThemeValue: Int = AppSettingsStateFlow.value.darkTheme.darkThemeValue,
        isHighContrastModeEnabled: Boolean = AppSettingsStateFlow.value.darkTheme.isHighContrastModeEnabled
    ) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(
                    darkTheme = AppSettingsStateFlow.value.darkTheme.copy(
                        darkThemeValue = darkThemeValue,
                        isHighContrastModeEnabled = isHighContrastModeEnabled
                    )
                )
            }
            kv.encode(DARK_THEME_VALUE, darkThemeValue)
            kv.encode(HIGH_CONTRAST, isHighContrastModeEnabled)
        }
    }

    fun modifyThemeSeedColor(colorArgb: Int, paletteStyleIndex: Int) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(seedColor = colorArgb, paletteStyleIndex = paletteStyleIndex)
            }
            kv.encode(THEME_COLOR, colorArgb)
            kv.encode(PALETTE_STYLE, paletteStyleIndex)
        }
    }

    fun switchDynamicColor(enabled: Boolean = !mutableAppSettingsStateFlow.value.isDynamicColorEnabled) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(isDynamicColorEnabled = enabled)
            }
            kv.encode(DYNAMIC_COLOR, enabled)
        }
    }

    //Cookies
    private const val COOKIE_HEADER = "# Netscape HTTP Cookie File\n" +
            "# Auto-generated by Spowlo built-in WebView\n"

    private val cookiesStateFlow: StateFlow<String> =
        DatabaseUtil.getCookiesFlow().distinctUntilChanged().map {
            it.fold(StringBuilder(COOKIE_HEADER)) { acc: StringBuilder, cookieProfile: CookieProfile ->
                acc.append(cookieProfile.content)
            }.toString()
        }.stateIn(applicationScope, started = SharingStarted.Eagerly, COOKIE_HEADER)

    fun getCookies(): String = cookiesStateFlow.value

}

data class DarkThemePreference(
    val darkThemeValue: Int = FOLLOW_SYSTEM,
    val isHighContrastModeEnabled: Boolean = false
) {
    companion object {
        const val FOLLOW_SYSTEM = 1
        const val ON = 2
        const val OFF = 3
    }

    @Composable
    fun isDarkTheme(): Boolean {
        return if (darkThemeValue == FOLLOW_SYSTEM)
            isSystemInDarkTheme()
        else darkThemeValue == ON
    }

    @Composable
    fun getDarkThemeDesc(): String {
        return when (darkThemeValue) {
            FOLLOW_SYSTEM -> stringResource(R.string.follow_system)
            ON -> stringResource(R.string.on)
            else -> stringResource(R.string.off)
        }
    }

}