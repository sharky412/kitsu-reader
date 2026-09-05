package org.koitharu.kotatsu.core.prefs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins every [AppSettings] preference key to its on-disk string value.
 *
 * These strings are persisted in the user's SharedPreferences. Changing one silently resets that
 * preference for every existing user, and several of them are additionally matched as literals
 * elsewhere in the app:
 *
 *  * [org.koitharu.kotatsu.settings.work.WorkScheduleManager] switches on key constants to decide
 *    whether background workers must be rescheduled.
 *  * `res/xml/pref_*.xml` binds preferences by `android:key`.
 *  * [org.koitharu.kotatsu.settings.search.SettingsSearchHelper] indexes those same keys.
 *
 * A key may be added freely. Renaming or removing one must be a deliberate, migrated change:
 * update this pin in the same commit and add the corresponding preference migration.
 */
class AppSettingsKeysTest {

	@Test
	fun `preference keys keep their persisted values`() {
		for ((constant, expected) in PINNED_KEYS) {
			assertEquals("Preference key changed value", expected, constant)
		}
	}

	@Test
	fun `preference keys are unique`() {
		val seen = HashMap<String, Int>()
		for ((key, _) in PINNED_KEYS) {
			seen[key] = (seen[key] ?: 0) + 1
		}
		val duplicates = seen.filterValues { it > 1 }.keys
		assertTrue("Duplicate preference keys: $duplicates", duplicates.isEmpty())
	}

