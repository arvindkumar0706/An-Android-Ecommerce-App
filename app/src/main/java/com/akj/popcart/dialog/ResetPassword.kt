package com.akj.popcart.dialog

import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.akj.popcart.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

fun Fragment.setupBottomSheetDialog(
    onSendClick:(String) -> Unit
){
    var dialog=BottomSheetDialog(requireContext(), R.style.DialogStyle)
    var view=layoutInflater.inflate(R.layout.reset_password_dialog,null)
    dialog.setContentView(view)
    dialog.behavior.state=BottomSheetBehavior.STATE_EXPANDED
    dialog.show()

    var emailid=view.findViewById<EditText>(R.id.email)
    var cancelButton=view.findViewById<Button>(R.id.Cancelbutton)
    var sendButton=view.findViewById<Button>(R.id.Sendbutton)

    sendButton.setOnClickListener{
        val email=emailid.text.toString().trim()
        onSendClick(email)
        dialog.dismiss()
    }

    cancelButton.setOnClickListener{
        dialog.dismiss()
    }


}