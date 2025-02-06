package org.concentus;

class LTPScaleControl {

    /* Calculation of LTP state scaling */
    static void silk_LTP_scale_ctrl(
            SilkChannelEncoder psEnc, /* I/O  encoder state                                                               */
            SilkEncoderControl psEncCtrl, /* I/O  encoder control                                                             */
            int condCoding /* I    The type of conditional coding to use                                       */
    ) {
        int round_loss;

        if (condCoding == SilkConstants.CODE_INDEPENDENTLY) {
            /* Only scale if first frame in packet */
            round_loss = psEnc.PacketLoss_perc + psEnc.nFramesPerPacket;
            psEnc.indices.LTP_scaleIndex = (byte) Inlines.silk_LIMIT(
                    Inlines.silk_SMULWB(Inlines.silk_SMULBB(round_loss, psEncCtrl.LTPredCodGain_Q7), ((int) ((0.1f) * ((long) 1 << (9)) + 0.5))/*Inlines.SILK_CONST(0.1f, 9)*/), 0, 2);
        } else {
            /* Default is minimum scaling */
            psEnc.indices.LTP_scaleIndex = 0;
        }
        psEncCtrl.LTP_scale_Q14 = SilkTables.silk_LTPScales_table_Q14[psEnc.indices.LTP_scaleIndex];
    }
}
