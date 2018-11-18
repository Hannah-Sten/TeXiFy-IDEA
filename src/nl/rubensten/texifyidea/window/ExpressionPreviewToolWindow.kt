package nl.rubensten.texifyidea.window


import com.intellij.openapi.wm.ToolWindow

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.Calendar

class ExpressionPreviewToolWindow(toolWindow: ToolWindow) {
    private val refreshToolWindowButton: JButton? = null
    private val hideToolWindowButton: JButton? = null
    private val currentDate: JLabel? = null
    private val currentTime: JLabel? = null
    private val timeZone: JLabel? = null
    val content: JPanel? = null

    init {
        hideToolWindowButton!!.addActionListener { toolWindow.hide(null) }
        refreshToolWindowButton!!.addActionListener { currentDateTime() }

        this.currentDateTime()
    }


    fun currentDateTime() {
        // Get current date and time
        val instance = Calendar.getInstance()
        currentDate!!.text = (instance.get(Calendar.DAY_OF_MONTH).toString() + "/"
                + (instance.get(Calendar.MONTH) + 1).toString() + "/" +
                instance.get(Calendar.YEAR).toString())
        currentDate.icon = ImageIcon(javaClass.getResource("/myToolWindow/Calendar-icon.png"))
        val min = instance.get(Calendar.MINUTE)
        val strMin: String
        if (min < 10) {
            strMin = "0" + min.toString()
        } else {
            strMin = min.toString()
        }
        currentTime!!.text = instance.get(Calendar.HOUR_OF_DAY).toString() + ":" + strMin
        currentTime.icon = ImageIcon(javaClass.getResource("/myToolWindow/Time-icon.png"))
        // Get time zone
        val gmt_Offset = instance.get(Calendar.ZONE_OFFSET).toLong() // offset from GMT in milliseconds
        var str_gmt_Offset = (gmt_Offset / 3600000).toString()
        str_gmt_Offset = if (gmt_Offset > 0) "GMT + $str_gmt_Offset" else "GMT - $str_gmt_Offset"
        timeZone!!.text = str_gmt_Offset
        timeZone.icon = ImageIcon(javaClass.getResource("/myToolWindow/Time-zone-icon.png"))


    }
}