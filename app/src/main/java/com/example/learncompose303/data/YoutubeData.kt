package com.example.learncompose303.data

import androidx.annotation.DrawableRes
import com.example.learncompose303.R

data class YoutubeData(
    @DrawableRes
    val thumbnail: Int,
    val videoTitle: String,
    val channelName: String
)

fun getYoutubeVideos(): List<YoutubeData> {
    val videos = arrayListOf<YoutubeData>()
    for (i in 0..11) {
        videos.add(YoutubeData(imageList[i], titlesList[i], channelList[i]))
    }
    return videos
}

val imageList = listOf(
    R.drawable.google,
    R.drawable.facebook,
    R.drawable.loc1,
    R.drawable.google,
    R.drawable.facebook,
    R.drawable.loc1,
    R.drawable.google,
    R.drawable.facebook,
    R.drawable.loc1,
    R.drawable.google,
    R.drawable.facebook,
    R.drawable.loc1
)

val titlesList = listOf(
    "Abc",
    "Abc",
    "Abc",
    "Abc",
    "Abc",
    "Abc",
    "Abc",
    "Abc",
    "Abc",
    "Abc",
    "Abc",
    "Abc"
)


val channelList = listOf(
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a",
    "KJKJjsajlslnacknacl ljsabclclaknbclkacs aljcbalkncalnca;lknclkanc oa ojaolaj cja ljc a"
)