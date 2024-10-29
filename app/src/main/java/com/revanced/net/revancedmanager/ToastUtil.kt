import android.app.AlertDialog
import android.content.Context
import android.widget.TextView
import android.widget.Toast

object ToastUtil {

    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    fun showSmallToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        val toast = Toast.makeText(context, message, duration)
        val view = toast.view
        view?.let {
            val textView = it.findViewById<TextView>(android.R.id.message)
            textView?.textSize = 8f  // Set the desired text size here
        }

        toast.show()
    }

    fun showPopup(context: Context, title: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}