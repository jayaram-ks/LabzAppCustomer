package com.labzapp.customer.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.labzapp.customer.databinding.FragmentPrescFileTypeDialogBinding


class PrescFileTypeDialog : DialogFragment() {

    private var _binding: FragmentPrescFileTypeDialogBinding? = null
    private val binding get() = _binding!!
    private var uploadFrom : String = "gal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescFileTypeDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.presGallery.setOnClickListener {
            dialog?.dismiss()
            uploadFrom = "gal"
            setFragmentResult("uploadKey", bundleOf("upload_from" to uploadFrom))
        }
        binding.presCamera.setOnClickListener{
            dialog?.dismiss()
            uploadFrom = "cam"
            setFragmentResult("uploadKey", bundleOf("upload_from" to uploadFrom))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PrescFileTypeDialog"
    }
}