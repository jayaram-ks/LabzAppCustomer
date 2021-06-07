package com.labzapp.customer.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.labzapp.customer.activities.BookingActivity
import com.labzapp.customer.activities.HomeActivity
import com.labzapp.customer.activities.ProfileUpdateActivity
import com.labzapp.customer.databinding.FragmentSelfDialogBinding


class SelfDialogFragment : DialogFragment() {

    private var _binding: FragmentSelfDialogBinding? = null
    private val binding get() = _binding!!
    private var forSelf : String = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /** The system calls this to get the DialogFragment's layout, regardless
    of whether it's being displayed as a dialog or an embedded fragment. */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelfDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** The system calls this only when creating the layout in a dialog. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.selfbtn.setOnClickListener {
            dialog?.dismiss()
            forSelf = "1"
            setFragmentResult("bookingKey", bundleOf("booking_for" to forSelf))


        }
        binding.anotherbtn.setOnClickListener{
            dialog?.dismiss()
            forSelf = "2"
            setFragmentResult("bookingKey", bundleOf("booking_for" to forSelf))
        }
    }


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SelfDialogFragment"
    }
}