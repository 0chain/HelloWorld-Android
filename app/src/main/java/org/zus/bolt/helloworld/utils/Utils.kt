package org.zus.bolt.helloworld.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class Utils(var applicationContext: Context) {
    companion object {

    }

    fun getConfigFromAssets(fileName: String): String? {
        val jsonString: String = try {
            val file: InputStream = applicationContext.getAssets().open(fileName)
            val size = file.available()
            val buffer = ByteArray(size)
            file.read(buffer)
            file.close()
            val json = JSONObject(String(buffer, StandardCharsets.UTF_8))
            json.getJSONObject("config").toString()

        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return jsonString
    }
}

