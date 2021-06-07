package com.labzapp.customer.utilities

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.provider.Settings.Global.getString
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.labzapp.customer.R

fun toastz(cnxt: Context, messg: String)
{
    val tst = Toast.makeText(cnxt, messg, Toast.LENGTH_LONG)
    tst.setGravity( Gravity.FILL_HORIZONTAL, 0, 0)
    tst.show()
}

fun snackze(cnxtv: View, messg: String,idz:Int){
    Snackbar.make(cnxtv,messg,Snackbar.LENGTH_LONG).setTextColor(Color.WHITE).setAnchorView(idz).setBackgroundTint(Color.RED).show()
}

fun snackzsucc(cnxtv: View, messg: String,idz:Int){
    Snackbar.make(cnxtv,messg,Snackbar.LENGTH_LONG).setTextColor(Color.WHITE).setAnchorView(idz).setBackgroundTint(Color.parseColor("#FF228B22")).show()
}



