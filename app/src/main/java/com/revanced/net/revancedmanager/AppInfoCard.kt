import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.revanced.net.revancedmanager.DownloadEvent
import org.greenrobot.eventbus.EventBus
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import com.revanced.net.revancedmanager.OpenAppEvent
import com.revanced.net.revancedmanager.UninstallEvent
import java.text.DecimalFormat

@Composable
fun AppInfoCard(app: AppItem) {
    val context = LocalContext.current
    val decimalFormat = DecimalFormat("#0.0")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 3.dp, 8.dp, 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF242329)
        )
    ) {
        Column(
            modifier = Modifier.padding(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(app.logo)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${app.title} icon",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(app.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        app.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        buildAnnotatedString {
                            append("Installed: ")
                            withStyle(style = SpanStyle(color = Color(0xFF3295E3))) {
                                append(app.currentVersion ?: "Not installed")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        buildAnnotatedString {
                            append("Latest: ")
                            withStyle(style = SpanStyle(color = Color(0xFF3295E3))) {
                                append(app.latestVersion)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row {
                    if (app.status == AppItemStatus.UnknownStatus
                        ||app.status == AppItemStatus.UpToDate
                        ||app.status == AppItemStatus.NotInstalled
                        ||app.status == AppItemStatus.UpdateAvailable
                        ) {


                        if (app.status == AppItemStatus.UnknownStatus
                            ||app.status == AppItemStatus.NotInstalled
                        ) {
                            IconButton(onClick = {
                                EventBus.getDefault().post(DownloadEvent(app.packageName))
                            },
                                modifier = Modifier.size(36.dp),)
                            {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = "Install"
                                )
                            }
                        }

                        if (app.status == AppItemStatus.UpdateAvailable
                        ) {
                            IconButton(onClick = {
                                EventBus.getDefault().post(DownloadEvent(app.packageName))
                            },
                                modifier = Modifier.size(36.dp),) {
                                Icon(
                                    imageVector = Icons.Filled.Upload,
                                    contentDescription = "Update",
                                    tint = Color.Yellow
                                )
                            }
                        }

                        if (app.status == AppItemStatus.UpToDate
                            ||app.status == AppItemStatus.UpdateAvailable
                        ) {
                            IconButton(onClick = {
                                EventBus.getDefault().post(UninstallEvent(app.packageName))
                            },
                                modifier = Modifier.size(36.dp),) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Uninstall",
                                    tint = Color.Red
                                )
                            }
                            IconButton(onClick = {
                                EventBus.getDefault().post(OpenAppEvent(app.packageName))
                            },
                                modifier = Modifier.size(36.dp),) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Open app"
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            when (app.status){
                                AppItemStatus.PendingDownload -> {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(8.dp),color = Color.Yellow)
                                    Text(text = "Pending download",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                AppItemStatus.Downloading -> {
                                    LinearProgressIndicator(progress = { app.downloadProgress }, modifier = Modifier.fillMaxWidth().padding(8.dp))
                                    Text(text = "Downloading ${decimalFormat.format(app.downloadProgress * 100)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                AppItemStatus.Installing -> {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(8.dp),color = Color.Yellow)
                                    Text(text = "Installing",style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                AppItemStatus.UnInstalling -> {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(8.dp),color = Color.Red)
                                    Text(text = "UnInstalling",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                else -> {
                                    println("Err: Unknow status ${app.status}")
                                }
                            }
                            /*if (app.downloadProgress > 0f) {
                                LinearProgressIndicator(
                                    progress = { app.downloadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    color = Color.Yellow
                                )
                            }
                            Text(
                                text = "Downloading ${(app.downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                textAlign = TextAlign.Center
                            )*/
                        }
                    }
                }
            }
        }
    }
}