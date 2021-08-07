package com.codeminion.nfcsample

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import java.lang.StringBuilder
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private val MIFARE_CHUNK_SIZE_BYTES = MifareUltralight.PAGE_SIZE//4
    private val MIFATE_ULTRALIGHT_MAX_PAGES = 36

    var mDisplayField: TextView? = null
    var mWriteButton: View? = null
    var mFoundTag: Tag? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDisplayField = findViewById(R.id.tag_content);
        mWriteButton = findViewById(R.id.btn_write_tag)

        mWriteButton?.setOnClickListener {
            mFoundTag?.let { tag ->

                writeTag(
                    tag = tag,
                    message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                )
                //writeTag(tag = tag, message = "No Data")
                //writeTag(tag = tag, message = "Hello Brother!")
                //writeTag(tag = tag, message = "00:80:A3:8B:51:FD")
                //writeTag(tag = tag, message = "0080A38B51FD")
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.also {
                mFoundTag = it
                val tagText = readTag(it)
                mDisplayField?.text = tagText
            }
        }

    }

    fun formatMsgForMifare(msg: String): List<String> {
        val leftOver = msg.length % MIFARE_CHUNK_SIZE_BYTES
        var outMsg = msg
        if (leftOver != 0) {
            outMsg = msg.padEnd(
                (msg.length / MIFARE_CHUNK_SIZE_BYTES + 1) * MIFARE_CHUNK_SIZE_BYTES,
                ' '
            )
        }
        return outMsg.chunked(MIFARE_CHUNK_SIZE_BYTES)
    }

    fun writeTag(tag: Tag, message: String, totalPages:Int = MIFATE_ULTRALIGHT_MAX_PAGES) {
        MifareUltralight.get(tag)?.use { ultralight ->
            ultralight.connect()

            Charset.forName("US-ASCII").also { usAscii ->
                // Clear anything that might have been written.
                // MIFARE Ultralight has 36 pages of user data.
                for (i in 0.until(totalPages)) {
                    ultralight.writePage(4 + i, byteArrayOf(0, 0, 0, 0))
                }

                val chunks = formatMsgForMifare(message)
                var offset = 0
                for (chunk in chunks) {
                    // Each page in the MIFARE is 4 bytes long.
                    ultralight.writePage(
                        MIFARE_CHUNK_SIZE_BYTES + offset,
                        chunk.toByteArray(usAscii)
                    )
                    offset += 1
                }
            }
        }
    }

    fun readTag(tag: Tag, totalPages:Int = MIFATE_ULTRALIGHT_MAX_PAGES): String? {
        return MifareUltralight.get(tag)?.use { mifare ->
            mifare.connect()

            val builder:StringBuilder = StringBuilder()
            for (i in 0 until totalPages step MifareUltralight.PAGE_SIZE) {
                val payload = mifare.readPages(4 + i)
                val byteStr = String(payload, Charset.forName("US-ASCII"))
                builder.append(byteStr)
            }
            builder.toString()
        }
    }
}
