package com.arsenii.fitnessapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

// Custom view that draws the bodyweight graph
class BodyweightGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dataPoints = mutableListOf<Pair<Date, Float>>()
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        linePaint.apply {
            color = Color.WHITE
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
        pointPaint.color = Color.WHITE
        textPaint.apply {
            color = Color.WHITE
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
        datePaint.apply {
            color = Color.WHITE
            textSize = 25f
            textAlign = Paint.Align.CENTER
        }
    }

    // Takes raw data and prepares it for drawing
    fun setData(data: Map<String, *>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dataPoints.clear()
        data.forEach { (dateStr, weight) ->
            try {
                val date = dateFormat.parse(dateStr)
                val weightFloat = weight.toString().toFloat()
                if (date != null) {
                    dataPoints.add(Pair(date, weightFloat))
                }
            } catch (e: Exception) { /* Ignore malformed data */ }
        }
        dataPoints.sortBy { it.first }
        invalidate() // Redraw the view
    }

    // Where the drawing happens
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (dataPoints.size) {
            0 -> canvas.drawText("No data available.", width / 2f, height / 2f, textPaint)
            1 -> drawSinglePoint(canvas)
            else -> drawFullGraph(canvas)
        }
    }

    // Draws one data point
    private fun drawSinglePoint(canvas: Canvas) {
        val point = dataPoints.first()
        val x = width / 2f
        val y = height / 2f

        canvas.drawCircle(x, y, 15f, pointPaint)

        val weightText = "${round(point.second * 10) / 10f} kg"
        canvas.drawText(weightText, x, y - 40f, textPaint)

        val dateText = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(point.first)
        canvas.drawText(dateText, x, y + 50f, datePaint)
    }

    // Draws the full graph
    private fun drawFullGraph(canvas: Canvas) {
        val padding = 80f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        val minWeight = dataPoints.minOf { it.second } - 5f
        val maxWeight = dataPoints.maxOf { it.second } + 5f
        val weightRange = if (maxWeight - minWeight == 0f) 1f else maxWeight - minWeight

        val xStep = chartWidth / (dataPoints.size - 1)
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

        for (i in dataPoints.indices) {
            val x = padding + i * xStep
            val y = padding + chartHeight * (1 - (dataPoints[i].second - minWeight) / weightRange)

            canvas.drawCircle(x, y, 10f, pointPaint)

            val weightText = "${round(dataPoints[i].second * 10) / 10f} kg"
            canvas.drawText(weightText, x, y - 30f, textPaint)

            val dateText = dateFormat.format(dataPoints[i].first)
            canvas.drawText(dateText, x, height - padding + 40f, datePaint)

            // Draws line to next point
            if (i < dataPoints.size - 1) {
                val nextX = padding + (i + 1) * xStep
                val nextY = padding + chartHeight * (1 - (dataPoints[i + 1].second - minWeight) / weightRange)
                canvas.drawLine(x, y, nextX, nextY, linePaint)
            }
        }
    }
}
