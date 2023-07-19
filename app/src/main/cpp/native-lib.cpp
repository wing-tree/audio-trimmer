#include <jni.h>
#include <string>
#include <vector>
#include "compress_type.h"

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/avutil.h"
#include "libavutil/samplefmt.h"
}

static AVCodecContext *av_codec_ctx;
static AVFormatContext *av_fmt_ctx = nullptr;
static AVStream *av_stream = nullptr;
static int audio_stream_idx = -1;
static AVFrame *frame = NULL;
static AVPacket *pkt = NULL;

#include <android/log.h>

#define  LOG_TAG    "your-log-tag"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

double get_sample(const AVCodecContext* codecCtx, uint8_t* buffer, int sampleIndex) {
    int64_t val = 0;
    double ret = 0;
    int sampleSize = av_get_bytes_per_sample(codecCtx->sample_fmt);

    switch(sampleSize) {
        case 1:
            val = (reinterpret_cast<uint8_t*>(buffer))[sampleIndex];
            val -= 127;
            break;
        case 2:
            val = (reinterpret_cast<int16_t*>(buffer))[sampleIndex];
            break;
        case 4: val = (reinterpret_cast<int32_t*>(buffer))[sampleIndex];
            break;
        case 8: val = (reinterpret_cast<int64_t*>(buffer))[sampleIndex];
            break;
        default:
            return 0;
    }

    // Check which data type is in the sample.
    switch(codecCtx->sample_fmt) {
        case AV_SAMPLE_FMT_U8:
        case AV_SAMPLE_FMT_S16:
        case AV_SAMPLE_FMT_S32:
        case AV_SAMPLE_FMT_U8P:
        case AV_SAMPLE_FMT_S16P:
        case AV_SAMPLE_FMT_S32P:
            // integer => Scale to [-1, 1] and convert to float.
            ret = val / (static_cast<float>(((1 << (sampleSize*8-1))-1)));
            break;
        case AV_SAMPLE_FMT_FLT:
        case AV_SAMPLE_FMT_FLTP:
            // float => reinterpret
            ret = *reinterpret_cast<float*>(&val);
            break;
        case AV_SAMPLE_FMT_DBL:
        case AV_SAMPLE_FMT_DBLP:
            // double => reinterpret and then static cast down
            ret = *reinterpret_cast<double*>(&val);
            break;
        default:
            return 0;
    }

    return ret;
}

std::string concat_temp_amplitudes_data(
        std::vector<int>* temp_data
) {
    std::string temp_sample_str;
    for(int temp_sample : *temp_data) {
        temp_sample_str += std::to_string(temp_sample) + "\n";
    }
    return temp_sample_str;
}

static int decode_packet(
        AVCodecContext *dec,
        const AVPacket *pkt,
        std::vector<int>* temp_samples,
        std::string* errors
) {
    int ret = 0;

    // submit the packet to the decoder
    ret = avcodec_send_packet(dec, pkt);
    if (ret < 0) {
        //add_error(errors, PACKET_SUBMITTING_PROC_CODE);
        return ret;
    }

    // get all the available frames from the decoder
    while (ret >= 0) {
        ret = avcodec_receive_frame(dec, frame);
        if (ret < 0) {
            // those two return values are special and mean there is no output
            // frame available, but there were no errors during decoding
            if (ret == AVERROR_EOF || ret == AVERROR(EAGAIN)) {
                return 0;
            }
            //add_error(errors, DECODING_PROC_CODE);
            return ret;
        }

        // write the frame data to output file
        if(dec->codec->type == AVMEDIA_TYPE_AUDIO) {
            double sum = 0;
            for(int i = 0; i < frame->nb_samples; i++) {
                double sample = get_sample(av_codec_ctx, frame->data[0], i);
                sum += sample * sample;
            }
            temp_samples->push_back(((int)(sqrt(sum / frame->nb_samples) * 100)));
        }

        av_frame_unref(frame);
        if (ret < 0) {
            return ret;
        }
    }

    return 0;
}

