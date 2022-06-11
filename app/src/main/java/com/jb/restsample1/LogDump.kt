package com.jb.restsample1


import android.app.Activity
import android.util.Log
import java.io.*

class LogDump {


    companion object {
        //see http://android-delight.blogspot.com/2016/06/how-to-write-app-logcat-to-sd-card-in.html
/*        private val isExternalStorageReadOnly: Boolean get() {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
        }
        private val isExternalStorageAvailable: Boolean get() {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == extStorageState
        }*/

        fun writeLogCat(mActivity: Activity) {
/*            if (!isExternalStorageAvailable || isExternalStorageReadOnly) {
                throw Exception("Cannot access External Storage (none available or Read Only")
            }*/

            try {
                val process: Process = Runtime.getRuntime().exec("logcat -d -s $TAG:V")
                val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
                var log = "My Dump of Logcat\n"

                bufferedReader.use {
                    it.forEachLine { ln ->
                        log += ln + "\n"
                    }
                }

                Log.i(TAG, "writeLogCat")
                val fileName = "logcat.txt"
                val cacheFile = File(mActivity.cacheDir.absolutePath+"/tides/", fileName)

                if (!cacheFile.exists()) cacheFile.createNewFile()

                val fileOutPutStream = FileOutputStream(cacheFile)
                fileOutPutStream.write(log.toByteArray())
                fileOutPutStream.close()

/*
                val filepath = "MyFileStorage"

                val fileName = "logcat.txt"
                val sdCardFile = File(mActivity.getExternalFilesDir(filepath), fileName)

                if (!sdCardFile.exists()) sdCardFile.createNewFile()


                val fileOutPutStream = FileOutputStream(sdCardFile)
                fileOutPutStream.write(log.toByteArray())
                fileOutPutStream.close()
*/
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }


        }
    }
}