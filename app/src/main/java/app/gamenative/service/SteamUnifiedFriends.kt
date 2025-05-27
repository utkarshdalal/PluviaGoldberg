package app.gamenative.service

import androidx.room.withTransaction
import app.gamenative.data.FriendMessage
import app.gamenative.data.OwnedGames
import `in`.dragonbra.javasteam.enums.EAccountType
import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.enums.EUniverse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient
import `in`.dragonbra.javasteam.rpc.service.Chat
import `in`.dragonbra.javasteam.rpc.service.FriendMessages
import `in`.dragonbra.javasteam.rpc.service.FriendMessagesClient
import `in`.dragonbra.javasteam.rpc.service.Player
import `in`.dragonbra.javasteam.rpc.service.PlayerClient
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodNotification
import `in`.dragonbra.javasteam.types.SteamID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

// References:
// https://github.com/marwaniaaj/RichLinksJetpackCompose/tree/main
// https://blog.stackademic.com/rick-link-representation-in-jetpack-compose-d33956e8719e
// https://github.com/lukasroberts/AndroidLinkView
// https://github.com/android/compose-samples/tree/main/Jetchat
// https://github.com/LossyDragon/Vapulla

// TODO
//  Implement Reactions
//  OfflineMessageNotificationCallback ?
//  FriendMsgEchoCallback ?
//  EmoticonListCallback ?

typealias AckMessageNotification = SteammessagesFriendmessagesSteamclient.CFriendMessages_AckMessage_Notification.Builder
typealias IncomingMessageNotification = SteammessagesFriendmessagesSteamclient.CFriendMessages_IncomingMessage_Notification.Builder
typealias FriendNicknameChanged = SteammessagesPlayerSteamclient.CPlayer_FriendNicknameChanged_Notification.Builder

