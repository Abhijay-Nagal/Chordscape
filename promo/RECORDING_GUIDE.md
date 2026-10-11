# Recording guide - Keyscape promo video

You record short clips of the app on your **phone** and your **tablet**. They are shown inside device frames in
the video, so viewers see Keyscape running on real hardware. Everything else (titles, music, captions) is added later.

## Before you record (both devices)
1. Install the latest build: `dist/Keyscape-1.4.1-android.apk`.
2. Turn on **Do Not Disturb** (no notifications pop up during a clip).
3. Charge the battery or plug in; set **brightness to maximum**.
4. Hold the device in **landscape**, the way Keyscape is used.
5. Make the app look "lived in": open a few songs, finish 2-3 lessons, add 3-4 favourites (hearts), so the screens
   show progress bars and stars instead of empty states.
6. Turn the sound off for the recording (the video gets its own clean piano soundtrack).

## How to record
**Easiest - Android's built-in recorder:** swipe down twice -> *Screen record* (add the tile if missing) ->
*Record audio: off*, *Show touches on screen: off* -> Start. Stop from the notification shade.

**Highest quality (optional) - over USB from this computer:**
- Turn on *Developer options -> USB debugging*, connect the cable, then in a terminal:
  `C:\Users\HP\AppData\Local\Android\Sdk\platform-tools\adb.exe shell screenrecord --bit-rate 20000000 /sdcard/clip1.mp4`
  (stops after 3 minutes or with Ctrl+C), then
  `...\adb.exe pull /sdcard/clip1.mp4 promo\recordings\phone\`

## Recording tips
- Each clip **6-12 seconds**. Start 1 second before the action, and **hold still 2 seconds at the end**.
- Move **slowly and deliberately**: one tap, wait, next tap. Fast swipes look nervous on video.
- Scroll with long, smooth swipes.
- If something goes wrong, just record that clip again - short clips are easy to redo.

## The clips (record on both devices where marked)
| # | Clip | Device | What to do |
|---|---|---|---|
| 1 | Song player - Waterfall | phone + tablet | Songs -> Für Elise -> Hard / Waterfall / Listen -> START; let it play 10 s |
| 2 | Song player - hand button | tablet | During clip 1 (or a new take): tap the hand button: Both -> Right -> Left |
| 3 | Song player - Fallen Note | phone | Any famous song, Fallen Note guide mode, 8 s of playback |
| 4 | Song library | phone | Open Songs, scroll slowly down through the list, tap a style chip (e.g. Romantic) |
| 5 | Song setup | tablet | Open a song; tap Medium, Sheet Music, Note Wait (slowly), then START |
| 6 | Lesson | tablet | Learn -> Piano Foundations -> any lesson; page through 2-3 steps, tap the keys |
| 7 | Chord Explorer | phone | More -> Chord Explorer; tap a few roots and chord types, press Play chord |
| 8 | Ear Trainer | tablet | More -> Ear Trainer; answer 2 questions (one right is enough) |
| 9 | Storybook | tablet | Home -> Composers -> Beethoven -> Storybook; turn 3 pages slowly |
| 10 | MIDI (if you have a keyboard) | tablet | Settings -> MIDI keyboard with the keyboard connected; play a few keys (they light up) |

## Where to put the files
Copy the recordings (MP4) into:
- `promo/recordings/phone/`
- `promo/recordings/tablet/`

Name them by clip number, e.g. `01-song-player.mp4`, `04-song-library.mp4`. Then tell Claude they are there.
