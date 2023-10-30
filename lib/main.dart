import 'dart:async';
import 'package:appcheck/appcheck.dart';
import 'package:flutter/material.dart';
import 'package:flutter_donation_buttons/flutter_donation_buttons.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:url_launcher/url_launcher_string.dart';
import 'app_item_list.dart';
import 'network_requester.dart';
import 'ns_app_info.dart';
import 'app_item.dart';

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
  late List<NsAppInfo> appWidgets;

  List<AppItem> appItems = [];

  _MyHomePageState() {
    appItems = AppItemList.GetAppList();
    appWidgets = [];

    for (var app in appItems) {
      appWidgets.add(NsAppInfo(appItem: app, onNeedToReload: triggerReload));
    }
  }

  Future<void> refresh() async {
    var settingReader = RevancedClient();
    var setting = await settingReader.getSetting();

    for (var app in appItems) {
      try {
        var appInfo = await AppCheck.checkAvailability(app.packageName);
        if (appInfo != null) {
          app.currentVersion = appInfo.versionName ?? "";
        }
      } catch (e) {
        print('Something really unknown: $e');
        app.currentVersion = "";
      }

      if (setting?.packages != null) {
        var matchedSettingPackage = setting.packages
            .where((el) =>
                (app.packageTitle != null &&
                    el.packageTitle == app.packageTitle) ||
                (app.packageTitle == null && el.packageName == app.packageName))
            .firstOrNull;
        if (matchedSettingPackage != null) {
          app.latestVersion = matchedSettingPackage.version ?? "";
          app.downloadUrl = matchedSettingPackage.downloadUrl;
        }
      }

      // app.setVersion(appVersion ?? "", latestVersion ?? "", apkUrl);
      app.Widget?.updateInfo();
    }
  }

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
          Column(children: appWidgets),
          SizedBox(height: 20),

          // Text(
          //   "OTHER APPS",
          //   textAlign: TextAlign.center,
          //   style: TextStyle(color: Colors.blue),
          // ),
          // appYoutubeMusicRevanced,
          // appRevancedManager,
          // appTiktokRevanced,
          // appRedditRevanced,
          // appTwitchRevanced,
          // SizedBox(height: 30),
          Center(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                KofiButton(kofiName: 'revancednet', kofiColor: KofiColor.Blue),
              ],
            ),
          ),Center(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton(
                    onPressed: _launchURL,
                    child: Text('Visit revanced.net'),
                    style: ButtonStyle(
                      backgroundColor: MaterialStateProperty.all(Colors.lightBlue),
                    ))
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
        const Duration(seconds: 5), (Timer t) => refresh().then((value) => {}));
  }

  Future<void> triggerReload() async {
    await refresh();
  }

  _launchURL() async {
    const url = 'https://revanced.net'; // Replace with the URL you want to open
    if (await canLaunchUrlString(url)) {
      await launchUrlString(url);
    } else {
      throw 'Could not launch $url';
    }
  }
}
