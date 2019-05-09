package com.develop.todo.tutoriallib

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast

import develop.com.todo.tutoriallib.GuideView
import develop.com.todo.tutoriallib.config.DismissType
import develop.com.todo.tutoriallib.config.Gravity
import develop.com.todo.tutoriallib.listener.GuideListener

class MainActivity : AppCompatActivity() {

    internal lateinit var view1: View
    internal lateinit var view2: View
    internal lateinit var view3: View
    internal lateinit var view4: View
    internal lateinit var view5: View
    private var mGuideView: GuideView? = null
    private var builder: GuideView.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        view1 = findViewById(R.id.view1)
        view2 = findViewById(R.id.view2)
        view3 = findViewById(R.id.view3)
        view4 = findViewById(R.id.view4)
        view5 = findViewById(R.id.view5)

        builder = GuideView.Builder(this)
                .setTitle("Guide Title Text")
                .setContentText("Guide Description Text\n .....Guide Description Text\n .....Guide Description Text .....")
                .setGravity(Gravity.center)
                .setDismissType(DismissType.outside)
                .setTargetView(view1)
                .setGuideListener(object : GuideListener {
                    override fun onDismiss(view: View) {
                        when (view.id) {
                            R.id.view1 -> builder!!.setTargetView(view2).build()
                            R.id.view2 -> builder!!.setTargetView(view3).build()
                            R.id.view3 -> builder!!.setTargetView(view4).build()
                            R.id.view4 -> builder!!.setTargetView(view5).build()
                            R.id.view5 -> return
                        }
                        mGuideView = builder!!.build()
                        mGuideView!!.show()
                    }

                    override fun onClose() {
                        Toast.makeText(this@MainActivity, "close", Toast.LENGTH_SHORT).show()

                        finish()
                    }
                })

        mGuideView = builder!!.build()
        mGuideView!!.show()

        updatingForDynamicLocationViews()
    }

    private fun updatingForDynamicLocationViews() {
        view4.onFocusChangeListener = View.OnFocusChangeListener { view, b -> mGuideView!!.updateGuideViewLocation() }
    }

}
