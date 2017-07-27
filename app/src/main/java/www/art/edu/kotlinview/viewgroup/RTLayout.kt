package www.art.edu.kotlinview.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import www.art.edu.kotlinview.KotlinViewActivity

/**
 * Created by Administrator on 2017/7/23.
 */
class RTLayout : LinearLayout {

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        KotlinViewActivity.TLog.l("两参数构造")
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                KotlinViewActivity.TLog.l("RTLayout---dispatchTouchEvent-DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                KotlinViewActivity.TLog.l("RTLayout---dispatchTouchEvent-MOVE")
            }
            MotionEvent.ACTION_UP -> {
                KotlinViewActivity.TLog.l("RTLayout---dispatchTouchEvent-UP")
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                KotlinViewActivity.TLog.l("RTLayout---onTouchEvent-DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                KotlinViewActivity.TLog.l("RTLayout---onTouchEvent-MOVE")
            }
            MotionEvent.ACTION_UP -> {
                KotlinViewActivity.TLog.l("RTLayout---onTouchEvent-UP")
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                KotlinViewActivity.TLog.l("RTLayout---onInterceptTouchEvent-DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                KotlinViewActivity.TLog.l("RTLayout---onInterceptTouchEvent-MOVE")
            }
            MotionEvent.ACTION_UP -> {
                KotlinViewActivity.TLog.l("RTLayout---onInterceptTouchEvent-UP")
            }
        }
        return true
    }

}