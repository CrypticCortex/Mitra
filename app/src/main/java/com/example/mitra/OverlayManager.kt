package com.example.mitra

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat

class OverlayManager(private val context: Context) {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var inputField: EditText
    private lateinit var sendButton: Button
    private lateinit var chatContainer: LinearLayout
    private lateinit var inputContainer: View
    private lateinit var chatOptions: View
    private lateinit var chatScrollView: ScrollView
    private lateinit var rootLayout: View
    private val chatHistory = mutableListOf<String>()

    fun showGradientOverlay() {
        setupWindowManager()
        inflateOverlayView()
        setupViews()
        setupListeners()
        setupKeyboardVisibilityListener()
        addOverlayToWindow()
    }

    private fun setupWindowManager() {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun inflateOverlayView() {
        overlayView = LayoutInflater.from(context).inflate(R.layout.gradient_overlay_with_close, null)
    }

    private fun setupViews() {
        rootLayout = overlayView.findViewById(R.id.root_layout)
        inputField = overlayView.findViewById(R.id.input_field)
        sendButton = overlayView.findViewById(R.id.button_send)
        chatContainer = overlayView.findViewById(R.id.chat_container)
        inputContainer = overlayView.findViewById(R.id.input_container)
        chatOptions = overlayView.findViewById(R.id.chat_options)
        chatScrollView = overlayView.findViewById(R.id.chat_scroll_view)
    }

    private fun setupKeyboardVisibilityListener() {
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootLayout.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootLayout.rootView.height
            val keypadHeight = screenHeight - r.bottom

            if (keypadHeight > screenHeight * 0.15) {
                adjustInputContainerTranslation(-keypadHeight.toFloat() - inputContainer.height)
            } else {
                adjustInputContainerTranslation(0f)
            }
        }
    }

    private fun adjustInputContainerTranslation(translationY: Float) {
        inputContainer.animate().translationY(translationY).setDuration(200).start()
    }

    private fun setupListeners() {
        overlayView.findViewById<Button>(R.id.button_close).setOnClickListener {
            removeGradientOverlay()
        }

        overlayView.findViewById<Button>(R.id.button_continue_chat).setOnClickListener {
            continueChatMode()
        }

        overlayView.findViewById<Button>(R.id.button_new_chat).setOnClickListener {
            newChatMode()
        }

        sendButton.setOnClickListener {
            handleSendAction()
        }

        inputField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleSendAction()
                true
            } else {
                false
            }
        }
    }

    private fun continueChatMode() {
        showChatUI()
        loadChatHistory()
        focusOnInput()
    }

    private fun newChatMode() {
        showChatUI()
        resetChatHistory()
        focusOnInput()
    }

    private fun showChatUI() {
        chatOptions.visibility = View.GONE
        chatScrollView.visibility = View.VISIBLE
        inputContainer.visibility = View.VISIBLE
    }

    private fun loadChatHistory() {
        chatContainer.removeAllViews()
        for (message in chatHistory) {
            addMessageToChat(message)
        }
    }

    private fun resetChatHistory() {
        chatContainer.removeAllViews()
        chatHistory.clear()
    }

    private fun focusOnInput() {
        inputField.requestFocus()
        showKeyboard(inputField)
    }

    private fun handleSendAction() {
        val text = inputField.text.toString().trim()
        if (text.isNotEmpty()) {
            processMessage(text)
            clearInputField()
            scrollToBottom()
        }
    }

    private fun processMessage(message: String) {
        addMessageToChat(message)
        chatHistory.add(message)
    }

    private fun clearInputField() {
        inputField.text.clear()
    }

    private fun scrollToBottom() {
        chatScrollView.post { chatScrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun addMessageToChat(message: String) {
        val messageView = createMessageView(message)
        chatContainer.addView(messageView)
        scrollToBottom()
        ensureInputFieldVisible()
    }

    private fun createMessageView(message: String): TextView {
        return TextView(context).apply {
            text = message
            textSize = 24f
            setTextColor(Color.BLACK)
            background = ContextCompat.getDrawable(context, R.drawable.pill_background)
            setPadding(32, 16, 32, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, 32)
            }
        }
    }

    private fun ensureInputFieldVisible() {
        inputContainer.visibility = View.VISIBLE
        inputField.requestFocus()
        showKeyboard(inputField)
    }

    private fun showKeyboard(view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun addOverlayToWindow() {
        val params = getWindowLayoutParams()
        windowManager.addView(overlayView, params)
        addPostUpdateView(params)
    }

    private fun getWindowLayoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP
    }

    private fun addPostUpdateView(params: WindowManager.LayoutParams) {
        Handler(Looper.getMainLooper()).postDelayed({
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            windowManager.updateViewLayout(overlayView, params)
        }, 100)
    }

    fun removeGradientOverlay() {
        if (::overlayView.isInitialized && overlayView.isAttachedToWindow) {
            windowManager.removeView(overlayView)
        }
    }
}
