import 'app_item.dart';

class AppItemList {
  static List<AppItem> appItems = [
    AppItem(
      title: "ReVanced Manager",
      description:
          "ReVanced Manager is a free app from revanced.net that lets you install and manage YouTube ReVanced, MicroG, and other Revanced apps.",
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
        packageName: "com.mgoogle.android.gms"),
    AppItem(
        title: "Youtube Music ReVanced",
        description:
            "A free, ad-free, and modded version of YouTube Music with background playback and lyrics.",
        packageName: "app.revanced.android.apps.youtube.music"),
    AppItem(
        title: "Youtube ReVanced Extended",
        description:
            "RVX YouTube is a free, ad-free, and modded version of YouTube with background playback, sponsorblock, and more features.",
        packageName: "app.rvx.android.youtube"),
    AppItem(
        title: "ReVanced YT Music Extended",
        description:
            "RVX YT Music is a modified version of the YouTube Music app that removes ads and adds additional features like background playback, sponsorblock and more..",
        packageName: "app.rvx.android.apps.youtube.music"),
    AppItem(
        title: "ReVanced Duolingo",
        description:
            "A modded version of the Duolingo language learning app with premium features for free, such as unlimited hearts, streak freezes, and ad removal.",
        packageName: "com.duolingo"),
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
        title: "RVX Reddit",
        description:
            "RVX Reddit is an other modified version of the official Reddit app that removes ads, adds features, and improves the overall user experience.",
        packageName: "com.reddit.frontpage",
        packageTitle: "revanced_reddit_ext"),
    AppItem(
        title: "TikTok ReVanced",
        description:
            "TikTok ReVanced is a free, ad-free, and modded version of TikTok with additional features such as no watermark, no live stream ads, and more.",
        packageName: "com.ss.android.ugc.trill"),
    AppItem(
        title: "ReVanced Twitch",
        description:
            "ReVanced Twitch is a modified version of the official Twitch app that offers a number of premium features for free, including ad-free viewing, background playback, and higher video quality.",
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
        packageName: "com.tumblr")
  ];

  static List<AppItem> GetAppList() {
    return appItems;
  }
}
