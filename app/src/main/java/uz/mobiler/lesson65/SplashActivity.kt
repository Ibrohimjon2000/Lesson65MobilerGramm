package uz.mobiler.lesson65

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import uz.mobiler.lesson65.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var zoom: Animation
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        zoom = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom)
        binding.image.startAnimation(zoom)

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}