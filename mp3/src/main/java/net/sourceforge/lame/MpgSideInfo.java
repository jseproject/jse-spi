package net.sourceforge.lame;

class MpgSideInfo {
	static final class CH {
		final MpgGrInfo gr[] = new MpgGrInfo[2];
		private CH() {
			gr[0] = new MpgGrInfo();
			gr[1] = new MpgGrInfo();
		}
		private final void clear() {
			gr[0].clear();
			gr[1].clear();
		}
	}
	int main_data_begin;
	int private_bits;
	final CH ch[] = new CH[2];
	//
	MpgSideInfo() {
		ch[0] = new CH();
		ch[1] = new CH();
	}
	final void clear() {
		main_data_begin = 0;
		private_bits = 0;
		ch[0].clear();
		ch[1].clear();
	}
}
