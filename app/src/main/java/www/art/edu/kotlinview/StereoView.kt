package www.art.edu.kotlinview

import android.content.Context
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.Interpolator
import android.widget.Scroller

/**
 * Created by Administrator on 2017/7/18.
 */
class StereoView : ViewGroup {


    private var mStartScreen: Int = 1   //当开始的时候，初始化的时候，展示的是第几屏
    private var mResistanceForce = 1.8f
    private lateinit var mScroller: Scroller //处理内部滑动  的一个工具类
    private var mAngle: Float = 90F
    private var isCan3D: Boolean = true

    private var mContext: Context
    private var mTouchSlop: Int = 0  //触摸的滑动
    private var mVelocityTracker: VelocityTracker? = null
    private lateinit var mCamera: Camera
    private lateinit var mMatrix: Matrix
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private val standerSpeed = 2000//stander机器架子。  速度2000
    //TODO 此处竟然又起名为  规定的速度——  预先规定的阈值速度
    //TODO 这不性质就和    mTouchSlop 触摸的滑动值（允许误差值）  很像了
    //TODO mTouchSlop 是调用系统API的默认预设内部值，进行初始化的

    private val flingSpeed = 800 //猛砍速度。  速度800
    private var addCount: Int = 0 // 手离开屏幕后，需要增加的页面次数。
    private var alreadyAdd: Int = 0 //对滑动多页时，  已经新增的页面次数的记录（新增了多少页面）
    private var isAdding: Boolean = false//fling猛砍时，正在添加新页面。在添加绘制时，不要开启Camera的绘制，不然页面会出现闪动
    private var mCurrentScreen: Int = 0//记录当前item  （当前所处的视角页面）//正在行进中，操作中，目前展示或想要展示，的，是第几屏
    private var mStereoListener: StereoListener? = null//前后翻页的监听器。
    private var mDownX: Float = 0.0f
    private var mDownY: Float = 0.0f
    private var mTempY: Float = 0.0f
    private var isSliding: Boolean = false //是否正在滑动
    private var mState: State = State.Normal //初始判断的枚举。


    constructor(context: Context) : this(context, null) {
        //返回了自身的一个重载。
    }

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {
        //返回自身的一个重载。
        //并init了context
    }

    constructor(context: Context, attributeSet: AttributeSet?, style: Int) : super(context, attributeSet, style) {
        //构造函数的super函数，必须第一顺位  调用————也就在声明上  调用了。
        this.mContext = context
        init(mContext)
    }

