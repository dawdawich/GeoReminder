package com.gooldy.georeminder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.content_main.cardFragment


class MainActivity : AppCompatActivity(), CardContent.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener {
            with(supportFragmentManager.beginTransaction()) {
                val fragment = CardContent()
                setCustomAnimations(
                    R.anim.fragment_anim_slide_in_up,
                    R.anim.fragment_anim_slide_out_up,
                    R.anim.fragment_anim_slide_in_up,
                    R.anim.fragment_anim_slide_out_up
                )
                replace(cardFragment.id, fragment)
                addToBackStack(null)
                hide(fragment)
                commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will

        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onFragmentInteraction(params: Map<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
