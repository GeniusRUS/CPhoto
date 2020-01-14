package com.genius.coroutinesphoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.coroutines.CoroutineScope

class MainFragment : Fragment() {

    private lateinit var customImageView: CustomImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_main, container, false)

        customImageView = v.findViewById(R.id.image)

        customImageView.setOnClickListener {
            customImageView.requestPic(it.context as CoroutineScope)
        }

        return v
    }

    companion object {
        const val TAG = "MainFragment"
    }
}