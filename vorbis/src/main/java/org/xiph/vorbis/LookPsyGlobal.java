package org.xiph.vorbis;

/**
 * psychoacoustic setup
 */
class LookPsyGlobal {
    float ampmax = 0;
    int channels = 0;

    InfoPsyGlobal gi = null;
    final int[][] coupling_pointlimit = new int[2][LookPsy.P_NOISECURVES];

    // psy.c
    // void _vp_global_free(vorbis_look_psy_global *look) // use LookPsyGlobal = null
	/*void _vp_global_free(LookPsyGlobal look) {
		if( look != null ) {
			memset(look,0,sizeof(*look));
			_ogg_free(look);
		}
	}*/
}
