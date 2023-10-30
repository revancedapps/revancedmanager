import 'dart:convert';
import 'dart:io';

class RevancedClient {
  Future<SettingObject> getSetting() async {
    final client = HttpClient();
    var request =
        await client.getUrl(Uri.parse('https://revanced.net/app-info.json'));
    var response = await request.close();
    final string = await response.transform(utf8.decoder).join();
    var decoded = jsonDecode(string);

    var settingObjectFromJson = SettingObject.fromJson(decoded);

    return settingObjectFromJson;
  }
/*
  Future<File> _downloadFile(String url, String filename) async {
    var httpClient = new HttpClient();
    try{
      final client = HttpClient();
      var request = await client.getUrl(Uri.parse(url));
      var response = await request.close();

      final bytes = await response.transform(utf8.decoder);





      var request = await httpClient.getUrl(Uri.parse(url));
      var response = await request.close();
      var bytes = await consolidateHttpClientResponseBytes(response);
      final dir = await getTemporaryDirectory();//(await getApplicationDocumentsDirectory()).path;
      File file = new File('${dir.path}/$filename');
      await file.writeAsBytes(bytes);
      print('downloaded file path = ${file.path}');
      return file;
    }catch(error){
      print('pdf downloading error = $error');
      return File('');
    }*/
}

class SettingObject {
  List<Package> packages;

  SettingObject({
    required this.packages,
  });

  factory SettingObject.fromJson(Map<String, dynamic> json) => SettingObject(
        packages: List<Package>.from(
            json["packages"].map((x) => Package.fromJson(x))),
      );

  Map<String, dynamic> toJson() => {
        "packages": List<dynamic>.from(packages.map((x) => x.toJson())),
      };
}

class Package {
  String? packageTitle;
  String? packageName;
  String? version;
  String? downloadUrl;

  Package({
    required this.packageTitle,
    required this.packageName,
    required this.version,
    required this.downloadUrl,
  });

  factory Package.fromJson(Map<String, dynamic> json) => Package(
        packageTitle: json["packageTitle"],
        packageName: json["packageName"],
        version: json["version"],
        downloadUrl: json["downloadUrl"],
      );

  Map<String, dynamic> toJson() => {
        "packageTitle": packageTitle,
        "packageName": packageName,
        "version": version,
        "downloadUrl": downloadUrl,
      };
}
