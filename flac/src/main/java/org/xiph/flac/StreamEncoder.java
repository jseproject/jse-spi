/*
 * Copyright (c) 2024 Naoko Mitsurugi
 * Copyright (c) 2000-2009 Josh Coalson
 * Copyright (c) 2011-2022 Xiph.Org Foundation
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Xiph.Org Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xiph.flac;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class StreamEncoder implements
		OggEncoderAspectWriteCallbackProxy,
		StreamDecoderReadCallback,// verify_read_callback_
		StreamDecoderWriteCallback,// verify_write_callback_
        StreamDecoderMetadataCallback,// verify_metadata_callback_
		StreamDecoderErrorCallback,// verify_error_callback_
		StreamEncoderReadCallback,// file_read_callback_
		StreamEncoderWriteCallback,// file_write_callback_,
		StreamEncoderSeekCallback,// file_seek_callback_,
		StreamEncoderTellCallback// file_tell_callback_
{
	/*
	 * This is used to avoid overflow with unusual signals in 32-bit
	 * accumulator in the *precompute_partition_info_sums_* functions.
	 */
	private static final int  FLAC__MAX_EXTRA_RESIDUAL_BPS = 4;
	/** State values for a FLAC__StreamEncoder.
	 *
	 * The encoder's state can be obtained by calling FLAC__stream_encoder_get_state().
	 *
	 * If the encoder gets into any other state besides \c FLAC__STREAM_ENCODER_OK
	 * or \c FLAC__STREAM_ENCODER_UNINITIALIZED, it becomes invalid for encoding and
	 * must be deleted with FLAC__stream_encoder_delete().
	 */
	//typedef enum {
		/** The encoder is in the normal OK state and samples can be processed. */
		public static final int FLAC__STREAM_ENCODER_OK = 0;

		/** The encoder is in the uninitialized state; one of the
		 * FLAC__stream_encoder_init_*() functions must be called before samples
		 * can be processed.
		 */
		public static final int FLAC__STREAM_ENCODER_UNINITIALIZED = 1;

		/** An error occurred in the underlying Ogg layer.  */
		public static final int FLAC__STREAM_ENCODER_OGG_ERROR = 2;

		/** An error occurred in the underlying verify stream decoder;
		 * check FLAC__stream_encoder_get_verify_decoder_state().
		 */
		public static final int FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR = 3;

		/** The verify decoder detected a mismatch between the original
		 * audio signal and the decoded audio signal.
		 */
		public static final int FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA = 4;

		/** One of the callbacks returned a fatal error. */
		public static final int FLAC__STREAM_ENCODER_CLIENT_ERROR = 5;

		/** An I/O error occurred while opening/reading/writing a file.
		 * Check \c errno.
		 */
		public static final int FLAC__STREAM_ENCODER_IO_ERROR = 6;

		/** An error occurred while writing the stream; usually, the
		 * write_callback returned an error.
		 */
		public static final int FLAC__STREAM_ENCODER_FRAMING_ERROR = 7;

		/** Memory allocation failed. */
		public static final int FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR = 8;

	//} FLAC__StreamEncoderState;

	public static final String  FLAC__StreamEncoderStateString[] = {
		"FLAC__STREAM_ENCODER_OK",
		"FLAC__STREAM_ENCODER_UNINITIALIZED",
		"FLAC__STREAM_ENCODER_OGG_ERROR",
		"FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR",
		"FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA",
		"FLAC__STREAM_ENCODER_CLIENT_ERROR",
		"FLAC__STREAM_ENCODER_IO_ERROR",
		"FLAC__STREAM_ENCODER_FRAMING_ERROR",
		"FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR"
	};

	/** Possible return values for the FLAC__stream_encoder_init_*() functions.
	 */
	//typedef enum {
		/** Initialization was successful. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_OK = 0;

		/** General failure to set up encoder; call FLAC__stream_encoder_get_state() for cause. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR = 1;

		/** The library was not compiled with support for the given container
		 * format.
		 */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_UNSUPPORTED_CONTAINER = 2;

		/** A required callback was not supplied. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_CALLBACKS = 3;

		/** The encoder has an invalid setting for number of channels. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_NUMBER_OF_CHANNELS = 4;

		/** The encoder has an invalid setting for bits-per-sample.
		 * FLAC supports 4-32 bps but the reference encoder currently supports
		 * only up to 24 bps.
		 */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BITS_PER_SAMPLE = 5;

		/** The encoder has an invalid setting for the input sample rate. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_SAMPLE_RATE = 6;

		/** The encoder has an invalid setting for the block size. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BLOCK_SIZE = 7;

		/** The encoder has an invalid setting for the maximum LPC order. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_MAX_LPC_ORDER = 8;

		/** The encoder has an invalid setting for the precision of the quantized linear predictor coefficients. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_QLP_COEFF_PRECISION = 9;

		/** The specified block size is less than the maximum LPC order. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_BLOCK_SIZE_TOO_SMALL_FOR_LPC_ORDER = 10;

		/** The encoder is bound to the <A HREF="../format.html#subset">Subset</A> but other settings violate it. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE = 11;

		/** The metadata input to the encoder is invalid, in one of the following ways:
		 * - FLAC__stream_encoder_set_metadata() was called with a null pointer but a block count > 0
		 * - One of the metadata blocks contains an undefined type
		 * - It contains an illegal CUESHEET as checked by FLAC__format_cuesheet_is_legal()
		 * - It contains an illegal SEEKTABLE as checked by FLAC__format_seektable_is_legal()
		 * - It contains more than one SEEKTABLE block or more than one VORBIS_COMMENT block
		 */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA = 12;

		/** FLAC__stream_encoder_init_*() was called when the encoder was
		 * already initialized, usually because
		 * FLAC__stream_encoder_finish() was not called.
		 */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED = 13;

	//} FLAC__StreamEncoderInitStatus;

	public static final String FLAC__StreamEncoderInitStatusString[] = {
		"FLAC__STREAM_ENCODER_INIT_STATUS_OK",
		"FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR",
		"FLAC__STREAM_ENCODER_INIT_STATUS_UNSUPPORTED_CONTAINER",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_CALLBACKS",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_NUMBER_OF_CHANNELS",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BITS_PER_SAMPLE",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_SAMPLE_RATE",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BLOCK_SIZE",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_MAX_LPC_ORDER",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_QLP_COEFF_PRECISION",
		"FLAC__STREAM_ENCODER_INIT_STATUS_BLOCK_SIZE_TOO_SMALL_FOR_LPC_ORDER",
		"FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA",
		"FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED"
	};

	/** Return values for the FLAC__StreamEncoder read callback.
	 */
	//typedef enum {// java: changed. uses IOException and UnsupportedOperationException
		/** The read was OK and decoding can continue. */
		//private static final int FLAC__STREAM_ENCODER_READ_STATUS_CONTINUE = 0;

		/** The read was attempted at the end of the stream. */
		//private static final int FLAC__STREAM_ENCODER_READ_STATUS_END_OF_STREAM = 1;

		/** An unrecoverable error occurred. */
		//private static final int FLAC__STREAM_ENCODER_READ_STATUS_ABORT = 2;

		/** Client does not support reading back from the output. */
		private static final int FLAC__STREAM_ENCODER_READ_STATUS_UNSUPPORTED = 3;
	//} FLAC__StreamEncoderReadStatus;

	/** Maps a FLAC__StreamEncoderReadStatus to a C string.
	 *
	 *  Using a FLAC__StreamEncoderReadStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	private static final String FLAC__StreamEncoderReadStatusString[] = {
		"FLAC__STREAM_ENCODER_READ_STATUS_CONTINUE",
		"FLAC__STREAM_ENCODER_READ_STATUS_END_OF_STREAM",
		"FLAC__STREAM_ENCODER_READ_STATUS_ABORT",
		"FLAC__STREAM_ENCODER_READ_STATUS_UNSUPPORTED"
	};

	/** Return values for the FLAC__StreamEncoder write callback.
	 */
	//typedef enum {
		/** The write was OK and encoding can continue. */
		public static final int FLAC__STREAM_ENCODER_WRITE_STATUS_OK = 0;

		/** An unrecoverable error occurred.  The encoder will return from the process call. */
		public static final int FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR = 1;

	//} FLAC__StreamEncoderWriteStatus;

	/** Maps a FLAC__StreamEncoderWriteStatus to a C string.
	 *
	 *  Using a FLAC__StreamEncoderWriteStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamEncoderWriteStatusString[] = {
		"FLAC__STREAM_ENCODER_WRITE_STATUS_OK",
		"FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR"
	};

	/** Return values for the FLAC__StreamEncoder seek callback.
	 */
	//typedef enum {
		/** The seek was OK and encoding can continue. */
		public static final int FLAC__STREAM_ENCODER_SEEK_STATUS_OK = 0;

		/** An unrecoverable error occurred. */
		public static final int FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR = 1;

		/** Client does not support seeking. */
		public static final int FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED = 2;

	//} FLAC__StreamEncoderSeekStatus;

	/** Maps a FLAC__StreamEncoderSeekStatus to a C string.
	 *
	 *  Using a FLAC__StreamEncoderSeekStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamEncoderSeekStatusString[] = {
		"FLAC__STREAM_ENCODER_SEEK_STATUS_OK",
		"FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR",
		"FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED"
	};

	/** Return values for the FLAC__StreamEncoder tell callback.
	 */
	//typedef enum {// java: changed. uses IOException, UnsupportedOperationException
		/** The tell was OK and encoding can continue. */
		//private static final int FLAC__STREAM_ENCODER_TELL_STATUS_OK = 0;

		/** An unrecoverable error occurred. */
		//private static final int FLAC__STREAM_ENCODER_TELL_STATUS_ERROR = 1;

		/** Client does not support seeking. */
		private static final int FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED = 2;

	//} FLAC__StreamEncoderTellStatus;

	/** Maps a FLAC__StreamEncoderTellStatus to a C string.
	 *
	 *  Using a FLAC__StreamEncoderTellStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	private static final String FLAC__StreamEncoderTellStatusString[] = {
		"FLAC__STREAM_ENCODER_TELL_STATUS_OK",
		"FLAC__STREAM_ENCODER_TELL_STATUS_ERROR",
		"FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED"
	};

	@Override
	public int ogg_write_callback(StreamEncoder encoder, byte[] buffer, int offset, int bytes, int samples, int current_frame) {
		if( Format.FLAC__HAS_OGG ) {

		return encoder.write_callback.enc_write_callback( encoder, buffer, offset, bytes, samples, current_frame/*, client_data*/ );
}
		return StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
	}

	private static final class ApodizationSpecification {
		//typedef enum {
			private static final int FLAC__APODIZATION_BARTLETT = 0;
			private static final int FLAC__APODIZATION_BARTLETT_HANN = 1;
			private static final int FLAC__APODIZATION_BLACKMAN = 2;
			private static final int FLAC__APODIZATION_BLACKMAN_HARRIS_4TERM_92DB_SIDELOBE = 3;
			private static final int FLAC__APODIZATION_CONNES = 4;
			private static final int FLAC__APODIZATION_FLATTOP = 5;
			private static final int FLAC__APODIZATION_GAUSS = 6;
			private static final int FLAC__APODIZATION_HAMMING = 7;
			private static final int FLAC__APODIZATION_HANN = 8;
			private static final int FLAC__APODIZATION_KAISER_BESSEL = 9;
			private static final int FLAC__APODIZATION_NUTTALL = 10;
			private static final int FLAC__APODIZATION_RECTANGLE = 11;
			private static final int FLAC__APODIZATION_TRIANGLE = 12;
			private static final int FLAC__APODIZATION_TUKEY = 13;
			private static final int FLAC__APODIZATION_PARTIAL_TUKEY = 14;
			private static final int FLAC__APODIZATION_PUNCHOUT_TUKEY = 15;
			private static final int FLAC__APODIZATION_SUBDIVIDE_TUKEY = 16;
			private static final int FLAC__APODIZATION_WELCH = 17;
		//} FLAC__ApodizationFunction;

		/** FLAC__ApodizationFunction */
		int /*FLAC__ApodizationFunction*/ type;
		/*union {// TODO check using union and try to find a better solution
			struct {
				FLAC__real stddev;
			} gauss;
			struct {
				FLAC__real p;
			} tukey;
			struct {
				FLAC__real p;
				FLAC__real start;
				FLAC__real end;
			} multiple_tukey;
			struct {
				FLAC__real p;
				FLAC__int32 parts;
			} subdivide_tukey;
		} parameters;*/
		float stddev;
		float p;
		float start;
		float end;
		int parts;
	}
	private static final int FLAC__MAX_APODIZATION_FUNCTIONS = 32;
	//static class StreamEncoderProtected {// java: changed by 'default'
		int /*StreamEncoderState*/ state;
		boolean do_verify;// java: duplicate, verify renamed to do_verify
		boolean streamable_subset;
		boolean do_md5;
		boolean do_mid_side_stereo;
		boolean loose_mid_side_stereo;
		int channels;
		int bits_per_sample;
		int sample_rate;
		int blocksize;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		int num_apodizations;
		final ApodizationSpecification apodizations[] = new ApodizationSpecification[FLAC__MAX_APODIZATION_FUNCTIONS];
//#endif
		int max_lpc_order;
		int qlp_coeff_precision;
		boolean do_qlp_coeff_prec_search;
		boolean do_exhaustive_model_search;
		boolean do_escape_coding;
		int min_residual_partition_order;
		int max_residual_partition_order;
		int rice_parameter_search_dist;
		long total_samples_estimate;
		boolean limit_min_bitrate;
		StreamMetadata[] metadata;
		// int num_metadata_blocks;// java: use metadata.length
		long streaminfo_offset, seektable_offset, audio_offset;
//if( FLAC__HAS_OGG ) {
		final OggEncoderAspect ogg_encoder_aspect = new OggEncoderAspect();

		/*StreamEncoderProtected() {// moved to base class constructor
			for( int i = 0; i < apodizations.length; i++ ) {
				apodizations[i] = new ApodizationSpecification();
			}
		}*/
//}
	//}
	//StreamEncoderProtected protected_; /* avoid the C++ keyword 'protected' */

	/***********************************************************************
	 *
	 * Private class data
	 *
	 ***********************************************************************/
	private static final class verify_input_fifo {
		private final int data[][] = new int[Format.FLAC__MAX_CHANNELS][];
		private int size; /* of each data[] in samples */
		private int tail;
		//
		private final void append_to_verify_fifo_(final int input[][], final int input_offset, final int channels, final int wide_samples)
		{
			final int[][] d = this.data;// java
			int t = this.tail;// java
			for( int channel = 0; channel < channels; channel++ ) {
				System.arraycopy( input[channel], input_offset, d[channel], t, wide_samples );
			}

			t += wide_samples;
			this.tail = t;

			//FLAC__ASSERT(fifo->tail <= fifo->size);
		}

		private final void append_to_verify_fifo_interleaved_(final int input[], final int input_offset, final int channels, final int wide_samples)
		{
			int t = this.tail;
			final int[][] d = this.data;// java

			int sample = input_offset * channels;
			for( int wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
				for( int channel = 0; channel < channels; channel++ ) {
					d[channel][t] = input[sample++];
				}
				t++;
			}
			this.tail = t;

			//FLAC__ASSERT(fifo->tail <= fifo->size);
		}
	}

	private static final class verify_output {
		byte[] data = null;
		int offset = 0;
		//int capacity = 0;// FIXME never used field
		int bytes = 0;
	}

	//typedef enum {
		private static final int ENCODER_IN_MAGIC = 0;
		private static final int ENCODER_IN_METADATA = 1;
		private static final int ENCODER_IN_AUDIO = 2;
	//} EncoderStateHint;

	private static class CompressionLevels {
		private final boolean do_mid_side_stereo;
		private final boolean loose_mid_side_stereo;
		private final int max_lpc_order;
		private final int qlp_coeff_precision;
		private final boolean do_qlp_coeff_prec_search;
		private final boolean do_escape_coding;
		private final boolean do_exhaustive_model_search;
		private final int min_residual_partition_order;
		private final int max_residual_partition_order;
		private final int rice_parameter_search_dist;
		private final String apodization;

		private CompressionLevels(
					final boolean is_do_mid_side_stereo,
					final boolean is_loose_mid_side_stereo,
					final int i_max_lpc_order,
					final int i_qlp_coeff_precision,
					final boolean is_do_qlp_coeff_prec_search,
					final boolean is_do_escape_coding,
					final boolean is_do_exhaustive_model_search,
					final int i_min_residual_partition_order,
					final int i_max_residual_partition_order,
					final int i_rice_parameter_search_dist,
					final String s_apodization) {
			do_mid_side_stereo = is_do_mid_side_stereo;
			loose_mid_side_stereo = is_loose_mid_side_stereo;
			max_lpc_order = i_max_lpc_order;
			qlp_coeff_precision = i_qlp_coeff_precision;
			do_qlp_coeff_prec_search = is_do_qlp_coeff_prec_search;
			do_escape_coding = is_do_escape_coding;
			do_exhaustive_model_search = is_do_exhaustive_model_search;
			min_residual_partition_order = i_min_residual_partition_order;
			max_residual_partition_order = i_max_residual_partition_order;
			rice_parameter_search_dist = i_rice_parameter_search_dist;
			apodization = s_apodization;
		}
	}
	private static final CompressionLevels compression_levels_[] = {
		new CompressionLevels( false, false,  0, 0, false, false, false, 0, 3, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , true ,  0, 0, false, false, false, 0, 3, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , false,  0, 0, false, false, false, 0, 3, 0, "tukey(5e-1)" ),
		new CompressionLevels( false, false,  6, 0, false, false, false, 0, 4, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , true ,  8, 0, false, false, false, 0, 4, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , false,  8, 0, false, false, false, 0, 5, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , false,  8, 0, false, false, false, 0, 6, 0, "subdivide_tukey(2)" ),
		new CompressionLevels( true , false, 12, 0, false, false, false, 0, 6, 0, "subdivide_tukey(2)" ),
		new CompressionLevels( true , false, 12, 0, false, false, false, 0, 6, 0, "subdivide_tukey(3)" )

		/* here we use locale-independent 5e-1 instead of 0.5 or 0,5 */
	};

	//private static class StreamEncoderPrivate {// now replaced by 'private'
	private int input_capacity;                          /* current size (in samples) of the signal and residual buffers */
	private final int integer_signal[][] = new int[Format.FLAC__MAX_CHANNELS][];  /* the integer version of the input signal */
	private final int integer_signal_mid_side[][] = new int[2][];          /* the integer version of the mid-side input signal (stereo only) */
	private long[] integer_signal_33bit_side;           /* 33-bit side for 32-bit stereo decorrelation */
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	//private final float real_signal[][] = new float[Format.FLAC__MAX_CHANNELS][];      /* (@@@ currently unused) the floating-point version of the input signal */
	private final float real_signal_mid_side[][] = new float[2][];              /* (@@@ currently unused) the floating-point version of the mid-side input signal (stereo only) */
	private final float window[][] = new float[FLAC__MAX_APODIZATION_FUNCTIONS][]; /* the pre-computed floating-point window for each apodization function */
	private float[] windowed_signal;                      /* the integer_signal[] * current window[] */
//#endif
	private final int subframe_bps[] = new int[Format.FLAC__MAX_CHANNELS];        /* the effective bits per sample of the input signal (stream bps - wasted bits) */
	private final int subframe_bps_mid_side[] = new int[2];                /* the effective bits per sample of the mid-side input signal (stream bps - wasted bits + 0/1) */
	private final int residual_workspace[][][] = new int[Format.FLAC__MAX_CHANNELS][2][]; /* each channel has a candidate and best workspace where the subframe residual signals will be stored */
	private final int residual_workspace_mid_side[][][] = new int[2][2][];
	private final Subframe subframe_workspace[][] = new Subframe[Format.FLAC__MAX_CHANNELS][2];
	private final Subframe subframe_workspace_mid_side[][] = new Subframe[2][2];
		//final Subframe subframe_workspace_ptr[][] = new Subframe[Format.FLAC__MAX_CHANNELS][2];// FIXME why do not use &subframe_workspace?
		//final Subframe subframe_workspace_ptr_mid_side[][] = new Subframe[2][2];// FIXME why do not use &subframe_workspace_mid_side?
	private final PartitionedRiceContents partitioned_rice_contents_workspace[][] = new PartitionedRiceContents[Format.FLAC__MAX_CHANNELS][2];
	private final PartitionedRiceContents partitioned_rice_contents_workspace_mid_side[][] = new PartitionedRiceContents[Format.FLAC__MAX_CHANNELS][2];
		//final PartitionedRiceContents partitioned_rice_contents_workspace_ptr[][] = new PartitionedRiceContents[Format.FLAC__MAX_CHANNELS][2];// FIXME why do not use &?
		//final PartitionedRiceContents partitioned_rice_contents_workspace_ptr_mid_side[][] = new PartitionedRiceContents[Format.FLAC__MAX_CHANNELS][2];// FIXME why do not use &?
	private final int best_subframe[] = new int[Format.FLAC__MAX_CHANNELS];       /* index (0 or 1) into 2nd dimension of the above workspaces */
	private final int best_subframe_mid_side[] = new int[2];
	private final int best_subframe_bits[] = new int[Format.FLAC__MAX_CHANNELS];  /* size in bits of the best subframe for each channel */
	private final int best_subframe_bits_mid_side[] = new int[2];
	private long[] abs_residual_partition_sums;/* workspace where the sum of abs(candidate residual) for each partition is stored */
	private int[] raw_bits_per_partition;/* workspace where the sum of silog2(candidate residual) for each partition is stored */
	private BitWriter frame;/* the current frame being worked on */
	private int loose_mid_side_stereo_frames;/* rounded number of frames the encoder will use before trying both independent and mid/side frames again */
	private int loose_mid_side_stereo_frame_count;/* number of frames using the current channel assignment */
	private int /* FLAC__ChannelAssignment */ last_channel_assignment;
	private final StreamInfo streaminfo = new StreamInfo(); /* scratchpad for STREAMINFO as it is built */
	private SeekTable seek_table;/* pointer into encoder->protected_->metadata_ where the seek table is */
	private int current_sample_number;
	private int current_frame_number;
	private final MD5Context md5context = new MD5Context();
		//FLAC__CPUInfo cpuinfo;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		//unsigned (*local_fixed_compute_best_predictor)(const FLAC__int32 data[], unsigned data_len, FLAC__float residual_bits_per_sample[FLAC__MAX_FIXED_ORDER + 1]);
//#else
//		unsigned (*local_fixed_compute_best_predictor)(const FLAC__int32 data[], unsigned data_len, FLAC__fixedpoint residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1]);
//#endif
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		//void (*local_lpc_compute_autocorrelation)(const FLAC__real data[], unsigned data_len, unsigned lag, FLAC__real autoc[]);
		//void (*local_lpc_compute_residual_from_qlp_coefficients)(const FLAC__int32 *data, unsigned data_len, const FLAC__int32 qlp_coeff[], unsigned order, int lp_quantization, FLAC__int32 residual[]);
		//void (*local_lpc_compute_residual_from_qlp_coefficients_64bit)(const FLAC__int32 *data, unsigned data_len, const FLAC__int32 qlp_coeff[], unsigned order, int lp_quantization, FLAC__int32 residual[]);
		//void (*local_lpc_compute_residual_from_qlp_coefficients_16bit)(const FLAC__int32 *data, unsigned data_len, const FLAC__int32 qlp_coeff[], unsigned order, int lp_quantization, FLAC__int32 residual[]);
//#endif
	private boolean disable_mmx;
	private boolean disable_sse2;
	private boolean disable_ssse3;
	private boolean disable_sse41;
	private boolean disable_avx2;
	private boolean disable_fma;
	private boolean disable_constant_subframes;
	private boolean disable_fixed_subframes;
	private boolean disable_verbatim_subframes;
	private boolean is_ogg;
	private StreamEncoderReadCallback read_callback; /* currently only needed for Ogg FLAC */
	private StreamEncoderSeekCallback seek_callback;
	private StreamEncoderTellCallback tell_callback;
	private StreamEncoderWriteCallback write_callback;
	private StreamEncoderMetadataCallback metadata_callback;
	private StreamEncoderProgressCallback progress_callback;
	// private Object client_data;// java: don't need
	private int first_seekpoint_to_check;
	private OutputStream file; /* only used when encoding to a file */
	private long bytes_written;
	private long samples_written;
	private int frames_written;
	private int total_frames_estimate;
	/* unaligned (original) pointers to allocated data */
	//private final int integer_signal_unaligned[][] = new int[Format.FLAC__MAX_CHANNELS][];
	//private final int integer_signal_mid_side_unaligned[][] = new int[2][];
	//private final long[] integer_signal_33bit_side_unaligned;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	//private final float real_signal_unaligned[][] = new float[Format.FLAC__MAX_CHANNELS][]; /* (@@@ currently unused) */
	//private final float real_signal_mid_side_unaligned[][] = new float[2][]; /* (@@@ currently unused) */
	//private final float window_unaligned[][] = new float[FLAC__MAX_APODIZATION_FUNCTIONS][];
	//private float[] windowed_signal_unaligned;
