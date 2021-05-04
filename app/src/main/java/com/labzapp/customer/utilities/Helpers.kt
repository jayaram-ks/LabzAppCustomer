package com.labzapp.customer.utilities

import android.content.Context
import android.view.Gravity
import android.widget.Toast

fun toastz(cnxt: Context,messg: String, )
{
    val tst = Toast.makeText(cnxt, messg, Toast.LENGTH_LONG)
    tst.setGravity(Gravity.CENTER, 0, 0)
    tst.show()
}