package research.oibc.postech.oibc

import org.json.JSONObject

object DataJSONUtils {
    fun getDataDataFromJSON(dataJSONstr: String?): Array<SensorData>? {
        var ret: Array<SensorData>? = null

        val dataObjects = JSONObject(dataJSONstr)

        ret = Array<SensorData>(dataObjects.length(), { SensorData("", "", "", "", "", "") })

        for (i in 0 until dataObjects.length()) {
            ret[i].statusCode = dataObjects.getString("statusCode")
            ret[i].message = dataObjects.getString("message")

            val storedata = dataObjects.getJSONObject("data")
            ret[i].time = storedata.getJSONObject("latest").getString("time")
            ret[i].mtime = storedata.getJSONObject("latest").getString("mtime")
            ret[i].ctime = storedata.getJSONObject("latest").getString("ctime")
            ret[i].value = storedata.getJSONObject("latest").getString("value")
        }

        return ret
    }
}