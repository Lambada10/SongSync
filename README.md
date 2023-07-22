# SongSync
A simple Android app to download lyrics (.lrc files) for songs in your music library.

### Features
* Download lyrics for whole music library with a single click
* Download lyrics for individual songs in your music library
* Search for lyrics for songs not in your music library (and download them)

### Screenshots (v2.0.0)
![Screenshot 1](https://github.com/Lambada10/SongSync/raw/master/screenshots/screenshot1.png)
![Screenshot 2](https://github.com/Lambada10/SongSync/raw/master/screenshots/screenshot2.png)
![Screenshot 3](https://github.com/Lambada10/SongSync/raw/master/screenshots/screenshot3.png)
![Screenshot 4](https://github.com/Lambada10/SongSync/raw/master/screenshots/screenshot4.png)
![Screenshot 5](https://github.com/Lambada10/SongSync/raw/master/screenshots/screenshot5.png)
![Screenshot 6](https://github.com/Lambada10/SongSync/raw/master/screenshots/screenshot6.png)

### Installation
You can download the latest version of the app from the [releases page](https://github.com/Lambada10/SongSync/releases).

### Translation
If you would like to help translating this app, you can do so [here](https://translate.nift4.org/engage/songsync/).

### Building
To build this app, you will need to create a file called `local.properties` in the root directory of the project. This file should contain the following lines:
```properties
spotify_client_id=<your spotify client id>
spotify_client_secret=<your spotify client secret>
```
You can get these values by creating a new app on the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard/applications).

### License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](https://github.com/Lambada10/SongSync/blob/master/LICENSE) file for details.

### 3rd Party APIs
This app uses the [Spotify Web API](https://developer.spotify.com/documentation/web-api/) to get song metadata and the [spotify lyrics API](https://github.com/akashrchandran/spotify-lyrics-api) to get lyrics for songs.

### Friend projects
[Symphonica](https://github.com/AkaneTan/Symphonica)
