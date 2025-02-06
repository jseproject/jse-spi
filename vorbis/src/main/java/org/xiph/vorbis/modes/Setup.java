package org.xiph.vorbis.modes;

import org.xiph.vorbis.InfoMapping0;

public class Setup {
    /* a few static coder conventions */
    // FIXME unused _mode_template
	/*private static final InfoMode _mode_template[] = {// [2]
		new InfoMode( 0,0,0,0 ),
		new InfoMode( 1,0,0,1 )
	};*/

	/* mapping conventions:
	only one submap (this would change for efficient 5.1 support for example)*/
    /* Four psychoacoustic profiles are used, one for each blocktype */

    protected static final InfoMapping0 _map_nominal[] = {// [2]
            new InfoMapping0(
                    1, new int[]{0, 0}, new int[]{0}, new int[]{0}, 1, new int[]{0}, new int[]{1}),
            new InfoMapping0(
                    1, new int[]{0, 0}, new int[]{1}, new int[]{1}, 1, new int[]{0}, new int[]{1})
    };

    public static final InfoMapping0 _map_nominal_u[] = {// [2]
            new InfoMapping0(1, new int[]{0, 0, 0, 0, 0, 0}, new int[]{0}, new int[]{0}, 0, new int[]{0}, new int[]{0}),
            new InfoMapping0(1, new int[]{0, 0, 0, 0, 0, 0}, new int[]{1}, new int[]{1}, 0, new int[]{0}, new int[]{0})
    };
}
