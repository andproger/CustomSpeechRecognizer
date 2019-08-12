package com.truiton.customspeechrecognizer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class VoiceRecognitionActivity : AppCompatActivity() {
    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar1.visibility = View.INVISIBLE
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        Log.i(TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this))
        speech!!.setRecognitionListener(recognitionListener)

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
        recognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizerIntent!!.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

        toggleButton1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                progressBar1.visibility = View.VISIBLE
                progressBar1.isIndeterminate = true
                ActivityCompat.requestPermissions(this@VoiceRecognitionActivity,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        REQUEST_RECORD_PERMISSION)
            } else {
                progressBar1.isIndeterminate = false
                progressBar1.visibility = View.INVISIBLE
                speech!!.stopListening()
            }
        }

    }

    override fun onStop() {
        super.onStop()
        if (speech != null) {
            speech!!.destroy()
            Log.i(TAG, "destroy")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_PERMISSION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speech!!.startListening(recognizerIntent)
            } else {
                Toast.makeText(this@VoiceRecognitionActivity, "Permission Denied!", Toast
                        .LENGTH_SHORT).show()
            }
        }
    }

    private val recognitionListener = object : RecognitionListener {

        override fun onBeginningOfSpeech() {
            Log.i(TAG, "onBeginningOfSpeech")
            progressBar1.isIndeterminate = false
            progressBar1.max = 10
        }

        override fun onBufferReceived(buffer: ByteArray) {
            Log.i(TAG, "onBufferReceived: $buffer")
        }

        override fun onEndOfSpeech() {
            Log.i(TAG, "onEndOfSpeech")
            progressBar1.isIndeterminate = true
            toggleButton1.isChecked = false
        }

        override fun onError(errorCode: Int) {
            val errorMessage = getErrorText(errorCode)
            Log.d(TAG, "FAILED $errorMessage")
            textView1.text = errorMessage
            toggleButton1.isChecked = false
        }

        override fun onEvent(arg0: Int, arg1: Bundle) {
            Log.i(TAG, "onEvent")
        }

        override fun onPartialResults(arg0: Bundle) {
            Log.i(TAG, "onPartialResults")
        }

        override fun onReadyForSpeech(arg0: Bundle) {
            Log.i(TAG, "onReadyForSpeech")
        }

        override fun onResults(results: Bundle) {
            Log.i(TAG, "onResults")
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            var text = ""
            for (result in matches!!)
                text += result + "\n"

            textView1.text = text
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.i(TAG, "onRmsChanged: $rmsdB")
            progressBar1.progress = rmsdB.toInt()
        }
    }

    companion object {
        private const val REQUEST_RECORD_PERMISSION = 100
        private const val TAG = "TAG"

        fun getErrorText(errorCode: Int) = when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
    }
}