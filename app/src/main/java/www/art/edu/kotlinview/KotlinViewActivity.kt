package www.art.edu.kotlinview

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent

/**
 * Created by Administrator on 2017/7/16.
 */
class KotlinViewActivity : Activity() {

    object TLog {
        @JvmStatic
        fun l(str: String) {
            Log.i("KotlinView", str)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_kotlin_view)

        val contentView = findViewById(R.id.contentView)

        //contentView.addView(PolyView(this))

//        var stereoView = findViewById<StereoView>(R.id.stereo_view)
//        stereoView.setStartScreen(4)
//        stereoView.setAngle(100)
//        stereoView.setStereoListener(object : StereoView.StereoListener {
//            override fun toPre(currentScreen: Int) {
//            }
//
//            override fun toNext(currentScreen: Int) {
//            }
//
//        })


        val rtbutton = findViewById(R.id.rtbutton)
        rtbutton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    TLog.l("RTButton---onTouch---DOWN")
                }

                MotionEvent.ACTION_MOVE -> {
                    TLog.l("RTButton---onTouch---MOVE")
                }

                MotionEvent.ACTION_UP -> {
                    TLog.l("RTButton---onTouch---UP")
                }
            }
            return@setOnTouchListener super.onTouchEvent(event)
        }
        rtbutton.setOnClickListener { TLog.l("RTButton---onClick---!!!") }


        val rtlayout = findViewById(R.id.rtlayout)
        rtlayout.setOnTouchListener { v, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    TLog.l("RTLayout---onTouch---DOWN")
                }
                MotionEvent.ACTION_MOVE -> {
                    TLog.l("RTLayout---onTouch---MOVE")
                }
                MotionEvent.ACTION_UP -> {
                    TLog.l("RTLayout---onTouch---UP")
                }
            }
            return@setOnTouchListener super.onTouchEvent(event)
        }
        rtlayout.setOnClickListener { TLog.l("RTLayout---onClick---!!!") }

    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                TLog.l("Activity---dispatchTouchEvent---DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                TLog.l("Activity---dispatchTouchEvent---MOVE")
            }
            MotionEvent.ACTION_UP -> {
                TLog.l("Activity---dispatchTouchEvent---UP")
            }
        }
        return super.dispatchTouchEvent(event)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                TLog.l("Activity---onTouchEvent---DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                TLog.l("Activity---onTouchEvent---MOVE")
            }
            MotionEvent.ACTION_UP -> {
                TLog.l("Activity---onTouchEvent---UP")
            }
        }
        return super.onTouchEvent(event)
    }


    //正宗的完全步骤

    //TODO Activity---dispatchTouchEvent---UP
    //TODO RTButton---dispatchTouchEvent---UP
    //TODO RTButton---onTouch---UP
    //TODO RTButton---onTouchEvent---UP
    //TODO RTButton---onClick---!!!

    //Click  在 DOWN-MOVE-UP三步完成后，完成调用。





    //RTLayout  开启曙光拦截后
    //曙光
    //曙光
    //曙光  从泥潭里出来了（不要高兴的太早）
    //TODO Activity---dispatchTouchEvent---DOWN
    //TODO RTLayout---dispatchTouchEvent-DOWN
    //TODO RTLayout---onInterceptTouchEvent-DOWN
    //TODO RTLayout---onTouch---DOWN
    //TODO RTLayout---onTouchEvent-DOWN

    //可明显而见  interCept的一步。

    //TODO Activity---dispatchTouchEvent---UP
    //TODO RTLayout---dispatchTouchEvent-UP
    //TODO RTLayout---onTouch---UP
    //TODO RTLayout---onTouchEvent-UP
    //TODO RTLayout---onClick---!!!

    //为什么 UP时没interCept，原因自己想

}