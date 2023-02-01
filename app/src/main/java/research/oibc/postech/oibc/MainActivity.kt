package research.oibc.postech.oibc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.Toolbar


class MainActivity : AppCompatActivity() {

    private var AccessToken: String? = null


    private var fab_open: Animation? = null
    private var fab_close: Animation? = null
    private var isFabOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val ctl = findViewById<View>(R.id.collapsing_toolbar) as CollapsingToolbarLayout
        ctl.title = "POSWeather"


        getToken().execute()


        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close)

        val clickListener = View.OnClickListener { View ->
            when (View.id) {
                R.id.fab -> anim()
                R.id.fab1 -> {
                    anim()

                    val intent = Intent(this, Activity_info::class.java)
                    startActivity(intent)
                }
                R.id.fab2 -> {
                    anim()

                    val email = Intent(Intent.ACTION_SEND)
                    email.type = "plain/text"
                    val address = arrayOf("wonbin9413@postech.ac.kr")
                    email.putExtra(Intent.EXTRA_EMAIL, address)
                    email.putExtra(Intent.EXTRA_SUBJECT, "[ POSWeather ] 버그 리포트(Bug Report)")
                    email.putExtra(Intent.EXTRA_TEXT, "센서, 프로그램 오류 또는 개선사항을 알려주세요!\n\nPlease inform the sensor, program errors or improvements!")
                    startActivity(email)
                }
            }
        }

        fab.setOnClickListener(clickListener)
        fab1.setOnClickListener(clickListener)
        fab2.setOnClickListener(clickListener)
    }

    fun anim() {
        if (isFabOpen) {
            fab1.startAnimation(fab_close)
            fab2.startAnimation(fab_close)
            fab1.isClickable = false
            fab2.isClickable = false
            isFabOpen = false
        } else {
            fab1.startAnimation(fab_open)
            fab2.startAnimation(fab_open)
            fab1.isClickable = true
            fab2.isClickable = true
            isFabOpen = true
        }
    }


    fun getData() {
        GetDataTask("00137a1000000af3", "00137a1000000af3-temperature/series", 1).execute()
        GetDataTask("00137a1000000af3", "00137a1000000af3-humidity/series", 2).execute()
        GetDataTask("00137a1000000af3", "00137a1000000af3-dust/series", 3).execute()
        GetDataTask("6c96cff1916a", "6c96cff1916a-pressure/series", 4).execute()
        GetDataTask("6c96cff1916a", "6c96cff1916a-radiation/series", 5).execute()
    }

    inner class GetDataTask(gateway: String, sensor: String, type: Int): AsyncTask<Void, String, Array<SensorData>?>(){
        // 여기에 변수 추가해서 아래에서 호출하면 됨!
        var gateway = gateway
        var sensor = sensor
        var type = type

        override fun onPreExecute() {
            // 사전 작업 - 통신하기 전에 필요한 부분이 있으면 여기에 코딩해주세요! ex. 로딩중 아이콘 보여주기 등
        }
        override fun doInBackground(vararg p0: Void?): Array<SensorData>? {
            var map = HashMap<String, String>()
            // 따로 전달하고 싶은 데이터가 있을때 map에 추가하면 됨! map["TEST"] = "test"
            // url 마지막에 www.test.com?TEST=test <- 요렇게 ? 뒤에 부분에 추가됨
            var url = NetworkUtils.buildUrl("gateways/"+gateway+"/sensors/"+sensor, map)

            try {
                var DataListJSONString = NetworkUtils.getResponseFromHttpUrl(url, AccessToken!!)
                var ret = DataJSONUtils.getDataDataFromJSON(DataListJSONString) // JSON 파싱

                return ret
            } catch (e: Exception)
            {
                return null
            }
        }

        override fun onPostExecute(result: Array<SensorData>?) {
            if(result != null) {
                // result[0].value <- 이렇게 하면 받아온 데이터를 값에 접근할 수 있음

                var calendar = Calendar.getInstance()
                var now = calendar.timeInMillis
                var df = java.util.Date(result[0].time.toLong())
                var df2 = java.util.Date(now)
                var min = (df2.time - df.time) / 60000

                if (type == 1) {
                    test1Temperature.setText(result[0].value)
                    test1_time_temperature.text = "" + min + "분 전"
                }
                if (type == 2) {
                    test1Humidity.setText(result[0].value)
                    test1_time_humidity.text = "" + min + "분 전"
                }
                if (type == 3) {
                    test1Dust.setText(result[0].value)
                    test1_time_dust.text = "" + min + "분 전"
                }
                if (type == 4) {
                    test1Pressure.setText(result[0].value)
                    test1_time_pressure.text = "" + min + "분 전"
                }
                if (type == 5) {
                    test1Sun_quantum.setText(result[0].value)
                    test1_time_sun_quantum.text = "" + min + "분 전"
                }
            }
            else {
                Toast.makeText(this@MainActivity, "Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class getToken(): AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {
            var builtUri = Uri.parse("http://141.223.62.66:10001/token").buildUpon().build()

            var url: URL? = null
            try {
                url = URL(builtUri.toString())
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }

            val urlConnection = url?.openConnection() as HttpURLConnection
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

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                AccessToken = result
                getData()
            }
            else {
                Toast.makeText(this@MainActivity, "AccessToken Server Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
