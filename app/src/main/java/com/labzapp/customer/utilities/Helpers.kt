package com.labzapp.customer.utilities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.snackbar.Snackbar
import com.labzapp.customer.activities.HomeActivity
import com.labzapp.customer.activities.ProfileUpdateActivity
import com.labzapp.customer.activities.RegisterActivity
import com.labzapp.customer.storage.SharedPrefManager


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
    Snackbar.make(cnxtv,messg,Snackbar.LENGTH_LONG).setTextColor(Color.WHITE).setAnchorView(idz).setBackgroundTint(Color.parseColor("#FF228B22")).setDuration(4000).show()
}

fun snackzcolor(cnxtv: View, messg: String,idz:Int,colorstring:String,sduration:Int){
    Snackbar.make(cnxtv,messg,Snackbar.LENGTH_LONG).setTextColor(Color.WHITE).
    setAnchorView(idz).setBackgroundTint(Color.parseColor(colorstring)).setDuration(sduration).show()
}


fun logoutFromDevice(mcontext: Context){
    SharedPrefManager.getInstance(mcontext).clear()
    val intent = Intent(mcontext, RegisterActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(mcontext,intent,null)
}

fun gotoHome(cntxt:Context){
    val intent = Intent(cntxt, HomeActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(cntxt,intent,null)
}




