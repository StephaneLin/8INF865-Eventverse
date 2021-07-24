package com.boulin.eventverse.ui.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.boulin.eventverse.R

class ProfileParametersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_parameters, container, false)

        view.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.profile_menu_label)
    }

    //todo: correct layout issues
}