package com.hammelplus.downloader

import android.app.Activity
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var urlInput: EditText
    private lateinit var status: TextView
    private var queuedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 34, 28, 24)
            setBackgroundColor(0xFFF4F7FB.toInt())
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 16)
        }

        val title = TextView(this).apply {
            text = "Abo Zahra Pro"
            textSize = 30f
            setTextColor(0xFF173A63.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 6)
        }
        val subtitle = TextView(this).apply {
            text = "مدير تحميلات شامل — بدون إعلانات"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(0xFF526579.toInt())
            setPadding(0, 0, 0, 24)
        }
        urlInput = EditText(this).apply {
            hint = "الصق رابط الملف هنا"
            textSize = 16f
            setSingleLine(false)
            minLines = 2
            setPadding(16, 12, 16, 12)
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        val pasteButton = Button(this).apply {
            text = "لصق الرابط"
            setOnClickListener { pasteFromClipboard() }
        }
        val downloadButton = Button(this).apply {
            text = "ابدأ التحميل"
            setOnClickListener { startDownload() }
        }
        val queueButton = Button(this).apply {
            text = "فتح التحميلات في أندرويد"
            setOnClickListener {
                try {
                    val intent = android.content.Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                    startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(this@MainActivity, "افتح تطبيق التنزيلات في جهازك", Toast.LENGTH_LONG).show()
                }
            }
        }
        status = TextView(this).apply {
            text = "جاهز للتحميل. يمكنك إضافة روابط متعددة واحدًا بعد الآخر."
            textSize = 14f
            setTextColor(0xFF263746.toInt())
            setPadding(4, 18, 4, 18)
        }
        val note = TextView(this).apply {
            text = "ملاحظة: النسخة الأولية تحمل الروابط المباشرة التي يسمح بها الموقع. بعض المواقع تتطلب تسجيل الدخول أو لا تسمح بالتنزيل المباشر. الاستكمال يعتمد على دعم الخادم ومدير التنزيلات في أندرويد."
            textSize = 13f
            setTextColor(0xFF687888.toInt())
            setPadding(4, 12, 4, 12)
        }

        content.addView(title)
        content.addView(subtitle)
        content.addView(urlInput, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 8 })
        content.addView(pasteButton)
        content.addView(downloadButton)
        content.addView(queueButton)
        content.addView(status)
        content.addView(note)
        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData? = clipboard.primaryClip
        val text = if (clip != null && clip.itemCount > 0) clip.getItemAt(0).coerceToText(this)?.toString() else null
        if (!text.isNullOrBlank()) {
            urlInput.setText(text.trim())
            Toast.makeText(this, "تم لصق الرابط", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "الحافظة فارغة", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startDownload() {
        val raw = urlInput.text.toString().trim()
        if (raw.isEmpty()) {
            urlInput.error = "الصق رابطًا أولًا"
            return
        }

        val uri = try { Uri.parse(raw) } catch (_: Exception) { null }
        if (uri == null || (uri.scheme != "https" && uri.scheme != "http") || uri.host.isNullOrBlank()) {
            urlInput.error = "اكتب رابطًا صحيحًا يبدأ بـ https:// أو http://"
            return
        }

        try {
            val request = DownloadManager.Request(uri)
                .setTitle(uri.lastPathSegment?.takeIf { it.isNotBlank() } ?: "ملف جديد")
                .setDescription("Abo Zahra Pro")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeFileName(uri.lastPathSegment))
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            queuedCount += 1
            status.text = "تم إرسال $queuedCount عملية تحميل إلى مدير التنزيلات في أندرويد. يمكنك إضافة رابط آخر."
            urlInput.text.clear()
            Toast.makeText(this, "بدأ التحميل", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر بدء التحميل: ${e.localizedMessage ?: "خطأ غير معروف"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun safeFileName(name: String?): String {
        val cleaned = (name ?: "download_file").substringAfterLast('/').replace(Regex("[\\\\/:*?\"<>|]"), "_")
        return cleaned.ifBlank { "download_file" }
    }
}
