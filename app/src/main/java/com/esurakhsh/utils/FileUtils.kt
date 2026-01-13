package com.esurakhsh.utils

import com.esurakhsh.CallLogData
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FileUtils {

    fun convertCallLogsToCsv(calls: List<CallLogData>): String {
        val sb = StringBuilder()
        sb.append("Number,Type,Date,Duration(sec)\n")
        for (call in calls) {
            val typeStr = when (call.type) {
                1 -> "Incoming"
                2 -> "Outgoing"
                3 -> "Missed"
                else -> "Unknown"
            }
            sb.append("${call.number},${typeStr},${call.date},${call.duration}\n")
        }
        return sb.toString()
    }

    fun zipFiles(files: List<File>, zipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { out ->
            for (file in files) {
                if (!file.exists()) continue
                
                FileInputStream(file).use { fi ->
                    BufferedInputStream(fi).use { origin ->
                        val entry = ZipEntry(file.name)
                        out.putNextEntry(entry)
                        origin.copyTo(out)
                    }
                }
            }
        }
    }
}

