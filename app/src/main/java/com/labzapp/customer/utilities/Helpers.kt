package com.labzapp.customer.utilities

import android.content.Context
import android.content.res.Resources
import android.provider.Settings.Global.getString
import android.view.Gravity
import android.widget.Toast
import com.labzapp.customer.R

fun toastz(cnxt: Context, messg: String)
{
    val tst = Toast.makeText(cnxt, messg, Toast.LENGTH_LONG)
    tst.setGravity( Gravity.FILL_HORIZONTAL, 0, 0)
    tst.show()
}

