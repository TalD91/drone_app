package io.github.crazygoatstudio.mambodroid

/**
 * Created by xaviermarrib on 01/06/2017.
 */

fun rangeAngle(raw: Float): Int {
    var raw = raw

    if (raw < 20 && raw > -20) {
        return 0
    } else {
        if (raw > 70) raw = 70f
        if (raw < -70) raw = -70f
        if (raw > 0) {

            return (raw - 20).toInt() * 2
        } else {
            return (raw + 20).toInt() * 2
        }
    }
}