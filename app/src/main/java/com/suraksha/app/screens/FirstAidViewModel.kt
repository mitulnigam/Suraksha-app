package com.suraksha.app.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class FirstAidViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Hello! I am your First Aid Assistant. How can I help you today? You can ask about Bleeding, Burns, Fractures, Choking, or Fainting.", false)
    ))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val firstAidData = mapOf(
        "bleeding" to """
            🚑 **First Aid for Bleeding:**
            1. Apply direct pressure to the wound with a clean cloth or bandage.
            2. Elevate the injured area above the heart level if possible.
            3. If the cloth gets soaked, do NOT remove it; place another clean cloth on top.
            4. Keep the person calm and warm.
            
            ⚠️ **Call 102/108 immediately** if bleeding is heavy, spurt-like, or doesn't stop after 10 minutes of pressure.
            
            *Disclaimer: I am an AI assistant, not a medical professional.*
        """.trimIndent(),
        
        "burn" to """
            🚑 **First Aid for Burns:**
            1. Cool the burn immediately under cool (not cold) running water for at least 10-20 minutes.
            2. Remove any jewelry or tight clothing before the area begins to swell.
            3. Cover the burn loosely with a sterile dressing or clean plastic wrap (cling film).
            4. Do NOT apply ice, butter, or ointments to the burn.
            
            ⚠️ **Call 102/108 immediately** for large burns, face burns, or if the skin looks charred or white.
            
            *Disclaimer: I am an AI assistant, not a medical professional.*
        """.trimIndent(),
        
        "fracture" to """
            🚑 **First Aid for Fractures/Broken Bones:**
            1. Do NOT try to move or straighten the injured limb.
            2. Support the injury using a sling or folded clothing to keep it still.
            3. Apply a cold pack wrapped in a cloth to reduce swelling (max 20 mins).
            4. If there is an open wound, cover it with a clean dressing but do not press on the bone.
            
            ⚠️ **Call 102/108 immediately** if the person is in extreme pain, the limb is deformed, or they cannot move.
            
            *Disclaimer: I am an AI assistant, not a medical professional.*
        """.trimIndent(),
        
        "choking" to """
            🚑 **First Aid for Choking:**
            1. Encourage the person to cough strongly.
            2. Give up to 5 sharp back blows between the shoulder blades with the heel of your hand.
            3. If still choking, give up to 5 abdominal thrusts (Heimlich maneuver).
            4. Repeat back blows and abdominal thrusts until the object is forced out.
            
            ⚠️ **Call 102/108 immediately** if the person becomes unconscious or cannot breathe at all.
            
            *Disclaimer: I am an AI assistant, not a medical professional.*
        """.trimIndent(),
        
        "fainting" to """
            🚑 **First Aid for Fainting:**
            1. Lay the person flat on their back and elevate their legs about 12 inches.
            2. Loosen any tight clothing (collars, belts).
            3. Ensure the area is well-ventilated with fresh air.
            4. Check if they are breathing and have a pulse.
            
            ⚠️ **Call 102/108 immediately** if the person does not regain consciousness within 1 minute or has chest pain.
            
            *Disclaimer: I am an AI assistant, not a medical professional.*
        """.trimIndent(),
        
        "accident" to """
            🚑 **First Aid for General Accidents:**
            1. Check the scene for safety before approaching.
            2. Check the victim for responsiveness and breathing.
            3. If unconscious but breathing, place them in the recovery position (on their side).
            4. Keep the victim still and warm until help arrives.
            
            ⚠️ **Call 102/108 immediately** for any serious accident, head injury, or if the person is unresponsive.
            
            *Disclaimer: I am an AI assistant, not a medical professional.*
        """.trimIndent()
    )

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(userText, true)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            // Simulate a short delay for "thinking"
            delay(800)
            
            val responseText = getPredefinedResponse(userText)
            _messages.value = _messages.value + ChatMessage(responseText, false)
            _isLoading.value = false
        }
    }

    private fun getPredefinedResponse(input: String): String {
        val lowerInput = input.lowercase()
        
        for ((key, response) in firstAidData) {
            if (lowerInput.contains(key)) {
                return response
            }
        }
        
        return """
            I'm here to help with basic first aid. Please tell me more about the injury (e.g., is it a burn, bleeding, or a fracture?). 
            
            If this is a life-threatening emergency, please **call 102 or 108** immediately.
            
            *Disclaimer: I am an AI assistant, not a medical professional.*
        """.trimIndent()
    }
    
    fun sendQuickAction(action: String) {
        sendMessage(action)
    }
}
