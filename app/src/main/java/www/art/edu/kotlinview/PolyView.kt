package www.art.edu.kotlinview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View

/**
 * Created by Administrator on 2017/7/16.
 */
class PolyView : View {


    lateinit var myBitmap: Bitmap
    lateinit var myMatrix: Matrix


    constructor(context: Context) : super(context) {//父类构造 ，在此已返回。 //或者理解constructor没有返回值（或没有常规返回值）————

        initBitmapAndMeticx()

    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {// 父类构造，在此已返回。 //或者理解constructor没有返回值（或没有常规返回值）————

        //如何调用  无参或主参构造？

        initBitmapAndMeticx()

    }


    fun initBitmapAndMeticx() {
        myBitmap = BitmapFactory.decodeResource(resources, R.drawable.sierda)//获取图片

        myMatrix = Matrix()

        var src: FloatArray = floatArrayOf(
                0F, 0F,
                myBitmap.width.toFloat(), 0F,
                myBitmap.width.toFloat(), myBitmap.height.toFloat(),
                0F, myBitmap.height.toFloat()
        )//统一为了 FloatArray模式

        //图片在此处默认放大两倍（200%）

        var dst: FloatArray = floatArrayOf(

                0F, 0F,
                (myBitmap.width + 100).toFloat(), 200F,
                (myBitmap.width + 100).toFloat(), (myBitmap.height + 200).toFloat(),
                0F, (myBitmap.height + 200).toFloat()
        )//启动扭曲矩阵模式

        myMatrix.setPolyToPoly(src, 0, dst, 0, src.size shr 1)//向右  位运算  位移1位  （等效于  整除  除2）


        myMatrix.preScale(0.5F,0.5F)//此句放在poly后句才凑效。且Pre乘和Post乘 有 区别（和我预想的一致）。
        //这个位移的200高度。经过了 Scale 和  密度density的双重  拉缩。 由200 变为  40 （理论值  （pix））

    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.drawBitmap(myBitmap, myMatrix, null)//默认画笔。配置全部取默认值
    }


}