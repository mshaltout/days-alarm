
package daysalarm.mahmoud.com

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var startDateText: TextView
    private lateinit var endDateText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var percentText: TextView

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startDateText = findViewById(R.id.startDate)
        endDateText = findViewById(R.id.endDate)
        progressBar = findViewById(R.id.progressBar)
        percentText = findViewById(R.id.percentText)

        findViewById<Button>(R.id.pickStartBtn).setOnClickListener {
            pickDate { date ->
                startDate = date
                startDateText.text = formatDate(date)
                calculateProgress()
            }
        }

        findViewById<Button>(R.id.pickEndBtn).setOnClickListener {
            pickDate { date ->
                endDate = date
                endDateText.text = formatDate(date)
                calculateProgress()
                scheduleDailyWorker()
            }
        }
    }

    private fun pickDate(callback: (Calendar) -> Unit) {
        val c = Calendar.getInstance()
        val dp = DatePickerDialog(this, { _, y, m, d ->
            val date = Calendar.getInstance()
            date.set(y, m, d)
            callback(date)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        dp.show()
    }

    private fun formatDate(cal: Calendar): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
    }

    private fun calculateProgress() {
        if (startDate != null && endDate != null) {
            val now = Calendar.getInstance().timeInMillis
            val total = endDate!!.timeInMillis - startDate!!.timeInMillis
            val passed = now - startDate!!.timeInMillis
            val percent = ((passed * 100.0) / total).toInt().coerceIn(0, 100)

            progressBar.progress = percent
            percentText.text = "$percent% Passed"
        }
    }

    private fun scheduleDailyWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dailyReminder", ExistingPeriodicWorkPolicy.REPLACE, workRequest
        )
    }
}
