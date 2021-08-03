package com.labzapp.customer.fragments

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.labzapp.customer.databinding.FragmentAboutBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AboutFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.assistTxt.text = HtmlCompat.fromHtml(
            "For any assistance mail your queries to <font color=\"blue\" > <a href=\"mailto:labzappcare@gmail.com\">labzappcare@gmail.com.</a></font> <br><br> or call us <font color=\"black\" ><b><a href=\"sms:+918078900901\">+91 8078 900 901</a></b></font>.<br><br>Labzapp mobile application is owned and operated by Medinova wellness solutions Pvt.Ltd.\n",
            HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS
        )
        binding.assistTxt.movementMethod = LinkMovementMethod.getInstance();
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AboutFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}