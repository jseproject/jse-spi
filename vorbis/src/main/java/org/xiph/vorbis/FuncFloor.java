package org.xiph.vorbis;

/**
 * this would all be simpler/shorter with templates, but....<p>
 * Floor backend generic
 */
abstract class FuncFloor {
    final boolean is_pack_supported;

    //
    FuncFloor(boolean isPack) {
        is_pack_supported = isPack;
    }

    //
    abstract void pack(InfoFloor i, Buffer opg);

    abstract InfoFloor unpack(Info vi, Buffer opb);

    abstract LookFloor look(DspState vd, InfoFloor in);

    //abstract void free_info(InfoFloor);// use InfoFloor = null
    //abstract void free_look(LookFloor);// use LookFloor = null
    abstract Object inverse1(Block vb, LookFloor in);

    abstract boolean inverse2(Block vb, LookFloor in, Object buffer, float[] out);
}
