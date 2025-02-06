package org.xiph.vorbis;

public class MappingTemplate {
    final InfoMapping0[] map;
    final ResidueTemplate[] res;

    //
    public MappingTemplate(InfoMapping0[] pvim_map,
                           ResidueTemplate[] pvrt_res) {
        map = pvim_map;
        res = pvrt_res;
    }
}