static int get_format_from_sample_fmt(const char **fmt, enum AVSampleFormat sample_fmt) {
    int i;
    struct sample_fmt_entry {
        enum AVSampleFormat sample_fmt;
        const char *fmt_be, *fmt_le;
    } sample_fmt_entries[] = {
            { AV_SAMPLE_FMT_U8,  "u8",    "u8"    },
            { AV_SAMPLE_FMT_S16, "s16be", "s16le" },
            { AV_SAMPLE_FMT_S32, "s32be", "s32le" },
            { AV_SAMPLE_FMT_FLT, "f32be", "f32le" },
            { AV_SAMPLE_FMT_DBL, "f64be", "f64le" },
    };
    *fmt = NULL;

    for (i = 0; i < FF_ARRAY_ELEMS(sample_fmt_entries); i++) {
        struct sample_fmt_entry *entry = &sample_fmt_entries[i];
        if (sample_fmt == entry->sample_fmt) {
            *fmt = AV_NE(entry->fmt_be, entry->fmt_le);
            return 0;
        }
    }

    return -1;
}

static int open_codec_context(
        int *stream_idx,
        AVCodecContext **dec_ctx,
        AVFormatContext *fmt_ctx,
        enum AVMediaType type,
        std::string* errors
) {
    int ret, stream_index;
    AVStream *st;
    const AVCodec *dec = NULL;
    AVDictionary *opts = NULL;

    ret = av_find_best_stream(fmt_ctx, type, -1, -1, NULL, 0);
    if (ret < 0) {
        //add_error(errors, STREAM_NOT_FOUND_PROC_CODE);
        return ret;
    } else {
        stream_index = ret;
        st = fmt_ctx->streams[stream_index];

        // find decoder for the stream
        dec = avcodec_find_decoder(st->codecpar->codec_id);
        if (!dec) {
            //add_error(errors, CODEC_NOT_FOUND_PROC_CODE);
            return AVERROR(EINVAL);
        }

        // Allocate a codec context for the decoder
        *dec_ctx = avcodec_alloc_context3(dec);
        if (!*dec_ctx) {
            //add_error(errors, CODEC_CONTEXT_ALLOC_CODE);
            return AVERROR(ENOMEM);
        }

        // Copy codec parameters from input stream to output codec context
        if ((ret = avcodec_parameters_to_context(*dec_ctx, st->codecpar)) < 0) {
            //add_error(errors, CODEC_PARAMETERS_COPY_PROC_CODE);
            return ret;
        }

        // Init the decoders
        if ((ret = avcodec_open2(*dec_ctx, dec, &opts)) < 0) {
            //add_error(errors, CODEC_OPEN_PROC_CODE);
            return ret;
        }
        *stream_idx = stream_index;
    }

    return 0;
}

std::string compress_temp_amplitudes_data(
        std::vector<int>* temp_data,
        const int* compress_type
) {
    std::string compress_result;

    if(temp_data->empty())
        return compress_result;

    switch (*compress_type) {
        case COMPRESS_SKIP: {
            compress_result += std::to_string(temp_data->at(0));
            break;
        }
        case COMPRESS_PEEK: {
            std::sort(temp_data->begin(), temp_data->end());
            compress_result += std::to_string(temp_data->at(0));
            break;
        }
        case COMPRESS_AVERAGE: {
            int sum = 0;
            for(int temp_sample : *temp_data) {
                sum += temp_sample;
            }
            compress_result += std::to_string( (int) (sum / temp_data->size()) );
            break;
        }
    }
    compress_result += "\n";
    return compress_result;
}

