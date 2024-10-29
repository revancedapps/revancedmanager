import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.revanced.net.revancedmanager.RefreshEvent
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus



/**
 * Main composable for the ReVanced Manager app
 * Displays a list of apps and support buttons
 *
 * @param context Android context for system operations
 * @param viewModel ViewModel for managing app state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevancedManagerApp(context: Context, viewModel: AppViewModel = AppViewModel(context)) {
    val appList by viewModel.appList.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Helper function to launch URLs
    fun launchUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    LaunchedEffect(Unit) {
        EventBus.getDefault().post(RefreshEvent())
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        refreshAppVersions()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    "ReVanced Manager by revanced.net",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF3295E3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            items(
                items = appList,
                key = { app -> app.packageName + app.title }
            ) { app ->
                AppInfoCard(app)
            }
            // Support buttons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ko-fi support button
                    Button(
                        onClick = { launchUrl("https://ko-fi.com/revancednet") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4285F4),
                            contentColor = Color.White  // This sets the default color for all content inside button
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Coffee,
                            contentDescription = "Support on Ko-fi",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White  // Explicitly set icon color to white
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Support me on Ko-fi",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White  // Explicitly set text color to white
                        )
                    }

                    // Website button
                    Button(
                        onClick = { launchUrl("https://revanced.net") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3295E3),
                            contentColor = Color.White  // This sets the default color for all content inside button
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = "Visit website",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White  // Explicitly set icon color to white
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Visit revanced.net",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White  // Explicitly set text color to white
                        )
                    }
                }

                // Bottom spacing
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

fun refreshAppVersions() {
    EventBus.getDefault().post(RefreshEvent())
}
