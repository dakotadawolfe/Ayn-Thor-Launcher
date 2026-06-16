package com.launcher.aynthords.data.local

import com.launcher.aynthords.domain.model.Game

object FakeGameLibrary {
    val games = listOf(
        Game(
            id = "sotn",
            title = "Castlevania: Symphony of the Night",
            heroArtUrl = "https://www.mobygames.com/images/covers/l/21173-castlevania-symphony-of-the-night-playstation-front-cover.jpg",
            backgroundArtUrl = "https://www.mobygames.com/images/covers/l/21173-castlevania-symphony-of-the-night-playstation-front-cover.jpg",
            filePath = "/storage/emulated/0/ROMs/psx/Castlevania - Symphony of the Night.cue",
            platformId = "psx",
            playerId = "psx.com.retroarch.aarch64"
        ),
        Game(
            id = "re4",
            title = "Resident Evil 4",
            heroArtUrl = "https://www.mobygames.com/images/covers/l/9698-resident-evil-4-gamecube-front-cover.jpg",
            backgroundArtUrl = "https://www.mobygames.com/images/covers/l/9698-resident-evil-4-gamecube-front-cover.jpg"
        ),
        Game(
            id = "botw",
            title = "The Legend of Zelda: Breath of the Wild",
            heroArtUrl = "https://www.mobygames.com/images/covers/l/48841-the-legend-of-zelda-breath-of-the-wild-wii-u-front-cover.jpg",
            backgroundArtUrl = "https://www.mobygames.com/images/covers/l/48841-the-legend-of-zelda-breath-of-the-wild-wii-u-front-cover.jpg"
        )
    )
}
