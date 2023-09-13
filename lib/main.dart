import 'dart:async';

import 'package:appcheck/appcheck.dart';
import 'package:flutter/material.dart';
import 'package:flutter_donation_buttons/flutter_donation_buttons.dart';

import 'network_requester.dart';
import 'ns_app_info.dart';

void main() {
  runApp(const RevancedManagerApp());
}

class RevancedManagerApp extends StatelessWidget {
  const RevancedManagerApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        brightness: Brightness.dark,
        useMaterial3: true,
      ),
      home: MyHomePage(title: 'Revanced Manager - revanced.net'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;
  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  late NsAppInfo appYoutubeRevanced;
  late NsAppInfo appMicroG;
  late NsAppInfo appYoutubeRevancedExt;
  late NsAppInfo appYoutubeMusicRevanced;
  late NsAppInfo appRevancedManager;
  late NsAppInfo appTiktokRevanced;
  late NsAppInfo appRedditRevanced;
  late NsAppInfo appTwitchRevanced;

  late List<NsAppInfo> apps;

  _MyHomePageState() {
    appYoutubeRevanced = NsAppInfo(
        appTile: 'Youtube ReVanced',
        appSubTile:
            'YouTube Revanced is a modified version of the official YouTube that blocks ads and allows users to play videos in the background.',
        appIcon: 'images/youtube.png',
        appPackageName: 'app.revanced.android.youtube',
        onNeedToReload: triggerReload);
    appMicroG = NsAppInfo(
        appTile: 'MicroG',
        appSubTile:
            'MicroG is a free, open-source, and privacy-focused alternative to Google Play Services.',
        appIcon: 'images/microg.png',
        appPackageName: 'com.mgoogle.android.gms',
        onNeedToReload: triggerReload);

    appYoutubeRevancedExt = NsAppInfo(
      appTile: 'Youtube ReVanced Extended',
      appSubTile:
          'A free, ad-free, and modded version of YouTube with background playback, sponsorblock, and more features.',
      appIcon: 'images/youtube_ext.png',
      appPackageName: 'app.rvx.android.youtube',
      onNeedToReload: triggerReload,
    );

    appYoutubeMusicRevanced = NsAppInfo(
        appTile: 'Youtube Music ReVanced',
        appSubTile:
            'A free, ad-free, and modded version of YouTube Music with background playback and lyrics.',
        appIcon: 'images/youtube_music.png',
        appPackageName: 'app.revanced.android.apps.youtube.music',
        onNeedToReload: triggerReload);

    appRevancedManager = NsAppInfo(
        appTile: 'ReVanced Manager',
        appSubTile:
            'ReVanced Manager is a free app from revanced.net that lets you install and manage YouTube ReVanced, MicroG, and other Revanced apps.',
        appIcon: 'images/revanced_manager.png',
        appPackageName: 'com.revanced.net.revancedmanager',
        onNeedToReload: triggerReload);

    appTiktokRevanced = NsAppInfo(
        appTile: 'TikTok ReVanced',
        appSubTile:
            'TikTok ReVanced is a free, ad-free, and modded version of TikTok with additional features such as no watermark, no live stream ads, and more.',
        appIcon: 'images/tiktok.png',
        appPackageName: 'com.ss.android.ugc.trill',
        onNeedToReload: triggerReload);

    appRedditRevanced = NsAppInfo(
        appTile: 'Reddit ReVanced',
        appSubTile:
            'Reddit is a social news website where users submit content and vote on it. The most popular content is displayed to the most people.',
        appIcon: 'images/com.reddit.frontpage.png',
        appPackageName: 'com.reddit.frontpage',
        onNeedToReload: triggerReload);

    appTwitchRevanced = NsAppInfo(
        appTile: 'Twitch ReVanced',
        appSubTile:
            'Twitch is a live streaming platform where users can watch and interact with gamers, musicians, and other creators.',
        appIcon: 'images/tv.twitch.android.app.png',
        appPackageName: 'tv.twitch.android.app',
        onNeedToReload: triggerReload);

    apps = [
      appYoutubeRevanced,
      appMicroG,
      appYoutubeRevancedExt,
      appYoutubeMusicRevanced,
      appRevancedManager,
      appTiktokRevanced,
      appRedditRevanced,
      appTwitchRevanced
    ];
  }

  Future<void> refresh() async {
    // for (var app in apps) {
    //   app.setLoading(true);
    // }

    var settingReader = RevancedClient();
    var setting = await settingReader.getSetting();

    for (var app in apps) {
      String appVersion = "";
      String latestVersion = "";
      String? apkUrl = "";
      try {
        var appInfo = await AppCheck.checkAvailability(app.appPackageName);
        if (appInfo != null) {
          appVersion = appInfo.versionName ?? "";
        }
      } catch (e) {
        print('Something really unknown: $e');
      }

      if (setting?.packages != null) {
        var matchedSettingPackage = setting.packages
            .where((el) => el.packageName == app.appPackageName)
            .firstOrNull;
        if (matchedSettingPackage != null) {
          latestVersion = matchedSettingPackage.version ?? "";
          apkUrl = matchedSettingPackage.downloadUrl;
        }
      }

      app.setVersion(appVersion ?? "", latestVersion ?? "", apkUrl);
    }
  }
/*
  void _incrementCounter() {
    setState(() async {
      var installedApps = await AppCheck.getInstalledApps();
      int totalApps = 0;
      for (var app in installedApps!) {
        totalApps++;
        if (app.packageName?.contains("rvx") == true) {
          showDialog(
              context: context,
              builder: (BuildContext context) => AlertDialog(
                    title: Text('Info'),
                    content: Text(
                        'Total Apps: ${totalApps} \nappName: ${app.appName}\npackageName: ${app.packageName}\nversionName: ${app.versionName}'),
                    actions: <Widget>[
                      TextButton(
                        onPressed: () => Navigator.pop(context, 'OK'),
                        child: const Text('OK'),
                      ),
                    ],
                  ));
        }
      }
    });
  }*/

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: ListView(
        children: [
          SizedBox(height: 20),
          Text(
            "COMMON APPS",
            textAlign: TextAlign.center,
            style: TextStyle(color: Colors.blue),
          ),
          appYoutubeRevancedExt,
          appMicroG,
          appYoutubeRevanced,
          SizedBox(height: 20),
          Text(
            "OTHER APPS",
            textAlign: TextAlign.center,
            style: TextStyle(color: Colors.blue),
          ),
          appYoutubeMusicRevanced,
          appRevancedManager,
          appTiktokRevanced,
          appRedditRevanced,
          appTwitchRevanced,
          SizedBox(height: 30),
          Center(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                KofiButton(kofiName: 'revancednet', kofiColor: KofiColor.Blue
                          )
              ],
            ),
          ),
          SizedBox(height: 100),
          // Text("SOCIALS"),
        ],
      ),
      // floatingActionButton: FloatingActionButton(
      //   onPressed: refresh,
      //   tooltip: 'Increment',
      //   child: const Icon(Icons.refresh),
      // ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Timer? timer;

  @override
  void initState() {
    super.initState();
    refresh().then((value) => {});
    timer = Timer.periodic(
        const Duration(seconds: 2), (Timer t) => refresh().then((value) => {}));
  }

  Future<void> triggerReload() async {
    await refresh();
  }
}
