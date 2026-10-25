# Binaural Player for Android

An Android application for generating binaural beats in real-time.

## About & Copyright

Binaural Player generates binaural beats natively on Android devices using real-time DSP synthesis.

### Authors & Contributors
- **Nicolas George** (<nicolas.george@normalesup.org>) - Original Android port & AudioTrack integration.
- **Jim Peters** - Author of [SBaGen](http://uazu.net/sbagen/), the underlying DSP synthesis engine.
- **Johnson Dcunha** - Developer & Maintainer.
- **Android Studio AI Assistant (Gemini)** - UI & Code modernization contributor.

## License

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; version 2.

The full license text can be read on the [GNU Website](http://www.gnu.org/licenses/gpl-2.0.txt).

## Features & Usage

1. **Presets:** Choose from built-in binaural beat presets.
2. **Sequence Editor:** View and edit `.sbg` sequence text scripts directly.
3. **Tone Generator:** Easily create custom dual-tone binaural frequencies with pink noise control.
4. **Player Control:** Real-time playback controls with pause, resume, and duration tracking.

To import external sequences:
- Transfer `.sbg` or `.txt` sequence files to your device storage or select them via the file picker in the app.

## Technical Details

- **UI Framework:** Modernized with Jetpack Compose & Material 3 design components.
- **DSP Engine:** C-based SBaGen native engine integrated via C++ / NDK CMake bridge.
- **Audio Output:** Real-time low-latency `AudioTrack` output stream.