//#endif
	//private final int residual_workspace_unaligned[][][] = new int[Format.FLAC__MAX_CHANNELS][2][];
	//private final int residual_workspace_mid_side_unaligned[][][] = new int[2][2][];
	//private long[] abs_residual_partition_sums_unaligned;
	//private int[] raw_bits_per_partition_unaligned;
		/*
		 * These fields have been moved here from private function local
		 * declarations merely to save stack space during encoding.
		 */
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	private final float lp_coeff[][] = new float[Format.FLAC__MAX_LPC_ORDER][Format.FLAC__MAX_LPC_ORDER]; /* from process_subframe_() */
//#endif
	private final PartitionedRiceContents partitioned_rice_contents_extra[] = new PartitionedRiceContents[2]; /* from find_best_partition_order_() */
		/*
		 * The data for the verify section
		 */
	private static final class verify {
		private StreamDecoder decoder;
		private int /* EncoderStateHint */ state_hint;
		private boolean needs_magic_hack;
		private final verify_input_fifo input_fifo = new verify_input_fifo();
		private final verify_output output = new verify_output();

		private final ErrorStats error_stats = new ErrorStats();
	}
	private final StreamEncoder.verify verify = new verify();
	private boolean is_being_deleted; /* if true, call to ..._finish() from ..._delete() will not call the callbacks */

		/*private StreamEncoderPrivate() {// creating moved to base class constructor
			for( int i = 0; i < 2; i++ ) {
				for( int k = 0; k < 2; k++ ) {
					subframe_workspace_mid_side[k][i] = new Subframe();
				}
				for( int c = 0; c < Format.FLAC__MAX_CHANNELS; c++ ) {
					subframe_workspace[c][i] = new Subframe();
					partitioned_rice_contents_workspace[c][i] = new PartitionedRiceContents();
					partitioned_rice_contents_workspace_mid_side[c][i] = new PartitionedRiceContents();
				}
				partitioned_rice_contents_extra[i] = new PartitionedRiceContents();
			}
		}*/
	//}// FLAC__StreamEncoderPrivate;
	//StreamEncoderPrivate private_; /* avoid the C++ keyword 'private' */

	/** Number of samples that will be overread to watch for end of stream.  By
	 * 'overread', we mean that the FLAC__stream_encoder_process*() calls will
	 * always try to read blocksize+1 samples before encoding a block, so that
	 * even if the stream has a total sample count that is an integral multiple
	 * of the blocksize, we will still notice when we are encoding the last
	 * block.  This is needed, for example, to correctly set the end-of-stream
	 * marker in Ogg FLAC.
	 *
	 * WATCHOUT: some parts of the code assert that OVERREAD_ == 1 and there's
	 * not really any reason to change it.
	 */
	private static final int OVERREAD_ = 1;

	/***********************************************************************
	 *
	 * Class constructor/destructor
	 *
	 */
	public StreamEncoder()// FLAC__stream_encoder_new()
	{
		//StreamEncoder encoder;
		//FLAC__ASSERT(sizeof(int) >= 4); /* we want to die right away if this is not true */

		//encoder = new StreamEncoder();

		//encoder.protected_ = new StreamEncoderProtected();
		for( int i = 0; i < this.apodizations.length; i++ ) {
			this.apodizations[i] = new ApodizationSpecification();
		}

		//encoder.private_ = new StreamEncoderPrivate();
		for( int i = 0; i < 2; i++ ) {
			for( int k = 0; k < 2; k++ ) {
				this.subframe_workspace_mid_side[k][i] = new Subframe();
			}
			for(int c = 0; c < Format.FLAC__MAX_CHANNELS; c++ ) {
				this.subframe_workspace[c][i] = new Subframe();
				this.partitioned_rice_contents_workspace[c][i] = new PartitionedRiceContents();
				this.partitioned_rice_contents_workspace_mid_side[c][i] = new PartitionedRiceContents();
			}
			this.partitioned_rice_contents_extra[i] = new PartitionedRiceContents();
		}

		this.frame = new BitWriter();

		this.file = null;

		this.state = FLAC__STREAM_ENCODER_UNINITIALIZED;

		set_defaults_();

		this.is_being_deleted = false;

		/*for( i = 0; i < Format.FLAC__MAX_CHANNELS; i++ ) {
			encoder.private_.subframe_workspace_ptr[i][0] = encoder.private_.subframe_workspace[i][0];
			encoder.private_.subframe_workspace_ptr[i][1] = encoder.private_.subframe_workspace[i][1];
		}
		for( i = 0; i < 2; i++ ) {
			encoder.private_.subframe_workspace_ptr_mid_side[i][0] = encoder.private_.subframe_workspace_mid_side[i][0];
			encoder.private_.subframe_workspace_ptr_mid_side[i][1] = encoder.private_.subframe_workspace_mid_side[i][1];
		}
		for( i = 0; i < Format.FLAC__MAX_CHANNELS; i++ ) {
			encoder.private_.partitioned_rice_contents_workspace_ptr[i][0] = encoder.private_.partitioned_rice_contents_workspace[i][0];
			encoder.private_.partitioned_rice_contents_workspace_ptr[i][1] = encoder.private_.partitioned_rice_contents_workspace[i][1];
		}
		for( i = 0; i < 2; i++ ) {
			encoder.private_.partitioned_rice_contents_workspace_ptr_mid_side[i][0] = encoder.private_.partitioned_rice_contents_workspace_mid_side[i][0];
			encoder.private_.partitioned_rice_contents_workspace_ptr_mid_side[i][1] = encoder.private_.partitioned_rice_contents_workspace_mid_side[i][1];
		}*/

		for(int i = 0; i < Format.FLAC__MAX_CHANNELS; i++ ) {
			this.partitioned_rice_contents_workspace[i][0].init();
			this.partitioned_rice_contents_workspace[i][1].init(  );
		}
		for( int i = 0; i < 2; i++ ) {
			this.partitioned_rice_contents_workspace_mid_side[i][0].init();
			this.partitioned_rice_contents_workspace_mid_side[i][1].init();
		}
		for( int i = 0; i < 2; i++ ) {
			this.partitioned_rice_contents_extra[i].init();
		}

		//return encoder;
	}

	public final void delete()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->protected_);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->private_->frame);

		this.is_being_deleted = true;

		finish();

		if( null != this.verify.decoder ) {
			this.verify.decoder.delete();
		}

		for(int i = 0; i < Format.FLAC__MAX_CHANNELS; i++ ) {
			this.partitioned_rice_contents_workspace[i][0].clear();
			this.partitioned_rice_contents_workspace[i][1].clear();
		}
		for( int i = 0; i < 2; i++ ) {
			this.partitioned_rice_contents_workspace_mid_side[i][0].clear();
			this.partitioned_rice_contents_workspace_mid_side[i][1].clear();
		}
		for( int i = 0; i < 2; i++ ) {
			this.partitioned_rice_contents_extra[i].clear();
		}

		this.frame = null;
		//encoder.private_ = null;
		//encoder.protected_ = null;
		//free( encoder );
	}

	/***********************************************************************
	 *
	 * Public class methods
	 *
	 ***********************************************************************/

	private final int /* FLAC__StreamEncoderInitStatus */ init_stream_internal_(
		final StreamEncoderReadCallback read_cb,
		final StreamEncoderWriteCallback write_cb,
		final StreamEncoderSeekCallback seek_cb,
		final StreamEncoderTellCallback tell_cb,
		final StreamEncoderMetadataCallback metadata_cb,
		// final Object client_data,
		final boolean isogg
	)
	{
		//FLAC__ASSERT(0 != encoder);

		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED;
		}

		if( ! Format.FLAC__HAS_OGG && isogg ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_UNSUPPORTED_CONTAINER;
		}

		if( null == write_cb || (seek_cb != null && null == tell_cb) ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_CALLBACKS;
		}

		if( this.channels == 0 || this.channels > Format.FLAC__MAX_CHANNELS ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_NUMBER_OF_CHANNELS;
		}

		if( this.channels != 2 ) {
			this.do_mid_side_stereo = false;
			this.loose_mid_side_stereo = false;
		}
		else if( ! this.do_mid_side_stereo ) {
			this.loose_mid_side_stereo = false;
		}

		if( this.bits_per_sample < Format.FLAC__MIN_BITS_PER_SAMPLE || this.bits_per_sample > Format.FLAC__MAX_BITS_PER_SAMPLE ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BITS_PER_SAMPLE;
		}

		if( ! Format.sample_rate_is_valid( this.sample_rate ) ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_SAMPLE_RATE;
		}

		if( this.blocksize == 0 ) {
			if( this.max_lpc_order == 0 ) {
				this.blocksize = 1152;
			} else {
				this.blocksize = 4096;
			}
		}

		if( this.blocksize < Format.FLAC__MIN_BLOCK_SIZE || this.blocksize > Format.FLAC__MAX_BLOCK_SIZE ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BLOCK_SIZE;
		}

		if( this.max_lpc_order > Format.FLAC__MAX_LPC_ORDER ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_MAX_LPC_ORDER;
		}

		if( this.blocksize < this.max_lpc_order ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_BLOCK_SIZE_TOO_SMALL_FOR_LPC_ORDER;
		}

		if( this.qlp_coeff_precision == 0 ) {
			if( this.bits_per_sample < 16 ) {
				/* @@@ need some data about how to set this here w.r.t. blocksize and sample rate */
				/* @@@ until then we'll make a guess */
				int i = 2 + this.bits_per_sample >>> 1;
				if( i < Format.FLAC__MIN_QLP_COEFF_PRECISION ) {
					i = Format.FLAC__MIN_QLP_COEFF_PRECISION;
				}
				this.qlp_coeff_precision = i;
			}
			else if( this.bits_per_sample == 16 ) {
				if( this.blocksize <= 192 ) {
					this.qlp_coeff_precision = 7;
				} else if( this.blocksize <= 384 ) {
					this.qlp_coeff_precision = 8;
				} else if( this.blocksize <= 576 ) {
					this.qlp_coeff_precision = 9;
				} else if( this.blocksize <= 1152 ) {
					this.qlp_coeff_precision = 10;
				} else if( this.blocksize <= 2304 ) {
					this.qlp_coeff_precision = 11;
				} else if( this.blocksize <= 4608 ) {
					this.qlp_coeff_precision = 12;
				} else {
					this.qlp_coeff_precision = 13;
				}
			}
			else {
				if( this.blocksize <= 384 ) {
					this.qlp_coeff_precision = Format.FLAC__MAX_QLP_COEFF_PRECISION - 2;
				} else if( this.blocksize <= 1152 ) {
					this.qlp_coeff_precision = Format.FLAC__MAX_QLP_COEFF_PRECISION - 1;
				} else {
					this.qlp_coeff_precision = Format.FLAC__MAX_QLP_COEFF_PRECISION;
				}
			}
			//FLAC__ASSERT(encoder.protected_.qlp_coeff_precision <= FLAC__MAX_QLP_COEFF_PRECISION);
		}
		else if( this.qlp_coeff_precision < Format.FLAC__MIN_QLP_COEFF_PRECISION || this.qlp_coeff_precision > Format.FLAC__MAX_QLP_COEFF_PRECISION ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_QLP_COEFF_PRECISION;
		}

		if( this.streamable_subset ) {
			if( ! Format.blocksize_is_subset( this.blocksize, this.sample_rate ) ) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
			if( ! Format.sample_rate_is_subset( this.sample_rate ) ) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
			if(
				this.bits_per_sample != 8 &&
				this.bits_per_sample != 12 &&
				this.bits_per_sample != 16 &&
				this.bits_per_sample != 20 &&
				this.bits_per_sample != 24 &&
				this.bits_per_sample != 32
			) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
			if( this.max_residual_partition_order > Format.FLAC__SUBSET_MAX_RICE_PARTITION_ORDER ) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
			if(
				this.sample_rate <= 48000 &&
				(
					this.blocksize > Format.FLAC__SUBSET_MAX_BLOCK_SIZE_48000HZ ||
					this.max_lpc_order > Format.FLAC__SUBSET_MAX_LPC_ORDER_48000HZ
				)
			) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
		}

		if( this.max_residual_partition_order >= (1 << Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN) ) {
			this.max_residual_partition_order = (1 << Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN) - 1;
		}
		if( this.min_residual_partition_order >= this.max_residual_partition_order ) {
			this.min_residual_partition_order = this.max_residual_partition_order;
		}

		final StreamMetadata[] meta_data = this.metadata;// java
