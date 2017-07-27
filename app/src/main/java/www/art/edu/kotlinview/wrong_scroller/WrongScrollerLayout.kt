package www.art.edu.kotlinview.wrong_scroller

import android.content.Context
import android.support.v4.view.ViewConfigurationCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Scroller
import www.art.edu.kotlinview.KotlinViewActivity

/**
 * Created by Administrator on 2017/7/25.
 */
class WrongScrollerLayout : ViewGroup {


    lateinit var mScroller: Scroller
    var mTouchSlop: Int = 0
    var mXDown: Int = 0
    var mXCurMove: Int = 0
    var mXLastMove: Int = 0
    var leftBorder: Int = 0
    var rightBorder: Int = 0

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        mScroller = Scroller(context)
        var configuration = ViewConfiguration.get(context)
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration)//获得随屏幕尺寸适配的，合适误差值。
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        (0..(childCount - 1))
                .map { getChildAt(it) }
                .forEach { measureChild(it, widthMeasureSpec, heightMeasureSpec) }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed) {
            for (i in 0..(childCount - 1)) {
                val childView = getChildAt(i)
                childView.layout(i * childView.measuredWidth, 0, (i + 1) * childView.measuredWidth, childView.measuredHeight)
            }
            leftBorder = getChildAt(0).left
            rightBorder = getChildAt(childCount - 1).right
        }
    }

    override fun dispatchTouchEvent(Event: MotionEvent): Boolean {

        return super.dispatchTouchEvent(Event)//执行父类，返回父类结果值。
    }


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mXDown = event.rawX.toInt()//得到在屏幕上的坐标(（就是绝对坐标）
                mXLastMove = mXDown //最近一次
            }//仅做记录功能。
            MotionEvent.ACTION_MOVE -> {
                mXCurMove = event.rawX.toInt()
                var diff = Math.abs(mXCurMove - mXDown)//计算，在不断移动中，距离最开始的落指位置有多远
                mXLastMove = mXCurMove//刷新一下  最近一次的值
                if (diff > mTouchSlop) {
                    return true  //如果大于了 Slop误差值。那么拦截，  拦截后，交给ViewGroup的onTouchEvent()事件，进行操作。
                    //交给下面的Touch事件

                    //TODO 此处，Down事件，可以交由给layout中的单一 View实现。然后  MOVE UP事件，交给layout本身处置就好。
                    //这也符合之前的  下拉刷新控件，  里面的思路：————点击view有效，下拉时只有layout有效
                }
            }
            MotionEvent.ACTION_UP -> {
                //不作处理。
            }
        }
        //如果ACTION——MOVE没有  return true， 那么走到这里  return false
        return super.onInterceptTouchEvent(event)//默认返回false  。（？？？excuse me 我知道默认返回false的，这里还不如写false）
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {
                KotlinViewActivity.TLog.l("执行了ACTION_MOVE")
                mXCurMove = event.rawX.toInt()//进入onTouchEvent事件时，重新刷新一下event的坐标（不管是不是和intercept中的同一个event，大多数时间是，但是怕串）
                var deltaScrolledX = -(mXCurMove - mXLastMove)//下滑则上显————（此处负值的处理，有一点小技巧。具体见我收藏夹。）
                //这个scrolledX  ，就是相对移动
                //getScrollX 就是取得当前等效于scrollTo()的值
                if (scrollX + deltaScrolledX < leftBorder) {//layout的左边界。
                    scrollTo(leftBorder, 0)
                    return true //onTouchEvent事件返回了true。后面其他的所有Event，都只能接到CAN_CEL事件了。
                    //于是这一层事件，到这里也该完毕了。
                }
                //此处必用 else   if
                else if (scrollX + width + deltaScrolledX > rightBorder) {//TODO 此处 width的意思——————不是layout的内部宽度全长！！！ 而是  layout显示的宽度（因为可滚动，所以内部宽度更宽，外部的宽度是写死的。）
                    //TODO 设想一种情况——————  getScrollX  +  getWidth ==  rightBorder  ，这种情况  瞬间就能明白了
                    scrollTo(rightBorder - width, 0)//快顶格了，赶紧拉回来。让它处于最多顶格的位置。
                    return true //onTouchEvent事件返回了true。在此之后的分发，都只能收到 Event - CANCEL  了。
                }
                scrollBy(deltaScrolledX, 0)//取到，经过负值处理的相对移（滚）动（屏挪动）值。
                mXLastMove = mXCurMove //记录最新值

            }
        //长距离scroll
            MotionEvent.ACTION_UP -> {//TODO 此处，仔细查看，View自带的scrollTo，和Scroller的startScroll区别。
                var targetIndex = (scrollX + width / 2) / width //当前的滑动的位置，加上width的一半（半个屏幕），然后进行四舍五入（在中线左边，则滑到左侧的边栏了；在中线右边，则滑到右侧的边栏0.0）
                var deltaX = targetIndex * width - scrollX
                mScroller.startScroll(scrollX, 0, deltaX, 0, 999)//该死的，这里又完全相反了。Google的神奇API
                //TODO Scroller 和 View.scrollTo  看上去相反，其实就原理深入而言，还是一致的。
                //TODO 还是原来的那一句话：把指定的坐标，移动到显示区域的左上角。
                invalidate()
            }
        }

        return super.onTouchEvent(event)//如果是最普通的 scrollBy(deltaScrolledX, 0) ，按照父类的来。
        //父类默认按照   ClickListener（）  或者  LongClickListener（）  值来。（查看源码）
    }

    //细分
    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {//判断滚动————是否已经完成。//专用API
            if (mScroller.computeScrollOffset()) {//判断滚动————是否已经完成。//专用API
                scrollTo(mScroller.currX, mScroller.currY)//逐步转化过程？？？
                //可以手动写出来的过程？？
                invalidate()
                KotlinViewActivity.TLog.l("执行了computeScroll")
            }
            super.computeScroll()
        }


        //Scroller ——————》细分版的微分的View.scrollTo 【每10毫秒大概执行一次。10毫秒为最细单位。】
        //View.scrollTo ——————》单次版的积分的Scroller。（一次跳到。适合MOVE事件，因为MOVE事件16毫秒一次。）

        //而相形之下，Scroller自然适合的是，  一次事件（DOWN 、 UP），大段位移。（一句话实现分解。）

    }

}