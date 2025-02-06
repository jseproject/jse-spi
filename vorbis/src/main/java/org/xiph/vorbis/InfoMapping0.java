package org.xiph.vorbis;

public class InfoMapping0 extends InfoMapping {
    /**
     * <= 16
     */
    int submaps = 0;
    /**
     * up to 256 channels in a Vorbis stream
     */
    final int[] chmuxlist = new int[256];

    /**
     * [mux] submap to floors
     */
    final int[] floorsubmap = new int[16];
    /**
     * [mux] submap to residue
     */
    final int[] residuesubmap = new int[16];

    int coupling_steps = 0;
    final int[] coupling_mag = new int[256];
    final int[] coupling_ang = new int[256];

    //
    InfoMapping0() {
    }

    public InfoMapping0(
            int i_submaps,
            final int[] pi_chmuxlist,
            final int[] pi_floorsubmap,
            final int[] pi_residuesubmap,
            int i_coupling_steps,
            final int[] pi_coupling_mag,
            final int[] pi_coupling_ang) {
        submaps = i_submaps;
        System.arraycopy(pi_chmuxlist, 0, chmuxlist, 0, pi_chmuxlist.length);
        System.arraycopy(pi_floorsubmap, 0, floorsubmap, 0, pi_floorsubmap.length);
        System.arraycopy(pi_residuesubmap, 0, residuesubmap, 0, pi_residuesubmap.length);
        coupling_steps = i_coupling_steps;
        System.arraycopy(pi_coupling_mag, 0, coupling_mag, 0, pi_coupling_mag.length);
        System.arraycopy(pi_coupling_ang, 0, coupling_ang, 0, pi_coupling_ang.length);
    }

    InfoMapping0(InfoMapping0 m) {
        submaps = m.submaps;
        System.arraycopy(m.chmuxlist, 0, chmuxlist, 0, m.chmuxlist.length);
        System.arraycopy(m.floorsubmap, 0, floorsubmap, 0, m.floorsubmap.length);
        System.arraycopy(m.residuesubmap, 0, residuesubmap, 0, m.residuesubmap.length);
        coupling_steps = m.coupling_steps;
        System.arraycopy(m.coupling_mag, 0, coupling_mag, 0, m.coupling_mag.length);
        System.arraycopy(m.coupling_ang, 0, coupling_ang, 0, m.coupling_ang.length);
    }
}
