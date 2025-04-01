package com.example.myapplicationtmppp.ui.game

import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R

class GamificationActivity : AppCompatActivity() {
    private lateinit var savingsGameManager: SavingsGameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamification)
        savingsGameManager = SavingsGameManager(this)

        intent.getStringExtra("NEW_BADGE_ID")?.let { badgeId ->
            highlightBadge(badgeId) // Metodă pentru a evidenția insigna
        }

        setupUI()
        loadProgress()
    }

    private fun highlightBadge(badgeId: String) {
        findViewById<LinearLayout>(R.id.containerBadges).let { container ->
            (0 until container.childCount).forEach { i ->
                val badgeView = container.getChildAt(i)
                if (badgeView.getTag(R.id.badge_id) == badgeId) {
                    // Aplică un efect vizual
                    badgeView.animate()
                        .scaleX(1.3f)
                        .scaleY(1.3f)
                        .setInterpolator(OvershootInterpolator())
                        .setDuration(500)
                        .start()
                }
            }
        }
    }

    private fun setupUI() {
        // Buton de închidere
        findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            finish()
        }

        // Buton de reset (pentru debug)
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            resetProgress()
        }
    }

    private fun loadProgress() {
        val progress = savingsGameManager.loadProgress()
        updateProgressUI(progress)
    }

    private fun updateProgressUI(progress: SavingsGameManager.UserProgress) {
        // Progres general
        findViewById<TextView>(R.id.tvTotalSaved).text =
            "Total economisit: ${"%.2f".format(progress.totalSaved)} LEI"
        findViewById<TextView>(R.id.tvLevel).text = "Nivel ${progress.level}"

        // ProgressBar pentru nivel următor
        val nextLevelXp = progress.level * 100
        val progressXp = (progress.totalSaved % 100).toInt()
        findViewById<ProgressBar>(R.id.progressBar).apply {
            max = 100
        }
        findViewById<TextView>(R.id.tvXp).text = "$progressXp/$nextLevelXp XP"

        // Insigne
        val badgesContainer = findViewById<LinearLayout>(R.id.containerBadges)
        badgesContainer.removeAllViews()
        savingsGameManager.getAllBadges().forEach { badge ->
            val badgeView = layoutInflater.inflate(R.layout.item_badge, badgesContainer, false).apply {
                // Setează tag-ul cu ID-ul badge-ului
                setTag(R.id.badge_id, badge.id) // Folosește ID-ul definit în ids.xml

                // Restul configurației...
                findViewById<ImageView>(R.id.ivBadge).setImageResource(badge.iconResId)
                findViewById<TextView>(R.id.tvBadgeName).text = badge.name
            }
            badgesContainer.addView(badgeView)
        }
    }

    private fun resetProgress() {
        savingsGameManager.saveProgress(SavingsGameManager.UserProgress())
        loadProgress()
        Toast.makeText(this, "Progres resetat", Toast.LENGTH_SHORT).show()
    }
}