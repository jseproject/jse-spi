package org.xiph.vorbis;

/**
 * Mapping backend generic
 */
abstract class FuncMapping {
    abstract void pack(Info vi, InfoMapping vm, Buffer opb);

    abstract InfoMapping unpack(Info vi, Buffer opb);

    //abstract void free_info(InfoMapping vm);// use InfoMapping = null
    abstract int forward(Block vb);

    abstract int inverse(Block vb, InfoMapping vm);
}
