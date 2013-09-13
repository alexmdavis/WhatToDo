A random Google Task picker. Now with extra attitude!

![ScreenShot](https://raw.github.com/alexmdavis/WhatToDo/screenshots/WhatToDo-shot1.png) . ![ScreenShot](https://raw.github.com/alexmdavis/WhatToDo/screenshots/WhatToDo-shot2.png)

(Note to designers: I mean the actual physical gutters on my house.)

Google API access depends on a key in string resource R.id.google_api_key, not included in this repository. (Use an xml file in res/values.)

OAuth (2.0), logging, and some AsyncTask code from Google Task API sample (see quick ref below)

TODO:

- Scaling to other resolutions is no go. Tested only on my phone (320x480 160dpi) portrait. (Android Studio emulations are currently a bit spotty.) Current GridLayout is theoretically mostly sound, but maybe reversion to nested LinearLayouts is required. May need some min/max sizes need to be added. Font sizes are also a problem.
- Lots of null (or other undesired) result checking
- Exception handling
- Unit tests
- Proper class member privacy declarations
- Start up with most recently used task list (shared prefs...)
- Use real ActionBar and action menu for moods, rather than spinner
- Make spinner text bigger (selected value, and value list)
- Better resource practices: strings, colors, widget styles (especially) etc.
- Deal with nested tasks
- Emulation test rig (bypass Google Services authentication)
- Simpler / more current auth scheme? (See https://github.com/antonyt/Task-Master/)
- Add Maven/Gradle and ProGuard
- Random mood should change spinner selection?
- Remove redundancy of 'ordinary' mood strings (currently in both layout xml for init, and string xml for re-selection)
- probabilistic mood string grammars!
- gradient_blue drawable is probably unnecessary
- Apache 2.0 license requires marking code changes. Probably I should just rewrite the reused stuff, most of which will be gone anyway after these TODO's.
- Remove files from project (and ignore files in git) if not necessary

Quick ref:
- API javadoc https://developers.google.com/resources/api-libraries/documentation/tasks/v1/java/latest/
- Android color palette https://developer.android.com/design/style/color.html
- Google Task API sample https://code.google.com/p/google-api-java-client/source/browse/tasks-android-sample/?repo=samples
