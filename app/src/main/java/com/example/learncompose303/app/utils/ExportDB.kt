package com.example.learncompose303.app.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

fun exportDatabase(context: Context, dbName: String) {
    // Get the path to the Room database
    val dbPath = context.getDatabasePath(dbName)
    val currentTime = System.currentTimeMillis()

    // For Android 10+ (Scoped Storage)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$dbName-backup_${currentTime}.db")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/x-sqlite3")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOCUMENTS + "/MyAppBackups"
            )
        }

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        )

        if (uri != null) {
            try {
                val inputStream = FileInputStream(dbPath)
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)

                inputStream.use { input ->
                    outputStream?.use { output ->
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (input.read(buffer).also { length = it } > 0) {
                            output.write(buffer, 0, length)
                        }
                    }
                }

                Toast.makeText(context, "Database exported successfully", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to export database", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Failed to create export file", Toast.LENGTH_SHORT).show()
        }
    } else {
        // For Android 9 and below, use legacy storage methods (if needed)
        val externalStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (externalStorageDir != null) {
            val backupFile = File(externalStorageDir, "$dbName-backup_${currentTime}.db")
            copyFile(FileInputStream(dbPath), FileOutputStream(backupFile))
            Toast.makeText(context, "Database exported successfully", Toast.LENGTH_SHORT).show()
        }
    }
}

@Throws(IOException::class)
fun copyFile(inputStream: FileInputStream, outputStream: FileOutputStream) {
    inputStream.use { input ->
        outputStream.use { output ->
            val buffer = ByteArray(1024)
            var length: Int
            while (input.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
        }
    }
}
