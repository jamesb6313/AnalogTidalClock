package com.jb.restsample1

//https://mobiledeveloperblog.com/kickstart-making-rest-calls-with-okhttp-and-kotlin-coroutines-in-android/
//https://github.com/justmobiledev/android-kotlin-rest-okhttp-2/blob/main/app/src/main/java/com/example/kotlinrestokhttp/MainActivity.kt

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.jb.restsample1.LogDump.Companion.writeLogCat
import com.jb.restsample1.TidalCalc
import com.jb.restsample1.model.TidalExtremes
import com.jb.restsample1.model.TidalInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask

const val TAG = "testInfo"
class MainActivity : AppCompatActivity() {

    companion object {
        // API information
        private const val API_URL_SITE = "https://tides.p.rapidapi.com/tides?"
        private const val API_SEARCH =
            "longitude=45.09477" +      // Port Williams longitude = 'longitude=45.09477'
            "&latitude=-64.40658" +     // Port Williams latitude = '&latitude=-64.40658'
            "&interval=180" +           // interval = 180 is tide height every 3 hours
            "&duration=7200"            // duration = 7200 is extremes every 5 days
        private const val COMPLETE_URL = "$API_URL_SITE$API_SEARCH"
    }

    var textViewExt1: TextView? = null
    var textViewExt2: TextView? = null
    var textViewExt3: TextView? = null
    var textViewExt4: TextView? = null
    var textViewStatus: TextView? = null

    var buttonFetch: Button? = null
    var buttonStop: Button? = null

    // Create OkHttp Client
    var client: OkHttpClient = OkHttpClient()
    var jsonStr: String? = null
    private var modelTideInfo : TidalInfo? = null
    var statusText : String? = null

    //Tidal Calc vars
    private var tc : TidalCalc = TidalCalc()

    //ClockFace vars
    private var cf: ClockFace? = null

    private fun getRequest(sUrl: String): String? {
        var result: String? = null

        try {
            // Create URL
            val url = URL(sUrl)

            // Build request
            val request = Request.Builder().url(url)
                .get()
                .addHeader("X-RapidAPI-Host", "tides.p.rapidapi.com")
                .addHeader("X-RapidAPI-Key", "945fbddd63msh0ada0e013dd2149p1b349ajsn40c5e8c41e14")
                //.addHeader("X-RapidAPI-Host", "covid-193.p.rapidapi.com")
                //.addHeader("X-RapidAPI-Host", "aerodatabox.p.rapidapi.com")
                //.addHeader("X-RapidAPI-Host", "livescore6.p.rapidapi.com")
                //.addHeader("Content-Type", "application/json; utf-8")
                //.addHeader("Accept", "application/json")
                .build()

            // Execute request
            val response = client.newCall(request).execute()
            result = response.body?.string()

            if (response.code != 200) {
                Log.i(TAG,"ERROR - response.code = $response")
                modelTideInfo = null
                statusText = "Status: ERROR - response.code = $response"
                result = null
            }
        }
        catch(err:Error) {
            modelTideInfo = null
            Log.e(TAG, "Error when executing get request: "+err.localizedMessage)
            statusText = "Status: Error when executing get request: "+err.localizedMessage
            result = null
        }

        return result
    }

