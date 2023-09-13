import 'dart:io';

import 'package:app_installer/app_installer.dart';
import 'package:external_app_launcher/external_app_launcher.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart' as syspaths;

class NsAppInfo extends StatefulWidget {
  final String appTile;
  final String appSubTile;
  final String appIcon;
  final String appPackageName;
  late String? appApkUrl = "";

  final Function() onNeedToReload;

  NsAppInfoState? state;

  NsAppInfo(
      {super.key,
      required this.appTile,
      required this.appSubTile,
      required this.appIcon,
      required this.appPackageName,
      required this.onNeedToReload}) {}

  @override
  State<StatefulWidget> createState() {
    state = NsAppInfoState();
    return state!;
  }

  void setLoading(bool loadingStatus) {
    state?.setState(() {
      state!.isLoading = loadingStatus;
    });
  }

  void setVersion(String currentVersion, String latestVersion, String? apkUrl) {
    this.appApkUrl = apkUrl;
    if (state?.mounted == true) {
      state?.setState(() {
        state!._currentVersion = currentVersion;
        state!._latestVersion = latestVersion;
        state!.isLoading = false;

        if (state!.isInstalling == true &&
            currentVersion == latestVersion &&
            currentVersion.isNotEmpty) state!.isInstalling = false;

        // if (state!.isInstalling || state!.isUnInstalling) {
        //   print("tempStatusCount+1");
        //   state!.tempStatusCount += 1;
        //   if (state!.tempStatusCount > 2) {
        //     state!.isInstalling = false;
        //     state!.isUnInstalling = false;
        //     state!.tempStatusCount = 0;
        //   }
        // } else {
        //   state!.tempStatusCount = 0;
        // }
      });
    }
  }
}

class NsAppInfoState extends State<NsAppInfo> {
  String _currentVersion = "";
  String _latestVersion = "";
  bool isLoading = false;
  bool isDownloading = false;

  bool isInstalling = false;
  // bool isUnInstalling = false;

  double downloadPercent = 0.0;

