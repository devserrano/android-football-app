package mx.unam

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class StoryActivity : AppCompatActivity() {

    private var imagesArray = intArrayOf()
    private var currentIndex = 0

    private lateinit var storyImageView: ImageView
    private lateinit var progressContainer: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private val tiempoPorHistoria = 5000L

    private var animacionProgreso: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        storyImageView = findViewById(R.id.story_image_view)
        progressContainer = findViewById(R.id.progress_container)

        val btnClose = findViewById<ImageView>(R.id.btn_close_story)
        val clickLeft = findViewById<View>(R.id.click_left)
        val clickRight = findViewById<View>(R.id.click_right)

        imagesArray = intent.getIntArrayExtra("STORY_IMAGES") ?: intArrayOf()

        if (imagesArray.isNotEmpty()) {
            setupProgressBars()
            mostrarImagen(currentIndex)
            iniciarTemporizador()
        }

        clickLeft.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                mostrarImagen(currentIndex)
                iniciarTemporizador()
            }
        }

        clickRight.setOnClickListener {
            if (currentIndex < imagesArray.size - 1) {
                currentIndex++
                mostrarImagen(currentIndex)
                iniciarTemporizador()
            } else {
                finish()
            }
        }

        btnClose.setOnClickListener { finish() }
    }

    private fun iniciarTemporizador() {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            if (currentIndex < imagesArray.size - 1) {
                currentIndex++
                mostrarImagen(currentIndex)
                iniciarTemporizador()
            } else {
                finish()
            }
        }

        handler.postDelayed(runnable!!, tiempoPorHistoria)
    }

    private fun setupProgressBars() {
        progressContainer.weightSum = imagesArray.size.toFloat()
        progressContainer.removeAllViews()

        for (i in imagesArray.indices) {
            val pb = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            val params = LinearLayout.LayoutParams(0, 8, 1f)
            params.setMargins(6, 0, 6, 0)
            pb.layoutParams = params
            pb.max = 1000
            pb.progress = 0
            pb.progressTintList = ColorStateList.valueOf(Color.WHITE)
            pb.progressBackgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
            progressContainer.addView(pb)
        }
    }

    private fun mostrarImagen(index: Int) {
        storyImageView.setImageResource(imagesArray[index])

        animacionProgreso?.cancel()

        for (i in 0 until progressContainer.childCount) {
            val pb = progressContainer.getChildAt(i) as ProgressBar

            when {
                i < index -> pb.progress = 1000
                i > index -> pb.progress = 0
                i == index -> {
                    pb.progress = 0
                    animacionProgreso = ObjectAnimator.ofInt(pb, "progress", 0, 1000)
                    animacionProgreso?.duration = tiempoPorHistoria
                    animacionProgreso?.interpolator = LinearInterpolator()
                    animacionProgreso?.start()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable?.let { handler.removeCallbacks(it) }
        animacionProgreso?.cancel()
    }
}