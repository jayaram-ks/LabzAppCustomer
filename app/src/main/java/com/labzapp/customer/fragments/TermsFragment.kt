package com.labzapp.customer.fragments

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS
import androidx.core.text.HtmlCompat.fromHtml
import androidx.fragment.app.Fragment
import com.labzapp.customer.databinding.FragmentTermsBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TermsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentTermsBinding? = null
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

        _binding = FragmentTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.managBookTxt1.text = fromHtml("You have the option to modify/reschedule or cancel your booking at any time before the samples get collected. Sent us your request from your registered mobile number to <font color=\"black\" ><b><a href=\"sms:+918078900901\">+91 8078 900 901</a></b></font> or e-mail us to<font color=\"blue\" > <a href=\"mailto:labzappcare@gmail.com\">labzappcare@gmail.com.</a></font> Our booking manager will support you to re-schedule your booking to a future date. Don’t forget to mention your booking id number in your request.",FROM_HTML_OPTION_USE_CSS_COLORS)
        binding.managBookTxt1.movementMethod = LinkMovementMethod.getInstance();
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TermsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}