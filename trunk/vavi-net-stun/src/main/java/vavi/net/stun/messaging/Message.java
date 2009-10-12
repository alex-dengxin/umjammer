/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.messaging;

import vavi.util.Debug;


/**
 * Message.
 *
 * @author suno
 * Created on 2003/06/30
 */
public class Message {
    /** */
    private ID source;
    /** */
    private ID destination;

    /** */
    private byte[] message;

    /** */
    private Type messageType;

    /** */
    public enum Type {
        /** */
        CONTROL(3),
        /** */
        GENERAL(2),
        /** */
        STUN(1);
        /** */
        int type;
        /** */
        Type(int type) {
            this.type = type;
        }
        /** */
        static Type valueOf(int value) {
            for (Type type : values()) {
                if (type.type == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(value));
        }
    };

    /**
     * @param source from
     * @param destination to
     * @param messageType
     * @param data message data 
     */
    public Message(ID source, ID destination, Message.Type messageType, byte[] data) {
        this.source = source;
        this.destination = destination;
        this.messageType = messageType;
        this.message = data;
    }

    /** */
    public boolean isGeneralMessage() {
        return messageType.equals(Message.Type.GENERAL);
    }

    /** */
    public boolean isSTUNMessage() {
        return messageType.equals(Message.Type.STUN);
    }

    /** */
    public boolean isControlMessage() {
        return messageType.equals(Message.Type.CONTROL);
    }

    /** */
    public Message(byte[] rawData) {
//Debug.println("dump:\n" + StringUtil.getDump(rawData));
        int payLoadLength = ((rawData[0] & 0xff) << 8) | (rawData[1] & 0xff);
        byte[] data = new byte[payLoadLength];
        System.arraycopy(rawData, 2, data, 0, data.length);
        messageType = Type.valueOf(data[0]);
        byte[] bytes = new byte[data[1]];
        System.arraycopy(data, 2, bytes, 0, bytes.length);
        this.source = ID.getID(bytes);
        int offset = 2 + bytes.length;
        bytes = new byte[data[offset]];
        offset++;
        System.arraycopy(data, offset, bytes, 0, bytes.length);
        this.destination = ID.getID(bytes);
        offset += bytes.length;
        message = new byte[data.length - offset];
        System.arraycopy(data, offset, message, 0, message.length);
    }

    /** */
    public byte[] getData() {
        return message;
    }

    /** */
    public String getMessageContents() {
        return "FROM: " + source + ", TO: " + destination + ", DATA: " + new String(message);
    }

    /** */
    public byte[] toBytes() {
        byte[] sourceBytes = source.getBytes();
        byte[] destinationBytes = destination.getBytes();
        int byteSize = getData().length + sourceBytes.length + destinationBytes.length + 3;
        int offset = 0;
        byte[] messageData = new byte[byteSize];
        messageData[0] = (byte) messageType.type;
        messageData[1] = (byte) sourceBytes.length;
        offset = 2;
        System.arraycopy(sourceBytes, 0, messageData, offset, sourceBytes.length);
        offset += sourceBytes.length;
        messageData[offset] = (byte) destinationBytes.length;
        offset++;
        System.arraycopy(destinationBytes, 0, messageData, offset, destinationBytes.length);
        offset += destinationBytes.length;
        System.arraycopy(getData(), 0, messageData, offset, message.length);

        byte[] rawData = new byte[messageData.length + 2];
        int dataLength = messageData.length;
        rawData[0] = (byte) (0xff & (dataLength >> 8));
        rawData[1] = (byte) (0xff & (dataLength));
Debug.println("message length: " + dataLength + ", 1:" + rawData[0] + ", 2:" + rawData[1]);
        System.arraycopy(messageData, 0, rawData, 2, messageData.length);
        return rawData;
    }

    /** */
    public void setData(byte[] data) {
        this.message = data;
    }

    /** */
    public ID getSource() {
        return source;
    }

    /** */
    public void setSource(ID id) {
        this.source = id;
    }

    /** */
    public ID getDestination() {
        return destination;
    }

    /** */
    public void setDestination(ID id) {
        this.destination = id;
    }
}

/* */