    private fun init(context: Context) {//初始化一些数据
        mTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop//初始化这个  触摸的滑动
        mCamera = Camera()
        mMatrix = Matrix()
        mScroller = /*mScroller ?:*/ Scroller(context)//为空的话， 则初始化（将？：后面的值，赋予等号前面）
        //TODO Kotlin的一个小 Bug
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)//对于所有的子类view，都进行一次测量
        mWidth = measuredWidth //在测量时，取另一个方法的测量宽度
        mHeight = measuredHeight//在测量时，取另一个方法的测量高度
        scrollTo(0, mStartScreen * mHeight)//滑动到，  把所有item叠加后，的高度的那个位置
    }

    override
    fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {//测量自身在外部的尺寸
        var childTop = 0

        //看我秀一波操作。

        (0..(childCount - 1))//这样-1之后，就符合  i 这个字母的索引
                .asSequence()
                .map { getChildAt(it) }
                .filter { it.visibility != View.GONE }//判断是否可见，可见的留下来，进入下一步
                .forEach {
                    it.layout(0, childTop,
                            it.measuredWidth, childTop + it.measuredHeight)//基于childTop这个累加值的top高度和bottom高度
                    childTop += it.measuredHeight//childTop累加上  child的item高度。 }
                }
    }

    override fun dispatchTouchEvent(Event: MotionEvent): Boolean {//TODO 关键的事件分发三方法之一。要仔细看！！！


        val x = Event.x
        val y = Event.y

        when (Event.action) {
            MotionEvent.ACTION_DOWN -> {
                isSliding = false  //如果是按键按下，那么现在停止Sliding滑动
                mDownX = x  //按下的这一下的  x坐标，记下
                mDownY = y //按下的这一下的  y坐标，记下

                mScroller.takeIf { !it.isFinished }//用takeUnless也可以，但相当不顺手
                        ?.let {
                            //满足了条件
                            //如果当前，滑动类，没有停止下来
                            it.finalY = mScroller.currY//记录下当前滑到的坐标
                            it.abortAnimation()//停止滑动动画
                            scrollTo(0, scrollY)//View的scrollY属性。  用View的scrollTo方法，滑动到View的scrollY位置。
                            isSliding = true //因为此时，设置了一个scroll滚动事件，所以Sliding滑动事件，重新记录为正在滑动
                            //TODO 此处，是否涉及到，那个不断不断翻页的惯性翻页呢？？？？
                        }
            }
            MotionEvent.ACTION_MOVE -> {
                //控件正处Move状态（手指Move了）——而以前的开关记号，显示没有在滑动的状态的话
                isSliding.takeIf { !it }?.let {
                    //重新检测
                    isSliding = isCanSliding(Event)//检测的方法，标准是————根据人工的经验值的人工检验。（根据步长检验，人工修正）
                }
            }
            else -> {
            }
        }

        return super.dispatchTouchEvent(Event) //事件分发机制全通，这里的意思是————执行父类的分发方法。（遍历分发——子View进行事件检测。）
        //整体也作一个预备——【作为一个ViewGroup】
    }

    @Suppress("RedundantIf")//暂时不用极凝念缩短，——————这样展开写，会更清晰一点。
    private fun isCanSliding(ev: MotionEvent): Boolean {
        var moveX = ev.x  //正在不断移动中的触控点传来的移动点横坐标
        var moveY = ev.y  //正在不断移动中的触控点传来的移动点纵坐标

        if (Math.abs(moveX - mDownX) > mTouchSlop//移动了的点，跨越了的位移的横坐标，大于了临界值（自己随意设置的临界值，归根结底，是经验数值）
                &&
                Math.abs(moveY - mDownY) > Math.abs(moveX - mDownX)) {//移动了的点，在位移上，纵向位移距离远大于横向距离。（一句话总结）（实现起来有种干练的感觉）
            return true  //表示，正在Sliding滑动。（通过了人工判定条件）
        } else {
            return false
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {//TODO 关键的事件分发三方法之二。要仔细看！！！
        return isSliding //TODO  这里为什么要返回  isSliding  Sliding滑动状态的值？？  这里一定有个惊天的大秘密
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {//TODO dispatchTouchEvent 和 onTouchEvent 两者的关系是什么？？？？ 为何如此相像？
        //TODO 是否  onTouchEvent  是这个旋转容器自身的点击事件？
        //TODO 而  dispatchTouchEvent  是这个容器内的子child的点击事件？？？

        //Velocity 跟踪一连串事件                  求导
        //物理意义  瞬间速度。
        //触摸事件 MotionEvent 传给VelocityTracker的addMoveMent（）方法，分析MotionEvent对象在单位时间内发生的位移，来计算速度。

        mVelocityTracker = mVelocityTracker ?: VelocityTracker.obtain()//用速度追踪器
        mVelocityTracker?.addMovement(event)
        var y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isSliding) {//如果显示，当前正在Sliding滑动中（？？？？？）
                    var realDelta = mDownY - y     //mDownY 是何时记录下的 成员变量？
                    mDownY = y //记录新值
                    if (mScroller.isFinished) {//如果滚动类已经停下？
                        recycleMove(realDelta)//这里是为了什么？？？？？
                        //为了循环滚动？
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {

            }
            MotionEvent.ACTION_UP -> {//当触摸事件抬起的时候    UP事件
                if (isSliding) {
                    isSliding = false
                    mVelocityTracker?.computeCurrentVelocity(1000)//以1000毫秒为默认单位，计算位移的速度（表示未  像素点/秒）
                    val yVelocity: Float = mVelocityTracker?.yVelocity as Float//y方向的实时瞬间速度

                    if (yVelocity < -standerSpeed//自主修正了一下，  负的y速度，才应该是  向上在滑动呀
                            //或者向上滑动时，上一页的页面，展示出的高度，超过了ViewGroup高度的1/2。那么此时判定滑动状态为State.ToPre。
                            || (scrollY / mHeight - 0.5).toInt() <= (mStartScreen - 2)) {//(scrollY + mHeight / 2) / mHeight > mStartScreen
                        mState = State.ToPre//翻上一页
                    }
                    //TODO 假定getScroll是一直在记录  （一次滑动事件——按下去——滑动——弹起来）
                    //TODO 这里不论上滑下滑，对getScrollY() 与mStartScreen 的处理，都是很精彩的！
                    else if (yVelocity > standerSpeed //自主修正了一下，  正的y速度，  才应该是  向下在滑动呀
                            //TODO 假定getScroll是一直在记录  （一次滑动事件——按下去——滑动——弹起来）
                            || (scrollY / mHeight + 0.5).toInt() >= (mStartScreen + 1)) {//TODO 这里的数学取值，是-1
                        mState = State.ToNext //翻下一页
                    } else {
                        mState = State.Normal //没有翻页情况
                    }

                    changeByState(yVelocity)


                }

                mVelocityTracker?.recycle()//资源清理。
                mVelocityTracker = null
            }
        }
        return super.onTouchEvent(event)
    }

    //RecyCle是有  循环的意思吗？  滚动的意思？   回收利用的意思？
    private fun recycleMove(realDelta: Float) {
        var delta = realDelta * mHeight  //测量到的整个 ViewGroup的高度 ，  乘以变化的y值（基于何者而变化？）
        delta = delta / mResistanceForce //除以阻力系数  （如果阻力系数是  12，  那么除以12倍）


        if (Math.abs(delta) > (mHeight / 4)) {//如果 除以滚动系数后  ，大于   整个ViewGroup高度的1/4，则  停止
            return
        }

        //ScrollTo 滑动到  某绝对的位置。    ScrollBy  相对滑动到  给出的相对量
        scrollBy(0, delta.toInt()) //相对滑动一截。  从上往下滑动  除以滚动系数后   的 delta

        if (scrollY < 5 && mStartScreen != 0) {//如果  滑动的纵向距离（这又是哪里的一个纵向距离？？？）  < 5  而且，不是在child中首个Screen的话
            addPre()
            scrollBy(0, mHeight)//滑动，整个ViewGroup的  1/1完全高度。（向下的滑动）
        } else if (scrollY > ((childCount - 1) * mHeight - 5)) {//TODO  ？？？？ 大于child总数的减1数量。大于这么多的乘以一个ViewGroup的高度。再这个总高度减去5
            addNext()
            scrollBy(0, -mHeight)//向上滑动 整个ViewGroup 的 1/1完全高度
        }

    }


    private fun addPre() {//这里不仅仅是往前面添加一个前置Item的意思
        //TODO 完整意思是————把最后面的一个Item，重新移动到第一个Item上面去。  然后整个栈，向上移动一位。（画布也同时移一位。实现起来，效果就是，像那种三级联动的密码锁一样）
        mCurrentScreen = (mCurrentScreen - 1 + childCount) % childCount//意思，似乎和   mCurrentScreen = mCurrentScreen - 1 一样 //其实，后来经过细细细想，  mCurrentScreen为一个循环范围内的值。  如  -1 + 3    mod  3  ,那么结果就是2  从0-1-2 之间，变化
        var view = getChildAt(childCount - 1)//取出倒数第一个的View的复制品
        removeViewAt(childCount - 1)//取出掉  倒数第一个的原来模型
        addView(view, 0)//把之前复制出来的模型，插入到  第一位（索引0的位置）
        mStereoListener?.toPre(mCurrentScreen)  //TODO  交给它去做点什么？？？  在翻到了前一页之后  ？？？

    }

    private fun addNext() {//这里不仅仅是往后面添加一个后置Item的意思
        //TODO 完整意思是————把最千米那的一个Item，重新移动到最最后一个Item上面去。  然后整个栈，向下的位置移动一位。（画布也同时移一位。实现起来，效果就是，像那种三级联动的密码锁一样）
        //向下波动表盘
        mCurrentScreen = (mCurrentScreen + 1 + childCount) % childCount //同上。  //同时，这里似乎。。。mCurrentScreen，也有一个轮回的意思
        //TODO 负数的取余究竟是多少 ？？？？？
        //TODO 负数取余  我有一套自己的理论  ——  被除数  取余  除数。  不管被除数  正负  ， 也不管除数  正负。    以除数为步长，从0步进，  步数可以为负数，  不超过被除数，  最后与被除数之间的差  为  余数  ，步数（可为负）为商
        // - 1 整除  3   ，  等于   商 0   余   -1          66666666
        // 照这样看   + childCount   在   条件是   mCurrentScreen + 1  的时候，也可加可不加(结果都一样)

        var view = getChildAt(0)//原理同上
        removeViewAt(0)//原理同上
        addView(view, childCount - 1)//原理同上
        mStereoListener?.toNext(mCurrentScreen)//TODO  疑问同上 同上

    }

    //名为  根据state枚举值，进行变化
    private fun changeByState(yVelocity: Float) {//TODO  但实际上是做什么的呢？
        alreadyAdd = 0   //重置，当滑动多页时，  当前页数，已经添加的数量（滑动一屏，添加一页）

        if (scrollY != mHeight) {//当滑动距离不是那种非常巧合，刚好等于   测绘的一屏的高度的时候
            //TODO 但是这个判断条件是做什么的呢？？？

            when (mState) {//分别读取状态22
            //TODO  然后状态里的操作，封装在方法之内  又是干嘛的呢呢？？？？？
                State.Normal -> {
                    toNormalAction()
                }
                State.ToPre -> {
                    toPreAction(yVelocity)
                }
                State.ToNext -> {
                    toNextAction(yVelocity)
                }
            }

            invalidate()

        }

    }

    private fun toNormalAction() {//当State = Normal 时进行的操作 ——————————————
        var startY: Int
        var delta: Int
        var duration: Int

        mState = State.Normal  //这句，  重新设置 Normal是为了  防止异步时被转换？？？？（？）
        addCount = 0  //添加的数量为 0  ？？？？  还是已添加的数量为 0 ？？？？
        startY = scrollY
        delta = mHeight * mStartScreen - scrollY //变化，等于   每一屏高度  乘以屏数，  然后减去  已经滑动的Y的坐标？？？  是指即将发生的delta ，  还是已经在原有基础上变化的delta？？？？
        duration = (Math.abs(delta) * 4)   //经过目力估算，  delta确实是  还需要也就是即将发生的delta（接下来的动画的将要的变化值）
        //此处  也即是 ———— 250个像素点     *4后  消耗一秒的 duration时间   ————很清楚明白了吧  就是  *4 /1000 个时间消耗。    第一次看  也是很巧妙的操作


    }

    private fun toPreAction(yVelocity: Float) {//当State = ToPre ，往前翻一页时的操作 ——————————————
        var startY: Int
        var delta: Int
        var duration: Int

        mState = State.ToPre //同上
        addPre() // 调用之前的
        //TODO 完整意思是————把最后面的一个Item，重新移动到第一个Item上面去。  然后整个栈，向上移动一位。（画布也同时移一位。实现起来，效果就是，像那种三级联动的密码锁一样）
        //的方法

        var flingSpeedCount: Int = 0
        //三元运算符  大于0，则取其本身值。  小于或等于0，则取0值
        (yVelocity - standerSpeed).takeIf { it > 0 }
                ?.let { flingSpeedCount = it.toInt(); return@let true }
                ?: false.let { flingSpeedCount = 0;return@let false }
        //TODO 简单说来——————这里就是求正在往上面滑的实时速度——————但是把最小值设置为  2000（ 2000- 2000 = 0 ），根据 2000 来做一个基准。 ————- 即  低于2000的速度，  都视为同一个标准。

        addCount = flingSpeedCount / flingSpeed + 1
        //TODO 这里是，根据实时的滑动速度——————去求几倍速率——————  0---3999的速度，都视为  一屏的量（即这个范围之内，翻转一屏——）  无疑这个是很有创造性的操作！！！！
        //0-3999 同一档。
        // 4000-5999 一档。
        // 6000-7999 一档。
        // 8000-9999 一档。
        // 10000...  档档档档档。
        startY = scrollY + mHeight //TODO 在已经确认了   ——是向上滑动，  是向上已经滑动了的情况下。
        //TODO 又因该方法本身，是在  Action_UP 中调用  ———— so  其实之前已经滑动了的————
        //TODO  意思即是   startY   后面 提到的  scrollY这个值，  必定是  已经滑出了一定的值  ，也就是  startY 这个值， 基本上可以  看作是  可能超出一屏范围（带来真正的滑动的值）

        setScrollY(startY)  //TODO 和  scrollTo（x，y）的作用  ————是一样的！ 不要被迷惑了！


        //新句
        delta = startY + (mStartScreen - addCount) * mHeight
        //TODO 原句 delta = startY + (mStartScreen - addCount + 1) * mHeight
        //TODO startY是负值  ，  mStartScreen是正值    如果addCount = 0 的话。。。。
        //TODO 那么 mStartScreen  + 1   ，这个是什么意思 ？？？？   我觉得有问题

        //TODO 那么这下，就应该好了吧？
        //默认一屏——————  起始的高度——————mHeight 加上负数的 scrollY
        // 现在的页面的序数（Screen ——>是第几屏）  然后负数的—————— add几个屏 （addCount）  ，就增加几个屏的高度总和的位移
        //大致如此  ？？？

        duration = (Math.abs(delta) * 3)//经过查证分析。  这里下面startScroll 中，第五参数的 单位默认是ms（1/1000秒）
        //so  这里就是   移动的像素  *3 /1000  。  每333个像素点，   将会利用  1秒（1000毫秒）的滚动时间——1000个像素点，花3秒时间滚动。


        mScroller.startScroll(0, startY, 0, delta, duration)//看来我猜对了    从startY也就是初始记录的滑动位置。
        //滑动到   delta也就是  计算屏数差值后   将要即将发生的delta  值。  duration  是根据    250像素点/秒  速度  计算出的  消耗时间。

        //TODO 此处，可明显见到   delta包含了  对  startY  的叠加。——————大致如此

        addCount--  //——————————代码好像写串了
        //TODO addCount -- 的意思  是什么？是什么？？是什么？？？   是为了 未来的下一步作准备吗？   花多个不同的方法，  来一次次消耗  addCount  ，直到消耗完毕吗？？？


    }

    private fun toNextAction(yVelocity: Float) {//当State = ToNext ，向后继续翻一页时的操作 ——————————————
        var startY: Int
        var delta: Int
        var duration: Int

        mState = State.ToNext//同上
        addNext()//同上

        var flingSpeedCount: Int = 0 //同上
        (Math.abs(yVelocity) - standerSpeed).takeIf { it > 0 }
                ?.let { flingSpeedCount = it.toInt(); return@let true }
                ?: false.let { flingSpeedCount = 0;return@let false }
        //TODO 同上，只是多了一个绝对值——————————？？？？？？？  为什么和我预料的刚好相反？【已弄清】——————并不相反啊。  是往上滑动的呀。 往上滑，显示出下面的条目

        addCount = flingSpeedCount / flingSpeed + 1//同上

        startY = scrollY - mHeight  //TODO 滑动位置  减去   Height ————> 说明这里的滑动， 既有可能是从   当前一屏   的底部Bottom  开始的。

        setScrollY(startY)//同上

        //此时   startY 为负数
        //mHeight 为正数
        //mStartScreen为非负整数。


        delta = mHeight * mStartScreen - startY + (addCount - 1) * mHeight
        //TODO 此处 ， startY 可能本身就是一个   极大值？？？？——————从上瀑布一直往下，  一直到快到底部的  startY？？？
        //TODO 有别的解释么？？？ 这条看上去还稍微合理一点？？？
        Log.i("KotlinView", "向后滑一页  startY: $startY   \n   vVelocity:$yVelocity   \n    delta:$delta    \n    scrollY:$scrollY       addCount:$addCount")

        duration = Math.abs(delta) * 3//同上

        mScroller.startScroll(0, startY, 0, delta, duration)//TODO 这里的这句意思  我真的真的是搞不懂了。。。。  莫非delta还会是个负值？？？   负很多的值？？？？
        //TODO 真的是搞不懂了 。。。

        addCount--  //同上，怀疑同上

    }

    override fun computeScroll() {
        //如果正在滑动    那么设置为true
        if (mScroller.computeScrollOffset()) { //当  滑动类滑动并没有结束的时候，进入这个条件内部语句。
            when (mState) {
                State.ToPre -> {
                    //滑动到       mScroll的  （目前X值，  Y值加上  已添加屏数的总高  为Y）这个位置
                    scrollTo(mScroller.currX, mScroller.currY + mHeight * alreadyAdd)
                    if (scrollY < (mHeight + 2)
                            && addCount > 0) {//添加数仍比0大      而且，当前滑动到的位置Y，比一屏高度+2 的值，要小时 。   （小2 可能是设定了一个极小值？？？  给了一点点毛边的判断？）
                        //TODO 还是什么其余的原因呢？

                        isAdding = true //TODO 看这个的隐喻，是已经正式加入了Adding状态了？  开始往里面添加ITem了？
                        addPre() //同上
                        alreadyAdd++
                        addCount--  //同上  ——————>这里似乎暗示，是一个循环操作
                    }
                }

                State.ToNext -> {
                    scrollTo(mScroller.currX, mScroller.currY - mHeight * alreadyAdd)//TODO  这里似乎读懂了一个小暗示。如果是往上拉，要看下面的条目的话。下面的屏
                    //TODO 那么很明显，整个  ViewGroup会把  最下面的Item一屏清掉， 然后添加到最上面第一个Item的一屏的前面（这也是  addNext（）里面做的）
                    //TODO 也是 作者的核心思维  做的|
                    //TODO 如果这个读懂暗示是对的，  那么整个思路是不是清晰了很多？？

                    if (scrollY > mHeight
                            && addCount > 0) {//如果  上面那个条件语句  对 +2 这个值的猜测是对的  ，那么这里没有了 +2 应该怎么解释 ？？？
                        isAdding = true //同上
                        addNext() //同上
                        alreadyAdd++ //同上
                        addCount-- //同上
                    }
                }

                State.Normal -> {
                    scrollTo(mScroller.currX, mScroller.currY)//那么久滑到  滑动类本身的位置吧   ————————>但是这里，到底是停在原位呢  还是原本没有滑动，现在滑动到了指定地点呢？？
                }
            }
            postInvalidate()//三分支任一分支执行完毕，刷新页面
        }
        if (mScroller.isFinished) {//如果已经滑动完毕（全部滑动任务已完成）    已添加 ,  待添加 ,  清零 ;
            alreadyAdd = 0
            addCount = 0
        }
    }


    override fun dispatchDraw(canvas: Canvas) {//TODO 看这名字  好像叫  分发绘制？（意思是给子类们绘制？？？把大块拆分了，一块块给子类去绘制？？？？？）
        if (!isAdding
                && isCan3D) {//如果 ， 已经添加完毕  ————  并且  是能够3D绘制的。
            for (i in 0..(childCount - 1)) {
                drawScreen(canvas, i, getDrawingTime())//TODO 这里的drawingTime 是个什么东西？？？？提示还是  View的官方 Api
                //大意是  ——————绘制时的系统实时时间。  类似于  System.clock.currentTime（）
            }
        } else {
            isAdding = false //满足不在添加的过程中   和  不能够3D绘制，  这两个条件中任一者  （满足第一个条件有什么意义吗？）
            super.dispatchDraw(canvas)//不执行 自身的   绘制  Screen操作。   执行父类的默认的分发 Draw操作。
        }
    }


    private fun drawScreen(canvas: Canvas, i: Int, t_DrawingTime: Long) {
        var curScreenY = mHeight * i  //i 是当前的child 序号。  也就是说——  每一个child视作一样高。。。。总共到目前 第i个child时，  总共的ScreenY，则是每一屏的高度  乘以  i  屏数。
        if ((scrollY + mHeight) < curScreenY) {//如果当前计算的  ， 理应的理论高度，大于滑动+ 本来一屏的高度
            //TODO 这里我其实猜出了暗示的意思。  就是前面小于  scrollY+ 至少1屏 的高度的，都予以绘制。 ——————而但凡超出的，  都打断跳过绘制——————也就是说没有绘制
            //TODO 而这里经过的遍历循环，  其实内部经过了大量的筛选——只有满足  在滑动范围和  需要的滑动范围内   的， 才会留存下来
            return
        }
        if (curScreenY < scrollY - mHeight) {
            //TODO 逻辑同上
            //TODO 但这里更复杂了
            //TODO 我只能靠猜。——————这里是说  在以scrollY 为基准， 上下两个 mHeight  即上下的接触的两屏  之内 吗？（上个条件和这个条件共同组成）
            return
        }
        //经历了上面的筛选后
        var centerX: Float = (mWidth / 2).toFloat()  //中心点的坐标   成员变量X的一半
        var centerY: Float = takeIf { scrollY > curScreenY }
                ?.let { return@let (curScreenY + mHeight).toFloat() }
                ?: false.let { return@let curScreenY.toFloat() }//TODO 这里的  curScreenY的算法到底是什么
        //TODO  ？是怎么和scrollY进行交互的？？？  这点仍然是搞不清楚
        //TODO 运用了三元运算符
        //TODO 仍是基于curScreenY 计算，  区别是   就是本身值  还是加一屏  (mHeight)     （  但curScreenY又是怎么来的？？？）

        //TODO 当看到mAngel时，  我知道  ————Camera的3D 绘制来了，  很大概率上  也会伴随着  Matrix ，  以及两者的绘制（刚刚学过的内容）
        var degree: Float = (mAngle * (scrollY - curScreenY) / mHeight)
        //TODO 翻转的角度————————>经目测   是   滑动的差值——————>然后除以一整个 一屏的高度——————比如一屏高200像素，  而滑动的差值是 600 ，则说明要翻转3个屏
        //TODO 而mAngel代表的意思就是  一个屏  就mAngel角度  ————>那么 三个屏，就意味着  ，翻转三个屏  ，并且翻转   270 个  角度
        if (degree > 90 || degree < -90) {
            //TODO 意思是说，  超过正 90度——内翻  ，或超过负 90 度 （外翻）的话，  都会被筛选出去？？？
            //TODO 如果这样的话，  需要一个  多屏翻转的迭代递归分解机制？  这里有把多个翻转动作，分隔成小于正90度的  操作吗 ？？
            return
        }

        canvas.save() //预存一下画布

        mCamera.save()//预存一下3D的摄影头
        mCamera.rotateX(degree)//环绕 x 轴  旋转角度  ——————> 左手坐标系  法  ——> 就等于前后翻转
        mCamera.getMatrix(mMatrix) //把当前 3D Camera的内置矩阵，取出来  以待后面转换时用   （3D转2D的步骤）
        mCamera.restore()   //回到先前的存档点，   把所有设置改动  还原成功

        mMatrix.preTranslate(-centerX, -centerY)//基本思路  就是所说的  把图片的矩阵  移动到中心来。  作完处理  ，再移放回去
        mMatrix.postTranslate(centerX, centerY)//TODO 本意自然是移放回来。此处的疑问在于，————————前乘后乘，规范吗？？
        //TODO 后来细细一想，是规范的。   因为中间一大坨，都是在  camera.getMatrix()  里面得到的，不管进行了多少复杂的操作。  都凝缩成了  一张    表。同一张表。

        canvas.concat(mMatrix)//此句的作用在网上查了。  即，将Matrix中的坐标系转换，应用于 Canvas上，所有的已存在元素

        drawChild(canvas, getChildAt(i), t_DrawingTime)//TODO 在画布上————特定的指定时间，绘制————子类子视图（子类子视图，交给它们自己的View，的onDraw方法，它们自己去定义）

        canvas.restore() //画布——————回复到  进行复杂操作之前的状态。
        //TODO 这样倒是有一个好处——————频繁的绘制子视图的话    ，也能够使画布  保持原初的状态


        //TODO 好像复杂的方法，  到这个方法就为止了
    }


    fun setStartScreen(startScreen: Int): StereoView {//设置初始化时第一页第一屏展示的页面
        if (startScreen <= 0
                || (startScreen >= childCount - 1)) {
            throw  IndexOutOfBoundsException("startScreen初始视图，不能是子控件组中的第一个或最后一个")
            //此处抛出异常  抛出一个异常
        }
        //并非确定总屏数。  总屏数  是在  ViewGroup 实例，  或者  xml格式里面写死的。
        this.mStartScreen = startScreen //初始化  最开始，是从显示第几屏  （初始化时，所处的child位置）
        this.mCurrentScreen = startScreen //初始化  当前所处的屏数（当前位置第几屏）
        return this  //返回自身， 我估计  这是作者为了链式编程
    }

    fun setResistance(resistance: Float): StereoView {//传入阻力系数  —— 阻力系数，将用于之后。变化的滚动时，作为被除数。（-1次方指数函数）
        this.mResistanceForce = resistance
        return this//同上
    }

    fun setInterpolator(interpolator: Interpolator): StereoView {//传入  插值器  参数  （用于  设置  一个变化曲线 —————— 将原本的直线，变缓或变陡  我猜测好处在于  通用性  比较强   ）
        mScroller = Scroller(mContext, interpolator)
        //TODO  原来，在这里，Scroller的逐渐翻滚减速效果，不是Scroller内部设置控制的，也不是在draw的同时，我们手动控制的
        //TODO Resistance这个参数也没有直接控制动画中细节的减速效果，只是把滑动的长短，进行了缩短。
        //TODO 全部是由  Interpolator 插值器，这个系统给了默认十几种效果，但是非常具有普适效果，能够实现大多数的变化曲线
        //TODO 通过这样一个通用函数，来进行实现的   （而且  它还提供了自定义的详细功能，除开已有大部分效果，还可以定制某个特殊细节）

        return this//同上
    }


    fun setAngle(angle: Int): StereoView {
        this.mAngle = 180F - angle  //TODO 话说这里为什么？？？ 要翻转一下？？？
        //TODO 翻转一下的，我查看了前面源码   翻转后的值，是直接用在了代码里的    这是在做什么呢？  翻转了镜像  ？？？  内外空间翻转  ？？？
        return this //同上
    }

    fun setCan3D(can3D: Boolean): StereoView {
        this.isCan3D = can3D //TODO 设置变量————是否去支持3D绘制——————3D绘制展现时，会展现一个3D的效果
        //TODO 似乎  ————没有3D绘制，  从样式上看 ——就是平庙效果。  从代码上看——
        // TODO 就是在绘制子类时，  跳过Stereo本控件 的复杂绘制效果，  直接进入  默认的dispatchDraw(分发给子类绘制)事件（采用默认的绘制————
        // TODO 那么就意味着 drawScreen() 方法，是【核心中的核心】）
        return this
    }


    fun setItem(itemId: Int): StereoView {//瞬间跳转到  指定的第几屏（并且不跳过中间的  几屏的动画效果。  依次滑动过去，  依次全部显示动画效果）。
        //TODO 这个方法——————我冥冥之中，感觉对于揭示滚动原理的核心有着极重要的作用
        //TODO 即——，这个方法所涉及的方法    ————都是滚动原理中，最核心的方法

        Log.i("KotlinView", "之前的Screen屏数是 $mCurrentScreen")

        if (!mScroller.isFinished) {//如果点击了这一个按钮时，滚动效果还没结束
            mScroller.abortAnimation()//立即停止动画
            //TODO 但和后面标注的  ————“强制完成”——有什么关系呢？ ——莫非滑动数据都是瞬间完成，剩下的只有动画效果？？
            //TODO 此处发现，————不是如作者所想的  这里的abortAnimation  是有明显BUG的
        }

        if (itemId < 0
                || itemId > (childCount - 1)) {
            throw IndexOutOfBoundsException("Item的数目小于0，或者大于了总Count的索引！")
        }

        if (itemId > mCurrentScreen) {//比现有的屏数更大更下，往下翻页
            toNextAction((-standerSpeed - flingSpeed * (itemId - mCurrentScreen - 1)).toFloat())//传入  现有的速度系数（触摸的Move瞬间速度）
            //TODO 此处当为速度负数，无疑。因为模拟的是，手指向上滑动，带来速度，的时刻。
            //TODO 速度的值，在负数的前提下，  是基准速度， 加上
            //TODO 如果滑动一屏，基准速度   （1 - 1）*flingSpeed  (flingSpeed是标准的冲撞速度)
            //TODO 如果滑动二屏，是   基准速度   +  1 * flingSpeed  （其实就值而言，可以等效于  2000 + 800 ）
        }

        if (itemId < mCurrentScreen) {//比现有的屏数更小更上，往上翻页
            toPreAction((standerSpeed + flingSpeed * (mCurrentScreen - itemId - 1)).toFloat())//同上
            //TODO 此处为速度正数。因为模拟的是  手指向下滑动，向下的速度，是y为正数  的时刻。
            //其余同上
        }

        Log.i("KotlinView", "之后的Screen屏数是$mCurrentScreen， 之后的scrollY 滑动Y值坐标（滑至） 为 $scrollY")
        return this //同上

    }

    fun toPre(): StereoView {  //向前向上翻一页。模拟手指向下滑动。与当前屏间距仅为 一 屏。
        //TODO  其他操作，简直和  setItem一模一样！！ 犯的错误，也和  setItem一模一样！！ 只是itemId参数定死的  一模一样！！
        if (!mScroller.isFinished) {
            mScroller.abortAnimation()
        }
        toPreAction(standerSpeed.toFloat())
        return this
    }


    fun toNext(): StereoView {//向后向下翻一页。模拟手指向上滑动。与当前屏间距仅为 一 屏。
        //同上
        if (!mScroller.isFinished) {
            mScroller.abortAnimation()
        }
        toNextAction((-standerSpeed).toFloat())
        return this
    }

    fun setStereoListener(listener: StereoListener) {//手动设置  一个 包含toPre 和toNext 操作的已实现接口
        //方便进行     自定义配置管理  （甚至可以考虑  ，加入函数式编程。——————那是以后的事  了）
        this.mStereoListener = listener
    }

    interface StereoListener {
        fun toPre(currentScreen: Int)

        fun toNext(currentScreen: Int)

    }

    enum class State {//枚举原来是这样用的
    Normal,
        ToPre, ToNext
    }

}