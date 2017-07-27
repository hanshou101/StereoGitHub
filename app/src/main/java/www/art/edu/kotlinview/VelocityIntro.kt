package www.art.edu.kotlinview

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView

/**
 * Created by Administrator on 2017/7/18.
 */
class VelocityIntro : Activity() {

    lateinit var mInfo: TextView

    var mVelocityTracker: VelocityTracker? = null

    var mMaxVelocity: Float = 0F
    var mPointerId = 0

    var actionCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mVelocityTracker = VelocityTracker.obtain()
        mMaxVelocity = ViewConfiguration.get(this).scaledMaximumFlingVelocity.toFloat()

        mInfo = TextView(this)
        mInfo.setLines(4)
        mInfo.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mInfo.setTextColor(Color.BLACK)
        mInfo.gravity = Gravity.CENTER
        setContentView(mInfo)


        //33秒钟。2000千次MOVE事件。2000次TextView.setText(str)事件
        //一秒钟——60次。——————一秒钟可以处理60次。  60fps巧合吗？
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        var action: Int = event.action

        mVelocityTracker?.addMovement(event)

        val verTracker = mVelocityTracker

        when (action) {

            MotionEvent.ACTION_DOWN -> {
                mVelocityTracker = VelocityTracker.obtain() //修复Bug，在这里再次初始化（方便第二次点击时，重启）
                mPointerId = event.getPointerId(0)//触点
            }
            MotionEvent.ACTION_MOVE -> {
                verTracker?.computeCurrentVelocity(1000, mMaxVelocity)//求伪瞬时速度
                val velocityX = verTracker?.getXVelocity(mPointerId)
                val velocityY = verTracker?.getYVelocity(mPointerId)
                recordInfo(velocityX, velocityY)
            }
            MotionEvent.ACTION_UP -> {
                releaseVelocityTracker()
            }
            MotionEvent.ACTION_CANCEL -> {
                releaseVelocityTracker()
            }
            else -> {

            }

        }


        return super.onTouchEvent(event)
    }

    private fun releaseVelocityTracker() {
        mVelocityTracker?.clear()
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private val sFormatStr: String = "velocityX=%f\nvelocityY=%f\ncount=%d"

    //可以把频繁调用的事件——————中的Var，Val变量创建  优化
    private fun recordInfo(velocityX: Float?, velocityY: Float?) {
        actionCount++
        val info: String = String.format(sFormatStr, velocityX, velocityY, actionCount)
        mInfo.text = info

    }


}