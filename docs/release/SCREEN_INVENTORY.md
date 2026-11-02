# Final screen inventory

**58 declared route patterns**; production matrix has **59 concrete route/state cases**. Light/Dark and responsive evidence is in BETA_ACCEPTANCE_MATRIX. Rendering is not a blanket functional PASS.

| Screen | Route pattern | Concrete matrix case / other evidence |
|---|---|---|
| Splash | `splash_screen` | Actual MainActivity startup/navigation; separate splash-content test |
| Home | `home_screen` | home |
| Asma | `asma_screen` | asma |
| Prayer | `prayer_screen` | prayer |
| Quran | `quran_list` | quran_list |
| QuranReader | `quran_reader/{surahId}?ayahNumber={ayahNumber}` | quran_reader |
| Azkar | `azkar_screen` | azkar |
| Hadith | `hadith_screen` | hadith |
| Dua | `dua_screen` | duas |
| DuaDetail | `dua_detail/{duaId}` | dua_detail |
| Settings | `settings_screen` | settings |
| Statistics | `statistics_screen` | statistics |
| Reminders | `reminders_screen` | reminders |
| Donations | `donations_screen` | community |
| Profile | `profile_screen` | profile |
| DonationHistory | `donation_history` | history |
| AddDonation | `add_donation_screen/{type}` | add_offer, add_request |
| RequestHelp | `request_help_screen` | request_help |
| IhsanDetails | `ihsan_details/{id}` | offer_detail, missing_offer |
| LegacyDonationDetail | `donation_detail_screen/{id}` | Legacy redirect retained; unit route contract; not an independent product screen |
| ManageListings | `manage_listings/{type}` | manage_offers, manage_requests |
| EditProfile | `edit_profile` | edit_profile |
| Qibla | `qibla_screen` | qibla |
| Onboarding | `onboarding_screen` | onboarding |
| LocationPermission | `location_permission_screen` | location |
| GlobalSearch | `global_search` | search |
| DailyActivities | `daily_activities` | daily |
| Tasbih | `tasbih_screen` | tasbih |
| IhsanPlusDaily | `ihsan_plus_daily` | controlled_daily |
| HaramLive | `haram_live` | live_haram |
| NabawiLive | `nabawi_live` | live_nabawi |
| SanhyaMain | `sanhya_main` | sanhya |
| SanhyaStoryDetail | `sanhya_story_detail/{storyId}` | sanhya_detail |
| SanhyaEpisodes | `sanhya_episodes/{storyId}` | sanhya_episodes |
| SanhyaQuranVerses | `sanhya_quran_verses/{storyId}` | sanhya_verses |
| SanhyaFavorites | `sanhya_favorites` | sanhya_favorites |
| SanhyaWatchLater | `sanhya_watch_later` | sanhya_later |
| SanhyaSearch | `sanhya_search?query={query}` | sanhya_search |
| SanhyaSettings | `sanhya_settings` | sanhya_settings |
| NabiIhsanMain | `nabi_ihsan_main` | nabi |
| NabiIhsanEpisodeDetails | `nabi_ihsan_episode_details/{episodeId}` | nabi_detail |
| NabiIhsanEpisodes | `nabi_ihsan_episodes` | nabi_episodes |
| NabiIhsanRecipes | `nabi_ihsan_recipes` | nabi_recipes |
| NabiIhsanRecipeDetails | `nabi_ihsan_recipe_details/{recipeId}` | nabi_recipe_detail |
| NabiIhsanFavorites | `nabi_ihsan_favorites` | nabi_favorites |
| NabiIhsanWatchLater | `nabi_ihsan_watch_later` | nabi_later |
| NabiIhsanSearch | `nabi_ihsan_search?query={query}` | nabi_search |
| NabiIhsanSettings | `nabi_ihsan_settings` | nabi_settings |
| FahmMain | `fahm_main` | fahm |
| FahmEpisodes | `fahm_episodes` | fahm_episodes |
| FahmEpisodeDetail | `fahm_episode_detail/{episodeId}` | fahm_detail |
| FahmStations | `fahm_stations` | fahm_stations |
| FahmJourney | `fahm_journey` | fahm_journey |
| FahmSearch | `fahm_search?query={query}` | fahm_search |
| FahmSaved | `fahm_saved?tab={tab}` | fahm_saved |
| FahmCompletion | `fahm_completion/{episodeId}` | fahm_completion |
| FahmAbout | `fahm_about` | fahm_about |
| FahmSettings | `fahm_settings` | fahm_settings |

Optional detail targets added to existing roots: `hadithId`, `nameId`, `zikrId`. Default root routes remain valid; global results navigate to selected detail. `add_request` and `request_help` both use the request form; invalid/missing details have explicit states.

Registered/gated surfaces: IhsanPlusDaily is intentionally controlled, with no invented charity backend. PrayerAssist/CharityTrust experimental source components are not registered production destinations.

Dialogs/state surfaces: Quran quick-jump validation/page/juz selector, verse actions/source copy/share, reader selection and pending-license state, whole-page zoom and collapsed player; hadith details;7dua sections/details; Asma details/review disclosure; prayer city/settings and notification permissions; profile edit/auth/source dialog/local deletion confirmation; management edit/status/delete-cancel/confirm; Demo-reset confirmation; image-picker/album draft/keyboard; YouTube playing/error/external fallback; no location/no sensor/offline/missing record/empty search. Their evidence is described individually in the acceptance matrix and button audit.
