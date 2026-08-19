# Socially Base Library

This is the **base/core Android library** for **Socially**, a third-party Facebook client for Android. Socially let users restyle the app with interchangeable skins and themes (backgrounds, action bar colors, sliding-menu colors, fonts, and a custom color picker) instead of using Facebook’s default look.

Skin and theme variants of the Socially app depended on this library. The library holds the shared Facebook client: Graph API access, navigation shell, theming engine, and all feature screens.

Package: `com.bluebitapps.fbclientbase`  
Type: Eclipse ADT / Ant Android library project (`android.library=true`)  
Min SDK: 14 · Target SDK: 17

## Architecture

- **`FBClientApplication`** — application singleton. Holds Facebook session state, in-memory caches for feed/notifications/albums/events/groups/messages/friends/check-ins/birthdays/profile/likes/pages, XMPP chat connection, crash reporting (ACRA), and notification alarms.
- **`FBConnection`** — wraps the Facebook Android SDK (`com.facebook.android.Facebook`), OAuth permissions, access token persistence, and session validity.
- **`LaunchActivity` / `MainActivity`** — entry points. Launch chooses the next screen; Main performs Facebook authorize if needed, then hosts the sliding-menu shell.
- **`SectionManager`** — swaps fragments for each app section (news feed, photos, chat, settings, and so on).
- **`BaseThemedActivity` / `BaseSlidingMenuActivity` / `BaseFragment`** — themed activity and fragment bases that apply the selected skin, typeface, text size, and color.

## Functionality

### Theming and skins

The main product feature. Themes are declared in `res/xml/app_themes.xml` and applied through `ThemeFactory`.

- Dozens of built-in skins (Blue, Pink, Jeans, Zebra, Kimono, Purple Rain, and many more), each with background, action-bar color, menu color, and preview icon
- Theme picker (`ThemeSelectionFragment`)
- Custom color picker for action bar, sliding menu, and wallpaper (`ColorPickerActivity` / `ColorPickerFragment`, using Lars Werkman’s color picker)
- Per-theme layout inflation so screens pick up the current skin
- Text appearance: size, color, and typeface (Arial, Roboto, Verdana, Garamond, Gill, BM Solid, Cute)

### Facebook session and Graph API

- Login / logout via the Facebook Android SDK
- Persistent access token and expiry
- Graph API requests through `AsyncFacebookRunner`
- Permission set for feed, photos, events, groups, messages, check-ins, friends, likes, and publish actions

### Navigation shell

- Sliding side menu (`SlidingMenuFragment`) with unread-notification badges
- Search in the menu (users, pages, events, groups via Graph Search)
- Tabbed section switching through `SectionManager`
- Home / back handling and “clear top” themed activities

### News feed and wall

- Fetch, cache, and display news feed and user wall posts
- Pull-to-refresh and load-older pagination
- Post detail (`NewsFeedItemActivity`) with story tags
- Like and unlike posts
- Comment on posts (add and delete)
- Post a status update (`PostStatusUpdateActivity` / `PostStatusUpdateService`)
- Filter noisy story types (likes, event RSVPs, page shares, and similar)

### Photos and albums

- Album list and album contents
- Photo grid and pager
- Pinch-zoom photo viewer (`TouchImageViewActivity`)
- Photo comments
- Camera / gallery picker and upload (`PhotoSelectorActivity`, `UploadPhotoActivity` / `UploadPhotoService`)

### Messages

- Inbox of message threads
- Thread participants and conversation messages
- Fetch via Graph API (`MessagesService`)

### Chat (XMPP)

- Facebook Chat over XMPP (aSmack + `SASLXFacebookPlatformMechanism`)
- Online roster (`ChatRosterFragment`)
- One-to-one chat (`ChatConversationActivity`)

### Notifications

- Poll Facebook notifications on a user-configurable interval (`NotificationAlarm` + `NotificationsService`)
- Restart polling after reboot (`BootBroadcastReceiver`)
- Delivery as a status-bar notification or a themed in-app popup (`NotificationsAlertActivity`)
- Filter by notification type
- Unread count on the sliding-menu item
- Mark as read

### Friends, profiles, and friend requests

- Friends list
- User profile (about, wall, photos)
- Incoming friend requests (accept / decline)

### Events and birthdays

- Events the user is attending, maybe attending, invited to, or declined
- Event profile, wall, photos, and guest lists
- RSVP (join / maybe / decline)
- Friends’ birthdays

### Groups

- Groups list and group profile
- Group members
- Group wall / photos via the shared news-feed and photo screens

### Pages, likes, and subscriptions

- Facebook Page profiles
- Liked pages / objects (`LikesFragment`, `LikesService`)
- Subscriptions (people the user is subscribed to)

### Check-ins and places

- Friends’ recent check-ins
- Nearby places (location)
- Place map
- Place profile
- Post a check-in with location selection

### Settings, about, and ads

- Notification delivery method and poll frequency
- News-feed auto-refresh
- Font size, color, and typeface
- About screen
- AdMob banners (Socially and Socially Pink publisher IDs)
- “Remove ads” / rate-in-market menu items
- Kindle-oriented feature flags (no camera / no location)

### Localization and UI extras

- Strings for English, German, Spanish, French, Italian, Portuguese, and Chinese
- Pull-to-refresh list views (Handmark library, vendored)
- In-app Crouton toasts (vendored)
- Emoticons in text
- Image loading and caching (Universal Image Loader)
- Network-change receiver
- Crash reports (ACRA)

## Project layout

```
src/com/bluebitapps/fbclientbase/   Core client (features listed above)
src/com/bluebitapps/utils/          Helpers (images, network, fonts, AdMob, widgets)
src/com/handmark/pulltorefresh/     Vendored pull-to-refresh
src/com/larswerkman/colorpicker/    Vendored HSV color picker
src/de/neofonie/.../crouton/        Vendored Crouton toasts
res/                                Layouts, drawables, themes, translations
assets/fonts/                       Bundled typefaces
libs/                               JARs (aSmack, UIL, ACRA, support-v13, ViewBadger)
```

External Eclipse library references (not in this repo): SlidingMenu, Facebook Android SDK, Google Play services.

## License

MIT. See [LICENSE](LICENSE).
