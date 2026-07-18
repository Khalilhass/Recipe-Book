package com.example.recipe_book.util

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun TextInputEditText.textOrEmpty(): String = text?.toString()?.trim().orEmpty()

/** Shows/clears an inline error on a TextInputLayout without extra boilerplate at each call site. */
fun TextInputLayout.setErrorOrNull(message: String?) {
    error = message
    isErrorEnabled = message != null
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}
