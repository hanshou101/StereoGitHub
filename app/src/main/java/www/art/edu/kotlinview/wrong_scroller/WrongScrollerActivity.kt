package www.art.edu.kotlinview.wrong_scroller

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import www.art.edu.kotlinview.R

/**
 * Created by Administrator on 2017/7/25.
 */
class WrongScrollerActivity : Activity() {

    lateinit var layout: LinearLayout
    lateinit var scrollTo: Button
    lateinit var scrollBy: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_scroller)
        layout = findViewById(R.id.layout) as LinearLayout
        scrollTo = findViewById(R.id.scroll_to_btn) as Button
        scrollBy = findViewById(R.id.scroll_by_btn) as Button

        scrollTo.setOnClickListener { layout.scrollTo(-60,-100) }
        scrollBy.setOnClickListener { layout.scrollBy(-60,-100) }

    }

}