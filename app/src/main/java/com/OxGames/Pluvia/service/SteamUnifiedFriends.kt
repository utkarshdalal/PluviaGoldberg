package com.OxGames.Pluvia.service

import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.CChat_RequestFriendPersonaStates_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient
import `in`.dragonbra.javasteam.rpc.service.Chat
import `in`.dragonbra.javasteam.rpc.service.FriendMessages
import `in`.dragonbra.javasteam.rpc.service.FriendMessagesClient
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.types.SteamID
import timber.log.Timber
import java.io.Closeable

// For chat ideas, check out:
// https://github.com/marwaniaaj/RichLinksJetpackCompose/tree/main
// https://blog.stackademic.com/rick-link-representation-in-jetpack-compose-d33956e8719e
// https://github.com/lukasroberts/AndroidLinkView
// https://github.com/Aldikitta/JetComposeChatWithMe
// https://github.com/android/compose-samples/tree/main/Jetchat
// https://github.com/LossyDragon/Vapulla

typealias AckMessageNotification = SteammessagesFriendmessagesSteamclient.CFriendMessages_AckMessage_Notification.Builder
typealias IncomingMessageNotification = SteammessagesFriendmessagesSteamclient.CFriendMessages_IncomingMessage_Notification.Builder

class SteamUnifiedFriends(service: SteamService) : AutoCloseable {

    private val callbackSubscriptions: ArrayList<Closeable> = ArrayList()

    private var unifiedMessages: SteamUnifiedMessages? = null

    private var chat: Chat? = null

    private var messages: FriendMessages? = null

    // TODO OfflineMessageNotificationCallback ?
    // TODO FriendMsgEchoCallback ?
    // TODO EmoticonListCallback ?

    init {
        unifiedMessages = service.steamClient!!.getHandler<SteamUnifiedMessages>()
        chat = unifiedMessages!!.createService(Chat::class.java)
        messages = unifiedMessages!!.createService(FriendMessages::class.java)

        service.callbackManager!!.subscribeServiceNotification<FriendMessages, AckMessageNotification> {
            Timber.i("Ack-ing Message")
            // it.body.steamidPartner
            // TODO: 'read' a message since another client has opened the chat.
        }.also(callbackSubscriptions::add)

        service.callbackManager!!.subscribeServiceNotification<FriendMessagesClient, IncomingMessageNotification> {
            Timber.i("Incoming Message")
            when (it.body.chatEntryType) {
                EChatEntryType.Typing.code() -> {
                    // TODO: An incoming chat message is being typed up.
                }

                EChatEntryType.ChatMsg.code() -> {
                    // TODO: An incoming chat message has been received.
                }

                else -> Timber.w("Unknown incoming message, ${EChatEntryType.from(it.body.chatEntryType)}")
            }
        }.also(callbackSubscriptions::add)
    }

    override fun close() {
        unifiedMessages = null
        chat = null
        messages = null

        callbackSubscriptions.forEach {
            it.close()
        }
    }

    /**
     * Request a fresh state of Friend's PersonaStates
     */
    fun refreshPersonaStates() {
        val request = CChat_RequestFriendPersonaStates_Request.newBuilder().build()
        chat?.requestFriendPersonaStates(request)
    }

    suspend fun getRecentMessages(friendID: SteamID) {
        Timber.i("Getting Recent messages for: ${friendID.convertToUInt64()}")

        val request = SteammessagesFriendmessagesSteamclient.CFriendMessages_GetRecentMessages_Request.newBuilder().apply {
            steamid1 = SteamService.userSteamId!!.convertToUInt64() // You
            steamid2 = friendID.convertToUInt64() // Friend
            // The rest here and below is what steam has looking at NHA2
            count = 50
            rtime32StartTime = 0
            bbcodeFormat = true
            startOrdinal = 0
            timeLast = Int.MAX_VALUE
            ordinalLast = 0
        }.build()

        val response = messages!!.getRecentMessages(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to get message history for friend: ${friendID.convertToUInt64()}, ${response.result}")
            return
        }

        // TODO: Insert new messages into database
        // TODO: Do not dupe messages
        response.body.messagesList.forEach { message ->
            // message.accountid
            // message.timestamp
            // message.message
            // message.ordinal
            // message.reactionsList.forEach { reaction ->
            //     reaction.reaction
            //     reaction.reactionType
            //     reaction.reactionBytes
            //     reaction.reactorsList
            //     reaction.reactorsCount
            // }
        }
        Timber.i("More available: ${response.body.moreAvailable}")
    }