class SteamUnifiedFriends(
    private val service: SteamService,
) : AutoCloseable {

    private var unifiedMessages: SteamUnifiedMessages? = null

    private var chat: Chat? = null

    private var player: Player? = null

    private var friendMessages: FriendMessages? = null

    init {
        unifiedMessages = service.steamClient!!.getHandler<SteamUnifiedMessages>()

        chat = unifiedMessages!!.createService(Chat::class.java)

        player = unifiedMessages!!.createService(Player::class.java)

        friendMessages = unifiedMessages!!.createService(FriendMessages::class.java)

        with(service.callbackManager!!) {
            with(service.callbackSubscriptions) {
                add(subscribeServiceNotification<FriendMessagesClient, IncomingMessageNotification>(::onIncomingMessage))
                add(subscribeServiceNotification<FriendMessages, AckMessageNotification>(::onAckMessage))
                add(subscribeServiceNotification<PlayerClient, FriendNicknameChanged>(::onNickNameChanged))
            }
        }
    }

    override fun close() {
        unifiedMessages = null
        chat = null
        player = null
        friendMessages = null
    }

    /**
     * Request a fresh state of Friend's PersonaStates
     */
    fun refreshPersonaStates() {
        val request = SteammessagesChatSteamclient.CChat_RequestFriendPersonaStates_Request.newBuilder().build()
        chat?.requestFriendPersonaStates(request)
    }

    /**
     * Gets the last 50 messages from the specified friend. Steam may not provide all 50.
     */
    suspend fun getRecentMessages(friendID: Long) {
        Timber.i("Getting Recent messages for: $friendID")

        val userSteamID = SteamService.userSteamId!!

        val request = SteammessagesFriendmessagesSteamclient.CFriendMessages_GetRecentMessages_Request.newBuilder().apply {
            steamid1 = userSteamID.convertToUInt64() // You
            steamid2 = friendID // Friend
            // The rest here and below is what steam has looking at NHA2
            count = 50
            rtime32StartTime = 0
            bbcodeFormat = true
            startOrdinal = 0
            timeLast = Int.MAX_VALUE
            ordinalLast = 0
        }.build()

        val response = friendMessages!!.getRecentMessages(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to get message history for friend: $friendID, ${response.result}")
            return
        }

        val regex = "\\[U:\\d+:(\\d+)]".toRegex()
        val userSteamId3 = regex.find(userSteamID.render())!!.groupValues[1].toInt()
        val messages = response.body.messagesList.map { message ->
            FriendMessage(
                steamIDFriend = friendID,
                fromLocal = userSteamId3 == message.accountid,
                message = message.message,
                timestamp = message.timestamp,
            )
        }

        service.db.withTransaction {
            service.messagesDao.insertMessagesIfNotExist(messages)
        }

        Timber.i("More available: ${response.body.moreAvailable}")
    }

    /**
     * Sends a 'is typing' message to the specified friend.
     */
    suspend fun setIsTyping(friendID: Long) {
        Timber.i("Sending 'is typing' to $friendID")
        val request = SteammessagesFriendmessagesSteamclient.CFriendMessages_SendMessage_Request.newBuilder().apply {
            steamid = friendID
            chatEntryType = EChatEntryType.Typing.code()
        }.build()

        val response = friendMessages!!.sendMessage(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to send typing message to friend: $friendID, ${response.result}")
            return
        }

        // TODO: This, I believe returns a result with supplemental data to append to the database.
        // response.body.serverTimestamp
    }

    /**
     * Sends a chat message to the specified friend.
     */
    suspend fun sendMessage(friendID: Long, chatMessage: String) {
        Timber.i("Sending chat message to $friendID")
        val trimmedMessage = chatMessage.trim()

        if (trimmedMessage.isEmpty()) {
            Timber.w("Trying to send an empty message.")
            return
        }

        val request = SteammessagesFriendmessagesSteamclient.CFriendMessages_SendMessage_Request.newBuilder().apply {
            chatEntryType = EChatEntryType.ChatMsg.code()
            message = trimmedMessage
            steamid = friendID
            containsBbcode = true
            echoToSender = false
            lowPriority = false
        }.build()

        val response = friendMessages!!.sendMessage(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to send chat message to friend: $friendID, ${response.result}")
            return
        }

        service.db.withTransaction {
            service.messagesDao.insertMessageIfNotExists(
                FriendMessage(
                    steamIDFriend = friendID,
                    fromLocal = true,
                    message = response.body.modifiedMessage.ifEmpty { trimmedMessage },
                    timestamp = response.body.serverTimestamp,
                ),
            )
        }

        // Once chat notifications are implemented, we should clear it here as well.
    }

    /**
     * Acknowledge the message, this will mark other clients that we have read the message.
     */
    fun ackMessage(friendID: Long) {
        Timber.d("Ack-ing message for friend: $friendID")
        val request = SteammessagesFriendmessagesSteamclient.CFriendMessages_AckMessage_Notification.newBuilder().apply {
            steamidPartner = friendID
            timestamp = System.currentTimeMillis().div(1000).toInt()
        }.build()

        // This does not return anything.
        friendMessages!!.ackMessage(request)
    }

    /**
     * TODO
     */
    suspend fun getActiveMessageSessions() {
        Timber.i("Get Active message sessions")

        val request = SteammessagesFriendmessagesSteamclient.CFriendsMessages_GetActiveMessageSessions_Request.newBuilder().apply {
            lastmessageSince = 0
            onlySessionsWithMessages = true
        }.build()

        val response = friendMessages!!.getActiveMessageSessions(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to get active message sessions, ${response.result}")
            return
        }

        // response.body.timestamp

        response.body.messageSessionsList.forEach { session ->
            // session.accountidFriend
            // session.lastMessage
            // session.lastView
            // session.unreadMessageCount
        }
    }

    /**
     * TODO
     */
    // suspend fun getPerFriendPreferences()

    /**
     * TODO
     */
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

        val response = friendMessages!!.updateMessageReaction(request).await()

        if (response.result != EResult.OK) {
            Timber.w("Failed to get message reaction, ${response.result}")
            return
        }

        response.body.reactorsList.forEach { reactor ->
            // Last part of steamID3
        }
    }

    /**
     * Gets a list of games that the user owns. If the library is private, it will be empty.
     */
    suspend fun getOwnedGames(steamID: Long): List<OwnedGames> {
        val request = SteammessagesPlayerSteamclient.CPlayer_GetOwnedGames_Request.newBuilder().apply {
            steamid = steamID
            includePlayedFreeGames = true
            includeFreeSub = true
            includeAppinfo = true
            includeExtendedAppinfo = true
        }.build()

        val result = player?.getOwnedGames(request)?.await()

        if (result == null || result.result != EResult.OK) {
            Timber.w("Unable to get owned games!")
            return emptyList()
        }

        val list = result.body.gamesList.map { game ->
            OwnedGames(
                appId = game.appid,
                name = game.name,
                playtimeTwoWeeks = game.playtime2Weeks,
                playtimeForever = game.playtimeForever,
                imgIconUrl = game.imgIconUrl,
                sortAs = game.sortAs,
                rtimeLastPlayed = game.rtimeLastPlayed
            )
        }

        if (list.size != result.body.gamesCount) {
            Timber.w("List was not the same as given")
        }

        return list
    }

    /**
     * Another steam client (logged into the same account) has opened up chat to acknowledge the message(s).
     */
    private fun onAckMessage(notification: ServiceMethodNotification<AckMessageNotification>) {
        val friendID = notification.body.steamidPartner
        Timber.i("Ack-ing Message for $friendID")
        CoroutineScope(Dispatchers.IO).launch {
            service.db.withTransaction {
                val friend = service.friendDao.findFriend(friendID)
                friend?.let { service.friendDao.update(friend.copy(unreadMessageCount = 0)) }
            }
        }
    }

    /**
     * Someone has changed their nickname.
     */
    private fun onNickNameChanged(notification: ServiceMethodNotification<FriendNicknameChanged>) {
        CoroutineScope(Dispatchers.IO).launch {
            Timber.i("Nickname Changed for ${notification.body.accountid} -> ${notification.body.nickname}")
            val friendID = SteamID(notification.body.accountid.toLong(), EUniverse.Public, EAccountType.Individual)

            service.db.withTransaction {
                service.friendDao.findFriend(friendID.convertToUInt64())?.let {
                    service.friendDao.update(it.copy(nickname = notification.body.nickname))
                }
            }
        }
    }

    /**
     * We're receiving information that someone is either typing a message or sent a message.
     */
    private fun onIncomingMessage(notification: ServiceMethodNotification<IncomingMessageNotification>) {
        val steamIDFriend = notification.body.steamidFriend
        Timber.i("Incoming Message form $steamIDFriend")

        when (notification.body.chatEntryType) {
            EChatEntryType.Typing.code() -> {
                CoroutineScope(Dispatchers.IO).launch {
                    service.db.withTransaction {
                        val friend = service.friendDao.findFriend(steamIDFriend)

                        if (friend == null) {
                            Timber.w("Unable to find friend $steamIDFriend")
                            return@withTransaction
                        }

                        service.friendDao.update(friend.copy(isTyping = true))
                    }
                }
            }

            EChatEntryType.ChatMsg.code() -> {
                CoroutineScope(Dispatchers.IO).launch {
                    service.db.withTransaction {
                        val friend = service.friendDao.findFriend(steamIDFriend)

                        if (friend == null) {
                            Timber.w("Unable to find friend $steamIDFriend")
                            return@withTransaction
                        }

                        service.friendDao.update(friend.copy(isTyping = false))

                        val chatMsg = FriendMessage(
                            steamIDFriend = steamIDFriend,
                            fromLocal = false,
                            message = notification.body.message,
                            timestamp = notification.body.rtime32ServerTimestamp,
                        )

                        service.messagesDao.insertMessage(chatMsg)
                    }
                }
            }

            else -> Timber.w("Unknown incoming message, ${EChatEntryType.from(notification.body.chatEntryType)}")
        }
    }
}
