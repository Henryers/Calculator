package com.example.calculator

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.util.Locale
import java.util.Stack

class GenerateCalculator : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.generate_calculator)

        val btnReturn: Button = findViewById(R.id.btnReturn)
        // 设置按钮点击事件
        btnReturn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val etExpressionCount: EditText = findViewById(R.id.etExpressionCount)  // 输入要生成的式子数量
        val etOperatorCount: EditText = findViewById(R.id.etOperatorCount)  // 输入运算符数量
        val etOperators: EditText = findViewById(R.id.etOperators)  // 选择运算符（加、减、乘、除的组合）

        val btnGenerate: Button = findViewById(R.id.btnGenerate)

        btnGenerate.setOnClickListener {
            // 表达式数量
            val expressionCountStr = etExpressionCount.text.toString()
            // 表达式中数字个数
            val operatorCountStr = etOperatorCount.text.toString()
            // 操作符合集，如 +,-,/
            val operatorsStr = etOperators.text.toString()

            // 检查输入是否为空
            if (expressionCountStr.isEmpty() || operatorCountStr.isEmpty() || operatorsStr.isEmpty()) {
                Toast.makeText(this, "请输入有效的数量和运算符", Toast.LENGTH_SHORT).show()
            } else {
                val expressionCount = expressionCountStr.toInt()
                val operatorCount = operatorCountStr.toInt()

                // 将用户输入的运算符分割为列表
                val operators = operatorsStr.split(",")

                // 生成表达式并保存文件
                val (expressions, answers) = generateExpressionsAndAnswers(expressionCount, operatorCount, operators)
                saveToFiles(expressions, answers)
                Toast.makeText(this, "文件已保存", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 优先级定义
    fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            else -> 0
        }
    }

    // 生成随机运算数
    private fun generateOperand(): String {
        val operand = (Math.random() * 100) - 50  // 生成 -50 到 50 之间的浮点数
        return String.format(Locale.US, "%.2f", operand)
    }

    // 运算符计算
    private fun applyOperation(op: String, a: Double, b: Double): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else throw UnsupportedOperationException("除数不能为零")
            else -> 0.0
        }
    }

    // 生成随机表达式
//    private fun generateExpression(operatorsCount: Int, operators: List<String>): String {
//        var expression = generateOperand()
//        for (i in 0 until operatorsCount) {
//            val operator = operators.random()
//            val operand = generateOperand()
//            expression += " $operator $operand"
//        }
//        return expression
//    }

    /**
     * 生成随机表达式，包含括号
     * operatorsCount：数字数量
     * operators：操作符列表
     */
    private fun generateExpression(operatorsCount: Int, operators: List<String>): String {
        var expression = ""
        var openBrackets = 0 // 用于追踪未闭合的左括号数
        var flag = 0

        for (i in 1 until operatorsCount) {
            flag = 0

            // 随机决定是否插入左括号，在操作数之前插入
            if (Math.random() < 0.3 && openBrackets == 0) {
                expression += "( " // 在开头插入左括号
                openBrackets++
                flag = 1
            }

            // 添加操作数
            expression += generateOperand()

//            // 随机决定是否插入左括号，在操作数之后插入
//            if (Math.random() < 0.3 && openBrackets == 0) {
//                expression = "( $expression"
//                openBrackets++
//            }

            // 随机决定是否插入右括号，并确保左括号数量足够，且当前操作符之前不能有左括号，避免 (x)的情况出现
            if (Math.random() < 0.3 && openBrackets > 0 && flag == 0) {
                expression += " )"
                openBrackets--
            }

            // 添加随机运算符
            val operator = operators.random()
            expression += " $operator "

        }

        // 末尾添加操作数
        expression += generateOperand()

        // 随机决定是否插入右括号，并确保左括号数量足够
        if (Math.random() < 0.3 && openBrackets > 0) {
            expression += " )"
            openBrackets--
        }

        // 如果还有未闭合的左括号，自动关闭
        while (openBrackets > 0) {
            expression += " )"
            openBrackets--
        }

        Log.e("E", expression)
        return expression
    }

    // 计算表达式
    fun evaluateExpression(expression: String): Double {
        val values = Stack<Double>() // 操作数栈
        val ops = Stack<String>()    // 操作符栈

        val tokens = expression.split(" ")

        for (token in tokens) {
            when {
                // 当前 token 是数字，直接压入值栈
                token.toDoubleOrNull() != null -> values.push(token.toDouble())

                // 当前 token 是左括号
                token == "(" -> ops.push(token)

                // 当前 token 是右括号
                token == ")" -> {
                    // 确保 ops 栈非空，然后继续弹出并执行操作
                    while (ops.isNotEmpty() && ops.peek() != "(") {
                        if (values.size < 2) {
                            throw IllegalArgumentException("表达式不合法，操作数不足")
                        }
                        val op = ops.pop()
                        val b = values.pop()
                        val a = values.pop()
                        values.push(applyOperation(op, a, b))
                    }
                    if (ops.isNotEmpty() && ops.peek() == "(") {
                        ops.pop() // 去除左括号
                    }
                }

                // 当前 token 是运算符
                token in listOf("+", "-", "*", "/") -> {
                    // 确保运算符栈不为空并且优先级高
                    while (ops.isNotEmpty() && precedence(ops.peek()) >= precedence(token)) {
                        if (values.size < 2) {
                            throw IllegalArgumentException("表达式不合法，操作数不足")
                        }
                        val op = ops.pop()
                        val b = values.pop()
                        val a = values.pop()
                        values.push(applyOperation(op, a, b))
                    }
                    // 将当前运算符压入栈
                    ops.push(token)
                }
            }
        }

        // 处理剩余的运算符
        while (ops.isNotEmpty()) {
            if (values.size < 2) {
                Log.e("ExpressionError", "没操作数？" + values)
                Log.e("ExpressionError", expression)
                continue
//                throw IllegalArgumentException("表达式不合法，操作数不足")
            }
            val op = ops.pop()
            val b = values.pop()
            val a = values.pop()
            values.push(applyOperation(op, a, b))
        }

        return if (values.isNotEmpty()) values.pop() else throw IllegalArgumentException("表达式不合法")
    }

    // 生成 count条 表达式和答案
    private fun generateExpressionsAndAnswers(count: Int, operatorsCount: Int, operators: List<String>): Pair<List<String>, List<Double>> {
        val expressions = mutableListOf<String>()
        val answers = mutableListOf<Double>()

        for (i in 0 until count) {
            // 生成一条表达式和对应的答案，然后各自加到外层的大列表里
            val expression = generateExpression(operatorsCount, operators)
            val answer = evaluateExpression(expression)
            expressions.add(expression)
            answers.add(answer)
        }

        return Pair(expressions, answers)
    }

    // 保存表达式和答案到文件
    private fun saveToFiles(expressions: List<String>, answers: List<Double>) {
        val externalStorageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        println("什么目录？" + externalStorageDir)
        // 保存表达式到文本文件
        val expressionsFile = File(externalStorageDir, "expressions.txt")
        val expressionsWriter = FileWriter(expressionsFile)
        expressionsWriter.use { writer ->
            expressions.forEach { writer.write("$it\n") }
        }

        // 保存正确答案到另一个文件
        val answersFile = File(externalStorageDir, "answers.txt")
        val answersWriter = FileWriter(answersFile)
        answersWriter.use { writer ->
            answers.forEach { writer.write("$it\n") }
        }
    }
}
