package com.varnittyagi.googlemaps.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding = DataBindingUtil.setContentView(this,R.layout.activity_profile)

        binding.setNameProfile.setOnClickListener{
            binding.nameProfile.visibility = View.GONE
            binding.editNameProfile.visibility = View.VISIBLE

        }
        binding.setEmailProfile.setOnClickListener{
            binding.emailProfile.visibility = View.GONE
            binding.edtEmailProfile.visibility = View.VISIBLE

        }

    }
}