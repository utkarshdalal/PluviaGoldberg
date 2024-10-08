package com.OxGames.Pluvia.utils

import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.OutputStreamWriter

class FileUtils {
    companion object {
        fun makeDir(dirName: String) {
            val homeItemsDir = File(dirName)
            homeItemsDir.mkdirs()
        }
        fun makeFile(fileName: String, errorTag: String? = "FileUtils", errorMsg: ((Exception) -> String)? = null) {
            try {
                val file = File(fileName)
                if (!file.exists()) file.createNewFile()
            } catch (e: Exception) {
                Log.e(errorTag, errorMsg?.invoke(e) ?: "Error creating file: $e")
            }
        }
        fun createPathIfNotExist(filepath: String) {
            val file = File(filepath)
            var dirs = filepath
            // if the file path is not a directory and if we're not at the root directory then get the parent directory
            if (!filepath.endsWith('/') && filepath.lastIndexOf('/') > 0)
                dirs = file.parent!!
            makeDir(dirs)
        }

        fun readFileAsString(path: String, errorTag: String = "FileUtils", errorMsg: ((Exception) -> String)? = null): String? {
            var fileData: String? = null
            try {
                val r = BufferedReader(FileReader(path))
                val total = StringBuilder()
                var line: String?
                while ((r.readLine().also { line = it }) != null) {
                    total.append(line).append('\n')
                }
                fileData = total.toString()
            } catch (e: Exception) {
                Log.e(errorTag, errorMsg?.invoke(e) ?: "Error reading file: $e")
            }
            return fileData
        }
        fun writeStringToFile(data: String, path: String, errorTag: String? = "FileUtils", errorMsg: ((Exception) -> String)? = null) {
            createPathIfNotExist(path)

            try {
                val fOut = FileOutputStream(path)
                val myOutWriter = OutputStreamWriter(fOut)
                myOutWriter.append(data)
                myOutWriter.close()
                fOut.flush()
                fOut.close()
            } catch (e: Exception) {
                Log.e(errorTag, errorMsg?.invoke(e) ?: "Error writing to file: $e")
            }
        }
    }
}