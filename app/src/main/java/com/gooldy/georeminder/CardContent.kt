package com.gooldy.georeminder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.gooldy.georeminder.constants.ARG_PARAM1
import com.gooldy.georeminder.constants.ARG_PARAM2
import kotlinx.android.synthetic.main.fragment_card_content.*

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
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inflate = inflater.inflate(R.layout.fragment_card_content, container, false)
//        addGeo.setOnClickListener(this)
        inflate.findViewById<Button>(R.id.addGeo).setOnClickListener(this)
        return inflate
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(params: Map<String, Any>) {
        listener?.onFragmentInteraction(params)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            addGeo.id -> {
                val mainActivity = activity as MainActivity
                if (mainActivity.checkMapServices()) {
                    mainActivity.startMapActivity()
                }
            }
        }
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
        // TODO: Update argument type and name
        fun onFragmentInteraction(params: Map<String, Any>)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CardContent.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CardContent().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
