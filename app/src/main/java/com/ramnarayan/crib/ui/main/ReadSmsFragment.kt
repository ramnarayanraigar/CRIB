package com.ramnarayan.crib.ui.main

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ramnarayan.crib.R
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


class ReadSmsFragment : Fragment(), CoroutineScope {

    private lateinit var textCount: TextView
    private lateinit var editNumber: EditText
    private lateinit var editNoOfDays: EditText
    private lateinit var btnSubmit: Button

    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var isPermissionGranted = false


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    private var mutableNoOfMessage: MutableLiveData<Int> = MutableLiveData()

    private val noOfMessage: LiveData<Int>
        get() {
            return mutableNoOfMessage
        }

    companion object {
        fun newInstance() = ReadSmsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.read_sms_fragment, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        noOfMessage.observe(viewLifecycleOwner, ::setNoOfMessage)
        btnSubmit.setOnClickListener {
            if (isReadSmsPermissionGranted()) {
                val noOfDay = editNoOfDays.text.toString()
                val number = editNumber.text.toString()

                when {
                    number.isEmpty() -> {
                        Toast.makeText(requireContext(), "Please enter number", Toast.LENGTH_LONG)
                            .show()
                    }
                    noOfDay.isEmpty() -> {
                        Toast.makeText(
                            requireContext(),
                            "Please enter number of days",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                    else -> {
                        launch {
                            withContext(Dispatchers.IO) {
                                mutableNoOfMessage.postValue(getNoOfMessage(noOfDay, number))
                            }
                        }

                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please allow read sms permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestReadSmsPermission()
    }


    // Initialization of views
    private fun initViews(view: View) {
        textCount = view.findViewById(R.id.textCount)
        editNumber = view.findViewById(R.id.editMobileNo)
        editNoOfDays = view.findViewById(R.id.editNoOfDays)
        btnSubmit = view.findViewById(R.id.btnSubmit)
    }

    /*
    It will take number of sender and number
    of days argument entered by user
    and return total number of message for that number in specified days
     */
    private fun getNoOfMessage(noOfDay: String, number: String): Int {
        val count: Int
        val myMessage = Uri.parse("content://sms/")
        val cr: ContentResolver = requireActivity().contentResolver
        val date: Long =
            Date(System.currentTimeMillis() - noOfDay.toLong() * 86400000L).time
        val c = cr.query(
            myMessage, arrayOf(
                "date"
            ), "address = '$number' AND date > ?", arrayOf("" + date), null
        )

        count = c!!.count
        c.close()

        return count
    }

    /*
    It will get no of days from live data and update it on ui
    if no of message is 0 => Sorry, no message found will set on textview
    if no of message is N => N number of message found set on textview
     */
    private fun setNoOfMessage(count: Int) {
        if (count == 0) {
            textCount.text = getString(R.string.sorry_no_messages_found)
        } else {
            textCount.text =
                count.toString().plus(" ").plus("number of messages found")
        }
    }

    private fun requestReadSmsPermission() {
         permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            isPermissionGranted = isGranted
        }

        if (!isPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    private fun isReadSmsPermissionGranted() : Boolean {
        if (!isPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }

        return isPermissionGranted
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

}