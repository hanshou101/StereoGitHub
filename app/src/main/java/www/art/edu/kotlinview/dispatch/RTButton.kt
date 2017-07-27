package www.art.edu.kotlinview.dispatch

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Button

/**
 * Created by Administrator on 2017/7/20.
 */
class RTButton : Button {

    val touchSlop = 10
    var oldMoveX = 0F


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                Log.i("KotlinView", "RTButton---dispatchTouchEvent---DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i("KotlinView", "RTButton---dispatchTouchEvent---MOVE")
            }
            MotionEvent.ACTION_UP -> {
                Log.i("KotlinView", "RTButton---dispatchTouchEvent---UP")
            }
        }
        return super.dispatchTouchEvent(event)
    }

    //dispatchTouchEvent  永远在   onTouchEvent  前面

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                Log.i("KotlinView", "RTButton---onTouchEvent---DOWN")

                oldMoveX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
              //  if (event.x - oldMoveX >= touchSlop) {
                    Log.i("KotlinView", "RTButton---onTouchEvent---MOVE")
              //      oldMoveX = event.x
              //  }
            }
            MotionEvent.ACTION_UP -> {
                Log.i("KotlinView", "RTButton---onTouchEvent---UP")
            }
        }
        return super.onTouchEvent(event)
    }
}