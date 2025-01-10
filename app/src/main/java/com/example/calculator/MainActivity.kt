package com.example.calculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()    // 沉浸式布局，全景延伸到最上方的通知栏
        setContentView(R.layout.activity_main)

        // 首页导航按钮
        val btnToCommon: Button = findViewById(R.id.btnToCommon)
        val btnToGenerate: Button = findViewById(R.id.btnToGenerate)
        // 设置按钮点击事件
        btnToCommon.setOnClickListener {
            val intent = Intent(this, CommonCalculator::class.java)
            startActivity(intent)
        }
        // 设置按钮点击事件
        btnToGenerate.setOnClickListener {
            val intent = Intent(this, GenerateCalculator::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}