package no.daffern.xbeecommunication.XBee;

/**
 * Created by Daffern on 11.06.2016.
 *
 * Codes (or commands) for each of the different XBee frames implemented in the Frames dir
 *
 * See https://www.digi.com/resources/documentation/digidocs/pdfs/90000991.pdf for more info
 */
public class XBeeFrameType {
    public static final byte XBEE_TRANSMIT_REQUEST = 0x10;
    public static final byte XBEE_RECEIVE = (byte) 0x90;
    public static final byte XBEE_TRANSMIT_STATUS = (byte) 0x8B;
    public static final byte XBEE_AT_COMMAND = (byte) 0x08;
    public static final byte XBEE_AT_COMMAND_RESPONSE = (byte) 0x88;
}