    suspend fun setIsTyping(friendID: SteamID) {
        Timber.i("Sending 'is typing' to ${friendID.convertToUInt64()}")
        val request = SteammessagesFriendmessagesSteamclient.CFriendMessages_SendMessage_Request.newBuilder().apply {
            steamid = friendID.convertToUInt64()
            chatEntryType = EChatEntryType.Typing.code()
        }.build()

        val response = messages!!.sendMessage(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to send typing message to friend: ${friendID.convertToUInt64()}, ${response.result}")
            return
        }

        // TODO: This, I believe returns a result with supplemental data to append to the database.
        // response.body.serverTimestamp
    }

    suspend fun sendMessage(friendID: SteamID, chatMessage: String) {
        Timber.i("Sending chat message to ${friendID.convertToUInt64()}")
        val trimmedMessage = chatMessage.trim()

        if (trimmedMessage.isEmpty()) {
            Timber.w("Trying to send an empty message.")
            return
        }

        val request = SteammessagesFriendmessagesSteamclient.CFriendMessages_SendMessage_Request.newBuilder().apply {
            chatEntryType = EChatEntryType.ChatMsg.code()
            message = chatMessage
            steamid = friendID.convertToUInt64()
            containsBbcode = true
            echoToSender = false
            lowPriority = false
        }.build()

        val response = messages!!.sendMessage(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to send chat message to friend: ${friendID.convertToUInt64()}, ${response.result}")
            return
        }

        // TODO: This, I believe returns a result with supplemental data to append to the database.
        // TODO: We also need to append the message to our database

        // response.body.serverTimestamp

        // Once chat notifications are implemented, we should clear it here as well.
    }

    fun ackMessage(friendID: SteamID) {
        Timber.d("Ack-ing message for friend: ${friendID.convertToUInt64()}")
        val request = SteammessagesFriendmessagesSteamclient.CFriendMessages_AckMessage_Notification.newBuilder().apply {
            steamidPartner = friendID.convertToUInt64()
            timestamp = System.currentTimeMillis().div(1000).toInt()
        }.build()

        // This does not return anything.
        messages!!.ackMessage(request)
    }

    suspend fun getActiveMessageSessions() {
        Timber.i("Get Active message sessions")

        val request = SteammessagesFriendmessagesSteamclient.CFriendsMessages_GetActiveMessageSessions_Request.newBuilder().apply {
            lastmessageSince = 0
            onlySessionsWithMessages = true
        }.build()

        val response = messages!!.getActiveMessageSessions(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to get active message sessions, ${response.result}")
            return
        }

        // TODO

        // response.body.timestamp

        response.body.messageSessionsList.forEach { session ->
            // session.accountidFriend
            // session.lastMessage
            // session.lastView
            // session.unreadMessageCount
        }
    }

    // suspend fun getPerFriendPreferences()

    suspend fun updateMessageReaction(
        friendID: SteamID,
        serverTimestamp: Int,
        reactionType: SteammessagesFriendmessagesSteamclient.EMessageReactionType,
        reaction: String,
        isAdd: Boolean,
    ) {
        Timber.i(
            "Update message reaction: ${friendID.convertToUInt64()}, timestamp: $serverTimestamp, " +
                "type: $reactionType, reaction: $reaction, isAdd: $isAdd ",
        )

        val request = SteammessagesFriendmessagesSteamclient.CFriendMessages_UpdateMessageReaction_Request.newBuilder().apply {
            this.steamid = friendID.convertToUInt64()
            this.serverTimestamp = serverTimestamp
            this.ordinal = 0
            this.reactionType = reactionType
            this.reaction = reaction
            this.isAdd = isAdd
        }.build()

        val response = messages!!.updateMessageReaction(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to get message reaction, ${response.result}")
            return
        }

        response.body.reactorsList.forEach { reactor ->
            // Last part of steamID3
        }
    }
}
