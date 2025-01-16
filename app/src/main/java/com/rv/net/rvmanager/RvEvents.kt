package com.rv.net.rvmanager

data class DownloadEvent(
    val packageName: String,
)
class RefreshEvent


data class UninstallEvent(
    val packageName: String,
)
data class UninstallMainEvent(
    val packageName: String,
)

data class OpenAppEvent(
    val packageName: String,
)

data class UninstallCompletedEvent(
    val packageName: String,
    val isSuccess: Boolean?,
    val msg: String
)



data class DownloadProgressEvent(
    val packageName: String,
    val progress: Int,
    val downloadedBytes: Long,
    val totalBytes: Long
)

data class InstallCompletedEvent(
    val packageName: String,
    val isSuccess: Boolean?,
    val msg: String
)


data class InstallMainEvent(
    val packageName: String,
    val apkFilePath: String
)


data class DownloadCompleteEvent(val packageName: String, val filePath: String)