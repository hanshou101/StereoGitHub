package www.art.edu.kotlinview

/**
 * Created by Administrator on 2017/7/17.
 */


fun main(args: Array<String>) {


    //测试三元运算符

    //返回  takeIf之前调用的对象（因为是invoke方法，所以必不为 空 ）

    var bool = 5.takeIf { it > 4 }
            ?.let { println("No Null No Null No Null"); return@let true }
            ?: false.let { println("Yes Null Yes Null Yes Null");return@let false }

    println("$bool")

    println("go next step")


    //其实还有一个讲究，第二运算符的  return 值，极有讲究。——>true和一切非空值  ——>null

    println(-0.9.toInt())//这个值在这里是  0

    var b = 2
    var c = 3
    var a :Int= 1.takeIf { b > c }?.let { return@let b }
            ?: false.let { return@let c }

    println("计算的结果是 a$c")

    //此处可见  ————>三元运算符还是能够   很平稳的运行的  return@let 语句， 也成功的达到了效果。
    //但是：要注意的是  最好把  var a 这种的  类型值  如Int  写上。  方便进行三元运算符  类型是否写对的检查。
}

