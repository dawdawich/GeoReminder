package com.gooldy.georeminder.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gooldy.georeminder.R
import com.gooldy.georeminder.activities.MainActivity
import com.gooldy.georeminder.constants.ARG_PARAM_REMINDER
import com.gooldy.georeminder.data.Area
import com.gooldy.georeminder.data.AreaItemAdapter
import com.gooldy.georeminder.data.Reminder
import kotlinx.android.synthetic.main.fragment_card_content.*
import java.time.Instant
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CardContent.OnFragmentInteractionListener] interface
 * to handle interaction events.d
 * Use the [CardContent.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class CardContent : Fragment(), View.OnClickListener {

    private var reminder: Reminder? = null
    private var listener: OnFragmentInteractionListener? = null
    private var areas: MutableSet<Area> = mutableSetOf()
    private val itemAdapter: AreaItemAdapter = AreaItemAdapter(emptyList())
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reminder = it.getSerializable(ARG_PARAM_REMINDER) as Reminder?
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inflate = inflater.inflate(R.layout.fragment_card_content, container, false)
        val rSaveButton: Button = inflate.findViewById(R.id.saveButton)
        val rCancelButton: Button = inflate.findViewById(R.id.cancelButton)
        val rNameET: EditText = inflate.findViewById(R.id.reminderName)
        val rDescriptionET: EditText = inflate.findViewById(R.id.reminderDescription)
        val rRepeatReminderS: Switch = inflate.findViewById(R.id.sRepeatReminder)
        val rActiveReminderS: Switch = inflate.findViewById(R.id.sIsActive)
        rSaveButton.setOnClickListener {
            reminder?.let {
                sendInfoToActivity(it.apply {
                    reminderName = rNameET.text.toString()
                    reminderText = rDescriptionET.text.toString()
                    reminderAreas = areas.toSet()
                    repeatable = rRepeatReminderS.isChecked
                    isActive = rActiveReminderS.isChecked
                    modifyTime = Instant.now()
                }, true)
            } ?: run {
                sendInfoToActivity(Reminder(UUID.randomUUID(), rNameET.text.toString(), rDescriptionET.text.toString(),
                    areas.toSet(), Instant.now(),
                    Instant.now(), rRepeatReminderS.isChecked, rActiveReminderS.isChecked))
            }
            activity?.supportFragmentManager?.popBackStack()
        }
        rCancelButton.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.returnFab()
            activity?.supportFragmentManager?.popBackStack()
        }
        inflate.findViewById<Button>(R.id.addGeo).setOnClickListener(this)
        recyclerView = inflate.findViewById(R.id.cardsContainer)
        recyclerView.adapter = itemAdapter
        recyclerView.layoutManager = LinearLayoutManager(inflate.context)

        reminder?.let {
            rNameET.setText(it.reminderName)
            rDescriptionET.setText(it.reminderText)
            rRepeatReminderS.isChecked = it.repeatable
            rActiveReminderS.isChecked = it.isActive
            areas = it.reminderAreas.toMutableSet()
            recyclerView.adapter = AreaItemAdapter(areas.toList())
        }

        return inflate
    }

    private fun sendInfoToActivity(reminder: Reminder, isUpdate: Boolean = false) {
        listener?.onFragmentInteraction(reminder, isUpdate)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            addGeo.id -> {
                val mainActivity = activity as MainActivity
                if (mainActivity.checkMapServices()) {
                    mainActivity.startMapActivity()
                }
            }
        }
    }

    fun setMapCoordinate(area: Area) {
        areas.add(area)

        val itemAdapter = AreaItemAdapter(areas.toList())
        recyclerView.adapter = itemAdapter
        Log.d(TAG, "Achieve area from map, hor: ${area.latitude}, ver: ${area.longitude}, radius: ${area.radius}")
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(reminder: Reminder, isUpdate: Boolean = false)
    }

    companion object {

        const val TAG = "CardContent"

        @JvmStatic
        fun newInstance(reminder: Reminder?) =
            CardContent().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM_REMINDER, reminder)
                }
            }
    }
}
