package com.example.document

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class DocumentTtsManager(context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "DocumentTtsManager"
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var speechRate: Float = 1.0f

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            tts?.setSpeechRate(speechRate)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
            _isReady.value = true
            Log.i(TAG, "TTS initialized successfully")
        } else {
            Log.e(TAG, "TTS initialization failed")
        }
    }

    fun speak(text: String) {
        if (!_isReady.value || text.isBlank()) return
        stop()
        _isSpeaking.value = true
        // Chunk into max 3500 chars to avoid TTS length overflow
        val chunks = text.chunked(3500)
        chunks.forEachIndexed { index, chunk ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(chunk, queueMode, null, "utterance_$index")
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun toggleSpeak(text: String) {
        if (_isSpeaking.value) {
            stop()
        } else {
            speak(text)
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate
        tts?.setSpeechRate(rate)
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