    private fun fetch(sUrl: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = getRequest(sUrl)
            if (result != null) {
                try {
                    Log.i(TAG, "result = $result")
                    //jsonStr = readJSONFile(3)
                    createModel(result)

                    // Get the file Location and name where Json File are get stored
                    val fileName = cacheDir.absolutePath+"/tides/TidalExtremesJson.json"
                    //call write Json method
                    writeJSONtoFile(fileName, result)

                }
                catch(err:Error) {
                    modelTideInfo = null
                    Log.e(TAG,"Error when parsing JSON: "+err.localizedMessage)
                    statusText = "Status: Error when parsing JSON: "+err.localizedMessage
                }
            }
            else {
                modelTideInfo = null
                Log.e(TAG,"Error: Get request returned no response")
                statusText = "Status: Error: Get request returned no response"
            }
        }
    }

    private fun createModel(strToJson: String) {
        val gson = Gson()
        modelTideInfo = gson.fromJson(strToJson, TidalInfo::class.java)
    }

    private fun  readJSONFile(locationNo: Int): String? {
        try {
            var inputStream: InputStream? = null
            //val cacheDir: String
            when (locationNo) {
                0 ->inputStream = assets.open("testJSON.json")
                1 -> {
                    val cacheFn = cacheDir.absolutePath+"/tides/TidalExtremesJson.json"
                    inputStream = BufferedInputStream(FileInputStream(cacheFn))
                }
            }
            val size: Int = inputStream!!.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            return String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun writeJSONtoFile(fn:String, jsonResult: String) {
        //Create list to store the all Tags
        val ext = ArrayList<TidalExtremes>()
        // Add the Extremes to List
        modelTideInfo!!.extremes.forEachIndexed  { idx, ti ->
            //Log.i(TAG, "> Item ${idx}:\n${ti.datetime} - ${ti.timestamp} - ${ti.height} - ${ti.state}")
            val extItem = TidalExtremes(ti.timestamp, ti.datetime, ti.height, ti.state)
            ext.add(extItem)
        }

        //Create a Object of Post
        val tInfo = TidalInfo(modelTideInfo!!.status, modelTideInfo!!.latitude, modelTideInfo!!.longitude, ext)
        //Create a Object of Gson
        val gson = Gson()
        //Convert the Json object to JsonString
        val jsonString:String = gson.toJson(tInfo)
        //Initialize the File Writer and write into file
        val file=File(fn)
        file.writeText(jsonString)

        // file located "/data/user/0/com.jb.restsample1/cache/tides/TidalExtremesJson.json
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isNetworkConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun initalizeData() {
        val dir = File(cacheDir, "tides")
        if (!dir.exists()) {
            dir.mkdir()
        }
        var foundCache = false
        for (f in dir.listFiles()!!) {
            if (f.name == "TidalExtremesJson.json") {
                foundCache = true
                break
            }
        }

        if (!foundCache) {
//            jsonStr = readJSONFile(0) //use asset file - "testJSON.json"
//
//            // Get the file Location and name where Json File are get stored
//            val fileName = cacheDir.absolutePath+"/tides/TidalExtremesJson.json"
//            createModel(jsonStr!!)
//            writeJSONtoFile(fileName, jsonStr!!)

            // do fetch and create new file
                AlertDialog.Builder(this).setTitle("Fetch New Data File")
                    .setMessage("Fetch New Data File")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        // Launch get request
                        fetch(COMPLETE_URL)
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
                //return
            //}
        } else {
            // use cache file - "TidalExtremesJson.json"
            jsonStr = readJSONFile(1)
            createModel(jsonStr!!)
        }
    }

    fun DrawClockHand(rotationAngle: Double) {
        //line(cx,
        // cy,
        // cx + cos(mapValue) * minutesRadius,
        // cy + sin(mapValue) * minutesRadius);
        val xEndPt = cf!!.centerX + Math.cos(rotationAngle).toFloat() * cf!!.clockRadius
        val yEndPt = cf!!.centerY + Math.sin(rotationAngle).toFloat() * cf!!.clockRadius

        cf!!.setEndPoints(xEndPt, yEndPt)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        textViewExt1 = findViewById(R.id.textview_ext1)
        textViewExt2 = findViewById(R.id.textview_ext2)
        textViewExt3 = findViewById(R.id.textview_ext3)
        textViewExt4 = findViewById(R.id.textview_ext4)
        textViewStatus = findViewById(R.id.textview_status)

        buttonFetch = findViewById(R.id.fetchButton)
        buttonFetch = findViewById(R.id.stopButton)


        Log.i(TAG, "\n\nonCreate() - Start of session - Dump Logcat\n")

        cf = findViewById<View>(R.id.clockFace) as ClockFace

        if (!isNetworkConnected()) {
            AlertDialog.Builder(this).setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
            return
        }
        initalizeData()

//        val apiUrlSite = "https://livescore6.p.rapidapi.com/matches/v2/list-live?"
//        val apiUrlInputs = "Category=soccer"
//        val flightAPIUrl = "https://aerodatabox.p.rapidapi.com/flights/%7BsearchBy%7D/KL1395/2020-06-10"
//        val covidAPIUrl = "https://covid-193.p.rapidapi.com/countries"


        buttonFetch?.setOnClickListener(View.OnClickListener {
            // Launch get request
            fetch(COMPLETE_URL)

            //fetch("https://raw.githubusercontent.com/justmobiledev/android-kotlin-rest-1/main/support/data/bloginfo.json"
            //fetch(covidAPIUrl)
            //fetch(flightAPIUrl)
        })

        var task: Timer? = Timer()

        buttonStop?.setOnClickListener(View.OnClickListener {
            // use to stop timer
            if (task != null) {
                task!!.cancel()
                task = null
            }
        })

        doTimerFunction()

        //need val res for {ti.datetime} since = res.getString(R.string.extreme, ti.datetime, ti.status)
        val res: Resources = resources
        if (modelTideInfo != null) {
            task!!.scheduleAtFixedRate(
                timerTask() {
                    // Do Task..
                    doTimerFunction()
                }, 10000, 10000)
        }
    }

    private fun doTimerFunction() {
        tc.setNowUTC()
        cf!!.clearCanvas()
        var mHandLength = cf!!.clockRadius
        var mappingValue = 0.0

        val fIdx = tc.findNextTide(modelTideInfo!!)
        when (fIdx) {
            -1 -> { //not found message
                statusText = "search time > last entry in tides. Missed the last tide today?"

                Log.i(TAG, "fdx = -1")
                // Do fetch
                fetch(COMPLETE_URL)
                /*AlertDialog.Builder(applicationContext).setTitle("No Tidal Data found or the data is out of date")
                    .setMessage("Need internet to retrieve new data")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        fetch(apiUrlSite + apiUrlInputs)
                        Log.i(TAG, "fetch complete")
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()*/
            }
            0 -> {  //this should never happen
                val fndExt = modelTideInfo!!.extremes[fIdx]
                Log.i(TAG, "now UTC Time = ${tc.mNowUtc} & extreme UTC = ${fndExt.datetime}")
                statusText  = tc.timeToTideMsg(fndExt.datetime)
                Log.i(TAG, "fdx = 0, $statusText")
                mappingValue = tc.setClockHand(modelTideInfo!!, fIdx)
                DrawClockHand(mappingValue)
            }
            else -> {
                val fndExt = modelTideInfo!!.extremes[fIdx]
                Log.i(TAG, "now UTC Time = ${tc.mNowUtc} & extreme UTC = ${fndExt.datetime}")
                statusText = tc.timeToTideMsg(fndExt.datetime)
                Log.i(TAG, "fIdx = $fIdx, $statusText")
                mappingValue = tc.setClockHand(modelTideInfo!!, fIdx)
                DrawClockHand(mappingValue)
            }
        }
        textViewStatus?.text = statusText.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            Log.i(TAG, "MainActivity onPause() - Dump Logcat")
            writeLogCat(this@MainActivity)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "MainActivity onPause() error msg " + e.message)
        }
    }

}