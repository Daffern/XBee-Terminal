package no.daffern.xbeecommunication;

/**
 * Created by Daffern on 20.09.2016.
 *
 * Data sent and received always contains one of these prefixes which identifies the type of data and what it should be used for
 */

public class DataTypes {
    public static final byte APP_CHAT_MESSAGE = 0x6d; //m
    public static final byte APP_VOICE_MESSAGE = 0x76; //v
    public static final byte APP_VOICE_STATUS_MESSAGE = 0x75;
    public static final byte APP_SMS_MESSAGE = 0x73; //s
    public static final byte APP_SMS_STATUS_MESSAGE = 0x74; //t
}
