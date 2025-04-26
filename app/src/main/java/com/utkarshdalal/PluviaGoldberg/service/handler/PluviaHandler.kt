package com.utkarshdalal.PluviaGoldberg.service.handler

import com.utkarshdalal.PluviaGoldberg.service.callback.EmoticonListCallback
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientGetEmoticonList
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * Custom handler to handle dispatching that JavaSteam does not support.
 */
class PluviaHandler : ClientMsgHandler() {

    companion object {
        fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.msgType) {
            EMsg.ClientEmoticonList -> EmoticonListCallback(packetMsg)
            else -> null
        }
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        val callback = getCallback(packetMsg) ?: return

        client.postCallback(callback)
    }

    fun getEmoticonList() {
        ClientMsgProtobuf<CMsgClientGetEmoticonList.Builder>(
            CMsgClientGetEmoticonList::class.java,
            EMsg.ClientGetEmoticonList,
        ).also(client::send)
    }
}
