import 'package:Revanced_Manager_by_revanced.net/ns_app_info.dart';

class AppItem {
  String title;
  String description;
  String packageName;
  String? currentVersion;
  String? latestVersion;
  String? packageTitle;
  String? downloadUrl;

  NsAppInfo? Widget;

  AppItem({
    required this.title,
    required this.description,
    required this.packageName,
    this.packageTitle,
  });
}


