package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.labzapp.customer.R
import com.labzapp.customer.databinding.FragmentPatientDataBinding
import com.labzapp.customer.utilities.districtz

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PatientDataFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentPatientDataBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    private var districtid = 0
    private var custGender: Int = 0

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
        _binding = FragmentPatientDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val values: ArrayList<String> = ArrayList(districtz.values)
        val adapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner = binding.cdistrict
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        binding.radio1.setOnClickListener{ view -> onRadioButtonClicked(view) }
        binding.radio2.setOnClickListener { view -> onRadioButtonClicked(view) }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        //  parent.getItemAtPosition(pos)
        val keysz: ArrayList<Int> = ArrayList(districtz.keys)
        districtid =  keysz[pos]

    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio1 ->
                    if (checked) {
                        custGender = 1
                    }
                R.id.radio2 ->
                    if (checked) {
                        custGender = 2
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PatientDataFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "PatientDataFragment"
    }
}