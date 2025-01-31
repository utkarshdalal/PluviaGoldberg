package com.OxGames.Pluvia.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.OxGames.Pluvia.db.dao.EmoticonDao
import com.OxGames.Pluvia.db.dao.FriendMessagesDao
import com.OxGames.Pluvia.db.dao.SteamFriendDao
import com.OxGames.Pluvia.service.SteamService
import com.OxGames.Pluvia.ui.data.ChatState
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.dragonbra.javasteam.types.SteamID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val friendDao: SteamFriendDao,
    private val messagesDao: FriendMessagesDao,
    private val emoticonDao: EmoticonDao,
) : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private var chatJob: Job? = null
    private var typingJob: Job? = null
    private var lastTypingSent = 0L

    override fun onCleared() {
        super.onCleared()

        Timber.d("onCleared")

        chatJob?.cancel()
    }

    fun setFriend(id: Long) {
        Timber.d("Chatting with $id")
        chatJob?.cancel()

        chatJob = viewModelScope.launch {
            launch {
                // Since were initiating a chat, refresh our list of emoticons and stickers
                SteamService.getEmoticonList()
                SteamService.getRecentMessages(id)
                SteamService.ackMessage(id)
            }

            launch {
                emoticonDao.getAll().collect { list ->
                    Timber.tag("ChatViewModel").d("Got Emotes: ${list.size}")
                    _chatState.update { it.copy(emoticons = list) }
                }
            }

            launch {
                friendDao.findFriendFlow(id).collect { friend ->
                    if (friend == null) {
                        throw RuntimeException("Friend is null and cannot proceed")
                    }
                    Timber.tag("ChatViewModel").d("Friend update $friend")
                    _chatState.update { it.copy(friend = friend) }
                }
            }

            launch {
                messagesDao.getAllMessagesForFriend(id).collect { list ->
                    Timber.tag("ChatViewModel").d("New messages ${list.size}")
                    _chatState.update { it.copy(messages = list) }
                }
            }
        }
    }

    fun onTyping() {
        val now = System.currentTimeMillis()

        if (typingJob == null || now - lastTypingSent > 15000) {
            typingJob?.cancel()
            typingJob = viewModelScope.launch {
                SteamService.sendTypingMessage(_chatState.value.friend.id)
                lastTypingSent = now
            }
        }
    }

    fun onSendMessage(message: String) {
        typingJob?.cancel()
        typingJob = null

        viewModelScope.launch {
            with(_chatState.value.friend) {
                if (!SteamID(id).isValid) {
                    Timber.w("Friend ID invalid, not sending message")
                    return@launch
                }

                SteamService.sendMessage(id, message)
            }
        }
    }
}