	private companion object {

		/**
		 * Pairs of `AppSettings.KEY_X to "persisted_value"`. Referencing the constant directly means a
		 * rename breaks compilation; the assertion catches a changed value.
		 */
		val PINNED_KEYS = listOf(
			AppSettings.KEY_ADBLOCK to "adblock",
			AppSettings.KEY_LIST_MODE to "list_mode_2",
			AppSettings.KEY_LIST_MODE_HISTORY to "list_mode_history",
			AppSettings.KEY_LIST_MODE_FAVORITES to "list_mode_favorites",
			AppSettings.KEY_LIST_MODE_SUGGESTIONS to "list_mode_suggestions",
			AppSettings.KEY_THEME to "theme",
			AppSettings.KEY_COLOR_THEME to "color_theme",
			AppSettings.KEY_THEME_AMOLED to "amoled_theme",
			AppSettings.KEY_OFFLINE_DISABLED to "no_offline",
			AppSettings.KEY_PAGES_CACHE_CLEAR to "pages_cache_clear",
			AppSettings.KEY_HTTP_CACHE_CLEAR to "http_cache_clear",
			AppSettings.KEY_COOKIES_CLEAR to "cookies_clear",
			AppSettings.KEY_CHAPTERS_CLEAR to "chapters_clear",
			AppSettings.KEY_CHAPTERS_CLEAR_AUTO to "chapters_clear_auto",
			AppSettings.KEY_THUMBS_CACHE_CLEAR to "thumbs_cache_clear",
			AppSettings.KEY_SEARCH_HISTORY_CLEAR to "search_history_clear",
			AppSettings.KEY_UPDATES_FEED_CLEAR to "updates_feed_clear",
			AppSettings.KEY_GRID_SIZE to "grid_size",
			AppSettings.KEY_GRID_SIZE_PAGES to "grid_size_pages",
			AppSettings.KEY_REMOTE_SOURCES to "remote_sources",
			AppSettings.KEY_LOCAL_STORAGE to "local_storage",
			AppSettings.KEY_READER_DOUBLE_PAGES to "reader_double_pages",
			AppSettings.KEY_READER_DOUBLE_PAGES_SENSITIVITY to "reader_double_pages_sensitivity_2",
			AppSettings.KEY_READER_DOUBLE_FOLDABLE to "reader_double_foldable",
			AppSettings.KEY_READER_ZOOM_BUTTONS to "reader_zoom_buttons",
			AppSettings.KEY_READER_CONTROL_LTR to "reader_taps_ltr",
			AppSettings.KEY_READER_NAVIGATION_INVERTED to "reader_navigation_inverted",
			AppSettings.KEY_READER_FULLSCREEN to "reader_fullscreen",
			AppSettings.KEY_READER_VOLUME_BUTTONS to "reader_volume_buttons",
			AppSettings.KEY_READER_ORIENTATION to "reader_orientation",
			AppSettings.KEY_TRACKER_ENABLED to "tracker_enabled",
			AppSettings.KEY_TRACKER_WIFI_ONLY to "tracker_wifi",
			AppSettings.KEY_TRACKER_FREQUENCY to "tracker_freq",
			AppSettings.KEY_TRACK_SOURCES to "track_sources",
			AppSettings.KEY_TRACK_CATEGORIES to "track_categories",
			AppSettings.KEY_TRACK_WARNING to "track_warning",
			AppSettings.KEY_TRACKER_NOTIFICATIONS to "tracker_notifications",
			AppSettings.KEY_TRACKER_NO_NSFW to "tracker_no_nsfw",
			AppSettings.KEY_TRACKER_DOWNLOAD to "tracker_download",
			AppSettings.KEY_NOTIFICATIONS_SETTINGS to "notifications_settings",
			AppSettings.KEY_NOTIFICATIONS_SOUND to "notifications_sound",
			AppSettings.KEY_NOTIFICATIONS_VIBRATE to "notifications_vibrate",
			AppSettings.KEY_NOTIFICATIONS_LIGHT to "notifications_light",
			AppSettings.KEY_NOTIFICATIONS_INFO to "tracker_notifications_info",
			AppSettings.KEY_READER_ANIMATION to "reader_animation2",
			AppSettings.KEY_READER_CONTROLS to "reader_controls",
			AppSettings.KEY_READER_MODE to "reader_mode",
			AppSettings.KEY_READER_MODE_DETECT to "reader_mode_detect",
			AppSettings.KEY_READER_CROP to "reader_crop",
			AppSettings.KEY_APP_PASSWORD to "app_password",
			AppSettings.KEY_APP_PASSWORD_NUMERIC to "app_password_num",
			AppSettings.KEY_PROTECT_APP to "protect_app",
			AppSettings.KEY_PROTECT_APP_BIOMETRIC to "protect_app_bio",
			AppSettings.KEY_ZOOM_MODE to "zoom_mode",
			AppSettings.KEY_BACKUP to "backup",
			AppSettings.KEY_RESTORE to "restore",
			AppSettings.KEY_BACKUP_PERIODICAL_ENABLED to "backup_periodic",
			AppSettings.KEY_BACKUP_PERIODICAL_FREQUENCY to "backup_periodic_freq",
			AppSettings.KEY_BACKUP_PERIODICAL_TRIM to "backup_periodic_trim",
			AppSettings.KEY_BACKUP_PERIODICAL_COUNT to "backup_periodic_count",
			AppSettings.KEY_BACKUP_PERIODICAL_OUTPUT to "backup_periodic_output",
			AppSettings.KEY_BACKUP_PERIODICAL_LAST to "backup_periodic_last",
			AppSettings.KEY_HISTORY_GROUPING to "history_grouping",
			AppSettings.KEY_UPDATED_GROUPING to "updated_grouping",
			AppSettings.KEY_PROGRESS_INDICATORS to "progress_indicators",
			AppSettings.KEY_REVERSE_CHAPTERS to "reverse_chapters",
			AppSettings.KEY_GRID_VIEW_CHAPTERS to "grid_view_chapters",
			AppSettings.KEY_INCOGNITO_NSFW to "incognito_nsfw",
			AppSettings.KEY_PAGES_NUMBERS to "pages_numbers",
			AppSettings.KEY_SCREENSHOTS_POLICY to "screenshots_policy",
			AppSettings.KEY_PAGES_PRELOAD to "pages_preload",
			AppSettings.KEY_SUGGESTIONS to "suggestions",
			AppSettings.KEY_SUGGESTIONS_WIFI_ONLY to "suggestions_wifi",
			AppSettings.KEY_SUGGESTIONS_EXCLUDE_NSFW to "suggestions_exclude_nsfw",
			AppSettings.KEY_SUGGESTIONS_EXCLUDE_TAGS to "suggestions_exclude_tags",
			AppSettings.KEY_SUGGESTIONS_DISABLED_SOURCES to "suggestions_disabled_sources",
			AppSettings.KEY_SUGGESTIONS_NOTIFICATIONS to "suggestions_notifications",
			AppSettings.KEY_SHIKIMORI to "shikimori",
			AppSettings.KEY_ANILIST to "anilist",
			AppSettings.KEY_MAL to "mal",
			AppSettings.KEY_KITSU to "kitsu",
			AppSettings.KEY_DOWNLOADS_METERED_NETWORK to "downloads_metered_network",
			AppSettings.KEY_DOWNLOADS_FORMAT to "downloads_format",
			AppSettings.KEY_ALL_FAVOURITES_VISIBLE to "all_favourites_visible",
			AppSettings.KEY_DOH to "doh",
			AppSettings.KEY_EXIT_CONFIRM to "exit_confirm",
			AppSettings.KEY_INCOGNITO_MODE to "incognito",
			AppSettings.KEY_READER_MULTITASK to "reader_multitask",
			AppSettings.KEY_SYNC to "sync",
			AppSettings.KEY_SYNC_SETTINGS to "sync_settings",
			AppSettings.KEY_READER_BAR to "reader_bar",
			AppSettings.KEY_READER_BAR_TRANSPARENT to "reader_bar_transparent",
			AppSettings.KEY_READER_CHAPTER_TOAST to "reader_chapter_toast",
			AppSettings.KEY_READER_BACKGROUND to "reader_background",
			AppSettings.KEY_READER_SCREEN_ON to "reader_screen_on",
			AppSettings.KEY_SHORTCUTS to "dynamic_shortcuts",
			AppSettings.KEY_READER_TAP_ACTIONS to "reader_tap_actions",
			AppSettings.KEY_READER_OPTIMIZE to "reader_optimize",
			AppSettings.KEY_LOCAL_LIST_ORDER to "local_order",
			AppSettings.KEY_HISTORY_ORDER to "history_order",
			AppSettings.KEY_FAVORITES_ORDER to "fav_order",
			AppSettings.KEY_WEBTOON_GAPS to "webtoon_gaps",
			AppSettings.KEY_WEBTOON_ZOOM to "webtoon_zoom",
			AppSettings.KEY_WEBTOON_ZOOM_OUT to "webtoon_zoom_out",
			AppSettings.KEY_WEBTOON_PULL_GESTURE to "webtoon_pull_gesture",
			AppSettings.KEY_PREFETCH_CONTENT to "prefetch_content",
			AppSettings.KEY_APP_LOCALE to "app_locale",
			AppSettings.KEY_SOURCES_GRID to "sources_grid",
			AppSettings.KEY_UPDATES_UNSTABLE to "updates_unstable",
			AppSettings.KEY_TIPS_CLOSED to "tips_closed",
			AppSettings.KEY_SSL_BYPASS to "ssl_bypass",
			AppSettings.KEY_READER_AUTOSCROLL_SPEED to "as_speed",
			AppSettings.KEY_READER_AUTOSCROLL_FAB to "as_fab",
			AppSettings.KEY_MIRROR_SWITCHING to "mirror_switching",
			AppSettings.KEY_PROXY to "proxy",
			AppSettings.KEY_PROXY_TYPE to "proxy_type_2",
			AppSettings.KEY_PROXY_ADDRESS to "proxy_address",
			AppSettings.KEY_PROXY_PORT to "proxy_port",
			AppSettings.KEY_PROXY_AUTH to "proxy_auth",
			AppSettings.KEY_PROXY_LOGIN to "proxy_login",
			AppSettings.KEY_PROXY_PASSWORD to "proxy_password",
			AppSettings.KEY_IMAGES_PROXY to "images_proxy_2",
			AppSettings.KEY_LOCAL_MANGA_DIRS to "local_manga_dirs",
			AppSettings.KEY_DISABLE_NSFW to "no_nsfw",
			AppSettings.KEY_RELATED_MANGA to "related_manga",
			AppSettings.KEY_NAV_MAIN to "nav_main",
			AppSettings.KEY_NAV_LABELS to "nav_labels",
			AppSettings.KEY_NAV_PINNED to "nav_pinned",
			AppSettings.KEY_MAIN_FAB to "main_fab",
			AppSettings.KEY_32BIT_COLOR to "enhanced_colors",
			AppSettings.KEY_SOURCES_ORDER to "sources_sort_order",
			AppSettings.KEY_SOURCES_CATALOG to "sources_catalog",
			AppSettings.KEY_CF_BRIGHTNESS to "cf_brightness",
			AppSettings.KEY_CF_CONTRAST to "cf_contrast",
			AppSettings.KEY_CF_INVERTED to "cf_inverted",
			AppSettings.KEY_CF_GRAYSCALE to "cf_grayscale",
			AppSettings.KEY_CF_BOOK to "cf_book",
			AppSettings.KEY_PAGES_TAB to "pages_tab",
			AppSettings.KEY_DETAILS_TAB to "details_tab",
			AppSettings.KEY_DETAILS_LAST_TAB to "details_last_tab",
			AppSettings.KEY_READING_TIME to "reading_time",
			AppSettings.KEY_PAGES_SAVE_DIR to "pages_dir",
			AppSettings.KEY_PAGES_SAVE_ASK to "pages_dir_ask",
			AppSettings.KEY_STATS_ENABLED to "stats_on",
			AppSettings.KEY_FEED_HEADER to "feed_header",
			AppSettings.KEY_SEARCH_SUGGESTION_TYPES to "search_suggest_types",
			AppSettings.KEY_SOURCES_VERSION to "sources_version",
			AppSettings.KEY_SOURCES_ENABLED_ALL to "sources_enabled_all",
			AppSettings.KEY_QUICK_FILTER to "quick_filter",
			AppSettings.KEY_COLLAPSE_DESCRIPTION to "description_collapse",
			AppSettings.KEY_BACKUP_TG_ENABLED to "backup_periodic_tg_enabled",
			AppSettings.KEY_BACKUP_TG_CHAT to "backup_periodic_tg_chat_id",
			AppSettings.KEY_MANGA_LIST_BADGES to "manga_list_badges",
			AppSettings.KEY_TAGS_WARNINGS to "tags_warnings",
			AppSettings.KEY_DISCORD_RPC to "discord_rpc",
			AppSettings.KEY_DISCORD_RPC_SKIP_NSFW to "discord_rpc_skip_nsfw",
			AppSettings.KEY_DISCORD_TOKEN to "discord_token",
			AppSettings.KEY_APP_VERSION to "app_version",
			AppSettings.KEY_IGNORE_DOZE to "ignore_dose",
			AppSettings.KEY_TRACKER_DEBUG to "tracker_debug",
			AppSettings.KEY_LINK_WEBLATE to "about_app_translation",
			AppSettings.KEY_LINK_TELEGRAM to "about_telegram",
			AppSettings.KEY_LINK_GITHUB to "about_github",
			AppSettings.KEY_LINK_MANUAL to "about_help",
			AppSettings.KEY_PROXY_TEST to "proxy_test",
			AppSettings.KEY_OPEN_BROWSER to "open_browser",
			AppSettings.KEY_HANDLE_LINKS to "handle_links",
			AppSettings.KEY_BACKUP_TG to "backup_periodic_tg",
			AppSettings.KEY_BACKUP_TG_OPEN to "backup_periodic_tg_open",
			AppSettings.KEY_BACKUP_TG_TEST to "backup_periodic_tg_test",
			AppSettings.KEY_CLEAR_MANGA_DATA to "manga_data_clear",
			AppSettings.KEY_STORAGE_USAGE to "storage_usage",
			AppSettings.KEY_WEBVIEW_CLEAR to "webview_clear",
			// AppSettings.KEY_IMAGES_PROXY_OLD ("images_proxy") is private and cannot be pinned
			// from here. It is a legacy key read only for migration; if it is ever made public,
			// add it above.
		)
	}
}
