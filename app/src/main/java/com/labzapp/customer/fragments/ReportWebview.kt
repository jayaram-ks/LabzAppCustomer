package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.labzapp.customer.databinding.FragmentReportWebviewBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ReportWebview : Fragment() {

    private var _binding: FragmentReportWebviewBinding? = null
    private val binding get() = _binding!!
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportWebviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateWebContent()
        binding.refreshButton.setOnClickListener { updateWebContent() }
    }

    private fun updateWebContent(){
        val webview: WebView = binding.repWebview
        webview.getSettings().setJavaScriptEnabled(true)
        webview.clearHistory()
        webview.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$param1")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReportWebview().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}