extern "C" JNIEXPORT jobject JNICALL
Java_wing_tree_audio_trimmer_AudioTrimmer_getAmplitudes(
        JNIEnv* env,
        jobject,
        jstring jaudio_path,
        jint jcompress_type,
        jint jframes_per_second
) {
    int ret = 0;
    // input params
    const int preferred_frames_per_second = (int) jframes_per_second;
    int compress_type = (int) jcompress_type;
    const char* input_audio_path = env->GetStringUTFChars(jaudio_path, nullptr);

    int current_frame_idx = 0;
    int current_progress = 0;
    int nb_frames = 0;
    int actual_frames_per_second;
    int compression_divider;
    double duration = 0.0;

    // 리턴 클래스는 정의해야 할 듯.
    // return wrapper class
    jclass amplitudaResultClass = (env)->FindClass("wing/tree/audio/trimmer/model/Result");
    jmethodID constructor = (env)->GetMethodID(amplitudaResultClass, "<init>", "()V");

    // wrapper fields
    jfieldID duration_field = (env)->GetFieldID(amplitudaResultClass, "duration", "D");
    jfieldID amplitudes_field = (env)->GetFieldID(amplitudaResultClass, "amplitudes", "Ljava/lang/String;");

    // create wrapper object
    jobject amplitudaResultReturnObject = (env)->NewObject(amplitudaResultClass, constructor);

    // prepare result containers
    std::vector<int> temp_data;
    std::string amplitudes_data;
    std::string errors_data;

    if (avformat_open_input(&av_fmt_ctx, input_audio_path, nullptr, nullptr) < 0) {
        // handle error. + return.
    }

    // retrieve stream information
    if (avformat_find_stream_info(av_fmt_ctx, nullptr) < 0) {
        // handle error + return.
        //add_error(&errors_data, STREAM_INFO_NOT_FOUND_PROC_CODE);
        //goto end_return;
    }

    if (open_codec_context(&audio_stream_idx, &av_codec_ctx, av_fmt_ctx, AVMEDIA_TYPE_AUDIO, &errors_data) >= 0) {
        av_stream = av_fmt_ctx->streams[audio_stream_idx];
    }

    // dump input information to stderr
    av_dump_format(av_fmt_ctx, 0, input_audio_path, 0);

    if (!av_stream) {
        // handle error + return
//        ret = 1;
//        add_error(&errors_data, STREAM_NOT_FOUND_PROC_CODE);
//        goto end_cleanup;
    }

    frame = av_frame_alloc();
    if (!frame) {
        // handle error + return
//        ret = AVERROR(ENOMEM);
//        add_error(&errors_data, FRAME_ALLOC_CODE);
//        goto end_cleanup;
    }

    pkt = av_packet_alloc();
    if (!pkt) {
//        ret = AVERROR(ENOMEM);
//        add_error(&errors_data, PACKET_ALLOC_CODE);
//        goto end_cleanup;
    }

    // prepare duration from time base to seconds
    duration = av_fmt_ctx->duration * av_q2d(AV_TIME_BASE_Q);

    // full formula: (channels * rate * duration [seconds]) / frame_size
    // amplituda case - 1 [channel] instead of audio_dec_ctx->channels
    // also prevent division by zero exception
    if(av_codec_ctx->frame_size > 0) {
        nb_frames = (av_codec_ctx->sample_rate * (int) duration) / av_codec_ctx->frame_size;
    }

    actual_frames_per_second = (int) (nb_frames / duration);

    // cannot compress to preferred frames per second
    if(preferred_frames_per_second > actual_frames_per_second && actual_frames_per_second > 0) {
        //add_error(&errors_data, SAMPLE_OUT_OF_BOUNDS_PROC_CODE);
        compress_type = COMPRESS_NONE;
    }

    // no need to compress data
    if(preferred_frames_per_second == actual_frames_per_second/* || preferred_frames_per_second == 0*/) {
        compress_type = COMPRESS_NONE;
    } else {
        compression_divider = actual_frames_per_second / preferred_frames_per_second;
        // max compression - x2
        if(compression_divider < 2) {
            compression_divider = 2;
        }
    }

    // read frames from the file
    while (av_read_frame(av_fmt_ctx, pkt) >= 0) {
        bool is_audio_stream = pkt->stream_index == audio_stream_idx;
        // check if the packet belongs to a stream we are interested in, otherwise skip it
        if (is_audio_stream) {
            ret = decode_packet(av_codec_ctx, pkt, &temp_data, &errors_data);
            // compress data when current_frame_idx is compression_divider
            if(compress_type != COMPRESS_NONE && current_frame_idx % compression_divider == 0) {
                std::string temp_sample_str = compress_temp_amplitudes_data(&temp_data, &compress_type);
                amplitudes_data += temp_sample_str;
                //if(cache_enabled) write_cache_data(cache_file, &temp_sample_str);
                temp_data.clear();
            }
            if(compress_type == COMPRESS_NONE) {
                std::string temp_sample_str = concat_temp_amplitudes_data(&temp_data);
                amplitudes_data += temp_sample_str;
                //if(cache_enabled) write_cache_data(cache_file, &temp_sample_str);
                temp_data.clear();
            }
        }
        av_packet_unref(pkt);
        if (ret < 0) {
            break;
        }
        // update progress listener
//        if(valid_listener) {
//            int progress = nb_frames > 0 ? (current_frame_idx * 100) / nb_frames : 0;
//            if(current_progress != progress && progress <= 99) {
//                //env->CallVoidMethod(jlistener, on_progress_method, progress);
//                current_progress = progress;
//            }
//        }
        // Count only audio stream frames (this will prevent +100% progress for video processing)
        if(is_audio_stream) {
            current_frame_idx++;
        }
    }

    // flush the decoders
    if (av_codec_ctx) {
        decode_packet(av_codec_ctx, NULL, &temp_data, &errors_data);
    }

    if (av_stream) {
        enum AVSampleFormat sfmt = av_codec_ctx->sample_fmt;
        int n_channels = av_codec_ctx->channels;
        const char *fmt;

        if (av_sample_fmt_is_planar(sfmt)) {
            const char *packed = av_get_sample_fmt_name(sfmt);
            sfmt = av_get_packed_sample_fmt(sfmt);
            n_channels = 1;
        }

        if ((ret = get_format_from_sample_fmt(&fmt, sfmt)) < 0) {
//            add_error(&errors_data, UNSUPPORTED_SAMPLE_FMT_PROC_CODE);
//            goto end_cleanup;
        }
    }

    avcodec_free_context(&av_codec_ctx);
    avformat_close_input(&av_fmt_ctx);
    av_packet_free(&pkt);
    av_frame_free(&frame);

    env->ReleaseStringUTFChars(jaudio_path, input_audio_path);
    env->SetDoubleField(amplitudaResultReturnObject, duration_field, duration);
    env->SetObjectField(amplitudaResultReturnObject, amplitudes_field, env->NewStringUTF(amplitudes_data.c_str()));
    //env->SetObjectField(amplitudaResultReturnObject, errors_field, env->NewStringUTF(errors_data.c_str()));

    return amplitudaResultReturnObject;
}

