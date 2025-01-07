package com.example.calculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CommonCalculator : AppCompatActivity() {
    private lateinit var displayText: TextView
    private var expression: String = ""
    private var lastOperator: String = ""
    private var result: Double = 0.0
    private var isResultDisplayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.common_calculator)

        val btnReturn: Button = findViewById(R.id.btnReturn)
        // 设置按钮点击事件
        btnReturn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        displayText = findViewById(R.id.display)

        val buttons = listOf(
            R.id.button_7, R.id.button_8, R.id.button_9, R.id.button_divide,
            R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_multiply,
            R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_minus,
            R.id.button_0, R.id.button_clear, R.id.button_equals, R.id.button_plus
        )

        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener { onButtonClick((it as Button).text.toString()) }
        }

    }

    private fun onButtonClick(value: String) {
        when (value) {
            "C" -> {
                expression = ""
                displayText.text = "0"
                result = 0.0
                lastOperator = ""
            }
            "=" -> {
                val calcResult = evaluateExpression(expression)
                if (calcResult == null){
                    displayText.text = "未知错误，检查一下式子吧~"
                    // 重新初始化，防止错误的式子影响后续输入
                    result = 0.0
                    expression = ""
                    lastOperator = ""
                    isResultDisplayed = false
                }else{
                    displayText.text = calcResult.toString()
                    result = calcResult
                    expression = calcResult.toString()
                    lastOperator = ""
                    isResultDisplayed = true
                }
            }
            else -> {
                if (isResultDisplayed && value in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")) {
                    expression = value // 清空之前的结果，重新开始
                    displayText.text = expression
                    isResultDisplayed = false
                } else {
                    // 目前有点多余，除非后面加别的按钮
                    if (value in listOf("+", "-", "*", "/")) {
                        lastOperator = value
                    }
                    expression += value
                    displayText.text = expression
                    isResultDisplayed = false
                }
            }
        }
    }

    // 对字符串式子进行运算操作
    private fun evaluateExpression(expression: String): Double? {
        return try {
            val operators = listOf('+', '-', '*', '/')
            var currentOperator = ' '
            var result = 0.0
            var currentNumber = ""

            for (i in expression.indices) {
                val char = expression[i]
                // 遍历到+-*/
                if (char in operators) {
                    if (char == '-' && (i == 0 || expression[i - 1] in operators)) {
                        // 如果减号在表达式开头或前一个字符是运算符，认为是负号
                        currentNumber += char
                    } else {
                        result = if (currentOperator == ' ') {
                            currentNumber.toDouble()
                        } else {
                            performOperation(result, currentNumber.toDouble(), currentOperator)
                        }
                        currentOperator = char
                        currentNumber = ""
                    }
                } else {
                    currentNumber += char  // 数字直接拼接字符上去
                }
            }
            // 处理最后一个数字
            performOperation(result, currentNumber.toDouble(), currentOperator)
        } catch (e: Exception) {
            null
        }
    }



    // 执行运算操作
    private fun performOperation(left: Double, right: Double, operator: Char): Double {
        return when (operator) {
            '+' -> left + right
            '-' -> left - right
            '*' -> left * right
            '/' -> left / right
            else -> right
        }
    }
}