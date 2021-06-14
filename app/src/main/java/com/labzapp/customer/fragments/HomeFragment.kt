package com.labzapp.customer.fragments

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.labzapp.customer.BuildConfig
import com.labzapp.customer.R
import com.labzapp.customer.activities.BookingActivity
import com.labzapp.customer.adapters.BannersAdapter
import com.labzapp.customer.databinding.FragmentHomeBinding
import com.labzapp.customer.models.BannerData
import com.labzapp.customer.models.BannerResponse
import com.labzapp.customer.models.MakeCallResponse
import com.labzapp.customer.models.UploadPresResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.logoutFromDevice
import com.labzapp.customer.utilities.snackzcolor
import com.labzapp.customer.utilities.snackze
import com.labzapp.customer.utilities.snackzsucc
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val PERMISSION_REQUEST_CODE = 200

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var prescImageFrom = 1
    lateinit var currentPhotoPath: String
    private var touploadfile:File? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchBanners()
        //----BOOK A TEST----
        binding.bookNewTest.setOnClickListener{
            val intent = Intent(requireActivity(), BookingActivity::class.java)
            startActivity(intent)
        }
        //----REQUEST A CALL---
        binding.reqACall.setOnClickListener {
            requestACall()
        }
        //----VIEW LABS----
        binding.viewLabshome.setOnClickListener {
            val succFragment = LabsFragment()
            val trans = parentFragmentManager.beginTransaction()
            trans.replace(R.id.frame_container,succFragment)
            trans.addToBackStack(null)
            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            trans.commit()
        }
        //----UPLOAD PRESCRIPTION----
        binding.uploadPrescription.setOnClickListener {
            val frUploadtyp = PrescFileTypeDialog()
            val transPresc = childFragmentManager.beginTransaction()
            frUploadtyp.show(transPresc,null)
        }
        childFragmentManager.setFragmentResultListener("uploadKey", this) { key, bundle ->
            val  uploadFrom = bundle.getString("upload_from")
            if(uploadFrom == "gal"){   //gallery
                openGallery()
            }else if(uploadFrom == "cam"){
                checkPermissions()



            }
        }
    }


    private fun checkPermissions() {
        if (context?.let { ContextCompat.checkSelfPermission(it, CAMERA) } != PackageManager.PERMISSION_GRANTED) {

            view?.let { snackze(it,"Please enable permission for Camera",binding.uploadPrescription.id) }
            requestMultiplePermissions.launch(arrayOf(CAMERA))
        } else {
           // Log.d("ghg", "Permission Already Granted")
            openCamera()
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                //Log.d("dsf", "${it.key} = ${it.value}")
            }
            if (permissions[CAMERA] == true ) {
                //Log.d("DSF", "Permission granted cam")
                openCamera()
            } else {
                view?.let { snackze(it,"Permission not granted for Camera",binding.uploadPrescription.id) }
                //Log.d("TAG", "Permission not granted cam")
            }
        }






    private fun openCamera() {
        prescImageFrom = 1
        val photoFile: File? = try {
            createCapturedPhoto()
        } catch (ex: IOException) {
            // If there is error while creating the File, it will be null
            null
        }
        photoFile?.also {
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                it
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //val f = File(Environment.DIRECTORY_DCIM, "presc_temp.jpg")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            resultLauncher.launch(intent)
        }
    }

    private fun openGallery() {
        prescImageFrom = 2
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            touploadfile = null
            val tempfile = System.currentTimeMillis().toString()+".jpg"
            if (prescImageFrom == 1) {  //CAMERA
                val uri = Uri.parse(currentPhotoPath)
                binding.uploadImg.setImageURI(uri)
                if(binding.uploadImg.drawable != null) {
                    val image = (binding.uploadImg.drawable as BitmapDrawable).bitmap
                    touploadfile = convertBitmapToFile(tempfile, image)
                }

            } //GALLERY ------
            else if (prescImageFrom == 2) {
                val uri = Uri.parse(data?.data.toString())
                binding.uploadImg.setImageURI(uri)
                if(binding.uploadImg.drawable != null) {
                    val image = (binding.uploadImg.drawable as BitmapDrawable).bitmap
                    touploadfile = convertBitmapToFile(tempfile, image)
                }
            }

            if( touploadfile != null){
                uploadPrescription()
                binding.uploadImg.setImageURI(null)
            }
            else{
                view?.let{ snackze(it,"Invalid Image/No Image Selected",binding.bookNewTest.id) }
            }
        }
    }

    private fun convertBitmapToFile(fileName: String, bitmap: Bitmap): File {
        //create a file to write bitmap data
        val file = File(context?.cacheDir, fileName)
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70 /*ignored for PNG*/, bos)
        val bitMapData = bos.toByteArray()

        //write the bytes in file
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            fos?.write(bitMapData)
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    private fun uploadPrescription() {
        val authTokn: String? = "Bearer " + SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)

        val ApTokn: RequestBody? = apiTokn?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val uploadFileReq = touploadfile?.asRequestBody("image/jpeg".toMediaTypeOrNull())
        if ((uploadFileReq != null)) {
            var uploadFileMulti = MultipartBody.Part.createFormData(
                "prescription_file",
                touploadfile?.name,
                uploadFileReq
            )
            if(ApTokn != null) {
                val requestCall = apiService.uploadPrescription(authTokn, ApTokn, uploadFileMulti)
                view?.let{ snackzcolor(it,"Uploading Image....",binding.bookNewTest.id,"#0000FF",50000) }
                requestCall.enqueue(object : Callback<UploadPresResponse> {
                    override fun onResponse(
                        call: Call<UploadPresResponse>,
                        response: Response<UploadPresResponse>
                    ) {
                        val resp = response.body()

                        if (resp?.code == 200) {
                            resp.let {
                                view?.let{ snackzsucc(it,resp?.message.toString(),binding.bookNewTest.id) }
                            }

                        } else {
                            view?.let{ snackze(it,resp?.message.toString(),binding.bookNewTest.id) }
                        }
                    }
                    override fun onFailure(call: Call<UploadPresResponse>, t: Throwable) {
                        view?.let{ snackze(it,t?.message.toString(),binding.bookNewTest.id) }
                    }
                })
            }
        }
    }

    @Throws(IOException::class)
    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}",".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun fetchBanners() {

        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getBanners(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<BannerResponse> {
            override fun onResponse(call: Call<BannerResponse>, response: Response<BannerResponse>) {
                val resp = response.body()

                if (resp?.code == 401) {  // for API token mismatch with server on home page load - when user register in another device
                    view?.let{ emsg-> snackze(emsg ,"Your Authentication with this device Failed",binding.bookNewTest.id) }
                    lifecycleScope.launch{
                        delay(3000)
                        logoutFromDevice(requireContext())
                    }

                }else if (resp?.code == 200) {
                    showBanners(resp.banner)
                } else {
                    view?.let{snackze(it,resp?.message.toString(),binding.viewLabshome.id) }
                }
            }

            override fun onFailure(call: Call<BannerResponse>, t: Throwable) {
                view?.let{snackze(it,t.message.toString(),binding.viewLabshome.id) }
            }
        })
    }

    private fun showBanners(bannerlist: List<BannerData>)
    {
        if (!isAdded) return
        val progBar: ProgressBar = binding.progressBar
        progBar.visibility = View.GONE
        binding.viewPager2.adapter = BannersAdapter(requireContext(),bannerlist)

        lifecycleScope.launch {
            while(true){
                for(i in 0..bannerlist.size){
                    delay(3000)
                    if(i==0){
                        binding.viewPager2.setCurrentItem(i,true)
                    }else{
                        binding.viewPager2.setCurrentItem(i,true)
                    }
                }
            }
        }
    }

    private fun requestACall() {

        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.registerCustomerCall(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<MakeCallResponse> {
            override fun onResponse(call: Call<MakeCallResponse>, response: Response<MakeCallResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    view?.let{mss -> snackzsucc(mss,resp?.message.toString(),binding.viewLabshome.id) }
                    binding.reqACall.isClickable = false
                } else {
                    view?.let{snackze(it,resp?.message.toString(),binding.viewLabshome.id) }
                }
            }

            override fun onFailure(call: Call<MakeCallResponse>, t: Throwable) {
                view?.let{snackze(it,t.message.toString(),binding.viewLabshome.id) }
            }
        })
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}