extern "C" JNIEXPORT jstring JNICALL
Java_wing_tree_audio_trimmer_AudioTrimmer_trimAudio(
        JNIEnv *env,
        jobject,
        jstring inputPath,
        jstring outPath,
        jint startTime,
        jint endTime
) {
    const char *input_file_path = env->GetStringUTFChars(inputPath, nullptr);
    const char *out_file_path = env->GetStringUTFChars(outPath, nullptr);

    AVFormatContext *avformat_context = avformat_alloc_context();

    if (avformat_open_input(&avformat_context, input_file_path, nullptr, nullptr) != 0) {
        return nullptr; // Couldn't open file
    }

    // Retrieve avstream information
    if (avformat_find_stream_info(avformat_context, nullptr) < 0) {
        return nullptr; // Couldn't find avstream information
    }


    int best_stream = av_find_best_stream(avformat_context, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);

    if (best_stream == -1) {
        return nullptr; // Didn't find an audio avstream
    }

    AVStream *avstream = avformat_context->streams[best_stream];
    AVPacket avpacket;
//
    // Open output file
    AVFormatContext *outputContext;
    avformat_alloc_output_context2(&outputContext, nullptr, nullptr, out_file_path);
    AVStream *out_stream = avformat_new_stream(outputContext, nullptr);

    if (!out_stream) {
        return nullptr; // Failed allocating output avstream
    }

    out_stream->time_base = avstream->time_base;
    if (avcodec_parameters_copy(out_stream->codecpar, avstream->codecpar) < 0) {
        return nullptr; // Failed to copy codec parameters
    }

    avio_open(&outputContext->pb, out_file_path, AVIO_FLAG_WRITE);
    avformat_write_header(outputContext, nullptr);
    av_seek_frame(avformat_context, best_stream, startTime * AV_TIME_BASE, AVSEEK_FLAG_ANY);

    while (av_read_frame(avformat_context, &avpacket) >= 0) {
        if (avpacket.stream_index == best_stream) {
            AVPacket outPacket = avpacket;

            double packet_pts_seconds = avpacket.pts * av_q2d(avstream->time_base);

            if (packet_pts_seconds <= startTime) {
                continue;
            }

            // Update the avpacket to have the new time base
            av_packet_rescale_ts(&avpacket, avstream->time_base, out_stream->time_base);

            if (av_interleaved_write_frame(outputContext, &outPacket) < 0) {
                break;
            }
        }

        if ((avpacket.pts * av_q2d(avstream->time_base)) > endTime) {
            break;
        }
    }

    av_write_trailer(outputContext);
    avio_closep(&outputContext->pb);
    avformat_free_context(outputContext);

    avformat_close_input(&avformat_context);
    avformat_free_context(avformat_context);

    env->ReleaseStringUTFChars(inputPath, input_file_path);

    return env->NewStringUTF(out_file_path);
}
