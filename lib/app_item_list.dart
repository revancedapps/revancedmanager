import 'app_item.dart';

class AppItemList {
  static List<AppItem> appItems = [
    AppItem(
      title: "ReVanced Manager",
      description:
          "ReVanced Manager is a free app from revanced.net that lets you install and manage YouTube ReVanced, MicroG, and other apps.",
      packageName: "com.revanced.net.revancedmanager",
    ),
    AppItem(
        title: "Youtube ReVanced",
        description:
            "YouTube Revanced is a modified version of the official YouTube that blocks ads and allows users to play videos in the background.",
        packageName: "app.revanced.android.youtube"),
    AppItem(
        title: "MicroG",
        description:
            "MicroG is a free, open-source, and privacy-focused alternative to Google Play Services.",
        packageName: "app.revanced.android.gms"),

    AppItem(
        title: "Youtube Music ReVanced",
        description:
            "A free, ad-free, and modded version of YouTube Music with background playback and lyrics.",
        packageName: "app.revanced.android.apps.youtube.music"),

    //anddea
    AppItem(
        title: "Youtube ReVanced Extended - Anddea",
        description:
        "This is the latest and most frequently updated version of YouTube ReVanced Extended, developed by Anddea.",
        packageName: "anddea.youtube"),
    AppItem(
        title: "ReVanced YT Music Extended  - Anddea",
        description:
        "This is the latest and most frequently updated version of YouTube Music ReVanced Extended, developed by Anddea.",
        packageName: "anddea.youtube.music"),





    AppItem(
        title: "ReVanced Lightroom",
        description:
            "Revanced Lightroom is a modded version of the Adobe Lightroom app that unlocks all premium features for free.",
        packageName: "com.adobe.lrmobile"),
    AppItem(
        title: "Reddit ReVanced",
        description:
            "Reddit is a social news website where users submit content and vote on it. The most popular content is displayed to the most people.",
        packageName: "com.reddit.frontpage",
        packageTitle: "revanced_reddit"),

    AppItem(
        title: "TikTok ReVanced",
        description:
            "TikTok ReVanced is a free, ad-free, and modded version of TikTok with additional features such as no watermark, no live stream ads, and more...",
        packageName: "com.ss.android.ugc.trill"),
    AppItem(
        title: "ReVanced Twitch",
        description:
            "ReVanced Twitch is a modified version of the Twitch that offers a number of premium features for free, including ad-free, background playback, higher video quality...",
        packageName: "tv.twitch.android.app"),
    AppItem(
        title: "SmartTube - YouTube for TV",
        description:
            "SmartTube is an open-source, ad-free version of the YouTube app for Android TVs and TV boxes.",
        packageName: "com.teamsmart.videomanager.tv"),
    AppItem(
        title: "ReVanced Tumblr",
        description:
            "ReVanced Tumblr is a modified version of the official Tumblr app that removes ads and other unwanted features.",
        packageName: "com.tumblr"),
    AppItem(
        title: "ReVanced Nyx Music Player",
        description:
            "The Revanced NYX Music Player is a modified version of the NYX Music Player with unlocked pro features.",
        packageName: "com.awedea.nyx"),
    AppItem(
        title: "Spotify",
        description:
            "A modified version of the official Spotify app with amoled black theme...",
        packageName: "com.spotify.music",
        packageTitle: "sp"),




    //OLD APPS

    AppItem(
        title: "MicroG (old)",
        description:
        "MicroG is a free, open-source, and privacy-focused alternative to Google Play Services.",
        packageName: "com.mgoogle.android.gms"),



    //inotia00
    AppItem(
        title: "Youtube ReVanced Extended",
        description:
        "RVX YouTube is a free, ad-free, and modded version of YouTube with background playback, sponsorblock, and more features.",
        packageName: "app.rvx.android.youtube"),
    AppItem(
        title: "ReVanced YT Music Extended",
        description:
        "A modified version of the YouTube Music app that removes ads additional features like background playback, sponsorblock...",
        packageName: "app.rvx.android.apps.youtube.music"),
    AppItem(
        title: "RVX Reddit",
        description:
        "RVX Reddit is an other modified version of the official Reddit app that removes ads, adds features, and improves the overall user experience.",
        packageName: "com.reddit.frontpage",
        packageTitle: "revanced_reddit_ext"),
    AppItem(
        title: "ReVanced Duolingo",
        description:
        "Modded version of the Duolingo language learning app with premium features, such as unlimited hearts, streak freezes, ad removal...",
        packageName: "com.duolingo"),

    //rufusin
    AppItem(
        title: "Youtube ReVanced Extended - Rufusin",
        description:
        "Another version of YouTube ReVanced Extended, developed in collaboration with Rufusin.",
        packageName: "app.rve.android.youtube"),
    AppItem(
        title: "ReVanced YT Music Extended  - Rufusin",
        description:
        "Another version of YouTube Music ReVanced Extended, developed in collaboration with Rufusin.",
        packageName: "com.rve.android.apps.youtube.music"),

  ];

  static List<AppItem> GetAppList() {
    return appItems;
  }
}
