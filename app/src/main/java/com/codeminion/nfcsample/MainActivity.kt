package com.codeminion.nfcsample

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NdefFormatable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private val MIFARE_CHUNK_SIZE_BYTES = 4

    var mDisplayField:TextView? = null
    var mWriteButton: View? = null
    var mFoundTag: Tag? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDisplayField = findViewById(R.id.tag_content);
        mWriteButton = findViewById(R.id.btn_write_tag)

        mWriteButton?.setOnClickListener {
            mFoundTag?.let { tag->
                //writeTag(tag = tag, message = "No Data")
                writeTag(tag = tag, message = "Hello Brother!")
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

    fun formatMsgForMifare(msg:String ):List<String> {
        val leftOver = msg.length % MIFARE_CHUNK_SIZE_BYTES
        var outMsg = msg
        if (leftOver != 0) {
            outMsg = msg.padEnd((msg.length / MIFARE_CHUNK_SIZE_BYTES + 1) * MIFARE_CHUNK_SIZE_BYTES, ' ')
        }
        return outMsg.chunked(MIFARE_CHUNK_SIZE_BYTES)
    }

    fun writeTag(tag: Tag, message:String) {
        MifareUltralight.get(tag)?.use { ultralight ->
            ultralight.connect()

            Charset.forName("US-ASCII").also { usAscii ->
                // Clear anything that might have been written.
                // MIFARE Ultralight has 36 pages of user data.
                for (i in 0.until(36)) {
                    ultralight.writePage(4 + i, byteArrayOf(0,0,0,0))
                }

                val chunks = formatMsgForMifare(message)
                var offset = 0
                for (chunk in chunks) {
                    // Each page in the MIFARE is 4 bytes long.
                    ultralight.writePage(MIFARE_CHUNK_SIZE_BYTES + offset, chunk.toByteArray(usAscii))
                    offset +=1
                }
            }
        }
    }

    fun readTag(tag: Tag): String? {
        return MifareUltralight.get(tag)?.use { mifare ->
            mifare.connect()
            val payload = mifare.readPages(4)
            String(payload, Charset.forName("US-ASCII"))
        }
    }
}
