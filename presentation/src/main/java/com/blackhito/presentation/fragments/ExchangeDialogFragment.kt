package com.blackhito.presentation.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExchangeDialogFragment(private val message: String): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialogBuilder = MaterialAlertDialogBuilder(it)
            dialogBuilder.setMessage(message)
                .setPositiveButton("OK") {
                    dialog, _ -> dialog.cancel()
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}