package com.example.myapplicationtmppp.ui.scanner

import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplicationtmppp.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val imageUriString = intent.getStringExtra("IMAGE_PATH")
        val deepseekResponse = intent.getStringExtra("DEEPSEEK_RESPONSE")

        displayImage(imageUriString)
        processAndDisplayResponse(deepseekResponse)
    }

    private fun displayImage(uriString: String?) {
        uriString?.let {
            findViewById<ImageView>(R.id.imageViewResult).setImageURI(Uri.parse(it))
        }
    }

    private fun processAndDisplayResponse(response: String?) {
        val resultTextView = findViewById<TextView>(R.id.textViewResults).apply {
            movementMethod = ScrollingMovementMethod.getInstance()
            typeface = android.graphics.Typeface.MONOSPACE
        }

        try {
            response?.let {
                val jsonResponse = JSONObject(it)
                val content = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                val formattedJson = try {
                    formatJsonString(content)
                } catch (e: JSONException) {
                    "Invalid JSON format: ${e.message}\n\nRaw content:\n$content"
                }

                resultTextView.text = formattedJson
            } ?: run {
                resultTextView.text = "No response data available"
            }
        } catch (e: Exception) {
            resultTextView.text = "Error parsing response: ${e.message}\n\nRaw response:\n$response"
        }
    }

    private fun formatJsonString(jsonString: String): SpannableStringBuilder {
        return try {
            val json = JSONObject(jsonString)
            val formatted = StringBuilder()
            formatJson(json, formatted, 0)

            val colorSpan = ForegroundColorSpan(
                ContextCompat.getColor(this, R.color.json_highlight)
            )

            SpannableStringBuilder(formatted.toString()).apply {
                setSpan(colorSpan, 0, length, 0)
            }
        } catch (e: JSONException) {
            throw JSONException("Invalid JSON structure: ${e.message}")
        }
    }

    private fun formatJson(obj: Any?, result: StringBuilder, indentLevel: Int) {
        when (obj) {
            is JSONObject -> {
                result.append("{\n")
                val keys = obj.keys()
                var first = true
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (!first) result.append(",\n")
                    appendIndent(result, indentLevel + 1)
                    result.append("\"$key\": ")
                    formatJson(obj[key], result, indentLevel + 1)
                    first = false
                }
                result.append("\n")
                appendIndent(result, indentLevel)
                result.append("}")
            }
            is JSONArray -> {
                result.append("[\n")
                for (i in 0 until obj.length()) {
                    if (i > 0) result.append(",\n")
                    appendIndent(result, indentLevel + 1)
                    formatJson(obj[i], result, indentLevel + 1)
                }
                result.append("\n")
                appendIndent(result, indentLevel)
                result.append("]")
            }
            is String -> result.append("\"${obj}\"")
            else -> result.append(obj)
        }
    }

    private fun appendIndent(sb: StringBuilder, indentLevel: Int) {
        repeat(indentLevel * 4) { sb.append(' ') }
    }
}