  // int tempStatusCount = 0;
  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.all(3.0),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          ListTile(
            leading: Image.asset(widget.appIcon, width: 64, height: 64),
            title: Text(widget.appTile),
            subtitle: Text(widget.appSubTile,
                style: TextStyle(
                  fontSize: 11.0,
                  color: Colors.grey,
                )),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: <Widget>[
              if (isLoading)
                Container(
                    width: 25,
                    height: 25,
                    margin: EdgeInsets.only(left: 40),
                    child: CircularProgressIndicator())
              else
                Container(
                    margin: EdgeInsets.only(left: 15.0, bottom: 5.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        RichText(
                          text: TextSpan(
                            text: 'Installed: ',
                            style:
                                TextStyle(fontSize: 12.0, color: Colors.grey),
                            children: <TextSpan>[
                              TextSpan(
                                  text: (_currentVersion?.isNotEmpty == true)
                                      ? _currentVersion
                                      : "Not installed",
                                  style: TextStyle(
                                      fontWeight: FontWeight.bold,
                                      color: Colors.blue)),
                            ],
                          ),
                        ),
                        RichText(
                          text: TextSpan(
                            text: 'Latest: ',
                            style:
                                TextStyle(fontSize: 12.0, color: Colors.grey),
                            children: <TextSpan>[
                              TextSpan(
                                  text: (_latestVersion?.isNotEmpty == true)
                                      ? _latestVersion
                                      : "N/A",
                                  style: TextStyle(
                                      fontWeight: FontWeight.bold,
                                      color: Colors.blue)),
                            ],
                          ),
                        )
                      ],
                    )),
              Spacer(),
              if (_currentVersion?.isNotEmpty != true &&
                  widget.appApkUrl?.isNotEmpty == true &&
                  !isDownloading &&
                  // !isUnInstalling &&
                  !isInstalling)
                TextButton(
                  child: const Text('Install'),
                  onPressed: DownloadAndInstallApk,
                ),
              if (_currentVersion?.isNotEmpty == true &&
                  _latestVersion?.isNotEmpty == true &&
                  _currentVersion != _latestVersion &&
                  !isDownloading &&
                  !isInstalling)
                TextButton(
                  child: const Text('Update'),
                  onPressed: DownloadAndInstallApk,
                ),
              if (_currentVersion?.isNotEmpty == true &&
                  !widget.appPackageName.contains('revancedmanager') &&
                  !isDownloading &&
                  // !isUnInstalling &&
                  !isInstalling)
                Container(
                  margin: EdgeInsets.only(left: 0.0),
                  child: IconButton(
                      icon: Icon(
                        Icons.delete,
                        color: Colors.red,
                      ),
                      onPressed: () async {
                        // isUnInstalling = true;
                        const platform = MethodChannel('com.flutter.uninstall');
                        String package = widget.appPackageName;

                        var uninstallResult =
                            await platform.invokeMethod("Uninstall", {
                          "package": package,
                        });
                        print(uninstallResult);
                        widget.onNeedToReload();
                      }),
                ),
              if (_currentVersion?.isNotEmpty == true &&
                  !widget.appPackageName.contains('gms') &&
                  !isDownloading &&
                  // !isUnInstalling &&
                  !isInstalling)
                Container(
                  margin: EdgeInsets.only(left: 0.0),
                  child: IconButton(
                      icon: Icon(
                        Icons.open_in_new,
                        color: Colors.blue,
                      ),
                      onPressed: () async {
                        await LaunchApp.openApp(
                            androidPackageName: widget.appPackageName);
                      }),
                ),
              if (isDownloading)
                TextButton(
                    onPressed: () {},
                    child: Text(
                        "Downloading ${downloadPercent.toStringAsFixed(1)}%")),
              if (isInstalling)
                TextButton(onPressed: () {}, child: Text("Installing...")),
              // if (isUnInstalling)
              //   TextButton(onPressed: () {}, child: Text("Removing...")),
              SizedBox(width: 8),
            ],
          ),
        ],
      ),
    );
  }

  Future<bool> doesFileExist(String filePath) async {
    File file = File(filePath);
    return await file.exists();
  }
  void showYesNoDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('Confirmation'),
          content: Text('The file has been downloaded previously. Do you want to install it instead of downloading a new file?'),
          actions: <Widget>[
            // No button
            TextButton(
              child: Text('No'),
              onPressed: () {
                // Close the dialog and perform any additional actions
                Navigator.of(context).pop(false);
              },
            ),
            // Yes button
            TextButton(
              child: Text('Yes, install it!'),
              onPressed: () {
                // Close the dialog and perform any additional actions
                Navigator.of(context).pop(true);
              },
            ),
          ],
        );
      },
    );
  }

  void DownloadAndInstallApk() async {
    var tempDir = await syspaths.getTemporaryDirectory();
    var apkPath =
        "${tempDir.path}/${widget.appPackageName}.${_latestVersion}.apk";
    print(apkPath);

    bool fileExists = await doesFileExist(apkPath);
    bool needToDownload = true;
    if (fileExists) {
      print('File exists');

      needToDownload = false;

     /* showYesNoDialog(xxx)..then((result) {
        if (result != null && result) {
          // User selected Yes, perform desired action
          needToDownload = false;
        } else {
          // User selected No or closed the dialog, handle accordingly
        }
      });*/
    }

    isDownloading = true;
    isInstalling = false;
    final dio = Dio();
    if (needToDownload) {
      var response = await dio.download(
        widget.appApkUrl!,
        apkPath,
        onReceiveProgress: (count, total) {
          setState(() {
            downloadPercent = 100.0 * count / total;
          });
          print('Downloading ${(100.0 * count / total).toStringAsFixed(1)}%');
        },
      );
    }

    print('Download completed');

    print(apkPath);
    isDownloading = false;
    isInstalling = true;
    // File file = File(apkPath);
    // var raf = file.openSync(mode: FileMode.write);
    // // response.data is List<int> type
    // raf.writeFromSync(response.data);
    // await raf.close();

    AppInstaller.installApk(apkPath);
    widget.onNeedToReload();
  }
}
