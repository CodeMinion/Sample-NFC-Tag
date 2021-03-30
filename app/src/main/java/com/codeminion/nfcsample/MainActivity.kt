package com.codeminion.nfcsample

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

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
                writeTag(tag)
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

    fun writeTag(tag: Tag) {
        MifareUltralight.get(tag)?.use { ultralight ->
            ultralight.connect()
            Charset.forName("US-ASCII").also { usAscii ->
                ultralight.writePage(4, "Hello".toByteArray(usAscii))
                ultralight.writePage(5, " Bro".toByteArray(usAscii))
                ultralight.writePage(6, "ther".toByteArray(usAscii))
                ultralight.writePage(7, "!!!!".toByteArray(usAscii))
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
