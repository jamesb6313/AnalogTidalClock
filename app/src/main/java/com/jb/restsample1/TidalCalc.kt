package com.jb.restsample1

import android.util.Log
import com.jb.restsample1.model.TidalInfo
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

class TidalCalc {

    var mNowUtc: String? = null

    fun setNowUTC() {
        val mTime = Calendar.getInstance().time
        val outputFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA)
        //outputFmt.timeZone = TimeZone.getTimeZone("UTC")  //can use either ADT or UTC - same result in ADT
        // see: if affected in AST
        //outputFmt.timeZone.dstSavings   //current value 0 on May 29

        val outputStr =  outputFmt.format(mTime)
        mNowUtc = outputStr.substring(0, 10) + "T" + outputStr.substring(11) + "+00:00"
    }

    fun convertUTCTODate(utc: String) : Long? {
        val utcString = utc.substring(0, 10) + " " + utc.substring(11,19)
        val utcDateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA)
        utcDateFmt.timeZone = TimeZone.getTimeZone("UTC")  //can use either ADT or UTC - same result in ADT

        return utcDateFmt.parse(utcString)?.time
    }

    //See:https://stackoverflow.com/questions/44386394/is-there-any-method-in-kotlin-which-allow-me-to-translate-a-value-from-a-range-i
    fun convertMap(number: Float, original: IntRange, target: IntRange): Double {
        val ratio = (number - original.first) / (original.last - original.first)
        return (ratio * (target.last - target.first)).toDouble() + target.first
    }

    fun setClockHandAngle(mExtremes: TidalInfo, foundIdx: Int) : Double{
        val tval2 = mExtremes.extremes[foundIdx].datetime
        val timeVal = mNowUtc
        val earlyVal: String
        val lateVal: String

        // Get Range between Low-High, High-Low Tides in minutes
        if (foundIdx == 0) {
            earlyVal = tval2
            lateVal = mExtremes.extremes[foundIdx + 1].datetime  //nextTidalExtUtc after found
        } else {
            earlyVal = mExtremes.extremes[foundIdx - 1].datetime     // Tidal Ext before found
            lateVal = tval2
        }
/*        Log.i(TAG, "//////////////////////////////////////////////////////////")
        Log.i(TAG, "earlyVal = $earlyVal")
        Log.i(TAG, "lateVal = $lateVal")
        Log.i(TAG, "Approaching - ${mExtremes.extremes[foundIdx].state}")
        Log.i(TAG, "//////////////////////////////////////////////////////////")*/

        val tidalInterval = calcDiff(earlyVal, lateVal)     //minsInHr
        var nowToTideInterval = calcDiff(timeVal, lateVal)  //curMinsInHr - if foundIdx != 0

        if (foundIdx == 0) {
            nowToTideInterval = calcDiff(timeVal, earlyVal)
        }
        var angle: Double

        if (mExtremes.extremes[foundIdx].state == "LOW TIDE") {
            angle = convertMap((tidalInterval - nowToTideInterval).toFloat(), (0..tidalInterval.toInt()), (0..180))
            Log.i(TAG, "angle (# between 0 & 180) = $angle")
            angle = (Math.toRadians(angle) - (Math.PI/2))
        } else {
            angle = convertMap((tidalInterval - nowToTideInterval).toFloat(), (0..tidalInterval.toInt()), (180..360))
            Log.i(TAG, "angle (# between 180 & 360) = $angle")
            angle = (Math.toRadians(angle) - (Math.PI/2))
        }
       /* Log.i(TAG, "minInHr = tidalInterval (High to Low) = $tidalInterval")
        Log.i(TAG, "curMinInHr = nowToTideInterval (now to tide extreme in Minutes) = $nowToTideInterval")
*/
        //line(cx, cy, cx + cos(mapValue) * minutesRadius, cy + sin(mapValue) * minutesRadius);
        return angle
    }

    fun findNextTide(t: TidalInfo): Int {
        val testVal = convertUTCTODate(mNowUtc!!)

        t.extremes.forEachIndexed { idx, te ->
                val checkTime = convertUTCTODate(te.datetime)
                if (checkTime != null) {
                    if (checkTime - testVal!! > 0) {
                        Log.i(TAG, "found index = $idx")
                        return idx
                    }
                }
            }
        return -1
    }

    fun timeToTideMsg(foundExtremeUtc : String): String {
        val t1 = mNowUtc
        val dMin = calcDiff(t1,foundExtremeUtc)

        val dHr = floor(dMin / 60).toInt()
        val d = (dMin - (dHr * 60)).toInt()

        return "Next tidal extreme in $dHr hrs and $d mins"
    }

    fun calcDiff(earlyUtc: String?, lateUtc: String?): Double {
        val eDate = convertUTCTODate(earlyUtc!!)
        val lDate = convertUTCTODate(lateUtc!!)
        val diff = lDate?.minus(eDate!!)

        if (diff != null) {
            if (diff < 0) {
                Log.i(TAG, "time difference < 0")
                return -1.0
            } else {
                val diffDays = floor((diff / 10000 /60 /60 /24).toDouble())
                if (diffDays > 0) {
                    Log.i(TAG, "time difference a day or greater?")
                    return -1.0
                }
                val diffHr = floor((diff /1000 /60 /60).toDouble())
                val diffMin = floor((diff /1000 /60).toDouble())

                val d = floor((diff /1000 /60).toDouble()) - (diffHr * 60)
                Log.i(TAG, "$diffHr Hrs and $d mins")
                return diffMin
            }
        } else {
            Log.i(TAG, "time difference < 0")
            return -1.0
        }
    }

}