package research.oibc.postech.oibc

import android.net.Uri
import android.util.Log
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.HashMap
import java.net.*

object NetworkUtils {

    private val TAG = NetworkUtils::class.java!!.getSimpleName()
    private val BASE_URL = "https://api.iotp.postech.ac.kr/v2/"

    fun buildUrl(filename: String, params: HashMap<String, String>): URL? {
        var startUri = Uri.parse(BASE_URL + filename).buildUpon()

        for((key, value) in params){
            startUri.appendQueryParameter(key, value)
        }

        val builtUri = startUri.build()

        var url: URL? = null
        try {
            url = URL(builtUri.toString())
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        Log.v(TAG, "Built URI " + url!!)

        return url
    }

    @Throws(IOException::class)
    fun getResponseFromHttpUrl(url: URL?, AccessToken: String): String? {
        val urlConnection = url?.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("Authorization", "Bearer ${AccessToken}")
        try {
            val `in` = urlConnection.inputStream

            val scanner = Scanner(`in`)
            scanner.useDelimiter("\\A")

            val hasInput = scanner.hasNext()
            return if (hasInput) {
                scanner.next()
            } else {
                null
            }
        } finally {
            urlConnection.disconnect()
        }
    }
}
