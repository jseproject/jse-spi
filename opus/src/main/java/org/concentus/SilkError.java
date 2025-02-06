package org.concentus;

/// <summary>
/// Represents error messages from a silk encoder/decoder
/// </summary>
class SilkError {

    static int SILK_NO_ERROR = 0;

    // Encoder error messages

    /* Input length is not a multiple of 10 ms, or length is longer than the packet length */
    static int SILK_ENC_INPUT_INVALID_NO_OF_SAMPLES = -101;

    /* Sampling frequency not 8000, 12000 or 16000 Hertz */
    static int SILK_ENC_FS_NOT_SUPPORTED = -102;

    /* Packet size not 10, 20, 40, or 60 ms */
    static int SILK_ENC_PACKET_SIZE_NOT_SUPPORTED = -103;

    /* Allocated payload buffer too short */
    static int SILK_ENC_PAYLOAD_BUF_TOO_SHORT = -104;

    /* Loss rate not between 0 and 100 percent */
    static int SILK_ENC_INVALID_LOSS_RATE = -105;

    /* Complexity setting not valid, use 0...10 */
    static int SILK_ENC_INVALID_COMPLEXITY_SETTING = -106;

    /* Inband FEC setting not valid, use 0 or 1 */
    static int SILK_ENC_INVALID_INBAND_FEC_SETTING = -107;

    /* DTX setting not valid, use 0 or 1 */
    static int SILK_ENC_INVALID_DTX_SETTING = -108;

    /* CBR setting not valid, use 0 or 1 */
    static int SILK_ENC_INVALID_CBR_SETTING = -109;

    /* Internal encoder error */
    static int SILK_ENC_INTERNAL_ERROR = -110;

    /* Internal encoder error */
    static int SILK_ENC_INVALID_NUMBER_OF_CHANNELS_ERROR = -111;

    // Decoder error messages

    /* Output sampling frequency lower than internal decoded sampling frequency */
    static int SILK_DEC_INVALID_SAMPLING_FREQUENCY = -200;

    /* Payload size exceeded the maximum allowed 1024 bytes */
    static int SILK_DEC_PAYLOAD_TOO_LARGE = -201;

    /* Payload has bit errors */
    static int SILK_DEC_PAYLOAD_ERROR = -202;

    /* Payload has bit errors */
    static int SILK_DEC_INVALID_FRAME_SIZE = -203;
}
