package com.capstone.nik.mixology.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.capstone.nik.mixology.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Writes the account's exported [json] to the cache and opens the share sheet with it. */
suspend fun shareAccountExport(context: Context, json: String) {
    val file = withContext(Dispatchers.IO) {
        File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
            .resolve(EXPORT_FILE)
            .also { it.writeText(json) }
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.photos", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType("application/json")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(
        Intent.createChooser(send, context.getString(R.string.settings_export_share_title)),
    )
}

/** Matches the `exports` entry in `res/xml/file_paths.xml`. */
private const val EXPORT_DIR = "exports"
private const val EXPORT_FILE = "mixology-data.json"