if( Format.FLAC__HAS_OGG ) {

		/* reorder metadata if necessary to ensure that any VORBIS_COMMENT is the first, according to the mapping spec */
		if( isogg && null != meta_data/* && this.num_metadata_blocks > 1*/ ) {
			for( int i = 1, ie = meta_data.length; i < ie/* this.num_metadata_blocks */; i++ ) {
				if( null != meta_data[i] && meta_data[i].type == Format.FLAC__METADATA_TYPE_VORBIS_COMMENT ) {
					final StreamMetadata vc = meta_data[i];
					for( ; i > 0; i--) {
						meta_data[i] = meta_data[i - 1];
					}
					meta_data[0] = vc;
					break;
				}
			}
		}
}
		/* keep track of any SEEKTABLE block */
		if( null != meta_data/* && metadata_blocks > 0*/ ) {
			for( int i = 0, ie = meta_data.length; i < ie/* this.num_metadata_blocks */; i++ ) {
				if( null != meta_data[i] && meta_data[i].type == Format.FLAC__METADATA_TYPE_SEEKTABLE ) {
					this.seek_table = (SeekTable)meta_data[i];
					break; /* take only the first one */
				}
			}
		}

		/* validate metadata */
		/* if( null == meta_data && this.num_metadata_blocks > 0 ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
		}*/
		boolean metadata_has_vorbis_comment = false;
		if( null != meta_data ) {// java
			boolean metadata_has_seektable = false;// FIXME never uses metadata_has_seektable, metadata_picture_has_type1, metadata_picture_has_type2
			boolean metadata_picture_has_type1 = false;
			boolean metadata_picture_has_type2 = false;
			for( int i = 0, ie = meta_data.length; i < ie/* this.num_metadata_blocks */; i++ ) {
				final StreamMetadata m = meta_data[i];
				if( m.type == Format.FLAC__METADATA_TYPE_STREAMINFO ) {
					return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
				} else if( m.type == Format.FLAC__METADATA_TYPE_SEEKTABLE ) {
					if( metadata_has_seektable ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
					metadata_has_seektable = true;
					if( ! ((SeekTable)m).FLAC__format_seektable_is_legal() ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
				}
				else if( m.type == Format.FLAC__METADATA_TYPE_VORBIS_COMMENT ) {
					if( metadata_has_vorbis_comment ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
					metadata_has_vorbis_comment = true;
				}
				else if( m.type == Format.FLAC__METADATA_TYPE_CUESHEET ) {
					if( null != ((CueSheet)m).format_cuesheet_is_legal( ((CueSheet)m).is_cd ) ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
				}
				else if( m.type == Format.FLAC__METADATA_TYPE_PICTURE ) {
					final Picture picture = (Picture)m;
					if( null != picture.format_picture_is_legal() ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
					if( picture.picture_type == Format.FLAC__STREAM_METADATA_PICTURE_TYPE_FILE_ICON_STANDARD ) {
						if( metadata_picture_has_type1 ) {
							return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
						}
						metadata_picture_has_type1 = true;
						/* standard icon must be 32x32 pixel PNG */
						if(
							picture.picture_type == Format.FLAC__STREAM_METADATA_PICTURE_TYPE_FILE_ICON_STANDARD &&
							(
								(! picture.mime_type.equals("image/png") && ! picture.mime_type.equals("-.") ) ||
									picture.width != 32 ||
										picture.height != 32
							)
						) {
							return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
						}
					}
					else if( picture.picture_type == Format.FLAC__STREAM_METADATA_PICTURE_TYPE_FILE_ICON ) {
						if( metadata_picture_has_type2 ) {
							return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
						}
						metadata_picture_has_type2 = true;
					}
				}
			}
		}

		this.input_capacity = 0;
/*		for( i = 0; i < this.channels; i++ ) {
			this.integer_signal_unaligned[i] = this.integer_signal[i] = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
			this.real_signal_unaligned[i] = this.real_signal[i] = null;
//#endif
		}*/
		for( int i = 0; i < 2; i++ ) {
			/*this.integer_signal_mid_side_unaligned[i] = */this.integer_signal_mid_side[i] = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
			/*this.real_signal_mid_side_unaligned[i] = */this.real_signal_mid_side[i] = null;
//#endif
		}
		/*this.integer_signal_33bit_side_unaligned = */this.integer_signal_33bit_side = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		for( int i = 0; i < this.num_apodizations; i++ ) {
			/*this.window_unaligned[i] = */this.window[i] = null;
		}
		/*this.windowed_signal_unaligned = */this.windowed_signal = null;
//#endif
		for( int i = 0; i < this.channels; i++ ) {
			/*this.residual_workspace_unaligned[i][0] = */this.residual_workspace[i][0] = null;
			/*this.residual_workspace_unaligned[i][1] = */this.residual_workspace[i][1] = null;
			this.best_subframe[i] = 0;
		}
		for( int i = 0; i < 2; i++ ) {
			/*this.residual_workspace_mid_side_unaligned[i][0] = */this.residual_workspace_mid_side[i][0] = null;
			/*this.residual_workspace_mid_side_unaligned[i][1] = */this.residual_workspace_mid_side[i][1] = null;
			this.best_subframe_mid_side[i] = 0;
		}
		/*this.abs_residual_partition_sums_unaligned = */this.abs_residual_partition_sums = null;
		/*this.raw_bits_per_partition_unaligned = */this.raw_bits_per_partition = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		this.loose_mid_side_stereo_frames = (int)((double)this.sample_rate * 0.4 / (double)this.blocksize + 0.5);
//#else
		/* 26214 is the approximate fixed-point equivalent to 0.4 (0.4 * 2^16) */
		/* sample rate can be up to 1048575 Hz, and thus use 20 bits, so we do the multiply&divide by hand */
/*		FLAC__ASSERT(FLAC__MAX_SAMPLE_RATE <= 1048575);
		FLAC__ASSERT(FLAC__MAX_BLOCK_SIZE <= 65535);
		FLAC__ASSERT(encoder->protected_->sample_rate <= 1048575);
		FLAC__ASSERT(encoder.protected_.blocksize <= 65535);
		encoder.private_.loose_mid_side_stereo_frames = (unsigned)FLAC__fixedpoint_trunc((((FLAC__uint64)(encoder.protected_.sample_rate) * (FLAC__uint64)(26214)) << 16) / (encoder.protected_.blocksize<<16) + FLAC__FP_ONE_HALF);
#endif*/
		if( this.loose_mid_side_stereo_frames == 0 ) {
			this.loose_mid_side_stereo_frames = 1;
		}
		this.loose_mid_side_stereo_frame_count = 0;
		this.current_sample_number = 0;
		this.current_frame_number = 0;

		/* set state to OK; from here on, errors are fatal and we'll override the state then */
		this.state = FLAC__STREAM_ENCODER_OK;

if( Format.FLAC__HAS_OGG ) {
		this.is_ogg = isogg;
		if( isogg && ! this.ogg_encoder_aspect.init() ) {
			this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}
}

		this.read_callback = read_cb;
		this.write_callback = write_cb;
		this.seek_callback = seek_cb;
		this.tell_callback = tell_cb;
		this.metadata_callback = metadata_cb;
		// this.client_data = client_data;

		if( ! resize_buffers_( this.blocksize ) ) {
			/* the above function sets the state for us in case of an error */
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		if( ! this.frame.init() ) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		/*
		 * Set up the verify stuff if necessary
		 */
		if( this.do_verify ) {
			/*
			 * First, set up the fifo which will hold the
			 * original signal to compare against
			 */
			this.verify.input_fifo.size = this.blocksize + OVERREAD_;
			for( int i = 0; i < this.channels; i++ ) {
				try {
					this.verify.input_fifo.data[i] = new int[this.verify.input_fifo.size];
				} catch( final OutOfMemoryError e ) {
					this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
					return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
				}
			}
			this.verify.input_fifo.tail = 0;

			/*
			 * Now set up a stream decoder for verification
			 */
			if( null == this.verify.decoder ) {
				this.verify.decoder = new StreamDecoder();
				if( null == this.verify.decoder ) {
					this.state = FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR;
					return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
				}
			}

			if( this.verify.decoder.init_stream(
					this,// verify_read_callback_,
					/*seek_callback=*/null,
					/*tell_callback=*/null,
					/*length_callback=*/null,
					/*eof_callback=*/null,
					this,// verify_write_callback_,
					this,// verify_metadata_callback_,
					this//,// verify_error_callback_,
					/*client_data = this*/ ) != StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK ) {
				this.state = FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR;
				return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
			}
		}
		this.verify.error_stats.absolute_sample = 0;
		this.verify.error_stats.frame_number = 0;
		this.verify.error_stats.channel = 0;
		this.verify.error_stats.sample = 0;
		this.verify.error_stats.expected = 0;
		this.verify.error_stats.got = 0;

		/*
		 * These must be done before we write any metadata, because that
		 * calls the write_callback, which uses these values.
		 */
		this.first_seekpoint_to_check = 0;
		this.samples_written = 0;
		this.streaminfo_offset = 0;
		this.seektable_offset = 0;
		this.audio_offset = 0;

		/*
		 * write the stream header
		 */
		if( this.do_verify ) {
			this.verify.state_hint = ENCODER_IN_MAGIC;
		}
		if( ! this.frame.write_raw_uint32( Format.FLAC__STREAM_SYNC, Format.FLAC__STREAM_SYNC_LEN ) ) {
			this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}
		if( ! write_bitbuffer_( 0, /*is_last_block=*/false ) ) {
			/* the above function sets the state for us in case of an error */
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		/*
		 * write the STREAMINFO metadata block
		 */
		if( this.do_verify ) {
			this.verify.state_hint = ENCODER_IN_METADATA;
		}
		this.streaminfo.type = Format.FLAC__METADATA_TYPE_STREAMINFO;
		this.streaminfo.is_last = false; /* we will have at a minimum a VORBIS_COMMENT afterwards */
		this.streaminfo.length = StreamInfo.FLAC__STREAM_METADATA_STREAMINFO_LENGTH;
		final StreamInfo stream_info = this.streaminfo;
		stream_info.min_blocksize = this.blocksize; /* this encoder uses the same blocksize for the whole stream */
		stream_info.max_blocksize = this.blocksize;
		stream_info.min_framesize = 0; /* we don't know this yet; have to fill it in later */
		stream_info.max_framesize = 0; /* we don't know this yet; have to fill it in later */
		stream_info.sample_rate = this.sample_rate;
		stream_info.channels = this.channels;
		stream_info.bits_per_sample = this.bits_per_sample;
		stream_info.total_samples = this.total_samples_estimate; /* we will replace this later with the real total */
		Arrays.fill( stream_info.md5sum, 0, 16, (byte)0 );/* we don't know this yet; have to fill it in later */
		if( this.do_md5 ) {
			this.md5context.MD5Init();
		}
		if( ! StreamMetadata.add_metadata_block( this.streaminfo, this.frame ) ) {
			this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}
		if( ! write_bitbuffer_( 0, /*is_last_block=*/false ) ) {
			/* the above function sets the state for us in case of an error */
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		/*
		 * Now that the STREAMINFO block is written, we can init this to an
		 * absurdly-high value...
		 */
		stream_info.min_framesize = (1 << Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN) - 1;
		/* ... and clear this to 0 */
		stream_info.total_samples = 0;

		/*
		 * Check to see if the supplied metadata contains a VORBIS_COMMENT;
		 * if not, we will write an empty one (FLAC__add_metadata_block()
		 * automatically supplies the vendor string).
		 *
		 * WATCHOUT: the Ogg FLAC mapping requires us to write this block after
		 * the STREAMINFO.  (In the case that metadata_has_vorbis_comment is
		 * true it will have already insured that the metadata list is properly
		 * ordered.)
		 */
		if( ! metadata_has_vorbis_comment ) {
			final VorbisComment vorbis_comment = new VorbisComment();
			vorbis_comment.type = Format.FLAC__METADATA_TYPE_VORBIS_COMMENT;
			vorbis_comment.is_last = (meta_data == null);// (this.num_metadata_blocks == 0);
			vorbis_comment.length = 4 + 4; /* MAGIC NUMBER */
			vorbis_comment.vendor_string = null;
			vorbis_comment.num_comments = 0;
			vorbis_comment.comments = null;
			if( ! StreamMetadata.add_metadata_block( vorbis_comment, this.frame ) ) {
				this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
				return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
			}
			if( ! write_bitbuffer_( 0, /*is_last_block=*/false ) ) {
				/* the above function sets the state for us in case of an error */
				return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
			}
		}

		if( meta_data != null ) {// java
			/*
			 * write the user's metadata blocks
			 */
			final int metadata_blocks = meta_data.length;
			for( int i = 0; i < metadata_blocks /* this.num_metadata_blocks */; i++ ) {
				meta_data[i].is_last = (i == metadata_blocks - 1);
				if( ! StreamMetadata.add_metadata_block( meta_data[i], this.frame ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
				}
				if( ! write_bitbuffer_( 0, /*is_last_block=*/false ) ) {
					/* the above function sets the state for us in case of an error */
					return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
				}
			}
		}

		/* now that all the metadata is written, we save the stream offset */
		try {
			if( this.tell_callback != null ) {
				this.audio_offset = this.tell_callback.enc_tell_callback( this/*, this.client_data*/ );/* FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED just means we didn't get the offset; no error */
			}
		} catch(final IOException e) {
			this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		} catch(final UnsupportedOperationException e) {
		}

		if( this.do_verify ) {
			this.verify.state_hint = ENCODER_IN_AUDIO;
		}

		return FLAC__STREAM_ENCODER_INIT_STATUS_OK;
	}

	/** Initialize the encoder instance to encode native FLAC streams.
	 *
	 *  This flavor of initialization sets up the encoder to encode to a
	 *  native FLAC stream. I/O is performed via callbacks to the client.
	 *  For encoding to a plain file via filename or open \c FILE*,
	 *  FLAC__stream_encoder_init_file() and FLAC__stream_encoder_init_FILE()
	 *  provide a simpler interface.
	 *
	 *  This function should be called after FLAC__stream_encoder_new() and
	 *  FLAC__stream_encoder_set_*() but before FLAC__stream_encoder_process()
	 *  or FLAC__stream_encoder_process_interleaved().
	 *  initialization succeeded.
	 *
	 *  The call to FLAC__stream_encoder_init_stream() currently will also
	 *  immediately call the write callback several times, once with the \c fLaC
	 *  signature, and once for each encoded metadata block.
	 *
	 * \param  encoder            An uninitialized encoder instance.
	 * \param  write_callback     See FLAC__StreamEncoderWriteCallback.  This
	 *                            pointer must not be \c NULL.
	 * \param  seek_callback      See FLAC__StreamEncoderSeekCallback.  This
	 *                            pointer may be \c NULL if seeking is not
	 *                            supported.  The encoder uses seeking to go back
	 *                            and write some some stream statistics to the
	 *                            STREAMINFO block; this is recommended but not
	 *                            necessary to create a valid FLAC stream.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a tell_callback must also be supplied.
	 *                            Alternatively, a dummy seek callback that just
	 *                            returns \c FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the encoder.
	 * \param  tell_callback      See FLAC__StreamEncoderTellCallback.  This
	 *                            pointer may be \c NULL if seeking is not
	 *                            supported.  If \a seek_callback is \c NULL then
	 *                            this argument will be ignored.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a tell_callback must also be supplied.
	 *                            Alternatively, a dummy tell callback that just
	 *                            returns \c FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the encoder.
	 * \param  metadata_callback  See FLAC__StreamEncoderMetadataCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.  If the client provides a seek callback,
	 *                            this function is not necessary as the encoder
	 *                            will automatically seek back and update the
	 *                            STREAMINFO block.  It may also be \c NULL if the
	 *                            client does not support seeking, since it will
	 *                            have no way of going back to update the
	 *                            STREAMINFO.  However the client can still supply
	 *                            a callback if it would like to know the details
	 *                            from the STREAMINFO.
	 * \param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * \retval FLAC__StreamEncoderInitStatus
	 *    \c FLAC__STREAM_ENCODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamEncoderInitStatus for the meanings of other return values.
	 */
	public final int /* FLAC__StreamEncoderInitStatus */ init_stream(
			final StreamEncoderWriteCallback write_cb,
			final StreamEncoderSeekCallback seek_cb,
			final StreamEncoderTellCallback tell_cb,
			final StreamEncoderMetadataCallback metadata_cb//,
			// final Object client_data
	)
	{
		return init_stream_internal_(
			/*read_callback=*/null,
			write_cb,
			seek_cb,
			tell_cb,
			metadata_cb,
			// client_data,
			/*is_ogg=*/false
		);
	}

	/** Initialize the encoder instance to encode Ogg FLAC streams.
	 *
	 *  This flavor of initialization sets up the encoder to encode to a FLAC
	 *  stream in an Ogg container.  I/O is performed via callbacks to the
	 *  client.  For encoding to a plain file via filename or open \c FILE*,
	 *  FLAC__stream_encoder_init_ogg_file() and FLAC__stream_encoder_init_ogg_FILE()
	 *  provide a simpler interface.
	 *
	 *  This function should be called after FLAC__stream_encoder_new() and
	 *  FLAC__stream_encoder_set_*() but before FLAC__stream_encoder_process()
	 *  or FLAC__stream_encoder_process_interleaved().
	 *  initialization succeeded.
	 *
	 *  The call to FLAC__stream_encoder_init_ogg_stream() currently will also
	 *  immediately call the write callback several times to write the metadata
	 *  packets.
	 *
	 * \param  encoder            An uninitialized encoder instance.
	 * \param  read_callback      See FLAC__StreamEncoderReadCallback.  This
	 *                            pointer must not be \c NULL if \a seek_callback
	 *                            is non-NULL since they are both needed to be
	 *                            able to write data back to the Ogg FLAC stream
	 *                            in the post-encode phase.
	 * \param  write_callback     See FLAC__StreamEncoderWriteCallback.  This
	 *                            pointer must not be \c NULL.
	 * \param  seek_callback      See FLAC__StreamEncoderSeekCallback.  This
	 *                            pointer may be \c NULL if seeking is not
	 *                            supported.  The encoder uses seeking to go back
	 *                            and write some some stream statistics to the
	 *                            STREAMINFO block; this is recommended but not
	 *                            necessary to create a valid FLAC stream.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a tell_callback must also be supplied.
	 *                            Alternatively, a dummy seek callback that just
	 *                            returns \c FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the encoder.
	 * \param  tell_callback      See FLAC__StreamEncoderTellCallback.  This
	 *                            pointer may be \c NULL if seeking is not
	 *                            supported.  If \a seek_callback is \c NULL then
	 *                            this argument will be ignored.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a tell_callback must also be supplied.
	 *                            Alternatively, a dummy tell callback that just
	 *                            returns \c FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the encoder.
	 * \param  metadata_callback  See FLAC__StreamEncoderMetadataCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.  If the client provides a seek callback,
	 *                            this function is not necessary as the encoder
	 *                            will automatically seek back and update the
	 *                            STREAMINFO block.  It may also be \c NULL if the
	 *                            client does not support seeking, since it will
	 *                            have no way of going back to update the
	 *                            STREAMINFO.  However the client can still supply
	 *                            a callback if it would like to know the details
	 *                            from the STREAMINFO.
	 * \param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * \retval FLAC__StreamEncoderInitStatus
	 *    \c FLAC__STREAM_ENCODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamEncoderInitStatus for the meanings of other return values.
	 */
	public final int /* FLAC__StreamEncoderInitStatus */ init_ogg_stream(
			final StreamEncoderReadCallback read_cb,
			final StreamEncoderWriteCallback write_cb,
			final StreamEncoderSeekCallback seek_cb,
			final StreamEncoderTellCallback tell_cb,
			final StreamEncoderMetadataCallback metadata_cb//,
			// final Object client_data
	)
	{
		return init_stream_internal_(
			read_cb,
			write_cb,
			seek_cb,
			tell_cb,
			metadata_cb,
			// client_data,
			/*is_ogg=*/true
		);
	}

	private final int /* FLAC__StreamEncoderInitStatus */ init_FILE_internal_(
		final OutputStream f,
		final StreamEncoderProgressCallback progress_cb,
		//final Object client_data,
		final boolean isogg
	)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != file);

		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED;
		}

		/* double protection */
		if( f == null ) {
			this.state = FLAC__STREAM_ENCODER_IO_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		/*
		 * To make sure that our file does not go unclosed after an error, we
		 * must assign the FILE pointer before any further error can occur in
		 * this routine.
		 */
		//if( file == System.out )
		//	file = get_binary_stdout_(); /* just to be safe */

//#ifdef _WIN32
		/*
		 * Windows can suffer quite badly from disk fragmentation. This can be
		 * reduced significantly by setting the output buffer size to be 10MB.
		 */
//		if( GetFileType((HANDLE)_get_osfhandle( _fileno( file ) )) == FILE_TYPE_DISK )
//			setvbuf(file, NULL, _IOFBF, 10*1024*1024);
//#endif

		this.file = f;

		this.progress_callback = progress_cb;
		this.bytes_written = 0;
		this.samples_written = 0;
		this.frames_written = 0;

		final int /* FLAC__StreamEncoderInitStatus */ init_status = init_stream_internal_(
			this.file == (OutputStream)System.out ? null : isogg ? this /*file_read_callback_*/ : null,
					this /*file_write_callback_*/,
			this.file == (OutputStream)System.out ? null : this /*file_seek_callback_*/,
			this.file == (OutputStream)System.out ? null : this /*file_tell_callback_*/,
			/*metadata_callback=*/null,
			// client_data,
			isogg
		);
		if( init_status != FLAC__STREAM_ENCODER_INIT_STATUS_OK ) {
			/* the above function sets the state for us in case of an error */
			return init_status;
		}

		{
			final int block_size = get_blocksize();

			//FLAC__ASSERT(blocksize != 0);
			this.total_frames_estimate = (int)((get_total_samples_estimate() + (long)block_size - 1L) / block_size);
		}

		return init_status;
	}

	/** Initialize the encoder instance to encode native FLAC files.
	 *
	 *  This flavor of initialization sets up the encoder to encode to a
	 *  plain native FLAC file.  For non-stdio streams, you must use
	 *  FLAC__stream_encoder_init_stream() and provide callbacks for the I/O.
	 *
	 *  This function should be called after FLAC__stream_encoder_new() and
	 *  FLAC__stream_encoder_set_*() but before FLAC__stream_encoder_process()
	 *  or FLAC__stream_encoder_process_interleaved().
	 *  initialization succeeded.
	 *
	 * \param  encoder            An uninitialized encoder instance.
	 * \param  file               An open file.  The file should have been opened
	 *                            with mode \c "w+b" and rewound.  The file
	 *                            becomes owned by the encoder and should not be
	 *                            manipulated by the client while encoding.
	 *                            Unless \a file is \c stdout, it will be closed
	 *                            when FLAC__stream_encoder_finish() is called.
	 *                            Note however that a proper SEEKTABLE cannot be
	 *                            created when encoding to \c stdout since it is
	 *                            not seekable.
	 * \param  progress_callback  See FLAC__StreamEncoderProgressCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * \param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code encoder != NULL \endcode
	 *    \code file != NULL \endcode
	 * \retval FLAC__StreamEncoderInitStatus
	 *    \c FLAC__STREAM_ENCODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamEncoderInitStatus for the meanings of other return values.
	 */
	public final int /* FLAC__StreamEncoderInitStatus */ init_FILE(
			final RandomAccessInputOutputStream f,
			final StreamEncoderProgressCallback progress_cb//,
			//final Object client_data
	)
	{
		return init_FILE_internal_( f, progress_cb,/* client_data,*/ /*is_ogg=*/false );
	}

	/** Initialize the encoder instance to encode Ogg FLAC files.
	 *
	 *  This flavor of initialization sets up the encoder to encode to a
	 *  plain Ogg FLAC file.  For non-stdio streams, you must use
	 *  FLAC__stream_encoder_init_ogg_stream() and provide callbacks for the I/O.
	 *
	 *  This function should be called after FLAC__stream_encoder_new() and
	 *  FLAC__stream_encoder_set_*() but before FLAC__stream_encoder_process()
	 *  or FLAC__stream_encoder_process_interleaved().
	 *  initialization succeeded.
	 *
	 * \param  encoder            An uninitialized encoder instance.
	 * \param  file               An open file.  The file should have been opened
	 *                            with mode \c "w+b" and rewound.  The file
	 *                            becomes owned by the encoder and should not be
	 *                            manipulated by the client while encoding.
	 *                            Unless \a file is \c stdout, it will be closed
	 *                            when FLAC__stream_encoder_finish() is called.
	 *                            Note however that a proper SEEKTABLE cannot be
	 *                            created when encoding to \c stdout since it is
	 *                            not seekable.
	 * \param  progress_callback  See FLAC__StreamEncoderProgressCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * \param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code encoder != NULL \endcode
	 *    \code file != NULL \endcode
	 * \retval FLAC__StreamEncoderInitStatus
	 *    \c FLAC__STREAM_ENCODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamEncoderInitStatus for the meanings of other return values.
	 */
	public final int /* FLAC__StreamEncoderInitStatus */ init_ogg_FILE(
			final RandomAccessInputOutputStream f,
			final StreamEncoderProgressCallback progress_cb//,
			// final Object client_data
	)
	{
		return init_FILE_internal_( f, progress_cb,/* client_data,*/ /*is_ogg=*/true );
	}

	private final int /* FLAC__StreamEncoderInitStatus */ init_file_internal_(
		final String filename,
		final StreamEncoderProgressCallback progress_cb,
		// final Object client_data,
		final boolean isogg
	)
	{
		//FLAC__ASSERT(0 != encoder);

		/*
		 * To make sure that our file does not go unclosed after an error, we
		 * have to do the same entrance checks here that are later performed
		 * in FLAC__stream_encoder_init_FILE() before the FILE* is assigned.
		 */
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED;
		}

		try {
			final OutputStream f = filename != null ? new RandomAccessInputOutputStream( filename ) : System.out;
			return init_FILE_internal_( f, progress_cb,/* client_data,*/ isogg );
		} catch( final Exception e ) {
			this.state = FLAC__STREAM_ENCODER_IO_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}
	}

	/** Initialize the encoder instance to encode native FLAC files.
	 *
	 *  This flavor of initialization sets up the encoder to encode to a plain
	 *  FLAC file.  If POSIX fopen() semantics are not sufficient (for example,
	 *  with Unicode filenames on Windows), you must use
	 *  FLAC__stream_encoder_init_FILE(), or FLAC__stream_encoder_init_stream()
	 *  and provide callbacks for the I/O.
	 *
	 *  This function should be called after FLAC__stream_encoder_new() and
	 *  FLAC__stream_encoder_set_*() but before FLAC__stream_encoder_process()
	 *  or FLAC__stream_encoder_process_interleaved().
	 *  initialization succeeded.
	 *
	 * \param  encoder            An uninitialized encoder instance.
	 * \param  filename           The name of the file to encode to.  The file will
	 *                            be opened with fopen().  Use \c NULL to encode to
	 *                            \c stdout.  Note however that a proper SEEKTABLE
	 *                            cannot be created when encoding to \c stdout since
	 *                            it is not seekable.
	 * \param  progress_callback  See FLAC__StreamEncoderProgressCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * \param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * \retval FLAC__StreamEncoderInitStatus
	 *    \c FLAC__STREAM_ENCODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamEncoderInitStatus for the meanings of other return values.
	 */
	public final int /* FLAC__StreamEncoderInitStatus */ init_file(
			final String filename,
			final StreamEncoderProgressCallback progress_cb//,
			// final Object client_data
	)
	{
		return init_file_internal_( filename, progress_cb,/* client_data,*/ /*is_ogg=*/false );
	}

	/** Initialize the encoder instance to encode Ogg FLAC files.
	 *
	 *  This flavor of initialization sets up the encoder to encode to a plain
	 *  Ogg FLAC file.  If POSIX fopen() semantics are not sufficient (for example,
	 *  with Unicode filenames on Windows), you must use
	 *  FLAC__stream_encoder_init_ogg_FILE(), or FLAC__stream_encoder_init_ogg_stream()
	 *  and provide callbacks for the I/O.
	 *
	 *  This function should be called after FLAC__stream_encoder_new() and
	 *  FLAC__stream_encoder_set_*() but before FLAC__stream_encoder_process()
	 *  or FLAC__stream_encoder_process_interleaved().
	 *  initialization succeeded.
	 *
	 * \param  encoder            An uninitialized encoder instance.
	 * \param  filename           The name of the file to encode to.  The file will
	 *                            be opened with fopen().  Use \c NULL to encode to
	 *                            \c stdout.  Note however that a proper SEEKTABLE
	 *                            cannot be created when encoding to \c stdout since
	 *                            it is not seekable.
	 * \param  progress_callback  See FLAC__StreamEncoderProgressCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * \param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * \retval FLAC__StreamEncoderInitStatus
	 *    \c FLAC__STREAM_ENCODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamEncoderInitStatus for the meanings of other return values.
	 */
	public final int /* FLAC__StreamEncoderInitStatus */ init_ogg_file(
			final String filename,
			final StreamEncoderProgressCallback progress_cb//,
			//final Object client_data
	)
	{
		return init_file_internal_( filename, progress_cb,/* client_data,*/ /*is_ogg=*/true );
	}

	/** Finish the encoding process.
	 *  Flushes the encoding buffer, releases resources, resets the encoder
	 *  settings to their defaults, and returns the encoder state to
	 *  FLAC__STREAM_ENCODER_UNINITIALIZED.  Note that this can generate
	 *  one or more write callbacks before returning, and will generate
	 *  a metadata callback.
	 *
	 *  Note that in the course of processing the last frame, errors can
	 *  occur, so the caller should be sure to check the return value to
	 *  ensure the file was encoded properly.
	 *
	 *  In the event of a prematurely-terminated encode, it is not strictly
	 *  necessary to call this immediately before FLAC__stream_encoder_delete()
	 *  but it is good practice to match every FLAC__stream_encoder_init_*()
	 *  with a FLAC__stream_encoder_finish().
	 *
	 * \param  encoder  An uninitialized encoder instance.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * \retval FLAC__bool
	 *    \c false if an error occurred processing the last frame; or if verify
	 *    mode is set (see FLAC__stream_encoder_set_verify()), there was a
	 *    verify mismatch; else \c true.  If \c false, caller should check the
	 *    state with FLAC__stream_encoder_get_state() for more information
	 *    about the error.
	 */
	public final boolean finish()
	{
		//if( encoder == null )// java: check before calling
		//	return false;

		//FLAC__ASSERT(0 != encoder.private_);
		//FLAC__ASSERT(0 != encoder.protected_);

		if( this.state == FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			if( this.metadata != null ) { // True in case FLAC__stream_encoder_set_metadata was used but init failed
				this.metadata = null;
			}
			if( null != this.file) {
				if( this.file != System.out ) {
					try { this.file.close(); } catch( final IOException e ) {}
				}
				this.file = null;
			}
			return true;
		}

		boolean error = false;

		if( this.state == FLAC__STREAM_ENCODER_OK && ! this.is_being_deleted ) {
			if( this.current_sample_number != 0 ) {
				this.blocksize = this.current_sample_number;
				if( ! process_frame_( /*is_last_block=*/true ) ) {
					error = true;
				}
			}
		}

		if( this.do_md5 ) {
			this.md5context.MD5Final( this.streaminfo.md5sum );
		}

		if( ! this.is_being_deleted ) {
			if( this.state == FLAC__STREAM_ENCODER_OK ) {
				if( this.seek_callback != null ) {
if( Format.FLAC__HAS_OGG
					&& this.is_ogg ) {
						update_ogg_metadata_();
} else {
					update_metadata_();
}
					/* check if an error occurred while updating metadata */
					if( this.state != FLAC__STREAM_ENCODER_OK ) {
						error = true;
					}
				}
				if( this.metadata_callback != null ) {
					this.metadata_callback.enc_metadata_callback( this, this.streaminfo/*, this.client_data*/ );
				}
			}

			if( this.do_verify && null != this.verify.decoder && ! this.verify.decoder.finish() ) {
				if( ! error ) {
					this.state = FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA;
				}
				error = true;
			}
		}

		if( null != this.file ) {
			if( this.file != (OutputStream)System.out ) {
				try { this.file.close(); } catch( final IOException e ) {}
			}
			this.file = null;
		}

if( Format.FLAC__HAS_OGG ) {
		if( this.is_ogg ) {
			this.ogg_encoder_aspect.finish();
		}
}

		free_();
		set_defaults_();

		if( ! error ) {
			this.state = FLAC__STREAM_ENCODER_UNINITIALIZED;
		}

		return ! error;
	}

	/** Set the serial number for the FLAC stream to use in the Ogg container.
	 *
	 * @note
	 * This does not need to be set for native FLAC encoding.
	 *
	 * @note
	 * It is recommended to set a serial number explicitly as the default of '0'
	 * may collide with other streams.
	 *
	 * \default \c 0
	 * @param  value  See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_ogg_serial_number(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder.private_);
		//FLAC__ASSERT(0 != encoder.protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
if( Format.FLAC__HAS_OGG ) {
		/* can't check encoder.private_->is_ogg since that's not set until init time */
		this.ogg_encoder_aspect.set_serial_number( value );
		return true;
}
		return false;
	}

	/** Set the "verify" flag.  If \c true, the encoder will verify it's own
	 *  encoded output by feeding it through an internal decoder and comparing
	 *  the original signal against the decoded signal.  If a mismatch occurs,
	 *  the process call will return \c false.  Note that this will slow the
	 *  encoding process by the extra time required for decoding and comparison.
	 *
	 * \default \c false
	 * @param  value    Flag value (see above).
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_verify(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
//if( ! FLAC__MANDATORY_VERIFY_WHILE_ENCODING ) {
		this.do_verify = value;
//}
		return true;
	}

	/** Set the <A HREF="../format.html#subset">Subset</A> flag.  If \c true,
	 *  the encoder will comply with the Subset and will check the
	 *  settings during FLAC__stream_encoder_init_*() to see if all settings
	 *  comply.  If \c false, the settings may take advantage of the full
	 *  range that the format allows.
	 *
	 *  Make sure you know what it entails before setting this to \c false.
	 *
	 * \default \c true
	 * @param  value    Flag value (see above).
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_streamable_subset(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.streamable_subset = value;
		return true;
	}

	public final boolean set_do_md5(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.do_md5 = value;
		return true;
	}

	/** Set the number of channels to be encoded.
	 *
	 * \default \c 2
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_channels(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.channels = value;
		return true;
	}

	/** Set the sample resolution of the input to be encoded.
	 *
	 * \warning
	 * Do not feed the encoder data that is wider than the value you
	 * set here or you will generate an invalid stream.
	 *
	 * \default \c 16
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_bits_per_sample(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.bits_per_sample = value;
		return true;
	}

	/** Set the sample rate (in Hz) of the input to be encoded.
	 *
	 * \default \c 44100
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_sample_rate(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.sample_rate = value;
		return true;
	}

	/** Set the compression level
	 *
	 * The compression level is roughly proportional to the amount of effort
	 * the encoder expends to compress the file.  A higher level usually
	 * means more computation but higher compression.  The default level is
	 * suitable for most applications.
	 *
	 * Currently the levels range from \c 0 (fastest, least compression) to
	 * \c 8 (slowest, most compression).  A value larger than \c 8 will be
	 * treated as \c 8.
	 *
	 * This function automatically calls the following other \c _set_
	 * functions with appropriate values, so the client does not need to
	 * unless it specifically wants to override them:
	 * - FLAC__stream_encoder_set_do_mid_side_stereo()
	 * - FLAC__stream_encoder_set_loose_mid_side_stereo()
	 * - FLAC__stream_encoder_set_apodization()
	 * - FLAC__stream_encoder_set_max_lpc_order()
	 * - FLAC__stream_encoder_set_qlp_coeff_precision()
	 * - FLAC__stream_encoder_set_do_qlp_coeff_prec_search()
	 * - FLAC__stream_encoder_set_do_escape_coding()
	 * - FLAC__stream_encoder_set_do_exhaustive_model_search()
	 * - FLAC__stream_encoder_set_min_residual_partition_order()
	 * - FLAC__stream_encoder_set_max_residual_partition_order()
	 * - FLAC__stream_encoder_set_rice_parameter_search_dist()
	 *
	 * The actual values set for each level are:
	 * <table>
	 * <tr>
	 *  <td><b>level</b></td>
	 *  <td>do mid-side stereo</td>
	 *  <td>loose mid-side stereo</td>
	 *  <td>apodization</td>
	 *  <td>max lpc order</td>
	 *  <td>qlp coeff precision</td>
	 *  <td>qlp coeff prec search</td>
	 *  <td>escape coding</td>
	 *  <td>exhaustive model search</td>
	 *  <td>min residual partition order</td>
	 *  <td>max residual partition order</td>
	 *  <td>rice parameter search dist</td>
	 * </tr>
	 * <tr>  <td><b>0</b></td> <td>false</td> <td>false</td> <td>tukey(0.5)<td>                                     <td>0</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>3</td> <td>0</td> </tr>
	 * <tr>  <td><b>1</b></td> <td>true</td>  <td>true</td>  <td>tukey(0.5)<td>                                     <td>0</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>3</td> <td>0</td> </tr>
	 * <tr>  <td><b>2</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5)<td>                                     <td>0</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>3</td> <td>0</td> </tr>
	 * <tr>  <td><b>3</b></td> <td>false</td> <td>false</td> <td>tukey(0.5)<td>                                     <td>6</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>4</td> <td>0</td> </tr>
	 * <tr>  <td><b>4</b></td> <td>true</td>  <td>true</td>  <td>tukey(0.5)<td>                                     <td>8</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>4</td> <td>0</td> </tr>
	 * <tr>  <td><b>5</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5)<td>                                     <td>8</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>5</td> <td>0</td> </tr>
	 * <tr>  <td><b>6</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5);partial_tukey(2)<td>                    <td>8</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>6</td> <td>0</td> </tr>
	 * <tr>  <td><b>7</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5);partial_tukey(2)<td>                    <td>12</td> <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>6</td> <td>0</td> </tr>
	 * <tr>  <td><b>8</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5);partial_tukey(2);punchout_tukey(3)</td> <td>12</td> <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>6</td> <td>0</td> </tr>
	 * </table>
	 *
	 * \default \c 5
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_compression_level(int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		boolean ok = true;
		if( value >= compression_levels_.length ) {
			value = compression_levels_.length - 1;
		}
		final CompressionLevels level = compression_levels_[value];// java
		ok &= set_do_mid_side_stereo   ( level.do_mid_side_stereo );
		ok &= set_loose_mid_side_stereo( level.loose_mid_side_stereo );
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
//#if 1
		ok &= set_apodization                 ( level.apodization );
//#else
		/* equivalent to -A tukey(0.5) */
		//this.num_apodizations = 1;
		//this.apodizations[0].type = ApodizationSpecification.FLAC__APODIZATION_TUKEY;
		//this.apodizations[0]./*parameters.tukey.*/p = 0.5f;
//#endif
//#endif
		ok &= set_max_lpc_order               ( level.max_lpc_order );
		ok &= set_qlp_coeff_precision         ( level.qlp_coeff_precision );
		ok &= set_do_qlp_coeff_prec_search    ( level.do_qlp_coeff_prec_search );
		ok &= set_do_escape_coding            ( level.do_escape_coding );
		ok &= set_do_exhaustive_model_search  ( level.do_exhaustive_model_search );
		ok &= set_min_residual_partition_order( level.min_residual_partition_order );
		ok &= set_max_residual_partition_order( level.max_residual_partition_order );
		ok &= set_rice_parameter_search_dist  ( level.rice_parameter_search_dist );
		return ok;
	}

	/** Set the blocksize to use while encoding.
	 *
	 * The number of samples to use per frame.  Use \c 0 to let the encoder
	 * estimate a blocksize; this is usually best.
	 *
	 * \default \c 0
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_blocksize(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.blocksize = value;
		return true;
	}

	/** Set to \c true to enable mid-side encoding on stereo input.  The
	 *  number of channels must be 2 for this to have any effect.  Set to
	 *  \c false to use only independent channel coding.
	 *
	 * \default \c true
	 * @param  value    Flag value (see above).
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_do_mid_side_stereo(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED) {
			return false;
		}
		this.do_mid_side_stereo = value;
		return true;
	}

	/** Set to \c true to enable adaptive switching between mid-side and
	 *  left-right encoding on stereo input.  Set to \c false to use
	 *  exhaustive searching.  Setting this to \c true requires
	 *  FLAC__stream_encoder_set_do_mid_side_stereo() to also be set to
	 *  \c true in order to have any effect.
	 *
	 * \default \c false
	 * @param  value    Flag value (see above).
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_loose_mid_side_stereo(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED) {
			return false;
		}
		this.loose_mid_side_stereo = value;
		return true;
	}

	/*@@@@add to tests*/
	/** Sets the apodization function(s) the encoder will use when windowing
	 *  audio data for LPC analysis.
	 *
	 * The \a specification is a plain ASCII string which specifies exactly
	 * which functions to use.  There may be more than one (up to 32),
	 * separated by \c ';' characters.  Some functions take one or more
	 * comma-separated arguments in parentheses.
	 *
	 * The available functions are \c bartlett, \c bartlett_hann,
	 * \c blackman, \c blackman_harris_4term_92db, \c connes, \c flattop,
	 * \c gauss(STDDEV), \c hamming, \c hann, \c kaiser_bessel, \c nuttall,
	 * \c rectangle, \c triangle, \c tukey(P), \c partial_tukey(n[/ov[/P]]),
	 * \c punchout_tukey(n[/ov[/P]]), \c welch.
	 *
	 * For \c gauss(STDDEV), STDDEV specifies the standard deviation
	 * (0<STDDEV<=0.5).
	 *
	 * For \c tukey(P), P specifies the fraction of the window that is
	 * tapered (0<=P<=1).  P=0 corresponds to \c rectangle and P=1
	 * corresponds to \c hann.
	 *
	 * Specifying \c partial_tukey or \c punchout_tukey works a little
	 * different. These do not specify a single apodization function, but
	 * a series of them with some overlap. partial_tukey specifies a series
	 * of small windows (all treated separately) while punchout_tukey
	 * specifies a series of windows that have a hole in them. In this way,
	 * the predictor is constructed with only a part of the block, which
	 * helps in case a block consists of dissimilar parts.
	 *
	 * The three parameters that can be specified for the functions are
	 * n, ov and P. n is the number of functions to add, ov is the overlap
	 * of the windows in case of partial_tukey and the overlap in the gaps
	 * in case of punchout_tukey. P is the fraction of the window that is
	 * tapered, like with a regular tukey window. The function can be
	 * specified with only a number, a number and an overlap, or a number
	 * an overlap and a P, for example, partial_tukey(3), partial_tukey(3/0.3)
	 * and partial_tukey(3/0.3/0.5) are all valid. ov should be smaller than 1
	 * and can be negative.
	 *
	 * Example specifications are \c "blackman" or
	 * \c "hann;triangle;tukey(0.5);tukey(0.25);tukey(0.125)"
	 *
	 * Any function that is specified erroneously is silently dropped.  Up
	 * to 32 functions are kept, the rest are dropped.  If the specification
	 * is empty the encoder defaults to \c "tukey(0.5)".
	 *
	 * When more than one function is specified, then for every subframe the
	 * encoder will try each of them separately and choose the window that
	 * results in the smallest compressed subframe.
	 *
	 * Note that each function specified causes the encoder to occupy a
	 * floating point array in which to store the window. Also note that the
	 * values of P, STDDEV and ov are locale-specific, so if the comma
	 * separator specified by the locale is a comma, a comma should be used.
	 *
	 * \default \c "tukey(0.5)"
	 * @param  specification  See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 *    \code specification != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_apodization(final String specification)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		//FLAC__ASSERT(0 != specification);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
//#ifdef Format.FLAC__INTEGER_ONLY_LIBRARY
		//(void)specification; /* silently ignore since we haven't integerized; will always use a rectangular window */
//#else
		final ApodizationSpecification[] as = this.apodizations;// java
		int napodizations = 0;// this.num_apodizations = 0
		int i = 0;
		while( true ) {
			final int s = specification.indexOf( ';', i );
			final int n = s >= 0 ? (s - i) : specification.length();
			if( n == 8  && specification.startsWith("bartlett", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_BARTLETT;
			} else if( n == 13 && specification.startsWith("bartlett_hann", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_BARTLETT_HANN;
			} else if( n == 8  && specification.startsWith("blackman", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_BLACKMAN;
			} else if( n == 26 && specification.startsWith("blackman_harris_4term_92db", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_BLACKMAN_HARRIS_4TERM_92DB_SIDELOBE;
			} else if( n == 6  && specification.startsWith("connes", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_CONNES;
			} else if( n == 7  && specification.startsWith("flattop", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_FLATTOP;
			} else if( n > 7   && specification.startsWith("gauss(", i) ) {
				final float stddev = (float) Format.strtod( specification.substring( i + 6 ) );
				if( stddev > 0.0f && stddev <= 0.5f ) {
					as[napodizations]./*parameters.gauss.*/stddev = stddev;
					as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_GAUSS;
				}
			}
			else if( n == 7  && specification.startsWith("hamming", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_HAMMING;
			} else if( n == 4  && specification.startsWith("hann", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_HANN;
			} else if( n == 13 && specification.startsWith("kaiser_bessel", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_KAISER_BESSEL;
			} else if( n == 7  && specification.startsWith("nuttall", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_NUTTALL;
			} else if( n == 9  && specification.startsWith("rectangle", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_RECTANGLE;
			} else if( n == 8  && specification.startsWith("triangle", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_TRIANGLE;
			} else if( n > 7   && specification.startsWith("tukey(", i) ) {
				final float p = (float) Format.strtod( specification.substring( i + 6 ) );
				if( p >= 0.0f && p <= 1.0f ) {
					as[napodizations]./*parameters.tukey.*/p = p;
					as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_TUKEY;
				}
			}
			else if( n > 15   && specification.startsWith("partial_tukey(", i) ) {
				final int tukey_parts = (int) Format.strtod( specification.substring( i + 14 ) );
				final int si_1 = specification.indexOf('/', i );
				final float overlap = si_1 >= 0 ? Math.min( (float) Format.strtod( specification.substring( si_1 + 1 ) ), 0.99f ) : 0.1f;
				final float overlap_units = 1.0f / (1.0f - overlap) - 1.0f;
				final int si_2 = specification.indexOf('/', (si_1 >= 0 ? (si_1 + 1) : i));
				final float tukey_p = si_2 >= 0 ? (float) Format.strtod( specification.substring( si_2 + 1 ) ) : 0.2f;

				if( tukey_parts <= 1 ) {
					as[napodizations]./*parameters.tukey.*/p = tukey_p;
					as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_TUKEY;
				} else if( napodizations + tukey_parts < 32 ) {
					int m;
					for( m = 0; m < tukey_parts; m++ ) {
						as[napodizations]./*parameters.multiple_tukey.*/p = tukey_p;
						as[napodizations]./*parameters.multiple_tukey.*/start = m/(tukey_parts+overlap_units);
						as[napodizations]./*parameters.multiple_tukey.*/end = (m+1+overlap_units)/(tukey_parts+overlap_units);
						as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_PARTIAL_TUKEY;
					}
				}
			}
			else if( n > 16   && specification.startsWith("punchout_tukey(", i) ) {
				final int tukey_parts = (int) Format.strtod( specification.substring( i + 15 ) );
				final int si_1 = specification.indexOf('/', i );
				final float overlap = si_1 >= 0 ? Math.min( (float) Format.strtod( specification.substring( si_1 + 1 ) ), 0.99f ) : 0.2f;
				final float overlap_units = 1.0f / (1.0f - overlap) - 1.0f;
				final int si_2 = specification.indexOf('/', (si_1 >= 0 ? (si_1 + 1) : i));
				final float tukey_p = si_2 >= 0 ? (float) Format.strtod( specification.substring( si_2 + 1 ) ) : 0.2f;

				if( tukey_parts <= 1 ) {
					as[napodizations]./*parameters.tukey.*/p = tukey_p;
					as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_TUKEY;
				} else if( napodizations + tukey_parts < 32 ) {
					int m;
					for( m = 0; m < tukey_parts; m++ ) {
						as[napodizations]./*parameters.multiple_tukey.*/p = tukey_p;
						as[napodizations]./*parameters.multiple_tukey.*/start = m / (tukey_parts + overlap_units);
						as[napodizations]./*parameters.multiple_tukey.*/end = (m + 1 + overlap_units) / (tukey_parts + overlap_units);
						as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_PUNCHOUT_TUKEY;
					}
				}
			}
			else if( n > 17  && specification.startsWith("subdivide_tukey(", i) ) {
				final int parts = (int) Format.strtod( specification.substring( i + 16 ) );
				if( parts > 1) {
					final int si_1 = specification.indexOf('/', i );
					float p = si_1 >= 0 ? (float) Format.strtod( specification.substring( si_1 + 1 ) ) : 5e-1f;
					if( p > 1 ) {
						p = 1;
					} else if(p < 0) {
						p = 0;
					}
					as[napodizations]./*parameters.subdivide_tukey.*/parts = parts;
					as[napodizations]./*parameters.subdivide_tukey.*/p = p / parts;
					as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_SUBDIVIDE_TUKEY;
				}
			}
			else if( n == 5  && specification.startsWith("welch", i) ) {
				as[napodizations++].type = ApodizationSpecification.FLAC__APODIZATION_WELCH;
			}
			if( napodizations == 32 ) {
				break;
			}
			if( s >= 0 ) {
				i = s + 1;
			} else {
				break;
			}
		}
		if( napodizations == 0 ) {
			napodizations = 1;
			as[0].type = ApodizationSpecification.FLAC__APODIZATION_TUKEY;
			as[0]./*parameters.tukey.*/p = 0.5f;
		}
		this.num_apodizations = napodizations;
//#endif
		return true;
	}

	/** Set the maximum LPC order, or \c 0 to use only the fixed predictors.
	 *
	 * \default \c 8
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_max_lpc_order(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.max_lpc_order = value;
		return true;
	}

	/** Set the precision, in bits, of the quantized linear predictor
	 *  coefficients, or \c 0 to let the encoder select it based on the
	 *  blocksize.
	 *
	 * @note
	 * In the current implementation, qlp_coeff_precision + bits_per_sample must
	 * be less than 32.
	 *
	 * \default \c 0
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_qlp_coeff_precision(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.qlp_coeff_precision = value;
		return true;
	}

	/** Set to \c false to use only the specified quantized linear predictor
	 *  coefficient precision, or \c true to search neighboring precision
	 *  values and use the best one.
	 *
	 * \default \c false
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_do_qlp_coeff_prec_search(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.do_qlp_coeff_prec_search = value;
		return true;
	}

	/** Deprecated.  Setting this value has no effect.
	 *
	 * \default \c false
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_do_escape_coding(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
//#ifdef FUZZING_BUILD_MODE_UNSAFE_FOR_PRODUCTION
		/* was deprecated since FLAC 1.0.4 (24-Sep-2002), but is needed for
		 * full spec coverage, so this should be reenabled at some point.
		 * For now only enable while fuzzing */
//		encoder.protected_.do_escape_coding = value;
//#else
//		(void)value;
//#endif
		return true;
	}

	/** Set to \c false to let the encoder estimate the best model order
	 *  based on the residual signal energy, or \c true to force the
	 *  encoder to evaluate all order models and select the best.
	 *
	 * \default \c false
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_do_exhaustive_model_search(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.do_exhaustive_model_search = value;
		return true;
	}

	/** Set the minimum partition order to search when coding the residual.
	 *  This is used in tandem with
	 *  FLAC__stream_encoder_set_max_residual_partition_order().
	 *
	 *  The partition order determines the context size in the residual.
	 *  The context size will be approximately <tt>blocksize / (2 ^ order)</tt>.
	 *
	 *  Set both min and max values to \c 0 to force a single context,
	 *  whose Rice parameter is based on the residual signal variance.
	 *  Otherwise, set a min and max order, and the encoder will search
	 *  all orders, using the mean of each context for its Rice parameter,
	 *  and use the best.
	 *
	 * \default \c 0
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_min_residual_partition_order(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.min_residual_partition_order = value;
		return true;
	}

	/** Set the maximum partition order to search when coding the residual.
	 *  This is used in tandem with
	 *  FLAC__stream_encoder_set_min_residual_partition_order().
	 *
	 *  The partition order determines the context size in the residual.
	 *  The context size will be approximately <tt>blocksize / (2 ^ order)</tt>.
	 *
	 *  Set both min and max values to \c 0 to force a single context,
	 *  whose Rice parameter is based on the residual signal variance.
	 *  Otherwise, set a min and max order, and the encoder will search
	 *  all orders, using the mean of each context for its Rice parameter,
	 *  and use the best.
	 *
	 * \default \c 5
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_max_residual_partition_order(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.max_residual_partition_order = value;
		return true;
	}

	/** Deprecated.  Setting this value has no effect.
	 *
	 * \default \c 0
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_rice_parameter_search_dist(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
//#if 0
		/*@@@ deprecated: */
//		encoder.protected_.rice_parameter_search_dist = value;
//#else
//		(void)value;
//#endif
		return true;
	}

	/** Set an estimate of the total samples that will be encoded.
	 *  This is merely an estimate and may be set to \c 0 if unknown.
	 *  This value will be written to the STREAMINFO block before encoding,
	 *  and can remove the need for the caller to rewrite the value later
	 *  if the value is known before encoding.
	 *
	 * \default \c 0
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean set_total_samples_estimate(long value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		value = value < ((1L << Format.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN) - 1) ?
				value : ((1L << Format.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN) - 1);
		this.total_samples_estimate = value;
		return true;
	}

	/** Set the metadata blocks to be emitted to the stream before encoding.
	 *  A value of \c NULL, \c 0 implies no metadata; otherwise, supply an
	 *  array of pointers to metadata blocks.  The array is non-const since
	 *  the encoder may need to change the \a is_last flag inside them, and
	 *  in some cases update seek point offsets.  Otherwise, the encoder will
	 *  not modify or free the blocks.  It is up to the caller to free the
	 *  metadata blocks after encoding finishes.
	 *
	 * @note
	 * The encoder stores only copies of the pointers in the \a metadata array;
	 * the metadata blocks themselves must survive at least until after
	 * FLAC__stream_encoder_finish() returns.  Do not free the blocks until then.
	 *
	 * @note
	 * The STREAMINFO block is always written and no STREAMINFO block may
	 * occur in the supplied array.
	 *
	 * @note
	 * By default the encoder does not create a SEEKTABLE.  If one is supplied
	 * in the \a metadata array, but the client has specified that it does not
	 * support seeking, then the SEEKTABLE will be written verbatim.  However
	 * by itself this is not very useful as the client will not know the stream
	 * offsets for the seekpoints ahead of time.  In order to get a proper
	 * seektable the client must support seeking.  See next note.
	 *
	 * @note
	 * SEEKTABLE blocks are handled specially.  Since you will not know
	 * the values for the seek point stream offsets, you should pass in
	 * a SEEKTABLE 'template', that is, a SEEKTABLE object with the
	 * required sample numbers (or placeholder points), with \c 0 for the
	 * \a frame_samples and \a stream_offset fields for each point.  If the
	 * client has specified that it supports seeking by providing a seek
	 * callback to FLAC__stream_encoder_init_stream() or both seek AND read
	 * callback to FLAC__stream_encoder_init_ogg_stream() (or by using
	 * FLAC__stream_encoder_init*_file() or FLAC__stream_encoder_init*_FILE()),
	 * then while it is encoding the encoder will fill the stream offsets in
	 * for you and when encoding is finished, it will seek back and write the
	 * real values into the SEEKTABLE block in the stream.  There are helper
	 * routines for manipulating seektable template blocks; see metadata.h:
	 * FLAC__metadata_object_seektable_template_*().  If the client does
	 * not support seeking, the SEEKTABLE will have inaccurate offsets which
	 * will slow down or remove the ability to seek in the FLAC stream.
	 *
	 * @note
	 * The encoder instance \b will modify the first \c SEEKTABLE block
	 * as it transforms the template to a valid seektable while encoding,
	 * but it is still up to the caller to free all metadata blocks after
	 * encoding.
	 *
	 * @note
	 * A VORBIS_COMMENT block may be supplied.  The vendor string in it
	 * will be ignored.  libFLAC will use it's own vendor string. libFLAC
	 * will not modify the passed-in VORBIS_COMMENT's vendor string, it
	 * will simply write it's own into the stream.  If no VORBIS_COMMENT
	 * block is present in the \a metadata array, libFLAC will write an
	 * empty one, containing only the vendor string.
	 *
	 * @note The Ogg FLAC mapping requires that the VORBIS_COMMENT block be
	 * the second metadata block of the stream.  The encoder already supplies
	 * the STREAMINFO block automatically.  If \a metadata does not contain a
	 * VORBIS_COMMENT block, the encoder will supply that too.  Otherwise, if
	 * \a metadata does contain a VORBIS_COMMENT block and it is not the
	 * first, the init function will reorder \a metadata by moving the
	 * VORBIS_COMMENT block to the front; the relative ordering of the other
	 * blocks will remain as they were.
	 *
	 * @note The Ogg FLAC mapping limits the number of metadata blocks per
	 * stream to \c 65535.  If \a num_blocks exceeds this the function will
	 * return \c false.
	 *
	 * \default \c NULL, 0
	 * @param  meta_data    See above.
	 * @param  num_blocks  See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 *    \c false if the encoder is already initialized, or if
	 *    \a num_blocks > 65535 if encoding to Ogg FLAC, else \c true.
	 */
	public final boolean set_metadata(final StreamMetadata[] meta_data, final int num_blocks)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		//if( null == metadata )// java: moved to another if
		//	num_blocks = 0;
		//if( 0 == num_blocks )
		//	metadata = null;// FIXME potential null access
		/* realloc() does not do exactly what we want so... */
		if( this.metadata != null ) {
			this.metadata = null;
			// this.num_metadata_blocks = 0;
		}
		if( null != meta_data /*num_blocks != 0*/ ) {
			final StreamMetadata[] m = new StreamMetadata[num_blocks];
			for( int i = 0; i < num_blocks; i++ ) {// TODO check, c uses full copy
				m[i] = meta_data[i];
			}
			this.metadata = m;
			// this.num_metadata_blocks = num_blocks;// java: this.metadata.length
		}
if( Format.FLAC__HAS_OGG ) {
		if( ! this.ogg_encoder_aspect.set_num_metadata( num_blocks ) ) {
			return false;
		}
}
		return true;
	}

	boolean set_limit_min_bitrate(final boolean value) {
		// FLAC__ASSERT(0 != encoder);
		// FLAC__ASSERT(0 != encoder->private_);
		// FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.limit_min_bitrate = value;
		return true;
	}

	/*
	 * These four functions are not static, but not publicly exposed in
	 * include/FLAC/ either.  They are used by the test suite and in fuzzing
	 */
	boolean disable_instruction_set(final boolean value) {
		// FLAC__ASSERT(0 != encoder);
		// FLAC__ASSERT(0 != encoder->private_);
		// FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		return true;
	}

	/*
	 * These three functions are not static, but not publicly exposed in
	 * include/FLAC/ either.  They are used by the test suite.
	 */
	public final boolean disable_constant_subframes(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.disable_constant_subframes = value;
		return true;
	}

	public final boolean disable_fixed_subframes(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.disable_fixed_subframes = value;
		return true;
	}

	public final boolean disable_verbatim_subframes(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.disable_verbatim_subframes = value;
		return true;
	}

	public final int /* FLAC__StreamEncoderState */ get_state()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.state;
	}

	public final int /* FLAC__StreamDecoderState */ get_verify_decoder_state()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.do_verify ) {
			return this.verify.decoder.get_state();
		} else {
			return StreamDecoder.FLAC__STREAM_DECODER_UNINITIALIZED;
		}
	}

	public final String get_resolved_state_string()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR) {
			return FLAC__StreamEncoderStateString[this.state];
		} else {
			return this.verify.decoder.get_resolved_state_string();
		}
	}

	// java: changed
	public final ErrorStats get_verify_decoder_error_stats(/*long[] absolute_sample, int[] frame_number, int[] channel, int[] sample, int[] expected, int[] got*/)
	{
		return this.verify.error_stats;
	}

	public final boolean get_verify()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_verify;
	}

	public final boolean get_streamable_subset()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.streamable_subset;
	}

	public final boolean get_do_md5()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_md5;
	}

	public final int get_channels()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.channels;
	}

	public final int get_bits_per_sample()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.bits_per_sample;
	}

	public final int get_sample_rate()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.sample_rate;
	}

	public final int get_blocksize()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.blocksize;
	}

	public final boolean get_do_mid_side_stereo()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_mid_side_stereo;
	}

	public final boolean get_loose_mid_side_stereo()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.loose_mid_side_stereo;
	}

	public final int get_max_lpc_order()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.max_lpc_order;
	}

	public final int get_qlp_coeff_precision()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.qlp_coeff_precision;
	}

	public final boolean get_do_qlp_coeff_prec_search()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_qlp_coeff_prec_search;
	}

	public final boolean get_do_escape_coding()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_escape_coding;
	}

	public final boolean get_do_exhaustive_model_search()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_exhaustive_model_search;
	}

	public final int get_min_residual_partition_order()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.min_residual_partition_order;
	}

	public final int get_max_residual_partition_order()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.max_residual_partition_order;
	}

	public final int get_rice_parameter_search_dist()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.rice_parameter_search_dist;
	}

	public final long get_total_samples_estimate()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.total_samples_estimate;
	}

	boolean get_limit_min_bitrate()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.limit_min_bitrate;
	}

	public final boolean process(final int buffer[][], final int samples)
	{
		int j = 0, k = 0;
		final int nchannels = this.channels, block_size = this.blocksize, bps = this.bits_per_sample;
		final int sample_max = Integer.MAX_VALUE >> (32 - this.bits_per_sample);
		final int sample_min = Integer.MIN_VALUE >> (32 - this.bits_per_sample);

		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);

		if( this.state != FLAC__STREAM_ENCODER_OK ) {
			return false;
		}

		final int[] integer_signal_mid_side0 = this.integer_signal_mid_side[0];// java
		final int[] integer_signal_mid_side1 = this.integer_signal_mid_side[1];// java
		final long[] integer_signal_33_bit_side = this.integer_signal_33bit_side;// java
		final int[] buffer0 = buffer[0];// java
		final int[] buffer1 = buffer[1];// java
		final int[][] signal = this.integer_signal;// java
		do {
			int i = samples - j;
			int n = block_size + OVERREAD_ - this.current_sample_number;
			if( n > i ) {
				n = i;
			}

			if( this.do_verify ) {
				this.verify.input_fifo.append_to_verify_fifo_( buffer, j, nchannels, n );
			}

			for( int channel = 0; channel < nchannels; channel++ ) {
				for( i = this.current_sample_number, k = j; i <= blocksize && k < samples; i++, k++ ) {
					if( buffer[channel][k] < sample_min || buffer[channel][k] > sample_max ) {
						this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
						return false;
					}
				}
				final int b[] = buffer[channel];// java
				if( b == null ) {
					return false;
				}
				System.arraycopy( b, j, signal[channel], this.current_sample_number, n );
			}

			if( this.do_mid_side_stereo ) {
				//FLAC__ASSERT(channels == 2);
				/* "i <= blocksize" to overread 1 sample; see comment in OVERREAD_ decl */
				if( bps < 32 ) {
					for( i = this.current_sample_number; i <= block_size && j < samples; i++, j++ ) {
						integer_signal_mid_side1[i] = buffer0[j] - buffer1[j];
						integer_signal_mid_side0[i] = (buffer0[j] + buffer1[j]) >> 1; /* NOTE: not the same as 'mid = (buffer[0][j] + buffer[1][j]) / 2' ! */
					}
				} else {
					for( i = this.current_sample_number; i <= blocksize && j < samples; i++, j++) {
						this.integer_signal_33bit_side[i] = (long)buffer0[j] - (long)buffer1[j];
						this.integer_signal_mid_side[0][i] = (int)(((long)buffer0[j] + (long)buffer1[j]) >> 1); /* NOTE: not the same as 'mid = (buffer[0][j] + buffer[1][j]) / 2' ! */
					}
				}
			} else {
				j += n;
			}

			this.current_sample_number += n;

			/* we only process if we have a full block + 1 extra sample; final block is always handled by FLAC__stream_encoder_finish() */
			if( this.current_sample_number > block_size) {
				//FLAC__ASSERT(encoder->private_->current_sample_number == blocksize+OVERREAD_);
				//FLAC__ASSERT(OVERREAD_ == 1); /* assert we only overread 1 sample which simplifies the rest of the code below */
				if( ! process_frame_( /*is_last_block=*/false ) ) {
					return false;
				}
				/* move unprocessed overread samples to beginnings of arrays */
				for( int channel = 0; channel < nchannels; channel++ ) {
					signal[channel][0] = signal[channel][block_size];
				}
				if( this.do_mid_side_stereo ) {
					integer_signal_mid_side0[0] = integer_signal_mid_side0[block_size];
					if( bps < 32 ) {
						integer_signal_mid_side1[0] = integer_signal_mid_side1[block_size];
					} else {
						integer_signal_33_bit_side[0] = integer_signal_33_bit_side[block_size];
					}

				}
				this.current_sample_number = 1;
			}
		} while( j < samples );

		return true;
	}

	public final boolean process_interleaved(final int buffer[], final int samples)
	{
		final int nchannels = this.channels, block_size = this.blocksize, bps = this.bits_per_sample;
		final int sample_max = Integer.MAX_VALUE >> (32 - this.bits_per_sample);
		final int sample_min = Integer.MIN_VALUE >> (32 - this.bits_per_sample);

		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);

		if( this.state != FLAC__STREAM_ENCODER_OK ) {
			return false;
		}

		int j = 0, k = 0;
		/*
		 * we have several flavors of the same basic loop, optimized for
		 * different conditions:
		 */
		if( this.do_mid_side_stereo && nchannels == 2 ) {
			/*
			 * stereo coding: unroll channel loop
			 */
			final int[] integer_signal0 = this.integer_signal[0];// java
			final int[] integer_signal1 = this.integer_signal[1];// java
			final int[] integer_signal_mid_side0 = this.integer_signal_mid_side[0];// java
			final int[] integer_signal_mid_side1 = this.integer_signal_mid_side[1];// java
			final long[] integer_signal_33_bit_side = this.integer_signal_33bit_side;// java

			do {
				if( this.do_verify ) {
					int x = block_size + OVERREAD_ - this.current_sample_number;
					final int i = samples - j;
					if( x > i ) {
						x = i;
					}
					this.verify.input_fifo.append_to_verify_fifo_interleaved_( buffer, j, nchannels, x );
				}

				/* "i <= blocksize" to overread 1 sample; see comment in OVERREAD_ decl */
				int i;
				for( i = this.current_sample_number; i <= block_size && j < samples; i++, j++ ) {
					if( buffer[k]   < sample_min || buffer[k]   > sample_max ||
						buffer[k+1] < sample_min || buffer[k+1] > sample_max ) {
						this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
						return false;
					}
					integer_signal0[i] = buffer[k++];
					integer_signal1[i] = buffer[k++];
					if( bps < 32 ) {
						integer_signal_mid_side1[i] = integer_signal0[i] - integer_signal1[i];
						integer_signal_mid_side0[i] = (integer_signal0[i] + integer_signal1[i]) >> 1;
					} else {
						integer_signal_33_bit_side[i] = (long)integer_signal0[i] - (long)integer_signal1[i];
						integer_signal_mid_side0[i] = (int)(((long)integer_signal0[i] + (long)integer_signal1[i]) >> 1);
					}
				}
				this.current_sample_number = i;
				/* we only process if we have a full block + 1 extra sample; final block is always handled by FLAC__stream_encoder_finish() */
				if( i > block_size ) {
					if( ! process_frame_( /*is_last_block=*/false ) ) {
						return false;
					}
					/* move unprocessed overread samples to beginnings of arrays */
					//FLAC__ASSERT(i == blocksize+OVERREAD_);
					//FLAC__ASSERT(OVERREAD_ == 1); /* assert we only overread 1 sample which simplifies the rest of the code below */
					integer_signal0[0] = integer_signal0[block_size];
					integer_signal1[0] = integer_signal1[block_size];
					integer_signal_mid_side0[0] = integer_signal_mid_side0[block_size];
					integer_signal_mid_side1[0] = integer_signal_mid_side1[block_size];
					if( bps < 32 ) {
						integer_signal_mid_side1[0] = integer_signal_mid_side1[blocksize];
					} else {
						integer_signal_33_bit_side[0] = integer_signal_33_bit_side[blocksize];
					}
					this.current_sample_number = 1;
				}
			} while( j < samples );
		}
		else {
			final int[][] signal = this.integer_signal;// java
			/*
			 * independent channel coding: buffer each channel in inner loop
			 */
			do {
				if( this.do_verify ) {
					int x = block_size + OVERREAD_ - this.current_sample_number;
					final int i = samples - j;
					if( x > i ) {
						x = i;
					}
					this.verify.input_fifo.append_to_verify_fifo_interleaved_( buffer, j, nchannels, x );
				}

				/* "i <= blocksize" to overread 1 sample; see comment in OVERREAD_ decl */
				int i;
				for( i = this.current_sample_number; i <= block_size && j < samples; i++, j++ ) {
					for( int channel = 0; channel < nchannels; channel++ ) {
						if( buffer[k] < sample_min || buffer[k] > sample_max ) {
							this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
							return false;
						}
						signal[channel][i] = buffer[k++];
					}
				}
				this.current_sample_number = i;
				/* we only process if we have a full block + 1 extra sample; final block is always handled by FLAC__stream_encoder_finish() */
				if( i > block_size) {
					if( ! process_frame_( /*is_last_block=*/false ) ) {
						return false;
					}
					/* move unprocessed overread samples to beginnings of arrays */
					//FLAC__ASSERT(i == blocksize+OVERREAD_);
					//FLAC__ASSERT(OVERREAD_ == 1); /* assert we only overread 1 sample which simplifies the rest of the code below */
					for( int channel = 0; channel < nchannels; channel++ ) {
						signal[channel][0] = signal[channel][block_size];
					}
					this.current_sample_number = 1;
				}
			} while( j < samples );
		}

		return true;
	}

	/***********************************************************************
	 *
	 * Private class methods
	 *
	 ***********************************************************************/

	private final void set_defaults_()
	{
		//FLAC__ASSERT(0 != encoder);

/*#ifdef FLAC__MANDATORY_VERIFY_WHILE_ENCODING
		encoder.do_verify = true;
#else*/
		this.do_verify = false;
//#endif
		this.streamable_subset = true;
		this.do_md5 = true;
		this.do_mid_side_stereo = false;
		this.loose_mid_side_stereo = false;
		this.channels = 2;
		this.bits_per_sample = 16;
		this.sample_rate = 44100;
		this.blocksize = 0;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		this.num_apodizations = 1;
		this.apodizations[0].type = ApodizationSpecification.FLAC__APODIZATION_TUKEY;
		this.apodizations[0]./*parameters.tukey.*/p = 0.5f;
//#endif
		this.max_lpc_order = 0;
		this.qlp_coeff_precision = 0;
		this.do_qlp_coeff_prec_search = false;
		this.do_exhaustive_model_search = false;
		this.do_escape_coding = false;
		this.min_residual_partition_order = 0;
		this.max_residual_partition_order = 0;
		this.rice_parameter_search_dist = 0;
		this.total_samples_estimate = 0;
		this.limit_min_bitrate = false;
		this.metadata = null;
		// this.num_metadata_blocks = 0;

		this.seek_table = null;
		this.disable_constant_subframes = false;
		this.disable_fixed_subframes = false;
		this.disable_verbatim_subframes = false;
		this.is_ogg = false;
		this.read_callback = null;
		this.write_callback = null;
		this.seek_callback = null;
		this.tell_callback = null;
		this.metadata_callback = null;
		this.progress_callback = null;
		// this.client_data = null;

if( Format.FLAC__HAS_OGG ) {
		this.ogg_encoder_aspect.set_defaults();
}
		set_compression_level( 5 );
	}

	private final void free_()
	{

		//FLAC__ASSERT(0 != encoder);
		this.metadata = null;
		// this.num_metadata_blocks = 0;
/*
		for( i = 0; i < this.channels; i++ ) {
			this.integer_signal_unaligned[i] = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
			this.real_signal_unaligned[i] = null;
//#endif
		}
		for( i = 0; i < 2; i++ ) {
			this.integer_signal_mid_side_unaligned[i] = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
			this.real_signal_mid_side_unaligned[i] = null;
//#endif
		} */
		this.integer_signal_33bit_side = null;
/*#ifndef FLAC__INTEGER_ONLY_LIBRARY
		for( i = 0; i < this.num_apodizations; i++ ) {
			this.window_unaligned[i] = null;
		}
		this.windowed_signal_unaligned = null;
//#endif
		for( channel = 0; channel < this.channels; channel++ ) {
			for( i = 0; i < 2; i++ ) {
				this.residual_workspace_unaligned[channel][i] = null;
			}
		}
		for( channel = 0; channel < 2; channel++ ) {
			for( i = 0; i < 2; i++ ) {
				this.residual_workspace_mid_side_unaligned[channel][i] = null;
			}
		}
		this.abs_residual_partition_sums_unaligned = null;

		this.raw_bits_per_partition_unaligned = null;
*/
		if( this.do_verify ) {
			for( int i = 0; i < this.channels; i++ ) {
				this.verify.input_fifo.data[i] = null;
			}
		}
		this.frame.free();
	}

	private final boolean resize_buffers_(final int new_blocksize)
	{
		//FLAC__ASSERT(new_blocksize > 0);
		//FLAC__ASSERT(encoder->protected_->state == FLAC__STREAM_ENCODER_OK);
		//FLAC__ASSERT(encoder->private_->current_sample_number == 0);

		/* To avoid excessive malloc'ing, we only grow the buffer; no shrinking. */
		if( new_blocksize <= this.input_capacity ) {
			return true;
		}

	try {

		/* WATCHOUT: compute_residual_from_qlp_coefficients_asm_ia32_mmx() and ..._intrin_sse2()
		 * require that the input arrays (in our case the integer signals)
		 * have a buffer of up to 3 zeroes in front (at negative indices) for
		 * alignment purposes; we use 4 in front to keep the data well-aligned.
		 */

		for( int i = 0; i < this.channels; i++ ) {
			/*this.integer_signal_unaligned[i] = */this.integer_signal[i] = new int[new_blocksize + 4 + OVERREAD_];// already zeroed
			//if( ok ) {
				//memset(encoder.private_.integer_signal[i], 0, sizeof(FLAC__int32)*4);
				//encoder.private_.integer_signal[i] += 4;
			//}
		}
		for( int i = 0; i < 2; i++ ) {
			/*this.integer_signal_mid_side_unaligned[i] = */this.integer_signal_mid_side[i] = new int[new_blocksize + 4 + OVERREAD_];// already zeroed
			//if( ok ) {
				//memset(encoder.private_.integer_signal_mid_side[i], 0, sizeof(FLAC__int32)*4);
				//encoder.private_.integer_signal_mid_side[i] += 4;
			//}
		}
		this.integer_signal_33bit_side = new long[new_blocksize + 4 + OVERREAD_];// already zeroed
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		if( this.max_lpc_order > 0 ) {
			for( int i = 0; i < this.num_apodizations; i++ ) {
				/*this.window_unaligned[i] = */this.window[i] = new float[new_blocksize];
			}
			/*this.windowed_signal_unaligned = */this.windowed_signal = new float[new_blocksize];
		}
//#endif
		for( int channel = 0; channel < this.channels; channel++ ) {
			for( int i = 0; i < 2; i++ ) {
				/*this.residual_workspace_unaligned[channel][i] = */this.residual_workspace[channel][i] = new int[new_blocksize];
			}
		}

		boolean ok = true;

		for( int channel = 0; ok && channel < this.channels; channel++ ) {
			for( int i = 0; ok && i < 2; i++ ) {
				ok = ok && this.partitioned_rice_contents_workspace[channel][i].ensure_size( this.max_residual_partition_order );
				ok = ok && this.partitioned_rice_contents_workspace[channel][i].ensure_size( this.max_residual_partition_order );
			}
		}

		for( int channel = 0; channel < 2; channel++ ) {
			for( int i = 0; i < 2; i++ ) {
				/*this.residual_workspace_mid_side_unaligned[channel][i] = */this.residual_workspace_mid_side[channel][i] = new int[new_blocksize];
			}
		}

		for( int channel = 0; ok && channel < 2; channel++ ) {
			for( int i = 0; ok && i < 2; i++ ) {
				ok = ok && this.partitioned_rice_contents_workspace_mid_side[channel][i].ensure_size( this.max_residual_partition_order );
			}
		}

		for( int i = 0; ok && i < 2; i++ ) {
			ok = ok && this.partitioned_rice_contents_extra[i].ensure_size( this.max_residual_partition_order );
		}

		/* the *2 is an approximation to the series 1 + 1/2 + 1/4 + ... that sums tree occupies in a flat array */
		/*@@@ new_blocksize*2 is too pessimistic, but to fix, we need smarter logic because a smaller new_blocksize can actually increase the # of partitions; would require moving this out into a separate function, then checking its capacity against the need of the current blocksize&min/max_partition_order (and maybe predictor order) */
		/*this.abs_residual_partition_sums_unaligned = */this.abs_residual_partition_sums = new long[new_blocksize << 1];
		if( this.do_escape_coding ) {
			/*this.raw_bits_per_partition_unaligned = */this.raw_bits_per_partition = new int[new_blocksize << 1];
		}

		/* now adjust the windows if the blocksize has changed */
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		if( new_blocksize != this.input_capacity && this.max_lpc_order > 0 ) {
			for( int i = 0; i < this.num_apodizations; i++ ) {
				switch( this.apodizations[i].type ) {
					case ApodizationSpecification.FLAC__APODIZATION_BARTLETT:
						Window.bartlett( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_BARTLETT_HANN:
						Window.bartlett_hann( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_BLACKMAN:
						Window.blackman( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_BLACKMAN_HARRIS_4TERM_92DB_SIDELOBE:
						Window.blackman_harris_4term_92db_sidelobe( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_CONNES:
						Window.connes( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_FLATTOP:
						Window.flattop( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_GAUSS:
						Window.gauss( this.window[i], new_blocksize, this.apodizations[i]./*parameters.gauss.*/stddev );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_HAMMING:
						Window.hamming( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_HANN:
						Window.hann( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_KAISER_BESSEL:
						Window.kaiser_bessel( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_NUTTALL:
						Window.nuttall( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_RECTANGLE:
						Window.rectangle( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_TRIANGLE:
						Window.triangle( this.window[i], new_blocksize );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_TUKEY:
						Window.tukey( this.window[i], new_blocksize, this.apodizations[i]./*parameters.tukey.*/p );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_PARTIAL_TUKEY:
						Window.partial_tukey( this.window[i], new_blocksize, this.apodizations[i]./*parameters.multiple_tukey.*/p, this.apodizations[i]./*parameters.multiple_tukey.*/start, this.apodizations[i]./*parameters.multiple_tukey.*/end );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_PUNCHOUT_TUKEY:
						Window.punchout_tukey( this.window[i], new_blocksize, this.apodizations[i]./*parameters.multiple_tukey.*/p, this.apodizations[i]./*parameters.multiple_tukey.*/start, this.apodizations[i]./*parameters.multiple_tukey.*/end );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_SUBDIVIDE_TUKEY:
						Window.tukey( this.window[i], new_blocksize, this.apodizations[i]./*parameters.tukey.*/p );
						break;
					case ApodizationSpecification.FLAC__APODIZATION_WELCH:
						Window.welch( this.window[i], new_blocksize );
						break;
					default:
						//FLAC__ASSERT(0);
						/* double protection */
						Window.hann( this.window[i], new_blocksize );
						break;
				}
			}
		}
//#endif

			this.input_capacity = new_blocksize;
	} catch( final OutOfMemoryError e ) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
	}

		return true;
	}

	private final boolean write_bitbuffer_(final int samples, final boolean is_last_block)
	{
		final BitWriterHelperStruct buffer;

		//FLAC__ASSERT(FLAC__bitwriter_is_byte_aligned(encoder.private_.frame));

		if( null == (buffer = this.frame.get_buffer( /* buffer, bytes */ )) ) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
		}

		//bytes = buffer.length;
		if( this.do_verify ) {
			this.verify.output.data = buffer.bytebuffer;
			this.verify.output.offset = 0;
			this.verify.output.bytes = buffer.bytes;
			if( this.verify.state_hint == ENCODER_IN_MAGIC ) {
				this.verify.needs_magic_hack = true;
			}
			else {
				if( ! this.verify.decoder.process_single()
					    || ( ! is_last_block
						    && (get_verify_decoder_state() == StreamDecoder.FLAC__STREAM_DECODER_END_OF_STREAM) )
					    || this.state == FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR /* Happens when error callback was used */) {
					this.frame.release_buffer();
					this.frame.clear();
					if( this.state != FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA ) {
						this.state = FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR;
					}
					return false;
				}
			}
		}

		if( write_frame_( buffer.bytebuffer, buffer.bytes, samples, is_last_block ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
			this.frame.release_buffer();
			this.frame.clear();
			this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
			return false;
		}

		this.frame.release_buffer();
		this.frame.clear();

		if( samples > 0 ) {
			final StreamInfo stream_info = this.streaminfo;
			int m = stream_info.min_framesize;
			if( m > buffer.bytes ) {
				m = buffer.bytes;// min
			}
			stream_info.min_framesize = m;
			m = stream_info.max_framesize;
			if( m < buffer.bytes ) {
				m = buffer.bytes;// max
			}
			stream_info.max_framesize = m;
		}

		return true;
	}

	private final int /* FLAC__StreamEncoderWriteStatus */ write_frame_(final byte buffer[], final int bytes, final int samples, final boolean is_last_block)
	{
		int /* FLAC__StreamEncoderWriteStatus */ status;
		long output_position = 0;

		/* FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED just means we didn't get the offset; no error */
		try {
			if( this.tell_callback != null ) {
				output_position = this.tell_callback.enc_tell_callback( this/*, this.client_data*/ );
			}
		} catch(final IOException e) {
			this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
			return FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
		} catch(final UnsupportedOperationException e) {
		}

		/*
		 * Watch for the STREAMINFO block and first SEEKTABLE block to go by and store their offsets.
		 */
		if( samples == 0 ) {
			final int /* FLAC__MetadataType */ type = (buffer[0] & 0x7f);
			if( type == Format.FLAC__METADATA_TYPE_STREAMINFO ) {
				this.streaminfo_offset = output_position;
			} else if( type == Format.FLAC__METADATA_TYPE_SEEKTABLE && this.seektable_offset == 0 ) {
				this.seektable_offset = output_position;
			}
		}

		/*
		 * Mark the current seek point if hit (if audio_offset == 0 that
		 * means we're still writing metadata and haven't hit the first
		 * frame yet)
		 */
		if( null != this.seek_table && this.audio_offset > 0 && this.seek_table.num_points > 0 ) {
			final int block_size = get_blocksize();
			final long frame_first_sample = this.samples_written;
			final long frame_last_sample = frame_first_sample + (long)block_size - 1L;
			final SeekPoint[] points = this.seek_table.points;// java
			for( int i = this.first_seekpoint_to_check, ie = this.seek_table.num_points; i < ie; i++ ) {
				final SeekPoint p = points[i];// java
				final long test_sample = p.sample_number;
				if( test_sample > frame_last_sample ) {
					break;
				}
				else if( test_sample >= frame_first_sample ) {
					p.sample_number = frame_first_sample;
					p.stream_offset = output_position - this.audio_offset;
					p.frame_samples = block_size;
					this.first_seekpoint_to_check++;
					/* DO NOT: "break;" and here's why:
					 * The seektable template may contain more than one target
					 * sample for any given frame; we will keep looping, generating
					 * duplicate seekpoints for them, and we'll clean it up later,
					 * just before writing the seektable back to the metadata.
					 */
				}
				else {
					this.first_seekpoint_to_check++;
				}
			}
		}

if( Format.FLAC__HAS_OGG &&
			this.is_ogg ) {
			status = this.ogg_encoder_aspect.write_callback_wrapper(
				buffer,
				bytes,
				samples,
				this.current_frame_number,
				is_last_block,
				(OggEncoderAspectWriteCallbackProxy)this.write_callback,
				this//, this.client_data
			);
		} else {
			status = this.write_callback.enc_write_callback( this, buffer, 0, bytes, samples, this.current_frame_number/*, this.client_data*/ );
}

		if( status == FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
			this.bytes_written += bytes;
			this.samples_written += samples;
			/* we keep a high watermark on the number of frames written because
			 * when the encoder goes back to write metadata, 'current_frame'
			 * will drop back to 0.
			 */
			int max = this.current_frame_number + 1;
			if( max < this.frames_written ) {
				max = this.frames_written;
			}
			this.frames_written = max;
		} else {
			this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
		}

		return status;
	}

	/** Gets called when the encoding process has finished so that we can update the STREAMINFO and SEEKTABLE blocks.  */
	private final void update_metadata_()
	{
		@SuppressWarnings("unused")
		final byte b[] = new byte[6 >= Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH ? 6 : Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH];
		final StreamInfo stream_info = this.streaminfo;
		long samples = stream_info.total_samples;
		final int min_framesize = stream_info.min_framesize;
		final int max_framesize = stream_info.max_framesize;
		final int bps = stream_info.bits_per_sample;
		int /* FLAC__StreamEncoderSeekStatus */ seek_status;

		//FLAC__ASSERT(metadata.type == FLAC__METADATA_TYPE_STREAMINFO);

		/* All this is based on intimate knowledge of the stream header
		 * layout, but a change to the header format that would break this
		 * would also break all streams encoded in the previous format.
		 */

		/*
		 * Write MD5 signature
		 */
		{
			final int md5_offset =
					Format.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN
				) / 8;

			if( (seek_status = this.seek_callback.enc_seek_callback( this, this.streaminfo_offset + md5_offset/*, this.client_data*/ )) != FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
				if( seek_status == FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				}
				return;
			}
			if( this.write_callback.enc_write_callback( this, stream_info.md5sum, 0, 16, 0, 0/*, this.client_data*/ ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
				this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				return;
			}
		}

		/*
		 * Write total samples
		 */
		{
			final int total_samples_byte_offset =
					Format.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN
					- 4
				) / 8;
			if( samples > (1L << Format.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN) ) {
				samples = 0;
			}

			b[0] = (byte)(((bps - 1) << 4) | ((int)(samples >>> 32)/* & 0xFF*/));
			b[1] = (byte)((samples >>> 24)/* & 0xFF*/);
			b[2] = (byte)((samples >>> 16)/* & 0xFF*/);
			b[3] = (byte)((samples >>> 8)/* & 0xFF*/);
			b[4] = (byte)(samples/* & 0xFF*/);
			if( (seek_status = this.seek_callback.enc_seek_callback( this, this.streaminfo_offset + total_samples_byte_offset/*, this.client_data*/ )) != FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
				if( seek_status == FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				}
				return;
			}
			if( this.write_callback.enc_write_callback( this, b, 0, 5, 0, 0/*, this.client_data*/ ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
				this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				return;
			}
		}

		/*
		 * Write min/max framesize
		 */
		{
			final int min_framesize_offset =
					Format.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN
				) / 8;

			b[0] = (byte)((min_framesize >>> 16)/* & 0xFF*/);
			b[1] = (byte)((min_framesize >>> 8)/* & 0xFF*/);
			b[2] = (byte)(min_framesize/* & 0xFF*/);
			b[3] = (byte)((max_framesize >>> 16)/* & 0xFF*/);
			b[4] = (byte)((max_framesize >>> 8)/* & 0xFF*/);
			b[5] = (byte)(max_framesize/* & 0xFF*/);
			if( (seek_status = this.seek_callback.enc_seek_callback( this, this.streaminfo_offset + min_framesize_offset/*, this.client_data*/ )) != FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
				if( seek_status == FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				}
				return;
			}
			if( this.write_callback.enc_write_callback( this, b, 0, 6, 0, 0/*, this.client_data*/ ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
				this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				return;
			}
		}

		/*
		 * Write seektable
		 */
		if( null != this.seek_table && this.seek_table.num_points > 0 && this.seektable_offset > 0 ) {
			this.seek_table.FLAC__format_seektable_sort();

			//FLAC__ASSERT(FLAC__format_seektable_is_legal(encoder->private_->seek_table));

			if( (seek_status = this.seek_callback.enc_seek_callback( this, this.seektable_offset + Format.FLAC__STREAM_METADATA_HEADER_LENGTH/*, this.client_data*/ )) != FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
				if( seek_status == FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				}
				return;
			}

			final StreamEncoderWriteCallback write = this.write_callback;// java
			final SeekPoint[] points = this.seek_table.points;// java
			for( int i = 0, ie = this.seek_table.num_points; i < ie; i++ ) {
				final SeekPoint p = points[i];// java
				long xx = p.sample_number;
				b[7] = (byte)xx; xx >>>= 8;
				b[6] = (byte)xx; xx >>>= 8;
				b[5] = (byte)xx; xx >>>= 8;
				b[4] = (byte)xx; xx >>>= 8;
				b[3] = (byte)xx; xx >>>= 8;
				b[2] = (byte)xx; xx >>>= 8;
				b[1] = (byte)xx; xx >>>= 8;
				b[0] = (byte)xx; xx >>>= 8;
				xx = p.stream_offset;
				b[15] = (byte)xx; xx >>>= 8;
				b[14] = (byte)xx; xx >>>= 8;
				b[13] = (byte)xx; xx >>>= 8;
				b[12] = (byte)xx; xx >>>= 8;
				b[11] = (byte)xx; xx >>>= 8;
				b[10] = (byte)xx; xx >>>= 8;
				b[9] = (byte)xx; xx >>>= 8;
				b[8] = (byte)xx; xx >>>= 8;
				int x = p.frame_samples;
				b[17] = (byte)x; x >>>= 8;
				b[16] = (byte)x; x >>>= 8;
				if( write.enc_write_callback( this, b, 0, 18, 0, 0/*, this.client_data*/ ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
					return;
				}
			}
		}
	}

	/** Gets called when the encoding process has finished so that we can update the STREAMINFO and SEEKTABLE blocks.  */
	private final void update_ogg_metadata_()
	{
if( Format.FLAC__HAS_OGG ) {
		/* the # of bytes in the 1st packet that precede the STREAMINFO */
		final int FIRST_OGG_PACKET_STREAMINFO_PREFIX_LENGTH =
			OggMapping.FLAC__OGG_MAPPING_PACKET_TYPE_LENGTH +
			OggMapping.FLAC__OGG_MAPPING_MAGIC_LENGTH +
			OggMapping.FLAC__OGG_MAPPING_VERSION_MAJOR_LENGTH +
			OggMapping.FLAC__OGG_MAPPING_VERSION_MINOR_LENGTH +
			OggMapping.FLAC__OGG_MAPPING_NUM_HEADERS_LENGTH +
			Format.FLAC__STREAM_SYNC_LENGTH
		;


		//FLAC__ASSERT(metadata.type == FLAC__METADATA_TYPE_STREAMINFO);
		//FLAC__ASSERT(0 != this.seek_callback);

		/* Pre-check that client supports seeking, since we don't want the
		 * ogg_helper code to ever have to deal with this condition.
		 */
		if( this.seek_callback.enc_seek_callback( this, 0/*, this.client_data*/ ) == FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED ) {
			return;
		}

		/* All this is based on intimate knowledge of the stream header
		 * layout, but a change to the header format that would break this
		 * would also break all streams encoded in the previous format.
		 */

		/**
		 ** Write STREAMINFO stats
		 **/
		final OggPage page = new OggPage();
		OggEncoderAspect.simple_ogg_page__init( page );
		if( ! OggEncoderAspect.simple_ogg_page__get_at( this, this.streaminfo_offset, page,
				this.seek_callback, this.read_callback/*, this.client_data */ ) ) {
			OggEncoderAspect.simple_ogg_page__clear( page );
			return; /* state already set */
		}

		final StreamInfo stream_info = this.streaminfo;
		final long samples = stream_info.total_samples;
		final int min_framesize = stream_info.min_framesize;
		final int max_framesize = stream_info.max_framesize;
		/*
		 * Write MD5 signature
		 */
		{

			final int md5_offset =
				FIRST_OGG_PACKET_STREAMINFO_PREFIX_LENGTH +
				Format.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN
				) / 8;

			if( md5_offset + 16 > page.body_len) {
				this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
				OggEncoderAspect.simple_ogg_page__clear( page );
				return;
			}
			System.arraycopy( stream_info.md5sum, 0, page.body_base, page.body + md5_offset, 16 );
		}

		@SuppressWarnings("unused")
		final byte b[] = new byte[6 >= Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH ? 6 : Format.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH];
		/*
		 * Write total samples
		 */
		{
			final int total_samples_byte_offset =
				FIRST_OGG_PACKET_STREAMINFO_PREFIX_LENGTH +
				Format.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN
					- 4
				) / 8;

			if( total_samples_byte_offset + 5 > page.body_len) {
				this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
				OggEncoderAspect.simple_ogg_page__clear( page );
				return;
			}
			final int offset = page.body + total_samples_byte_offset;
			b[0] = (byte)(page.body_base[offset] & 0xF0);
			b[0] |= (byte)((samples >>> 32) & 0x0F);
			b[1] = (byte)((samples >>> 24)/* & 0xFF*/);
			b[2] = (byte)((samples >>> 16)/* & 0xFF*/);
			b[3] = (byte)((samples >>> 8)/* & 0xFF*/);
			b[4] = (byte)(samples/* & 0xFF*/);
			System.arraycopy( b, 0, page.body_base, offset, 5 );
		}

		/*
		 * Write min/max framesize
		 */
		{
			final int min_framesize_offset =
				FIRST_OGG_PACKET_STREAMINFO_PREFIX_LENGTH +
				Format.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Format.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Format.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN
				) / 8;

			if( min_framesize_offset + 6 > page.body_len ) {
				this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
				OggEncoderAspect.simple_ogg_page__clear( page );
				return;
			}
			b[0] = (byte)((min_framesize >>> 16)/* & 0xFF*/);
			b[1] = (byte)((min_framesize >>> 8)/* & 0xFF*/);
			b[2] = (byte)(min_framesize/* & 0xFF*/);
			b[3] = (byte)((max_framesize >>> 16)/* & 0xFF*/);
			b[4] = (byte)((max_framesize >>> 8)/* & 0xFF*/);
			b[5] = (byte)(max_framesize/* & 0xFF*/);
			System.arraycopy( b, 0, page.body_base, page.body + min_framesize_offset, 6 );
		}
		if( ! OggEncoderAspect.simple_ogg_page__set_at( this, this.streaminfo_offset, page,
				this.seek_callback, this.write_callback/*, this.client_data*/ ) ) {
			OggEncoderAspect.simple_ogg_page__clear( page );
			return; /* state already set */
		}
		OggEncoderAspect.simple_ogg_page__clear( page );

		/*
		 * Write seektable
		 */
		final int num_points = this.seek_table == null ? 0 : this.seek_table.num_points;// java
		if( null != this.seek_table && num_points > 0 && this.seektable_offset > 0 ) {

			this.seek_table.FLAC__format_seektable_sort();

			//FLAC__ASSERT(FLAC__format_seektable_is_legal(encoder->private_->seek_table));

			OggEncoderAspect.simple_ogg_page__init( page );
			if( ! OggEncoderAspect.simple_ogg_page__get_at( this, this.seektable_offset, page,
					this.seek_callback, this.read_callback/*, this.client_data*/ ) ) {
				OggEncoderAspect.simple_ogg_page__clear( page );
				return; /* state already set */
			}

			if( (Format.FLAC__STREAM_METADATA_HEADER_LENGTH + 18 * num_points) != page.body_len ) {
				this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
				OggEncoderAspect.simple_ogg_page__clear( page );
				return;
			}

			final SeekPoint[] points = this.seek_table.points;// java
			for(int i = 0, off = page.body + Format.FLAC__STREAM_METADATA_HEADER_LENGTH; i < num_points; i++, off += 18 ) {
				final SeekPoint p = points[i];// java
				long xx = p.sample_number;
				b[7] = (byte)xx; xx >>>= 8;
				b[6] = (byte)xx; xx >>>= 8;
				b[5] = (byte)xx; xx >>>= 8;
				b[4] = (byte)xx; xx >>>= 8;
				b[3] = (byte)xx; xx >>>= 8;
				b[2] = (byte)xx; xx >>>= 8;
				b[1] = (byte)xx; xx >>>= 8;
				b[0] = (byte)xx; xx >>>= 8;
				xx = p.stream_offset;
				b[15] = (byte)xx; xx >>>= 8;
				b[14] = (byte)xx; xx >>>= 8;
				b[13] = (byte)xx; xx >>>= 8;
				b[12] = (byte)xx; xx >>>= 8;
				b[11] = (byte)xx; xx >>>= 8;
				b[10] = (byte)xx; xx >>>= 8;
				b[9] = (byte)xx; xx >>>= 8;
				b[8] = (byte)xx; xx >>>= 8;
				int x = p.frame_samples;
				b[17] = (byte)x; x >>>= 8;
				b[16] = (byte)x; x >>>= 8;
				System.arraycopy( b, 0, page.body_base, off, 18 );
			}

			if( ! OggEncoderAspect.simple_ogg_page__set_at( this, this.seektable_offset, page,
					this.seek_callback, this.write_callback/*, this.client_data*/ ) ) {
				OggEncoderAspect.simple_ogg_page__clear( page );
				return; /* state already set */
			}
			OggEncoderAspect.simple_ogg_page__clear( page );
		}
}
	}

	private final boolean process_frame_(final boolean is_last_block)
	{
		//FLAC__ASSERT(encoder->protected_->state == FLAC__STREAM_ENCODER_OK);

		/*
		 * Accumulate raw signal to the MD5 signature
		 */
		if( this.do_md5 && ! this.md5context.MD5Accumulate( this.integer_signal, this.channels, this.blocksize, ((this.bits_per_sample + 7) >>> 3))) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
		}

		/*
		 * Process the frame header and subframes into the frame bitbuffer
		 */
		if( ! process_subframes_() ) {
			/* the above function sets the state for us in case of an error */
			return false;
		}

		/*
		 * Zero-pad the frame to a byte_boundary
		 */
		if( ! this.frame.zero_pad_to_byte_boundary() ) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
		}

		/*
		 * CRC-16 the whole thing
		 */
		//FLAC__ASSERT(FLAC__bitwriter_is_byte_aligned(encoder->private_->frame));
		try {
			final int crc = this.frame.get_write_crc16();
			if(	! this.frame.write_raw_uint32( crc, Format.FLAC__FRAME_FOOTER_CRC_LEN )) {
				this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
				return false;
			}
		} catch(final OutOfMemoryError e) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
		}

		/*
		 * Write it
		 */
		if( ! write_bitbuffer_( this.blocksize, is_last_block ) ) {
			/* the above function sets the state for us in case of an error */
			return false;
		}

		/*
		 * Get ready for the next frame
		 */
		this.current_sample_number = 0;
		this.current_frame_number++;
		this.streaminfo.total_samples += (long)this.blocksize;

		return true;
	}

	/*
	 * These routines are private to libFLAC
	 */
	/*unsigned FLAC__format_get_max_rice_partition_order(unsigned blocksize, unsigned predictor_order)
	{
		return
			FLAC__format_get_max_rice_partition_order_from_blocksize_limited_max_and_predictor_order(
				FLAC__format_get_max_rice_partition_order_from_blocksize(blocksize),
				blocksize,
				predictor_order
			);
	}*/

	private static int FLAC__format_get_max_rice_partition_order_from_blocksize(int blocksize)
	{
		int max_rice_partition_order = 0;
		while( 0 == (blocksize & 1) ) {
			max_rice_partition_order++;
			blocksize >>>= 1;
		}
		return Format.FLAC__MAX_RICE_PARTITION_ORDER <= max_rice_partition_order ? Format.FLAC__MAX_RICE_PARTITION_ORDER : max_rice_partition_order;
	}

	private final boolean process_subframes_()
	{
		final FrameHeader frame_header = new FrameHeader();
		int min_partition_order = this.min_residual_partition_order, max_partition_order;
		boolean do_independent, do_mid_side;
		final boolean backup_disable_constant_subframes = this.disable_constant_subframes;
		boolean all_subframes_constant = true;

		/*
		 * Calculate the min,max Rice partition orders
		 */
		max_partition_order = FLAC__format_get_max_rice_partition_order_from_blocksize( this.blocksize );
		if( max_partition_order > this.max_residual_partition_order ) {
			max_partition_order = this.max_residual_partition_order;
		}
		if( min_partition_order > max_partition_order ) {
			min_partition_order = max_partition_order;
		}

		/*
		 * Setup the frame
		 */
		frame_header.blocksize = this.blocksize;
		frame_header.sample_rate = this.sample_rate;
		frame_header.channels = this.channels;
		frame_header.channel_assignment = Format.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT; /* the default unless the encoder determines otherwise */
		frame_header.bits_per_sample = this.bits_per_sample;
		frame_header.number_type = Format.FLAC__FRAME_NUMBER_TYPE_FRAME_NUMBER;
		frame_header/*.number*/.frame_number = this.current_frame_number;

		/*
		 * Figure out what channel assignments to try
		 */
		if( this.do_mid_side_stereo ) {
			if( this.loose_mid_side_stereo ) {
				if( this.loose_mid_side_stereo_frame_count == 0 ) {
					do_independent = true;
					do_mid_side = true;
				}
				else {
					do_independent = (this.last_channel_assignment == Format.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT);
					do_mid_side = ! do_independent;
				}
			}
			else {
				do_independent = true;
				do_mid_side = true;
			}
		}
		else {
			do_independent = true;
			do_mid_side = false;
		}

		//FLAC__ASSERT(do_independent || do_mid_side);
		final Subframe[][] workspace = this.subframe_workspace;// java
		final int[] bps = this.subframe_bps;// java
		/*
		 * Check for wasted bits; set effective bps for each subframe
		 */
		if( do_independent ) {
			for( int channel = 0, nchannels = this.channels; channel < nchannels; channel++ ) {
				int w = get_wasted_bits_(this.integer_signal[channel], this.blocksize);
				if( w > this.bits_per_sample ) {
					w = this.bits_per_sample;
				}
				workspace[channel][0].wasted_bits = workspace[channel][1].wasted_bits = w;
				bps[channel] = this.bits_per_sample - w;
			}
		}
		if( do_mid_side ) {
			//FLAC__ASSERT(encoder->protected_->channels == 2);
			for( int channel = 0; channel < 2; channel++ ) {
				int w;
				if( this.bits_per_sample < 32 || channel == 0 ) {
					w = get_wasted_bits_(this.integer_signal_mid_side[channel], this.blocksize);
				} else {
					w = get_wasted_bits_wide_(this.integer_signal_33bit_side, this.integer_signal_mid_side[channel], this.blocksize);
				}

				if( w > this.bits_per_sample ) {
					w = this.bits_per_sample;
				}
				this.subframe_workspace_mid_side[channel][0].wasted_bits = this.subframe_workspace_mid_side[channel][1].wasted_bits = w;
				this.subframe_bps_mid_side[channel] = this.bits_per_sample - w + (channel == 0? 0 : 1);
			}
		}

		/*
		 * First do a normal encoding pass of each independent channel
		 */
		final int[] subframe = this.best_subframe;// java
		if( do_independent ) {
			for( int channel = 0, nchannels = this.channels; channel < nchannels; channel++ ) {
				if( this.limit_min_bitrate && all_subframes_constant && (channel + 1) == this.channels ) {
					/* This frame contains only constant subframes at this point.
					 * To prevent the frame from becoming too small, make sure
					 * the last subframe isn't constant */
					this.disable_constant_subframes = true;
				}
				if( !
					process_subframe_(
						min_partition_order,
						max_partition_order,
						frame_header,
						bps[channel],
						this.integer_signal[channel],
						this.subframe_workspace[channel],// FIXME this.subframe_workspace_ptr[channel],
						this.partitioned_rice_contents_workspace[channel],// FIXME this.partitioned_rice_contents_workspace_ptr[channel],
						this.residual_workspace[channel],
						subframe/* + channel*/,
						this.best_subframe_bits/* + channel*/,
						channel// java: added
					)
				) {
					return false;
				}
				if( this.subframe_workspace[channel][this.best_subframe[channel]].type != Format.FLAC__SUBFRAME_TYPE_CONSTANT ) {
					all_subframes_constant = false;
				}
			}
		}

		/*
		 * Now do mid and side channels if requested
		 */
		if( do_mid_side ) {
			//FLAC__ASSERT(encoder->protected_->channels == 2);

			for( int channel = 0; channel < 2; channel++ ) {
				Object integer_signal_;
				if( this.subframe_bps_mid_side[channel] <= 32 ) {
					integer_signal_ = this.integer_signal_mid_side[channel];
				} else {
					integer_signal_ = this.integer_signal_33bit_side;
				}
				if( !
					process_subframe_(
						min_partition_order,
						max_partition_order,
						frame_header,
						this.subframe_bps_mid_side[channel],
						integer_signal_,
						this.subframe_workspace_mid_side[channel],// FIXME this.subframe_workspace_ptr_mid_side[channel],
						this.partitioned_rice_contents_workspace_mid_side[channel],// FIXME this.partitioned_rice_contents_workspace_ptr_mid_side[channel],
						this.residual_workspace_mid_side[channel],
						this.best_subframe_mid_side/* + channel*/,
						this.best_subframe_bits_mid_side/* + channel*/,
						channel// java: added
					)
				) {
					return false;
				}
			}
		}

		/*
		 * Compose the frame bitbuffer
		 */
		if( do_mid_side ) {
			int left_bps = 0, right_bps = 0; /* initialized only to prevent superfluous compiler warning */
			Subframe left_subframe = null, right_subframe = null; /* initialized only to prevent superfluous compiler warning */
			int /* FLAC__ChannelAssignment */ channel_assignment;

			//FLAC__ASSERT(encoder->protected_->channels == 2);

			if( this.loose_mid_side_stereo && this.loose_mid_side_stereo_frame_count > 0 ) {
				channel_assignment = (this.last_channel_assignment == Format.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT ?
						Format.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT : Format.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE);
			}
			else {
				final int bits[] = new int[4]; /* WATCHOUT - indexed by FLAC__ChannelAssignment */

				//FLAC__ASSERT(FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT == 0);
				//FLAC__ASSERT(FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE   == 1);
				//FLAC__ASSERT(FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE  == 2);
				//FLAC__ASSERT(FLAC__CHANNEL_ASSIGNMENT_MID_SIDE    == 3);
				//FLAC__ASSERT(do_independent && do_mid_side);

				/* We have to figure out which channel assignent results in the smallest frame */
				bits[Format.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT] = this.best_subframe_bits         [0] + this.best_subframe_bits         [1];
				bits[Format.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE  ] = this.best_subframe_bits         [0] + this.best_subframe_bits_mid_side[1];
				bits[Format.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE ] = this.best_subframe_bits         [1] + this.best_subframe_bits_mid_side[1];
				bits[Format.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE   ] = this.best_subframe_bits_mid_side[0] + this.best_subframe_bits_mid_side[1];

				channel_assignment = Format.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT;
				int min_bits = bits[channel_assignment];

				/* When doing loose mid-side stereo, ignore left-side
				 * and right-side options */
				for( int ca = this.loose_mid_side_stereo ? 3 : 1; ca <= 3; ca++ ) {
					if( bits[ca] < min_bits ) {
						min_bits = bits[ca];
						channel_assignment = /*(FLAC__ChannelAssignment)*/ca;
					}
				}
			}

			frame_header.channel_assignment = channel_assignment;

			if( ! frame_header.add_header( this.frame ) ) {
				this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
				return false;
			}

			switch( channel_assignment ) {
				case Format.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT:
					left_subframe  = this.subframe_workspace         [0][subframe                   [0]];
					right_subframe = this.subframe_workspace         [1][subframe                   [1]];
					break;
				case Format.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE:
					left_subframe  = this.subframe_workspace         [0][subframe                   [0]];
					right_subframe = this.subframe_workspace_mid_side[1][this.best_subframe_mid_side[1]];
					break;
				case Format.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE:
					left_subframe  = this.subframe_workspace_mid_side[1][this.best_subframe_mid_side[1]];
					right_subframe = this.subframe_workspace         [1][subframe         [1]];
					break;
				case Format.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE:
					left_subframe  = this.subframe_workspace_mid_side[0][this.best_subframe_mid_side[0]];
					right_subframe = this.subframe_workspace_mid_side[1][this.best_subframe_mid_side[1]];
					break;
				default:
					//FLAC__ASSERT(0);
			}

			switch( channel_assignment ) {
				case Format.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT:
					left_bps  = bps                       [0];
					right_bps = bps                       [1];
					break;
				case Format.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE:
					left_bps  = bps                       [0];
					right_bps = this.subframe_bps_mid_side[1];
					break;
				case Format.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE:
					left_bps  = this.subframe_bps_mid_side[1];
					right_bps = bps                       [1];
					break;
				case Format.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE:
					left_bps  = this.subframe_bps_mid_side[0];
					right_bps = this.subframe_bps_mid_side[1];
					break;
				default:
					//FLAC__ASSERT(0);
			}

			/* note that encoder_add_subframe_ sets the state for us in case of an error */
			if( ! add_subframe_( frame_header.blocksize, left_bps , left_subframe , this.frame ) ) {
				return false;
			}
			if( ! add_subframe_( frame_header.blocksize, right_bps, right_subframe, this.frame ) ) {
				return false;
			}
		}
		else {
			if( ! frame_header.add_header( this.frame ) ) {
				this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
				return false;
			}

			for( int channel = 0, nchannels = this.channels; channel < nchannels; channel++ ) {
				if( ! add_subframe_( frame_header.blocksize, bps[channel], workspace[channel][ subframe[channel] ], this.frame ) ) {
					/* the above function sets the state for us in case of an error */
					return false;
				}
			}
		}

		if( this.loose_mid_side_stereo ) {
			this.loose_mid_side_stereo_frame_count++;
			if( this.loose_mid_side_stereo_frame_count >= this.loose_mid_side_stereo_frames ) {
				this.loose_mid_side_stereo_frame_count = 0;
			}
		}

		this.last_channel_assignment = frame_header.channel_assignment;
		this.disable_constant_subframes = backup_disable_constant_subframes;

		return true;
	}

// fixed.c
	private static final double M_LN2 = Math.log( 2.0 );//0.69314718055994530942
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	// java: you shouldn't use an offset to call the function
	// data + Format.FLAC__MAX_FIXED_ORDER -> data
	// data_len - Format.FLAC__MAX_FIXED_ORDER -> data_len
	private static int FLAC__fixed_compute_best_predictor(final int data[], final int data_len, final float residual_bits_per_sample[]/*[FLAC__MAX_FIXED_ORDER+1]*/)
//#else
//		unsigned FLAC__fixed_compute_best_predictor(const FLAC__int32 data[], unsigned data_len, FLAC__fixedpoint residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1])
//#endif
	{
		int total_error_0 = 0, total_error_1 = 0, total_error_2 = 0, total_error_3 = 0, total_error_4 = 0;
//#if 0// java: keep this old code because it is better optimized
		/* This code has been around a long time, and was written when compilers weren't able
		 * to vectorize code. These days, compilers are better in optimizing the next block
		 * which is also much more readable
		 */
		int last_error_0 = data[Format.FLAC__MAX_FIXED_ORDER - 1];
		int last_error_1 = data[Format.FLAC__MAX_FIXED_ORDER - 1] - data[Format.FLAC__MAX_FIXED_ORDER - 2];
		int last_error_2 = last_error_1 - (data[Format.FLAC__MAX_FIXED_ORDER - 2] - data[Format.FLAC__MAX_FIXED_ORDER - 3]);
		int last_error_3 = last_error_2 - (data[Format.FLAC__MAX_FIXED_ORDER - 2] - (data[Format.FLAC__MAX_FIXED_ORDER - 3] << 1) + data[Format.FLAC__MAX_FIXED_ORDER - 4]);

		/* total_error_* are 64-bits to avoid overflow when encoding FIXME they are 32 bits! copy-paste mistake from the FLAC__fixed_compute_best_predictor_wide?
		 * erratic signals when the bits-per-sample and blocksize are
		 * large.
		 */
		for(int i = Format.FLAC__MAX_FIXED_ORDER; i < data_len; i++ ) {
			int error  = data[i] ; total_error_0 += error < 0 ? -error : error;                  int save = error;
			error -= last_error_0; total_error_1 += error < 0 ? -error : error; last_error_0 = save; save = error;
			error -= last_error_1; total_error_2 += error < 0 ? -error : error; last_error_1 = save; save = error;
			error -= last_error_2; total_error_3 += error < 0 ? -error : error; last_error_2 = save; save = error;
			error -= last_error_3; total_error_4 += error < 0 ? -error : error; last_error_3 = save;
		}
//#else
//		for(int i = Format.FLAC__MAX_FIXED_ORDER; i < data_len; i++) {
//			total_error_0 += Math.abs(data[ i ]);
//			total_error_1 += Math.abs(data[ i ] - data[ i - 1 ]);
//			total_error_2 += Math.abs(data[ i ] - 2 * data[ i - 1 ] + data[ i - 2 ]);
//			total_error_3 += Math.abs(data[ i ] - 3 * data[ i - 1 ] + 3 * data[ i - 2 ] - data[ i - 3 ]);
//			total_error_4 += Math.abs(data[ i ] - 4 * data[ i - 1 ] + 6 * data[ i - 2 ] - 4 * data[ i - 3 ] + data [i - 4 ]);
//		}
//#endif

		int order;
		/* prefer lower order */
		if( total_error_0 <= Math.min(Math.min(Math.min(total_error_1, total_error_2), total_error_3), total_error_4) ) {
			order = 0;
		} else if( total_error_1 <= Math.min(Math.min(total_error_2, total_error_3), total_error_4) ) {
			order = 1;
		} else if( total_error_2 <= Math.min(total_error_3, total_error_4) ) {
			order = 2;
		} else if( total_error_3 <= total_error_4 ) {
			order = 3;
		} else {
			order = 4;
		}

		/* Estimate the expected number of bits per residual signal sample. */
		/* 'total_error*' is linearly related to the variance of the residual */
		/* signal, so we use it directly to compute E(|x|) */
		//FLAC__ASSERT(data_len > 0 || total_error_0 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_1 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_2 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_3 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_4 == 0);
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		residual_bits_per_sample[0] = (float)((total_error_0 > 0) ? Math.log(M_LN2 * total_error_0 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[1] = (float)((total_error_1 > 0) ? Math.log(M_LN2 * total_error_1 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[2] = (float)((total_error_2 > 0) ? Math.log(M_LN2 * total_error_2 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[3] = (float)((total_error_3 > 0) ? Math.log(M_LN2 * total_error_3 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[4] = (float)((total_error_4 > 0) ? Math.log(M_LN2 * total_error_4 / data_len) / M_LN2 : 0.0);
/*#else
		residual_bits_per_sample[0] = (total_error_0 > 0) ? local__compute_rbps_integerized(total_error_0, data_len) : 0;
		residual_bits_per_sample[1] = (total_error_1 > 0) ? local__compute_rbps_integerized(total_error_1, data_len) : 0;
		residual_bits_per_sample[2] = (total_error_2 > 0) ? local__compute_rbps_integerized(total_error_2, data_len) : 0;
		residual_bits_per_sample[3] = (total_error_3 > 0) ? local__compute_rbps_integerized(total_error_3, data_len) : 0;
		residual_bits_per_sample[4] = (total_error_4 > 0) ? local__compute_rbps_integerized(total_error_4, data_len) : 0;
#endif*/
		return order;
	}

//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	// java: you shouldn't use an offset to call the function
	// data + Format.FLAC__MAX_FIXED_ORDER -> data
	// data_len - Format.FLAC__MAX_FIXED_ORDER -> data_len
	private static int FLAC__fixed_compute_best_predictor_wide(final int data[], final int data_len, final float residual_bits_per_sample[]/*[FLAC__MAX_FIXED_ORDER+1]*/)
//#else
//		unsigned FLAC__fixed_compute_best_predictor_wide(const FLAC__int32 data[], unsigned data_len, FLAC__fixedpoint residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1])
//#endif
	{
// java: keep this old code because it better optimized
		int last_error_0 = data[Format.FLAC__MAX_FIXED_ORDER-1];
		int last_error_1 = data[Format.FLAC__MAX_FIXED_ORDER-1] - data[Format.FLAC__MAX_FIXED_ORDER-2];
		int last_error_2 = last_error_1 - (data[Format.FLAC__MAX_FIXED_ORDER-2] - data[Format.FLAC__MAX_FIXED_ORDER-3]);
		int last_error_3 = last_error_2 - (data[Format.FLAC__MAX_FIXED_ORDER-2] - (data[Format.FLAC__MAX_FIXED_ORDER-3] << 1) + data[Format.FLAC__MAX_FIXED_ORDER-4]);
		/* total_error_* are 64-bits to avoid overflow when encoding
		 * erratic signals when the bits-per-sample and blocksize are
		 * large.
		 */
		long total_error_0 = 0, total_error_1 = 0, total_error_2 = 0, total_error_3 = 0, total_error_4 = 0;

		for(int i = Format.FLAC__MAX_FIXED_ORDER; i < data_len; i++ ) {
			int error  = data[i]     ; total_error_0 += error < 0 ? -error : error;              int save = error;
			error -= last_error_0; total_error_1 += error < 0 ? -error : error; last_error_0 = save; save = error;
			error -= last_error_1; total_error_2 += error < 0 ? -error : error; last_error_1 = save; save = error;
			error -= last_error_2; total_error_3 += error < 0 ? -error : error; last_error_2 = save; save = error;
			error -= last_error_3; total_error_4 += error < 0 ? -error : error; last_error_3 = save;
		}
/*
		for(int i = Format.FLAC__MAX_FIXED_ORDER; i < data_len; i++) {
			total_error_0 += Math.abs(data[i]);
			total_error_1 += Math.abs(data[i] - data[i-1]);
			total_error_2 += Math.abs(data[i] - 2 * data[i-1] + data[i-2]);
			total_error_3 += Math.abs(data[i] - 3 * data[i-1] + 3 * data[i-2] - data[i-3]);
			total_error_4 += Math.abs(data[i] - 4 * data[i-1] + 6 * data[i-2] - 4 * data[i-3] + data[i-4]);
		}
*/
		int order;
		/* prefer lower order */
		if( total_error_0 <= Math.min(Math.min(Math.min(total_error_1, total_error_2), total_error_3), total_error_4) ) {
			order = 0;
		} else if( total_error_1 <= Math.min(Math.min(total_error_2, total_error_3), total_error_4) ) {
			order = 1;
		} else if( total_error_2 <= Math.min(total_error_3, total_error_4) ) {
			order = 2;
		} else if( total_error_3 <= total_error_4 ) {
			order = 3;
		} else {
			order = 4;
		}

		/* Estimate the expected number of bits per residual signal sample. */
		/* 'total_error*' is linearly related to the variance of the residual */
		/* signal, so we use it directly to compute E(|x|) */
		//FLAC__ASSERT(data_len > 0 || total_error_0 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_1 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_2 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_3 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_4 == 0);
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		residual_bits_per_sample[0] = (float)((total_error_0 > 0) ? Math.log(M_LN2 * total_error_0 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[1] = (float)((total_error_1 > 0) ? Math.log(M_LN2 * total_error_1 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[2] = (float)((total_error_2 > 0) ? Math.log(M_LN2 * total_error_2 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[3] = (float)((total_error_3 > 0) ? Math.log(M_LN2 * total_error_3 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[4] = (float)((total_error_4 > 0) ? Math.log(M_LN2 * total_error_4 / data_len) / M_LN2 : 0.0);
/*#else
		residual_bits_per_sample[0] = (total_error_0 > 0) ? local__compute_rbps_wide_integerized(total_error_0, data_len) : 0;
		residual_bits_per_sample[1] = (total_error_1 > 0) ? local__compute_rbps_wide_integerized(total_error_1, data_len) : 0;
		residual_bits_per_sample[2] = (total_error_2 > 0) ? local__compute_rbps_wide_integerized(total_error_2, data_len) : 0;
		residual_bits_per_sample[3] = (total_error_3 > 0) ? local__compute_rbps_wide_integerized(total_error_3, data_len) : 0;
		residual_bits_per_sample[4] = (total_error_4 > 0) ? local__compute_rbps_wide_integerized(total_error_4, data_len) : 0;
#endif*/

		return order;
	}

/*
#ifndef FLAC__INTEGER_ONLY_LIBRARY
#define CHECK_ORDER_IS_VALID(macro_order)		\
	if(order_##macro_order##_is_valid && total_error_##macro_order < smallest_error) { \
		order = macro_order;				\
		smallest_error = total_error_##macro_order ;	\
		residual_bits_per_sample[ macro_order ] = (float)((total_error_0 > 0) ? log(M_LN2 * (double)total_error_0 / (double)data_len) / M_LN2 : 0.0); \
	}							\
	else							\
		residual_bits_per_sample[ macro_order ] = 34.0f;
#else
#define CHECK_ORDER_IS_VALID(macro_order)		\
	if(order_##macro_order##_is_valid && total_error_##macro_order < smallest_error) { \
		order = macro_order;				\
		smallest_error = total_error_##macro_order ;	\
		residual_bits_per_sample[ macro_order ] = (total_error_##macro_order > 0) ? local__compute_rbps_wide_integerized(total_error_##macro_order, data_len) : 0; \
	}							\
	else							\
		residual_bits_per_sample[ macro_order ] = 34 * FLAC__FP_ONE;
#endif
*/
/*
	private static void CHECK_ORDER_IS_VALID(int macro_order) {
		if( order_N_is_valid && total_error_N < smallest_error ) {
			order = N;
			smallest_error = total_error_N;
			residual_bits_per_sample[ N ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		}
		else
			residual_bits_per_sample[ N ] = 34.0f;
	}
*/
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	private static int FLAC__fixed_compute_best_predictor_limit_residual(final int data[], final int data_len, final float residual_bits_per_sample[/*FLAC__MAX_FIXED_ORDER + 1*/])
//#else
//	uint32_t FLAC__fixed_compute_best_predictor_limit_residual(const FLAC__int32 data[], uint32_t data_len, FLAC__fixedpoint residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1])
//#endif
	{
		long total_error_0 = 0, total_error_1 = 0, total_error_2 = 0, total_error_3 = 0, total_error_4 = 0, smallest_error = Long.MAX_VALUE;
		long error_0, error_1, error_2, error_3, error_4;
		boolean order_0_is_valid = true, order_1_is_valid = true, order_2_is_valid = true, order_3_is_valid = true, order_4_is_valid = true;
		int order = 0;

		for( int i = 0; i < data_len; i++ ) {
			//error_0 = local_abs64((long)data[i]);
			//error_1 = (i > 0) ? local_abs64((long)data[i] - data[i-1]) : 0;
			//error_2 = (i > 1) ? local_abs64((long)data[i] - 2 * (long)data[i-1] + data[i-2]) : 0;
			//error_3 = (i > 2) ? local_abs64((long)data[i] - 3 * (long)data[i-1] + 3 * (long)data[i-2] - data[i-3]) : 0;
			//error_4 = (i > 3) ? local_abs64((long)data[i] - 4 * (long)data[i-1] + 6 * (long)data[i-2] - 4 * (long)data[i-3] + data[i-4]) : 0;
			// TODO java: try to optimize
			error_1 = 0; error_2 = 0; error_3 = 0; error_4 = 0;
			long error = (long)data[i];
			error_0 = error < 0 ? -error : error;
			if( i > 0 ) {
				error = (long)data[i] - data[i - 1];
				error_1 = error < 0 ? -error : error;
			}
			if( i > 1 ) {
				error  = (long)data[i] - ((long)data[i-1] << 1) + data[i-2];
				error_2 = error < 0 ? -error : error;
			}
			if( i > 2 ) {
				final long e = (long)data[i-2] - (long)data[i-1];
				error = (long)data[i] + (e << 1) + e - data[i-3];
				error_3 = error < 0 ? -error : error;
			}
			if( i > 3 ) {
				final long e = (long)data[i-1] + (long)data[i-3];
				final long e2 = (long)data[i-2];
				error = (long)data[i] - (e << 2) + (((e2 << 1) + e2) << 1) + data[i-4];
				error_4 = error < 0 ? -error : error;
			}

			total_error_0 += error_0;
			total_error_1 += error_1;
			total_error_2 += error_2;
			total_error_3 += error_3;
			total_error_4 += error_4;

			/* residual must not be INT32_MIN because abs(INT32_MIN) is undefined */
			if( error_0 > Integer.MAX_VALUE ) {
				order_0_is_valid = false;
			}
			if( error_1 > Integer.MAX_VALUE ) {
				order_1_is_valid = false;
			}
			if( error_2 > Integer.MAX_VALUE ) {
				order_2_is_valid = false;
			}
			if( error_3 > Integer.MAX_VALUE ) {
				order_3_is_valid = false;
			}
			if( error_4 > Integer.MAX_VALUE ) {
				order_4_is_valid = false;
			}
		}

		//CHECK_ORDER_IS_VALID(0);
		if( order_0_is_valid && total_error_0 < smallest_error ) {
			order = 0;
			smallest_error = total_error_0;
			residual_bits_per_sample[ 0 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 0 ] = 34.0f;
		}

		//CHECK_ORDER_IS_VALID(1);
		if( order_1_is_valid && total_error_1 < smallest_error ) {
			order = 1;
			smallest_error = total_error_1;
			residual_bits_per_sample[ 1 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 1 ] = 34.0f;
		}

		//CHECK_ORDER_IS_VALID(2);
		if( order_2_is_valid && total_error_2 < smallest_error ) {
			order = 2;
			smallest_error = total_error_2;
			residual_bits_per_sample[ 2 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 2 ] = 34.0f;
		}

		//CHECK_ORDER_IS_VALID(3);
		if( order_3_is_valid && total_error_3 < smallest_error ) {
			order = 3;
			smallest_error = total_error_3;
			residual_bits_per_sample[ 3 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 3 ] = 34.0f;
		}

		//CHECK_ORDER_IS_VALID(4);
		if( order_4_is_valid && total_error_4 < smallest_error ) {
			order = 4;
			smallest_error = total_error_4;
			residual_bits_per_sample[ 4 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 4 ] = 34.0f;
		}

		return order;
	}

//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	private static int FLAC__fixed_compute_best_predictor_limit_residual_33bit(final long data[], final int data_len, final float residual_bits_per_sample[/*FLAC__MAX_FIXED_ORDER+1*/])
//#else
//	uint32_t FLAC__fixed_compute_best_predictor_limit_residual_33bit(const FLAC__int64 data[], uint32_t data_len, FLAC__fixedpoint residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1])
//#endif
	{
		long total_error_0 = 0, total_error_1 = 0, total_error_2 = 0, total_error_3 = 0, total_error_4 = 0, smallest_error = Long.MAX_VALUE;
		long error_0, error_1, error_2, error_3, error_4;
		boolean order_0_is_valid = true, order_1_is_valid = true, order_2_is_valid = true, order_3_is_valid = true, order_4_is_valid = true;
		int order = 0;

		for( int i = 0; i < data_len; i++ ) {
			//error_0 = local_abs64(data[i]);
			//error_1 = (i > 0) ? local_abs64(data[i] - data[i-1]) : 0 ;
			//error_2 = (i > 1) ? local_abs64(data[i] - 2 * data[i-1] + data[i-2]) : 0;
			//error_3 = (i > 2) ? local_abs64(data[i] - 3 * data[i-1] + 3 * data[i-2] - data[i-3]) : 0;
			//error_4 = (i > 3) ? local_abs64(data[i] - 4 * data[i-1] + 6 * data[i-2] - 4 * data[i-3] + data[i-4]) : 0;
			// TODO java: try to optimize
			long error = (long)data[i];
			error_0 = error < 0 ? -error : error;
			error_1 = 0; error_2 = 0; error_3 = 0; error_4 = 0;
			if( i > 0 ) {
				error = data[i] - data[i-1];
				error_1 = error < 0 ? -error : error;
			}
			if( i > 1 ) {
				error = data[i] - ((long) data[i-1] << 1) + data[i-2];
				error_2 = error < 0 ? -error : error;
			}
			if( i > 2 ) {
				final long e = data[i-2] - data[i-1];
				error = data[i] + (e << 1) + e - data[i-3];
				error_3 = error < 0 ? -error : error;
			}
			if( i > 3 ) {
				final long e = data[i-1] + data[i-3];
				final long e2 = data[i-2];
				error = data[i] - (e << 2) + (((e2 << 1) + e2) << 1) + data[i-4];
				error_4 = error < 0 ? -error : error;
			}

			total_error_0 += error_0;
			total_error_1 += error_1;
			total_error_2 += error_2;
			total_error_3 += error_3;
			total_error_4 += error_4;

			/* residual must not be INT32_MIN because abs(INT32_MIN) is undefined */
			if( error_0 > Integer.MAX_VALUE ) {
				order_0_is_valid = false;
			}
			if( error_1 > Integer.MAX_VALUE ) {
				order_1_is_valid = false;
			}
			if( error_2 > Integer.MAX_VALUE ) {
				order_2_is_valid = false;
			}
			if( error_3 > Integer.MAX_VALUE ) {
				order_3_is_valid = false;
			}
			if( error_4 > Integer.MAX_VALUE ) {
				order_4_is_valid = false;
			}
		}

		//CHECK_ORDER_IS_VALID(0);
		if( order_0_is_valid && total_error_0 < smallest_error ) {
			order = 0;
			smallest_error = total_error_0;
			residual_bits_per_sample[ 0 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 0 ] = 34.0f;
		}

		//CHECK_ORDER_IS_VALID(1);
		if( order_1_is_valid && total_error_1 < smallest_error ) {
			order = 1;
			smallest_error = total_error_1;
			residual_bits_per_sample[ 1 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 1 ] = 34.0f;
		}

		//CHECK_ORDER_IS_VALID(2);
		if( order_2_is_valid && total_error_2 < smallest_error ) {
			order = 2;
			smallest_error = total_error_2;
			residual_bits_per_sample[ 2 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 2 ] = 34.0f;
		}

		//CHECK_ORDER_IS_VALID(3);
		if( order_3_is_valid && total_error_3 < smallest_error ) {
			order = 3;
			smallest_error = total_error_3;
			residual_bits_per_sample[ 3 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 3 ] = 34.0f;
		}

		//CHECK_ORDER_IS_VALID(4);
		if( order_4_is_valid && total_error_4 < smallest_error ) {
			order = 4;
			smallest_error = total_error_4;
			residual_bits_per_sample[ 4 ] = (float)((total_error_0 > 0) ? Math.log( M_LN2 * (double)total_error_0 / (double)data_len ) / M_LN2 : 0.0 );
		} else {
			residual_bits_per_sample[ 4 ] = 34.0f;
		}

		return order;
	}
	// end fixed.c

	// java: extracted in place to avoid creating arrays for input and output data
	/* private static void set_next_subdivide_tukey(final int parts, final int[] apodizations, final int[] current_depth, final int[] current_part) {
		// current_part is interleaved: even are partial, odd are punchout
		if( current_depth[ 0 ] == 2) {
			// For depth 2, we only do partial, no punchout as that is almost redundant
			if( current_part[ 0 ] == 0 ) {
				current_part[ 0 ] = 2;
			} else { // *current_path == 2
				current_part[ 0 ] = 0;
				current_depth[ 0 ]++;
			}
		} else if( current_part[ 0 ] < ((current_depth[ 0 ] << 1) - 1) ) {
			current_part[ 0 ]++;
		} else { // (*current_part) >= (2*(*current_depth)-1)
			current_part[ 0 ] = 0;
			current_depth[ 0 ]++;
		}

		// Now check if we are done with this SUBDIVIDE_TUKEY apodization
		if( current_depth[ 0 ] > parts ) {
			apodizations[ 0 ]++;
			current_depth[ 0 ] = 1;
			current_part[ 0 ] = 0;
		}
	} */

	private final boolean process_subframe_(
			final int min_partition_order,
			final int max_partition_order,
			final FrameHeader frame_header,
			final int subframe_b_p_s,
			final Object signal,// java renamed integer_signal
			final Subframe subframe[],// [2],
			final PartitionedRiceContents partitioned_rice_contents[],// [2],
			final int residual[][],// [2],
			final int[] bestsubframe,
			final int[] best_bits,
			final int channel// java: added as offset to best_subframe and best_bits
	)
	{
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		final float fixed_residual_bits_per_sample[] = new float[Format.FLAC__MAX_FIXED_ORDER + 1];
//#else
//		FLAC__fixedpoint fixed_residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1];
//#endif
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		final double[] autoc = new double[Format.FLAC__MAX_LPC_ORDER + 1]; /* WATCHOUT: the size is important even though encoder->protected_->max_lpc_order might be less; some asm and x86 intrinsic routines need all the space */
		final double[] autoc_root = new double[Format.FLAC__MAX_LPC_ORDER + 1]; /* This is for subdivide_tukey apodization */
		final double lpc_error[] = new double[Format.FLAC__MAX_LPC_ORDER];
//#endif
		/* only use RICE2 partitions if stream bps > 16 */
		final int rice_parameter_limit = get_bits_per_sample() > 16 ? Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_ESCAPE_PARAMETER : Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ESCAPE_PARAMETER;

		//FLAC__ASSERT(frame_header.blocksize > 0);

		/* verbatim subframe is the baseline against which we measure other compressed subframes */
		int _best_subframe = 0;// 1 or 0
		int _best_bits;
		if( this.disable_verbatim_subframes && frame_header.blocksize >= Format.FLAC__MAX_FIXED_ORDER ) {
			_best_bits = Integer.MAX_VALUE;
		} else {
			_best_bits = evaluate_verbatim_subframe_( signal, frame_header.blocksize, subframe_b_p_s, subframe[_best_subframe] );
		}
		best_bits[ 0 ] = _best_bits;

		if( frame_header.blocksize >= Format.FLAC__MAX_FIXED_ORDER ) {
			boolean signal_is_constant = false;
			int guess_fixed_order;
			/* The next formula determines when to use a 64-bit accumulator
			 * for the error of a fixed predictor, and when a 32-bit one. As
			 * the error of a 4th order predictor for a given sample is the
			 * sum of 17 sample values (1+4+6+4+1) and there are blocksize -
			 * order error values to be summed, the maximum total error is
			 * maximum_sample_value * (blocksize - order) * 17. As ilog2(x)
			 * calculates floor(2log(x)), the result must be 31 or lower
			 */
			if( subframe_b_p_s < 28 ) {
				if( subframe_b_p_s + Format.bitmath_ilog2((frame_header.blocksize - Format.FLAC__MAX_FIXED_ORDER) * 17) < 32 ) {
					guess_fixed_order = FLAC__fixed_compute_best_predictor( ((int[])signal)/* + Format.FLAC__MAX_FIXED_ORDER*/, frame_header.blocksize/* - Format.FLAC__MAX_FIXED_ORDER*/, fixed_residual_bits_per_sample );
				} else {
					guess_fixed_order = FLAC__fixed_compute_best_predictor_wide( ((int[])signal)/* + Format.FLAC__MAX_FIXED_ORDER*/, frame_header.blocksize/* - Format.FLAC__MAX_FIXED_ORDER*/, fixed_residual_bits_per_sample );
				}
			}
			else
				if( subframe_b_p_s <= 32 ) {
					guess_fixed_order = FLAC__fixed_compute_best_predictor_limit_residual( ((int[])signal), frame_header.blocksize, fixed_residual_bits_per_sample );
				} else {
					guess_fixed_order = FLAC__fixed_compute_best_predictor_limit_residual_33bit( ((long[])signal), frame_header.blocksize, fixed_residual_bits_per_sample );
				}

			/* check for constant subframe */
			if(
				! this.disable_constant_subframes &&
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
				fixed_residual_bits_per_sample[1] == 0.0f
//#else
//				fixed_residual_bits_per_sample[1] == FLAC__FP_ZERO
//#endif
			) {
				/* the above means it's possible all samples are the same value; now double-check it: */
				signal_is_constant = true;
				if( subframe_b_p_s <= 32 ) {
					final int[] integer_signal_ = (int[])signal;
					for( int i = 1, size = frame_header.blocksize; i < size; i++ ) {
						if( integer_signal_[0] != integer_signal_[i] ) {
							signal_is_constant = false;
							break;
						}
					}
				} else {
					final long[] integer_signal_ = (long[])signal;
					for( int i = 1, size = frame_header.blocksize; i < size; i++ ) {
						if( integer_signal_[0] != integer_signal_[i] ) {
							signal_is_constant = false;
							break;
						}
					}
				}
			}
			if( signal_is_constant ) {
				final int _candidate_bits;
				if( subframe_b_p_s <= 32 ) {
					_candidate_bits = evaluate_constant_subframe_( ((int[])signal)[0], frame_header.blocksize, subframe_b_p_s, subframe[ 1 ^ _best_subframe ]);
				} else {
					_candidate_bits = evaluate_constant_subframe_( ((long[])signal)[0], frame_header.blocksize, subframe_b_p_s, subframe[ 1 ^ _best_subframe ]);
				}

				if( _candidate_bits < _best_bits ) {
					_best_subframe ^= 1;// ! _best_subframe;
					_best_bits = _candidate_bits;
				}
			}
			else {
				if( ! this.disable_fixed_subframes || (this.max_lpc_order == 0 && _best_bits == Integer.MAX_VALUE) ) {
					/* encode fixed */
					int min_fixed_order, max_fixed_order;
					if( this.do_exhaustive_model_search ) {
						min_fixed_order = 0;
						max_fixed_order = Format.FLAC__MAX_FIXED_ORDER;
					}
					else {
						min_fixed_order = max_fixed_order = guess_fixed_order;
					}
					if( max_fixed_order >= frame_header.blocksize ) {
						max_fixed_order = frame_header.blocksize - 1;
					}
					for( int fixed_order = min_fixed_order; fixed_order <= max_fixed_order; fixed_order++ ) {
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
						if( fixed_residual_bits_per_sample[fixed_order] >= (float)subframe_b_p_s ) {
							continue; /* don't even try */
						}
//#else
//						if( FLAC__fixedpoint_trunc(fixed_residual_bits_per_sample[fixed_order]) >= (int)subframe_bps )
//							continue; /* don't even try */
//#endif
						final int _candidate_bits =
							evaluate_fixed_subframe_(
								signal,
								residual[ 1 ^ _best_subframe ],
								this.abs_residual_partition_sums,
								this.raw_bits_per_partition,
								frame_header.blocksize,
								subframe_b_p_s,
								fixed_order,
								rice_parameter_limit,
								min_partition_order,
								max_partition_order,
								this.do_escape_coding,
								this.rice_parameter_search_dist,
								subframe[ 1 ^ _best_subframe ],
								partitioned_rice_contents[ 1 ^ _best_subframe ]
							);
						if( _candidate_bits < _best_bits) {
							_best_subframe ^= 1;// ! _best_subframe;
							_best_bits = _candidate_bits;
						}
					}
				}

//#ifndef FLAC__INTEGER_ONLY_LIBRARY
				/* encode lpc */
				if( this.max_lpc_order > 0 ) {
					int lpc_max_order;
					if( this.max_lpc_order >= frame_header.blocksize ) {
						lpc_max_order = frame_header.blocksize-1;
					} else {
						lpc_max_order = this.max_lpc_order;
					}
					if( lpc_max_order > 0 ) {
						int b = 1;
						int c = 0;
						for( int a = 0; a < this.num_apodizations; a++ ) {
							int max_lpc_order_this_apodization = max_lpc_order;
							if( b == 1 ) {
								/* window full subblock */
								if( subframe_b_p_s <= 32 ) {
									LPC.window_data( (int[])signal, this.window[a], this.windowed_signal, frame_header.blocksize );
								} else {
									LPC.window_data_wide( (long[])signal,  this.window[a], this.windowed_signal, frame_header.blocksize );
								}
								// encoder.private_.local_lpc_compute_autocorrelation
								LPC.compute_autocorrelation( this.windowed_signal, frame_header.blocksize, max_lpc_order_this_apodization + 1, autoc );
								if(  this.apodizations[a].type == ApodizationSpecification.FLAC__APODIZATION_SUBDIVIDE_TUKEY ) {
									for( int i = 0; i < max_lpc_order_this_apodization; i++ ) {
										autoc_root[i] = autoc[i];
									}
									b++;
								} else {
									a++;
								}
							}
							else {
								/* window part of subblock */
								if( max_lpc_order_this_apodization >= frame_header.blocksize / b ) {
									max_lpc_order_this_apodization = frame_header.blocksize / b - 1;
									if( frame_header.blocksize / b > 0 ) {
										max_lpc_order_this_apodization = frame_header.blocksize / b - 1;
									} else {
										// set_next_subdivide_tukey( this.apodizations[a].parameters.subdivide_tukey.parts, &a, &b, &c);// java: extracted
										//private static void set_next_subdivide_tukey(final int parts, final int[] apodizations, final int[] current_depth, final int[] current_part) {
											// current_part is interleaved: even are partial, odd are punchout
											if( b == 2) {
												// For depth 2, we only do partial, no punchout as that is almost redundant
												if( c == 0 ) {
													c = 2;
												} else { /* *current_path == 2 */
													c = 0;
													b++;
												}
											} else if( c < ((b << 1) - 1) ) {
												c++;
											} else { /* (*current_part) >= (2*(*current_depth)-1) */
												c = 0;
												b++;
											}

											/* Now check if we are done with this SUBDIVIDE_TUKEY apodization */
											if( b > this.apodizations[ a ]./*parameters.subdivide_tukey.*/parts ) {
												a++;
												b = 1;
												c = 0;
											}
										//}
										continue;
									}
								}
								if( (c % 2) == 0 ) {
									/* on even c, evaluate the (c/2)th partial window of size blocksize/b  */
									if( subframe_b_p_s <= 32 ) {
										LPC.window_data_partial( (int[])signal,  this.window[a],  this.windowed_signal, frame_header.blocksize, frame_header.blocksize / b / 2, (c / 2 * frame_header.blocksize) / b );
									} else {
										LPC.window_data_partial_wide( (long[])signal,  this.window[a],  this.windowed_signal, frame_header.blocksize, frame_header.blocksize / b / 2, (c / 2 * frame_header.blocksize) / b );
									}
									LPC.compute_autocorrelation( this.windowed_signal, frame_header.blocksize / b, max_lpc_order_this_apodization + 1, autoc );
								}else{
									/* on uneven c, evaluate the root window (over the whole block) minus the previous partial window
									 * similar to tukey_punchout apodization but more efficient	*/
									for( int i = 0; i < max_lpc_order_this_apodization; i++ ) {
										autoc[i] = autoc_root[i] - autoc[i];
									}
								}
								/* Next function sets a, b and c appropriate for next iteration */
								// set_next_subdivide_tukey( this.apodizations[a].parameters.subdivide_tukey.parts, &a, &b, &c );// java: extracted
								//private static void set_next_subdivide_tukey(final int parts, final int[] apodizations, final int[] current_depth, final int[] current_part) {
									// current_part is interleaved: even are partial, odd are punchout
									if( b == 2) {
										// For depth 2, we only do partial, no punchout as that is almost redundant
										if( c == 0 ) {
											c = 2;
										} else { /* *current_path == 2 */
											c = 0;
											b++;
										}
									} else if( c < ((b << 1) - 1) ) {
										c++;
									} else { /* (*current_part) >= (2*(*current_depth)-1) */
										c = 0;
										b++;
									}

									/* Now check if we are done with this SUBDIVIDE_TUKEY apodization */
									if( b > this.apodizations[ a ]./*parameters.subdivide_tukey.*/parts ) {
										a++;
										b = 1;
										c = 0;
									}
								//}
							}

							/* if autoc[0] == 0.0, the signal is constant and we usually won't get here, but it can happen */
							if( autoc[0] != 0.0f ) {
								max_lpc_order_this_apodization = LPC.compute_lp_coefficients( autoc, max_lpc_order_this_apodization, this.lp_coeff, lpc_error );

								int lpc_min_order;
								if( this.do_exhaustive_model_search ) {
									lpc_min_order = 1;
								}
								else {
									final int guess_lpc_order =
										LPC.compute_best_order(
											lpc_error,
											max_lpc_order_this_apodization,
											frame_header.blocksize,
											subframe_b_p_s + (
												this.do_qlp_coeff_prec_search ?
													Format.FLAC__MIN_QLP_COEFF_PRECISION : /* have to guess; use the min possible size to avoid accidentally favoring lower orders */
													this.qlp_coeff_precision
											)
										);
									lpc_min_order = max_lpc_order_this_apodization = guess_lpc_order;
								}
								if( max_lpc_order_this_apodization >= frame_header.blocksize ) {
									max_lpc_order_this_apodization = frame_header.blocksize - 1;
								}
								for( int lpc_order = lpc_min_order; lpc_order <= max_lpc_order_this_apodization; lpc_order++ ) {
									final double lpc_residual_bits_per_sample = LPC.compute_expected_bits_per_residual_sample( lpc_error[lpc_order-1], frame_header.blocksize - lpc_order );
									if( lpc_residual_bits_per_sample >= (double)subframe_b_p_s ) {
										continue; /* don't even try */
									}
									int min_qlp_coeff_precision, max_qlp_coeff_precision;
									if( this.do_qlp_coeff_prec_search ) {
										min_qlp_coeff_precision = Format.FLAC__MIN_QLP_COEFF_PRECISION;
										/* try to keep qlp coeff precision such that only 32-bit math is required for decode of <=16bps(+1bps for side channel) streams */
										if( subframe_b_p_s <= 17 ) {
											max_qlp_coeff_precision = 32 - subframe_b_p_s - Format.bitmath_ilog2( lpc_order );
											if( max_qlp_coeff_precision > Format.FLAC__MAX_QLP_COEFF_PRECISION ) {
												max_qlp_coeff_precision = Format.FLAC__MAX_QLP_COEFF_PRECISION;
											}
											if( max_qlp_coeff_precision < min_qlp_coeff_precision ) {
												max_qlp_coeff_precision = min_qlp_coeff_precision;
											}
										} else {
											max_qlp_coeff_precision = Format.FLAC__MAX_QLP_COEFF_PRECISION;
										}
									} else {
										min_qlp_coeff_precision = max_qlp_coeff_precision = this.qlp_coeff_precision;
									}
									for( int coeff_precision = min_qlp_coeff_precision; coeff_precision <= max_qlp_coeff_precision; coeff_precision++ ) {
										final int _candidate_bits =
											evaluate_lpc_subframe_(
												signal,
												residual[ 1 ^ _best_subframe ],
												this.abs_residual_partition_sums,
												this.raw_bits_per_partition,
												this.lp_coeff[lpc_order - 1],
												frame_header.blocksize,
												subframe_b_p_s,
												lpc_order,
												coeff_precision,
												rice_parameter_limit,
												min_partition_order,
												max_partition_order,
												this.do_escape_coding,
												this.rice_parameter_search_dist,
												subframe[ 1 ^ _best_subframe ],
												partitioned_rice_contents[ 1 ^ _best_subframe ]
											);
										if( _candidate_bits > 0 ) { /* if == 0, there was a problem quantizing the lpcoeffs */
											if( _candidate_bits < _best_bits ) {
												_best_subframe ^= 1;// ! _best_subframe;
												_best_bits = _candidate_bits;
											}
										}
									}
								}
							}
						}
					}
				}
//#endif /* !defined FLAC__INTEGER_ONLY_LIBRARY */
			}
		}

		/* under rare circumstances this can happen when all but lpc subframe types are disabled: */
		if( _best_bits == Integer.MAX_VALUE ) {
			//FLAC__ASSERT(_best_subframe == 0);
			_best_bits = evaluate_verbatim_subframe_( signal, frame_header.blocksize, subframe_b_p_s, subframe[_best_subframe] );
		}

		bestsubframe[channel] = _best_subframe;
		best_bits[channel] = _best_bits;

		return true;
	}

	private final boolean add_subframe_(
		final int block_size,
		final int subframe_b_p_s,
		final Subframe subframe,
		final BitWriter frame_writer
	)
	{
		switch( subframe.type ) {
			case Format.FLAC__SUBFRAME_TYPE_CONSTANT:
				if( ! ((Subframe_Constant)subframe.data).add_constant( subframe_b_p_s, subframe.wasted_bits, frame_writer ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return false;
				}
				break;
			case Format.FLAC__SUBFRAME_TYPE_FIXED:
				final Subframe_Fixed fixed = (Subframe_Fixed) subframe.data;
				if( ! fixed.add_fixed( block_size - fixed.order, subframe_b_p_s, subframe.wasted_bits, frame_writer ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return false;
				}
				break;
			case Format.FLAC__SUBFRAME_TYPE_LPC:
				final Subframe_LPC lpc = (Subframe_LPC) subframe.data;
				if( ! lpc.add_lpc( block_size - lpc.order, subframe_b_p_s, subframe.wasted_bits, frame_writer ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return false;
				}
				break;
			case Format.FLAC__SUBFRAME_TYPE_VERBATIM:
				if( ! ((Subframe_Verbatim) subframe.data).add_verbatim( block_size, subframe_b_p_s, subframe.wasted_bits, frame_writer ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return false;
				}
				break;
			default:
				//FLAC__ASSERT(0);
		}

		return true;
	}

/*#define SPOTCHECK_ESTIMATE 0
//#if SPOTCHECK_ESTIMATE
	@SuppressWarnings("boxing")
	private final void spotcheck_subframe_estimate_(
		StreamEncoder encoder,
		int blocksize,
		int subframe_bps,
		final Subframe subframe,
		int estimate
	)
	{
		//boolean ret;
		BitWriter frame = new BitWriter();
		//if( frame == null ) {
		//	System.err.print("EST: can't allocate frame\n");
		//	return;
		//}
		if( ! frame.init() ) {
			System.err.print("EST: can't init frame\n");
			return;
		}
		ret = add_subframe_( blocksize, subframe_bps, subframe, frame );
		//FLAC__ASSERT(ret);
		{
			final int actual = frame.get_input_bits_unconsumed();
			if( estimate != actual )
				System.err.printf("EST: bad, frame#%d sub#%%d type=%8s est=%d, actual=%d, delta=%d\n", this.current_frame_number, Subframe.FLAC__SubframeTypeString[subframe.type], estimate, actual, (int)actual-(int)estimate);
		}
		frame = null;
	}
//#endif*/

	private static int evaluate_constant_subframe_(
		final long signal,
		final int blocksize,
		final int subframe_bps,
		final Subframe subframe
	)
	{
		subframe.type = Format.FLAC__SUBFRAME_TYPE_CONSTANT;
		final Subframe_Constant constant = new Subframe_Constant();
		constant.value = signal;
		subframe.data = constant;

		final int estimate = Format.FLAC__SUBFRAME_ZERO_PAD_LEN + Format.FLAC__SUBFRAME_TYPE_LEN + Format.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN + subframe.wasted_bits + subframe_bps;

//#if SPOTCHECK_ESTIMATE
//		spotcheck_subframe_estimate_(encoder, blocksize, subframe_bps, subframe, estimate);
//#else
//		(void)encoder, (void)blocksize;
//#endif

		return estimate;
	}

	// fixed.c
	private static void FLAC__fixed_compute_residual(final int data[], final int data_len, int order, final int residual[])
	{//java: order used as offset to data index
		switch( order ) {
			case 0:
				//FLAC__ASSERT(sizeof(residual[0]) == sizeof(data[0]));
				System.arraycopy( data, order, residual, 0, data_len );
				break;
			case 1:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = data[order] - data[order - 1];
				}
				break;
			case 2:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = data[order] - (data[order - 1] << 1) + data[order - 2];
				}
				break;
			case 3:
				for( int i = 0; i < data_len; i++, order++ ) {
					final int e = data[order - 2] - data[order - 1];
					residual[i] = data[order] + ((e << 1) + e) - data[order - 3];
				}
				break;
			case 4:
				for( int i = 0; i < data_len; i++, order++ ) {
					final int e = data[order - 2];
					residual[i] = data[order] + ((((e << 1) + e) - ((data[order - 1] + data[order - 3]) << 1)) << 1) + data[order - 4];
				}
				break;
			default:
				//FLAC__ASSERT(0);
		}
	}

	private static void FLAC__fixed_compute_residual_wide(final int data[], final int data_len, int order, final int residual[])
	{
		switch( order ) {
			case 0:
				//FLAC__ASSERT(sizeof(residual[0]) == sizeof(data[0]));
				System.arraycopy( data, order, residual, 0, data_len );
				break;
			case 1:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = (int)((long)data[order] - data[order-1]);
				}
				break;
			case 2:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = (int)((long)data[order] - (((long)data[order-1]) << 1) + data[order-2]);
				}
				break;
			case 3:
				for( int i = 0; i < data_len; i++, order++ ) {
					final long e = (long)data[order-2] - (long)data[order-1];
					residual[i] = (int)((long)data[order] + ((e << 1) + e) - data[order-3]);
				}
				break;
			case 4:
				for( int i = 0; i < data_len; i++, order++ ) {
					final long e = (long)data[order-2];
					residual[i] = (int) ((long)data[order] + ((((e << 1) + e) - (((long)data[order-1] + (long)data[order-3]) << 1)) << 1) + data[order-4]);
				}
				break;
			//default:
			//	FLAC__ASSERT(0);
		}
	}

	private static void FLAC__fixed_compute_residual_wide_33bit(final long data[], final int data_len, int order, final int residual[])
	{
		switch( order ) {
			case 0:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = (int) data[order];
				}
				break;
			case 1:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = (int) (data[order] - data[order-1]);
				}
				break;
			case 2:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = (int) (data[order] - (data[order-1] << 1) + data[order-2]);
				}
				break;
			case 3:
				for( int i = 0; i < data_len; i++, order++ ) {
					final long e = data[order-2] - data[order-1];
					residual[i] = (int) (data[order] + ((e << 1) + e) - data[order-3]);
				}
				break;
			case 4:
				for( int i = 0; i < data_len; i++, order++ ) {
					final long e = (long)data[order-2];
					residual[i] = (int) (data[order] + ((((e << 1) + e) - ((data[order-1] + data[order-3]) << 1)) << 1) + data[order-4]);
				}
				break;
			//default:
			//	FLAC__ASSERT(0);
		}
	}
	// end fixed.c

	private final int evaluate_fixed_subframe_(
		final Object signal,
		final int residual[],
		final long abs__residual_partition_sums[],
		final int raw_bits__per_partition[],
		final int block_size,
		final int subframe_b_p_s,
		final int order,
		final int rice_parameter_limit,
		final int min_partition_order,
		final int max_partition_order,
		final boolean is_do_escape_coding,
		final int rice_parameter_search_distance,
		final Subframe subframe,
		final PartitionedRiceContents partitioned_rice_contents
	)
	{
		final int residual_samples = block_size - order;

		if( (subframe_b_p_s + order) <= 32 ) {
			FLAC__fixed_compute_residual(((int[])signal)/* + order*/, residual_samples, order, residual );// java: changes inside the function
		} else if(subframe_b_p_s <= 32) {
			FLAC__fixed_compute_residual_wide(((int[])signal)/* + order*/, residual_samples, order, residual );// java: changes inside the function
		} else {
			FLAC__fixed_compute_residual_wide_33bit(((long[])signal)/* + order*/, residual_samples, order, residual );// java: changes inside the function
		}

		subframe.type = Format.FLAC__SUBFRAME_TYPE_FIXED;
		final Subframe_Fixed fixed = new Subframe_Fixed();
		subframe.data = fixed;

		fixed.entropy_coding_method.type = Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE;
		fixed.entropy_coding_method./*data.*/partitioned_rice.contents = partitioned_rice_contents;
		fixed.residual = residual;

		final int residual_bits =
			find_best_partition_order_(
				//encoder,// .private_,// java: uses direct access
				residual,
				abs__residual_partition_sums,
				raw_bits__per_partition,
				residual_samples,
				order,
				rice_parameter_limit,
				min_partition_order,
				max_partition_order,
				subframe_b_p_s,
				is_do_escape_coding,
				rice_parameter_search_distance,
				fixed.entropy_coding_method
			);

		fixed.order = order;
		if( subframe_b_p_s <= 32 ) {
			for( int i = 0; i < order; i++ ) {
				fixed.warmup[i] = ((int[])signal)[i];
			}
		} else {
			for( int i = 0; i < order; i++ ) {
				fixed.warmup[i] = ((long[])signal)[i];
			}
		}

		int estimate = Format.FLAC__SUBFRAME_ZERO_PAD_LEN + Format.FLAC__SUBFRAME_TYPE_LEN + Format.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN + subframe.wasted_bits + (order * subframe_b_p_s);
		if(residual_bits < Integer.MAX_VALUE - estimate) { // To make sure estimate doesn't overflow
			estimate += residual_bits;
		} else { // To make sure estimate doesn't overflow
			estimate = Integer.MAX_VALUE;
		}


//#if SPOTCHECK_ESTIMATE
//		spotcheck_subframe_estimate_(encoder, blocksize, subframe_bps, subframe, estimate);
//#endif

		return estimate;
	}

//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	private final int evaluate_lpc_subframe_(
		//StreamEncoder encoder,
		final Object signal,
		final int residual[],
		final long abs__residual_partition_sums[],
		final int raw__bits_per_partition[],
		final float lp_coeffs[],
		final int block_size,
		final int subframe_b_p_s,
		final int order,
		int qlp_coeffs_precision,
		final int rice_parameter_limit,
		final int min_partition_order,
		final int max_partition_order,
		final boolean is_do_escape_coding,
		final int rice_parameter_search_distance,
		final Subframe subframe,
		final PartitionedRiceContents partitioned_rice_contents
	)
	{
		final int qlp_coeff[] = new int[Format.FLAC__MAX_LPC_ORDER]; /* WATCHOUT: the size is important; some x86 intrinsic routines need more than lpc order elements */
		final int residual_samples = block_size - order;

		/* try to keep qlp coeff precision such that only 32-bit math is required for decode of <=16bps(+1bps for side channel) streams */
		if( subframe_b_p_s <= 17 ) {
			//FLAC__ASSERT(order > 0);
			//FLAC__ASSERT(order <= FLAC__MAX_LPC_ORDER);
			final int i = 32 - subframe_b_p_s - Format.bitmath_ilog2( order );
			qlp_coeffs_precision = qlp_coeffs_precision <= i ? qlp_coeffs_precision : i;
		}

		final int quantization = /*ret = */LPC.quantize_coefficients( lp_coeffs, order, qlp_coeffs_precision, qlp_coeff/*, &quantization*/ );
		if( quantization < 0 ) {
			return 0; /* this is a hack to indicate to the caller that we can't do lp at this order on this subframe */
		}

		if( LPC.max_residual_bps( subframe_b_p_s, qlp_coeff, order, quantization ) > 32 ) {
			if( subframe_b_p_s <= 32 ) {
				if( ! LPC.compute_residual_from_qlp_coefficients_limit_residual(((int[])signal)/* + order*/, residual_samples, qlp_coeff, order, quantization, residual ) ) {
					return 0;
				}
			}
			else
				if( ! LPC.compute_residual_from_qlp_coefficients_limit_residual_33bit(((long[])signal)/* + order*/, residual_samples, qlp_coeff, order, quantization, residual ) ) {
					return 0;
				}
		}
		else
			if( LPC.max_prediction_before_shift_bps( subframe_b_p_s, qlp_coeff, order ) <= 32 ) {
				if( subframe_b_p_s <= 16 && qlp_coeffs_precision <= 16 ) {
					LPC.compute_residual_from_qlp_coefficients( ((int[])signal)/* + order*/, residual_samples, qlp_coeff, order, quantization, residual );
				} else {
					LPC.compute_residual_from_qlp_coefficients( ((int[])signal)/* + order*/, residual_samples, qlp_coeff, order, quantization, residual );
				}
			} else {
				LPC.compute_residual_from_qlp_coefficients_wide( ((int[])signal)/* + order*/, residual_samples, qlp_coeff, order, quantization, residual );
			}

		subframe.type = Format.FLAC__SUBFRAME_TYPE_LPC;

		final Subframe_LPC lpc = new Subframe_LPC();
		subframe.data = lpc;
		lpc.entropy_coding_method.type = Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE;
		lpc.entropy_coding_method./*data.*/partitioned_rice.contents = partitioned_rice_contents;
		lpc.residual = residual;

		final int residual_bits =
			find_best_partition_order_(
				// encoder,// .private_,// java: uses direct access
				residual,
				abs__residual_partition_sums,
				raw__bits_per_partition,
				residual_samples,
				order,
				rice_parameter_limit,
				min_partition_order,
				max_partition_order,
				subframe_b_p_s,
				is_do_escape_coding,
				rice_parameter_search_distance,
				lpc.entropy_coding_method
			);

		lpc.order = order;
		lpc.qlp_coeff_precision = qlp_coeffs_precision;
		lpc.quantization_level = quantization;
		System.arraycopy( qlp_coeff, 0, lpc.qlp_coeff, 0, Format.FLAC__MAX_LPC_ORDER );

		if( subframe_b_p_s <= 32 ) {
			for( int i = 0; i < order; i++ ) {
				lpc.warmup[i] = ((int[])signal)[i];
			}
		} else {
			for( int i = 0; i < order; i++ ) {
				lpc.warmup[i] = ((long[])signal)[i];
			}
		}

		int estimate = Format.FLAC__SUBFRAME_ZERO_PAD_LEN + Format.FLAC__SUBFRAME_TYPE_LEN
				+ Format.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN + subframe.wasted_bits
				+ Format.FLAC__SUBFRAME_LPC_QLP_COEFF_PRECISION_LEN
				+ Format.FLAC__SUBFRAME_LPC_QLP_SHIFT_LEN + (order * (qlp_coeffs_precision + subframe_b_p_s));
		if( residual_bits < Integer.MAX_VALUE - estimate ) {// To make sure estimate doesn't overflow
			estimate += residual_bits;
		} else {
			estimate = Integer.MAX_VALUE;
		}
//#if SPOTCHECK_ESTIMATE
//		spotcheck_subframe_estimate_(encoder, blocksize, subframe_bps, subframe, estimate);
//#endif

		return estimate;
	}
//#endif

	private static int evaluate_verbatim_subframe_(
		final Object signal,
		final int blocksize,
		final int subframe_bps,
		final Subframe subframe
	)
	{
		subframe.type = Format.FLAC__SUBFRAME_TYPE_VERBATIM;

		final Subframe_Verbatim verbatim = new Subframe_Verbatim();
		subframe.data = verbatim;

		if( subframe_bps <= 32 ) {
			verbatim.data_type = Format.FLAC__VERBATIM_SUBFRAME_DATA_TYPE_INT32;
			verbatim.data = signal;
		}
		else {
			verbatim.data_type = Format.FLAC__VERBATIM_SUBFRAME_DATA_TYPE_INT64;
			verbatim.data = signal;
		}

		final int estimate = Format.FLAC__SUBFRAME_ZERO_PAD_LEN + Format.FLAC__SUBFRAME_TYPE_LEN + Format.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN + subframe.wasted_bits + (blocksize * subframe_bps);

//#if SPOTCHECK_ESTIMATE
//		spotcheck_subframe_estimate_( encoder, blocksize, subframe_bps, subframe, estimate );
//#else
//		(void)encoder;
//#endif

		return estimate;
	}

	private static int FLAC__format_get_max_rice_partition_order_from_blocksize_limited_max_and_predictor_order(final int limit, final int blocksize, final int predictor_order)
	{
		int max_rice_partition_order = limit;

		while( max_rice_partition_order > 0 && (blocksize >>> max_rice_partition_order) <= predictor_order ) {
			max_rice_partition_order--;
		}

		//FLAC__ASSERT(
		//	(max_rice_partition_order == 0 && blocksize >= predictor_order) ||
		//	(max_rice_partition_order > 0 && blocksize >> max_rice_partition_order > predictor_order)
		//);

		return max_rice_partition_order;
	}

	private final int find_best_partition_order_(
		//StreamEncoder private_,// StreamEncoderPrivate private_,
		final int residual[],
		final long abs__residual_partition_sums[],
		final int raw__bits_per_partition[],
		final int residual_samples,
		final int predictor_order,
		final int rice_parameter_limit,
		int min_partition_order,
		int max_partition_order,
		final int bps,
		final boolean is_do_escape_coding,
		final int rice_parameter_search_distance,
		final EntropyCodingMethod best_ecm
	)
	{
		int residual_bits, best_residual_bits = 0;
		int best_parameters_index = 0;// 0 or 1
		int best_partition_order = 0;
		final int block_size = residual_samples + predictor_order;

		max_partition_order = FLAC__format_get_max_rice_partition_order_from_blocksize_limited_max_and_predictor_order( max_partition_order, block_size, predictor_order );
		if( min_partition_order > max_partition_order ) {
			min_partition_order = max_partition_order;
		}

		//local_precompute_partition_info_sums( residual, abs_residual_partition_sums, residual_samples, predictor_order, min_partition_order, max_partition_order, bps );
		precompute_partition_info_sums_( residual, abs__residual_partition_sums, residual_samples, predictor_order, min_partition_order, max_partition_order, bps );

		if( is_do_escape_coding ) {
			precompute_partition_info_escapes_( residual, raw__bits_per_partition, residual_samples, predictor_order, min_partition_order, max_partition_order );
		}

		{
			for( int partition_order = max_partition_order, sum = 0; partition_order >= min_partition_order; partition_order-- ) {
				if( 0 > (residual_bits = //!
					set_partitioned_rice_(
//#ifdef EXACT_RICE_BITS_CALCULATION
//						residual,
//#endif
						abs__residual_partition_sums/* + sum*/,
						raw__bits_per_partition/* + sum*/,
						sum,// java: added as offset
						residual_samples,
						predictor_order,
						rice_parameter_limit,
						rice_parameter_search_distance,
						partition_order,
						is_do_escape_coding,
						this.partitioned_rice_contents_extra[ 1 ^ best_parameters_index ]//,
						//residual_bits
					))
				)
				{
					//FLAC__ASSERT(best_residual_bits != 0);
					break;
				}
				sum += 1 << partition_order;
				if( best_residual_bits == 0 || residual_bits < best_residual_bits ) {
					best_residual_bits = residual_bits;
					best_parameters_index ^= 1;
					best_partition_order = partition_order;
				}
			}
		}

		best_ecm./*data.*/partitioned_rice.order = best_partition_order;

		{
			/*
			 * We are allowed to de-const the pointer based on our special
			 * knowledge; it is const to the outside world.
			 */
			final PartitionedRiceContents prc = best_ecm./*data.*/partitioned_rice.contents;
			int partition;

			/* save best parameters and raw_bits */
			System.arraycopy( this.partitioned_rice_contents_extra[best_parameters_index].parameters, 0, prc.parameters, 0, 1 << best_partition_order );
			if( is_do_escape_coding ) {
				System.arraycopy( this.partitioned_rice_contents_extra[best_parameters_index].raw_bits, 0, prc.raw_bits, 0, 1 << best_partition_order );
			}
			/*
			 * Now need to check if the type should be changed to
			 * FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2 based on the
			 * size of the rice parameters.
			 */
			for( partition = 0; partition < (1 << best_partition_order); partition++ ) {
				if( prc.parameters[partition] >= Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ESCAPE_PARAMETER ) {
					best_ecm.type = Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2;
					break;
				}
			}
		}

		return best_residual_bits;
	}

	private static void precompute_partition_info_sums_(
		final int residual[],
		final long abs_residual_partition_sums[],
		final int residual_samples,
		final int predictor_order,
		final int min_partition_order,
		final int max_partition_order,
		final int bps
	)
	{
		final int default_partition_samples = (residual_samples + predictor_order) >> max_partition_order;
		int partitions = 1 << max_partition_order;

		//FLAC__ASSERT(default_partition_samples > predictor_order);

		/* first do max_partition_order */
		{
			final int threshold = 32 - Format.bitmath_ilog2( default_partition_samples );
			int residual_sample, end = -predictor_order;
			/* WATCHOUT: "bps + FLAC__MAX_EXTRA_RESIDUAL_BPS" is the maximum assumed size of the average residual magnitude */
			if( bps + FLAC__MAX_EXTRA_RESIDUAL_BPS < threshold ) {
				for( int partition = residual_sample = 0; partition < partitions; partition++ ) {
					int abs_residual_partition_sum = 0;// uint32
					end += default_partition_samples;
					for( ; residual_sample < end; residual_sample++) {
						final int a = residual[residual_sample];
						abs_residual_partition_sum += (a < 0) ? -a : a; /* abs(INT_MIN) is undefined, but if the residual is INT_MIN we have bigger problems */
					}
					abs_residual_partition_sums[partition] = abs_residual_partition_sum;// TODO java: it hopes, that in this case int32 is enough
					// abs_residual_partition_sums[partition] = ((long)abs_residual_partition_sum) & 0xffffffff;
				}
			}
			else { /* have to pessimistically use 64 bits for accumulator */
				for( int partition = residual_sample = 0; partition < partitions; partition++ ) {
					long abs_residual_partition_sum64 = 0;
					end += default_partition_samples;
					for( ; residual_sample < end; residual_sample++) {
						final int a = residual[residual_sample];
						abs_residual_partition_sum64 += (a < 0) ? -a : a; /* abs(INT_MIN) is undefined, but if the residual is INT_MIN we have bigger problems */
					}
					abs_residual_partition_sums[partition] = abs_residual_partition_sum64;
				}
			}
		}

		/* now merge partitions for lower orders */
		{
			int from_partition = 0, to_partition = partitions;
			for( int partition_order = max_partition_order - 1; partition_order >= min_partition_order; partition_order-- ) {
				partitions >>= 1;
				for( int i = 0; i < partitions; i++ ) {
					abs_residual_partition_sums[to_partition++] =
						abs_residual_partition_sums[from_partition    ] +
						abs_residual_partition_sums[from_partition + 1];
					from_partition += 2;
				}
			}
		}
	}

	private static void precompute_partition_info_escapes_(
		final int residual[],
		final int raw_bits_per_partition[],
		final int residual_samples,
		final int predictor_order,
		final int min_partition_order,
		final int max_partition_order
	)
	{
		int partition_order;
		int to_partition = 0;
		final int blocksize = residual_samples + predictor_order;

		/* first do max_partition_order */
		for( partition_order = max_partition_order; partition_order >= 0; /* partition_order-- */ ) {// dead code
			final int partitions = 1 << partition_order;
			final int default_partition_samples = blocksize >>> partition_order;

			//FLAC__ASSERT(default_partition_samples > predictor_order);

			for( int partition = 0, residual_sample = 0; partition < partitions; partition++ ) {
				int partition_samples = default_partition_samples;
				if( partition == 0 ) {
					partition_samples -= predictor_order;
				}
				int rmax = 0;
				for( int partition_sample = 0; partition_sample < partition_samples; partition_sample++ ) {
					int r = residual[residual_sample++];
					/* OPT: maybe faster: rmax |= r ^ (r>>31) */
					/*if( r < 0 )
						rmax |= ~r;
					else
						rmax |= r;*/
					if( r < 0 ) {
						r = ~r;
					}
					rmax |= r;
				}
				/* now we know all residual values are in the range [-rmax-1,rmax] */
				raw_bits_per_partition[partition] = rmax != 0 ? Format.bitmath_ilog2( rmax ) + 2 : 1;
			}
			to_partition = partitions;
			break; /*@@@ yuck, should remove the 'for' loop instead */
		}

		/* now merge partitions for lower orders */
		--partition_order;
		for( int from_partition = 0; partition_order >= min_partition_order; partition_order-- ) {
			int m, n;
			int i;
			final int partitions = 1 << partition_order;
			for( i = 0; i < partitions; i++ ) {
				m = raw_bits_per_partition[from_partition];
				from_partition++;
				n = raw_bits_per_partition[from_partition];
				raw_bits_per_partition[to_partition] = (m >= n ? m : n);
				from_partition++;
				to_partition++;
			}
		}
	}

//#ifdef EXACT_RICE_BITS_CALCULATION
//	private static int count_rice_bits_in_partition_(
//			final int rice_parameter,
//			final int partition_samples,
//			final int[] residual
//	)
//	{
//		int i, partition_bits =
//			Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN + /* actually could end up being FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN but err on side of 16bps */
//			(1 + rice_parameter) * partition_samples /* 1 for unary stop bit + rice_parameter for the binary portion */
//		;
//		for( i = 0; i < partition_samples; i++ )
//			partition_bits += ( ((residual[i] << 1) ^ (residual[i] >> 31)) >>> rice_parameter );
//		return (uint32_t)(flac_min(partition_bits,UIN32_MAX)); // To make sure the return value doesn't overflow
//	}
//#else
	private static int count_rice_bits_in_partition_(
		final int rice_parameter,
		final int partition_samples,
		final long abs_residual_partition_sum
	)
	{
		long v = Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN + /* actually could end up being FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN but err on side of 16bps */
				(1 + (long)rice_parameter) * (long)partition_samples + /* 1 for unary stop bit + rice_parameter for the binary portion */
				(
					rice_parameter != 0 ?
						(abs_residual_partition_sum >>> (rice_parameter - 1)) /* rice_parameter-1 because the real coder sign-folds instead of using a sign bit */
						: (abs_residual_partition_sum << 1) /* can't shift by negative number, so reverse */
				)
				- (partition_samples >>> 1);
		v = (v <= Integer.MAX_VALUE ? v : Integer.MAX_VALUE);// To make sure the return value doesn't overflow
		return (int) v;
				/* -(partition_samples>>1) to subtract out extra contributions to the abs_residual_partition_sum.
				 * The actual number of bits used is closer to the sum(for all i in the partition) of  abs(residual[i])>>(rice_parameter-1)
				 * By using the abs_residual_partition sum, we also add in bits in the LSBs that would normally be shifted out.
				 * So the subtraction term tries to guess how many extra bits were contributed.
				 * If the LSBs are randomly distributed, this should average to 0.5 extra bits per sample.
				 */
	}
//#endif

	/** @return < 0 - error, >= 0 - bits */
	private static int /* boolean */ set_partitioned_rice_(
//#ifdef EXACT_RICE_BITS_CALCULATION
//			const FLAC__int32 residual[],
//#endif
			final long[] abs_residual_partition_sums,
			final int[] raw_bits_per_partition,
			final int offset,// java: added as offset to abs_residual_partition_sums and raw_bits_per_partition
			final int residual_samples,
			final int predictor_order,
			final int rice_parameter_limit,
			final int rice_parameter_search_dist,
			final int partition_order,
			final boolean search_for_escapes,
			final PartitionedRiceContents partitioned_rice_contents//,
			// int[] bits// java: returned value
		)
	{
		int rice_parameter, partition_bits;
		int best_partition_bits, best_rice_parameter = 0;
		int bits_ = Format.FLAC__ENTROPY_CODING_METHOD_TYPE_LEN + Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN;
		int partition_samples;// TODO java check if long is required
		final int partitions = 1 << partition_order;
//#ifdef ENABLE_RICE_PARAMETER_SEARCH
//		uint32_t min_rice_parameter, max_rice_parameter;
//#else
//		(void)rice_parameter_search_dist;
//#endif

		// FLAC__ASSERT(rice_parameter_limit <= FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_ESCAPE_PARAMETER);

		final int[] parameters = partitioned_rice_contents.parameters;
		final int[] raw_bits = partitioned_rice_contents.raw_bits;

		for( int partition = 0, residual_sample = 0; partition < partitions; partition++ ) {
			partition_samples = (residual_samples + predictor_order) >>> partition_order;
			if( partition == 0 ) {
				if( partition_samples <= predictor_order ) {
					return -1;// false;
				} else {
					partition_samples -= predictor_order;
				}
			}
			final long mean = abs_residual_partition_sums[offset + partition];
			/* we are basically calculating the size in bits of the
			 * average residual magnitude in the partition:
			 *   rice_parameter = floor(log2(mean/partition_samples))
			 * 'mean' is not a good name for the variable, it is
			 * actually the sum of magnitudes of all residual values
			 * in the partition, so the actual mean is
			 * mean/partition_samples
			 */
//#if 0 /* old simple code */
//			for(rice_parameter = 0, k = partition_samples; k < mean; rice_parameter++, k <<= 1)
//				;
//#else
//#if defined FLAC__CPU_X86_64 /* and other 64-bit arch, too */
//			if(mean <= 0x80000000 / 512) { /* 512: more or less optimal for both 16- and 24-bit input */
//#else
			if( mean <= (0x80000000 >>> 3) ) { /* 32-bit arch: use 32-bit math if possible */
//#endif
				rice_parameter = 0;
				long k2 = partition_samples;
				while( k2 << 3 < mean ) { /* requires: mean <= (2^31)/8 */
					rice_parameter += 4; k2 <<= 4; /* tuned for 16-bit input */
				}
				while( k2 < mean ) { /* requires: mean <= 2^31 */
					rice_parameter++; k2 <<= 1;
				}
			}
			else {
				rice_parameter = 0;
				long k = partition_samples;
				if( mean <= (0x8000000000000000L >>> 7) ) { /* usually mean is _much_ smaller than this value */
					while( k << 7 < mean ) { /* requires: mean <= (2^63)/128 */
						rice_parameter += 8; k <<= 8; /* tuned for 24-bit input */
					}
				}
				while( k < mean ) { /* requires: mean <= 2^63 */
					rice_parameter++; k <<= 1;
				}
			}
//#endif
			if( rice_parameter >= rice_parameter_limit ) {
//#ifndef NDEBUG
//				fprintf(stderr, "clipping rice_parameter (%u -> %u) @6\n", rice_parameter, rice_parameter_limit - 1);
//#endif
				rice_parameter = rice_parameter_limit - 1;
			}

			best_partition_bits = Integer.MAX_VALUE;
/*#ifdef ENABLE_RICE_PARAMETER_SEARCH
			if(rice_parameter_search_dist) {
				if(rice_parameter < rice_parameter_search_dist)
					min_rice_parameter = 0;
				else
					min_rice_parameter = rice_parameter - rice_parameter_search_dist;
				max_rice_parameter = rice_parameter + rice_parameter_search_dist;
				if(max_rice_parameter >= rice_parameter_limit) {
#ifndef NDEBUG
					fprintf(stderr, "clipping rice_parameter (%u -> %u) @7\n", max_rice_parameter, rice_parameter_limit - 1);
#endif
					max_rice_parameter = rice_parameter_limit - 1;
				}
			}
			else
				min_rice_parameter = max_rice_parameter = rice_parameter;

			for(rice_parameter = min_rice_parameter; rice_parameter <= max_rice_parameter; rice_parameter++) {
#endif*/
//#ifdef EXACT_RICE_BITS_CALCULATION
//				partition_bits = count_rice_bits_in_partition_(rice_parameter, partition_samples, residual+residual_sample);
//#else
				partition_bits = count_rice_bits_in_partition_( rice_parameter, partition_samples, abs_residual_partition_sums[offset + partition] );
//#endif
				if( partition_bits < best_partition_bits ) {
					best_rice_parameter = rice_parameter;
					best_partition_bits = partition_bits;
				}
//#ifdef ENABLE_RICE_PARAMETER_SEARCH
//			}
//#endif
			if( search_for_escapes ) {
				partition_bits = Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN + Format.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_RAW_LEN + raw_bits_per_partition[offset + partition] * partition_samples;
				if( partition_bits <= best_partition_bits && raw_bits_per_partition[offset + partition] < 32 ) {
					raw_bits[partition] = raw_bits_per_partition[offset + partition];
					best_rice_parameter = 0; /* will be converted to appropriate escape parameter later */
					best_partition_bits = partition_bits;
				} else {
					raw_bits[partition] = 0;
				}
			}
			parameters[partition] = best_rice_parameter;
			if( best_partition_bits < Integer.MAX_VALUE - bits_ ) { // To make sure _bits doesn't overflow
				bits_ += best_partition_bits;
			} else { // To make sure _bits doesn't overflow
				bits_ = Integer.MAX_VALUE;
			}

			residual_sample += partition_samples;
		}

		//bits[0] = bits_;
		return bits_;//return true;
	}


	private static int get_wasted_bits_(final int signal[], final int samples)
	{
		int shift;
		int x = 0;

		for( int i = 0; i < samples && 0 == (x & 1); i++ ) {
			x |= signal[i];
		}

		if( x == 0 ) {
			shift = 0;
		}
		else {
			for( shift = 0; 0 == (x & 1); shift++ ) {
				x >>= 1;
			}
		}

		if( shift > 0 ) {
			for( int i = 0; i < samples; i++ ) {
				signal[i] >>= shift;
			}
		}

		return shift;
	}

	private static int get_wasted_bits_wide_(final long signal_wide[], final int signal[], final int samples)
	{
		int i, shift;
		long x = 0;

		for( i = 0; i < samples && ((x & 1) == 0); i++ ) {
			x |= signal_wide[i];
		}

		if( x == 0 ) {
			shift = 1;
		}
		else {
			for( shift = 0; ((x & 1) == 0); shift++ ) {
				x >>= 1;
			}
		}

		if( shift > 0 ) {
			for( i = 0; i < samples; i++ ) {
				signal[i] = (int)(signal_wide[i] >> shift);
			}
		}

		return shift;
	}


	@Override// implements StreamDecoderReadCallback, verify_read_callback_
	public int dec_read_callback(final StreamDecoder decoder, final byte buffer[], final int offset, int bytes/*, final Object client_data*/) throws IOException
	{
		// final StreamEncoder encoder = (StreamEncoder)client_data;// java: this
		final int encoded_bytes = this.verify.output.bytes;
		//(void)decoder;

		if( this.verify.needs_magic_hack ) {
			//FLAC__ASSERT(*bytes >= FLAC__STREAM_SYNC_LENGTH);
			bytes = Format.FLAC__STREAM_SYNC_LENGTH;
			System.arraycopy( Format.FLAC__STREAM_SYNC_STRING, 0, buffer, offset, bytes );
			this.verify.needs_magic_hack = false;
		}
		else {
			if( encoded_bytes == 0 ) {
				/*
				 * If we get here, a FIFO underflow has occurred,
				 * which means there is a bug somewhere.
				 */
				//FLAC__ASSERT(0);
				throw new IOException();// StreamDecoder.FLAC__STREAM_DECODER_READ_STATUS_ABORT;
			}
			else if( encoded_bytes < bytes ) {
				bytes = encoded_bytes;
			}
			System.arraycopy( this.verify.output.data, this.verify.output.offset, buffer, offset, bytes );
			this.verify.output.offset += bytes;// encoder.private_.verify.output.data += bytes[0];
			this.verify.output.bytes -= bytes;
		}

		return bytes;// return StreamDecoder.FLAC__STREAM_DECODER_READ_STATUS_CONTINUE;
	}

	@Override// StreamDecoderWriteCallback, verify_write_callback_
	public int /* FLAC__StreamDecoderWriteStatus */ dec_write_callback(final StreamDecoder decoder,
																	   final Frame fr, final int buffer[][], final int offset/*, final Object client_data*/)
	{
		// final StreamEncoder encoder = (StreamEncoder )client_data;// java: this
		final int nchannels = fr.header.channels;
		final int block_size = fr.header.blocksize;
		//final int bytes_per_block = blocksize << 2;// sizeof int32 = 4

		//(void)decoder;

		if( this.state == FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR ) {
			/* This is set when verify_error_callback_ was called */
			return StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
		}

		for( int channel = 0; channel < nchannels; channel++ ) {
			//if( 0 != Format.memcmp( buffer[channel], 0, encoder.private_.verify.input_fifo.data[channel], 0, bytes_per_block ) ) {// FIXME why need double loop?
				int i, bi, sample = 0;
				int expect = 0, got = 0;
				final int[] buff_ch = buffer[channel];
				final int[] data_ch = this.verify.input_fifo.data[channel];

				for( i = 0, bi = offset; i < block_size; i++, bi++ ) {
					if( buff_ch[bi] != data_ch[i] ) {
						sample = i;
						expect = data_ch[i];
						got = buff_ch[bi];
						break;
					}
				}
				if( i < block_size ) {// java: added
					//FLAC__ASSERT(i < blocksize);
					//FLAC__ASSERT(frame->header.number_type == FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER);
					this.verify.error_stats.absolute_sample = fr.header./*number.*/sample_number + sample;
					this.verify.error_stats.frame_number = (int)(fr.header./*number.*/sample_number / block_size);
					this.verify.error_stats.channel = channel;
					this.verify.error_stats.sample = sample;
					this.verify.error_stats.expected = expect;
					this.verify.error_stats.got = got;
					this.state = FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA;
					return StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
				}
			//}
		}
		/* dequeue the frame from the fifo */
		this.verify.input_fifo.tail -= block_size;
		//FLAC__ASSERT(encoder->private_->verify.input_fifo.tail <= OVERREAD_);
		for( int channel = 0; channel < nchannels; channel++ ) {
			System.arraycopy( this.verify.input_fifo.data[channel], block_size, this.verify.input_fifo.data[channel], 0, this.verify.input_fifo.tail );
		}
		return StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
	}

	@Override// implements StreamDecoderMetadataCallback, verify_metadata_callback_
	public void dec_metadata_callback(final StreamDecoder decoder, final StreamMetadata meta_data/*, final Object client_data*/) throws IOException
	{
		//(void)decoder, (void)metadata, (void)client_data;
	}

	@Override// implements StreamDecoderErrorCallback, verify_error_callback_
	public void dec_error_callback(final StreamDecoder decoder, final int /* FLAC__StreamDecoderErrorStatus */ status/*, final Object client_data*/)
	{
		// final StreamEncoder encoder = (StreamEncoder)client_data;// java: this
		//(void)decoder, (void)status;
		this.state = FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR;
	}

	@Override// implements StreamEncoderReadCallback, file_read_callback_
	public int /* FLAC__StreamEncoderReadStatus */ enc_read_callback(final StreamEncoder encoder, final byte buffer[], final int offset, final int bytes/* , final Object client_data*/)
			throws IOException, UnsupportedOperationException
	{
		//(void)client_data;

		final OutputStream f = encoder.file;
		if( f instanceof RandomAccessInputOutputStream ) {
			return ((RandomAccessInputOutputStream) f).read( buffer, offset, bytes );
		}
		throw new UnsupportedOperationException( FLAC__StreamEncoderReadStatusString[FLAC__STREAM_ENCODER_READ_STATUS_UNSUPPORTED] );
	}

	@Override// implements StreamEncoderSeekCallback, file_seek_callback_
	public int /* FLAC__StreamEncoderSeekStatus */ enc_seek_callback(final StreamEncoder encoder, final long absolute_byte_offset/*, final Object client_data*/)
	{
		//(void)client_data;// java: this

		final OutputStream f = encoder.file;
		if( f instanceof RandomAccessInputOutputStream ) {
			try {
				((RandomAccessInputOutputStream) f).seek( absolute_byte_offset );
			} catch(final IOException e) {
				return FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR;
			}
			return FLAC__STREAM_ENCODER_SEEK_STATUS_OK;
		}
		return FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED;
	}

	@Override//implements StreamEncoderTellCallback, file_tell_callback_
	public long enc_tell_callback(final StreamEncoder encoder/*, final Object client_data*/) throws IOException, UnsupportedOperationException
	{
		//(void)client_data;
		final OutputStream f = encoder.file;
		if( f instanceof RandomAccessInputOutputStream ) {
			return ((RandomAccessInputOutputStream) f).getFilePointer();
		}
		throw new UnsupportedOperationException( FLAC__StreamEncoderTellStatusString[FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED] );
	}

/*#ifdef FLAC__VALGRIND_TESTING
	private static size_t local__fwrite(final Object ptr, size_t size, size_t nmemb, OutputStream stream)
	{
		size_t ret = fwrite( ptr, size, nmemb, stream );
		if( ! ferror( stream ) )
			fflush( stream );
		return ret;
	}
#else
	#define local__fwrite fwrite
#endif*/

	@Override//implements StreamEncoderWriteCallback, file_write_callback_
	public int /* FLAC__StreamEncoderWriteStatus */ enc_write_callback(final StreamEncoder encoder,
			final byte buffer[], final int offset, final int bytes, final int samples, final int current_frame/*, final Object client_data*/)
	{
		//(void)client_data, (void)current_frame;

		try {
			/*if( */encoder.file.write( buffer, offset, bytes );// == bytes ) {
				final  boolean call_it;
if( Format.FLAC__HAS_OGG ) {
				/* We would like to be able to use 'samples > 0' in the
				 * clause here but currently because of the nature of our
				 * Ogg writing implementation, 'samples' is always 0 (see
				 * ogg_encoder_aspect.c).  The downside is extra progress
				 * callbacks.
				 */
				call_it = encoder.progress_callback != null && (encoder.is_ogg ? true : (samples > 0));
} else {
				call_it = encoder.progress_callback != null && (samples > 0);
}
			//}// if
			if( call_it ) {
				/* NOTE: We have to add +bytes, +samples, and +1 to the stats
				 * because at this point in the callback chain, the stats
				 * have not been updated.  Only after we return and control
				 * gets back to write_frame_() are the stats updated
				 */
				encoder.progress_callback.enc_progress_callback( encoder,
						encoder.bytes_written + bytes, encoder.samples_written + samples,
						encoder.frames_written + (samples != 0 ? 1 : 0), encoder.total_frames_estimate/*, encoder.client_data*/ );
			}
		} catch(final IOException e) {
			return FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
		}
		return FLAC__STREAM_ENCODER_WRITE_STATUS_OK;
	}
}
