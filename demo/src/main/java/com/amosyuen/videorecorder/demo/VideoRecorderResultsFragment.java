package com.amosyuen.videorecorder.demo;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amosyuen.videorecorder.activity.FFmpegPreviewActivity;
import com.amosyuen.videorecorder.activity.params.FFmpegPreviewActivityParams;
import com.amosyuen.videorecorder.demo.network.ApiService;
import com.amosyuen.videorecorder.demo.network.RetrofitInstance;
import com.amosyuen.videorecorder.recorder.params.EncoderParamsI;
import com.google.gson.JsonObject;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.media.MediaFormat.KEY_MIME;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AC3;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AMR_NB;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AMR_WB;
import static android.media.MediaFormat.MIMETYPE_AUDIO_EAC3;
import static android.media.MediaFormat.MIMETYPE_AUDIO_FLAC;
import static android.media.MediaFormat.MIMETYPE_AUDIO_G711_ALAW;
import static android.media.MediaFormat.MIMETYPE_AUDIO_G711_MLAW;
import static android.media.MediaFormat.MIMETYPE_AUDIO_MPEG;
import static android.media.MediaFormat.MIMETYPE_AUDIO_MSGSM;
import static android.media.MediaFormat.MIMETYPE_AUDIO_OPUS;
import static android.media.MediaFormat.MIMETYPE_AUDIO_QCELP;
import static android.media.MediaFormat.MIMETYPE_AUDIO_RAW;
import static android.media.MediaFormat.MIMETYPE_AUDIO_VORBIS;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_DOLBY_VISION;
import static android.media.MediaFormat.MIMETYPE_VIDEO_H263;
import static android.media.MediaFormat.MIMETYPE_VIDEO_HEVC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_MPEG2;
import static android.media.MediaFormat.MIMETYPE_VIDEO_MPEG4;
import static android.media.MediaFormat.MIMETYPE_VIDEO_RAW;
import static android.media.MediaFormat.MIMETYPE_VIDEO_VP8;
import static android.media.MediaFormat.MIMETYPE_VIDEO_VP9;
import static com.amosyuen.videorecorder.demo.VideoRecorderRequestFragment.FILE_PREFIX;


/**
 * Fragment for displaying the list of recorded videos.
 */
public class VideoRecorderResultsFragment extends Fragment {

    private static final String ADAPTER_KEY = "adapter";

    private static final String LOG_TAG = "VideoRecorderResults";

    private RecyclerView mRecyclerView;

    private VideoFileAdapter mVideoFileAdapter;

    ProgressDialog dialog ;

    File  fileTumb ;
    File VideoFileVodeo ;

    RetrofitInstance retrofitInstance ;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mVideoFileAdapter = new VideoFileAdapter();
            loadExistingFiles();
        } else {
            mVideoFileAdapter = (VideoFileAdapter) savedInstanceState.getSerializable(ADAPTER_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_recorder_results, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mVideoFileAdapter);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext())
                .showLastDivider()
                .build());




        this.retrofitInstance = new RetrofitInstance();

        AppCompatButton deleteAllButton = (AppCompatButton) view.findViewById(R.id.delete_all);
        deleteAllButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setCancelable(false)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(R.string.delete_all_video_files)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mVideoFileAdapter.clearVideoFiles();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        CategoryDatalist();
        Button post = view.findViewById(R.id.post);
        post.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {


                uploadFile(VideoFileVodeo,
                        "70",
                        "123Test",
                        "123",
                        "1",
                        "11,2",
                        "1.54",
                        "1",
                        "1",
                        "1",
                        "1",
                        "0",
                        "0",
                        "1",
                        "0",
                        "1"
                );



            }
        });


        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ADAPTER_KEY, mVideoFileAdapter);
    }

    public void addVideoFile(VideoFile videoFile) {

        Log.e("reViwo" ,  videoFile.getVideoFile().getAbsolutePath()+ " ") ;

       fileTumb= videoFile.getThumbnailFile();
      VideoFileVodeo = videoFile.getVideoFile();
        mVideoFileAdapter.addVideoFile(videoFile);

        Log.e("data... ", "addVideoFile: " +  fileTumb + "  "  + VideoFileVodeo );
        mRecyclerView.smoothScrollToPosition(0);
    }

    private void loadExistingFiles() {
        File dir = getContext().getExternalCacheDir();
        File[] files = dir.listFiles();
        ArrayList<File> videoFiles = new ArrayList<>();
        HashMap<String, File> thumbnailFiles = new HashMap<>();
        for (File file : files) {
            String[] parts = file.getName().split("\\.");
            if (parts.length != 2 || !parts[0].startsWith(FILE_PREFIX)) {
                continue;
            }
            String ext = parts[1];
            if (ext.equals(VideoRecorderRequestFragment.THUMBNAIL_FILE_EXTENSION)) {
                thumbnailFiles.put(parts[0], file);
            } else {
                try {
                    EncoderParamsI.OutputFormat.valueOf(ext.toUpperCase());
                    videoFiles.add(file);
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, String.format("Unsupported video file: %s", file.getName()));
                }
            }
        }

        for (File file : videoFiles) {
            String[] parts = file.getName().split("\\.");
            File thumbnailFile = thumbnailFiles.get(parts[0]);
            if (thumbnailFile == null) {
                Log.e(LOG_TAG, String.format("Video file is missing thumbnail %s", file.getName()));
                continue;
            }
            VideoFile videoFile = VideoFile.create(file, thumbnailFile);
            // Try reading the file. Delete unreadable files.
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            try {

                Log.e("Video FILe", videoFile.getVideoFile().getAbsolutePath() + " " ) ;
                metadataRetriever.setDataSource(videoFile.getVideoFile().getAbsolutePath());
                mVideoFileAdapter.mVideoFiles.add(videoFile);
            } catch (Exception e) {
                videoFile.delete();
            } finally {
                metadataRetriever.release();
            }
        }

        if (!mVideoFileAdapter.mVideoFiles.isEmpty()) {
            Collections.sort(mVideoFileAdapter.mVideoFiles);
            mVideoFileAdapter.notifyDataSetChanged();
        }
    }

    static class VideoFileAdapter
            extends RecyclerView.Adapter<VideoFileAdapter.VideoFileViewHolder>
            implements Serializable {

        private List<VideoFile> mVideoFiles = new ArrayList<>();

        public VideoFileAdapter() {
            setHasStableIds(true);
        }

        public void addVideoFile(VideoFile videoFile) {
            mVideoFiles.add(0, videoFile);
            notifyItemInserted(0);
        }

        public void deleteVideoFile(VideoFile videoFile) {
            videoFile.delete();
            int index = mVideoFiles.indexOf(videoFile);
            mVideoFiles.remove(index);
            notifyItemRemoved(index);
        }

        public void clearVideoFiles() {
            for (VideoFile videoFile : mVideoFiles) {
                videoFile.delete();
            }
            mVideoFiles.clear();
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return mVideoFiles.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return mVideoFiles.size();
        }

        @Override
        public VideoFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_video_file, parent, false);
            return new VideoFileViewHolder(v);
        }

        @Override
        public void onBindViewHolder(VideoFileViewHolder holder, int position) {
            holder.setVideoFile(mVideoFiles.get(position));
        }

        class VideoFileViewHolder extends RecyclerView.ViewHolder {

            private View mView;
            private ImageView mThumbnailImageView;
            private TextView mThumbnailFileSizeTextView;
            private TextView mVideoFileDateTextView;
            private TextView mVideoFileSizeTextView;
            private TextView mVideoFileFormatTextView;
            private TextView mVideoLengthTextView;

            private TextView mVideoCodecTextView;
            private TextView mVideoWidthTextView;
            private TextView mVideoHeightTextView;
            private TextView mVideoFrameRateTextView;
            private TextView mVideoBitrateTextView;

            private TextView mAudioCodecTextView;
            private TextView mAudioSampleRateTextView;
            private TextView mAudioChannelsTextView;

            private VideoFile mVideoFile;

            public VideoFileViewHolder(View view) {
                super(view);
                mView = view;

                mThumbnailImageView = (ImageView) view.findViewById(R.id.thumbnail);
                mThumbnailFileSizeTextView = (TextView) view.findViewById(R.id.thumbnail_file_size);

                mVideoFileDateTextView = (TextView) view.findViewById(R.id.video_file_date);
                mVideoFileSizeTextView = (TextView) view.findViewById(R.id.video_file_size);
                mVideoFileFormatTextView = (TextView) view.findViewById(R.id.video_file_format);
                mVideoLengthTextView = (TextView) view.findViewById(R.id.video_length);

                mVideoCodecTextView = (TextView) view.findViewById(R.id.video_codec);
                mVideoWidthTextView = (TextView) view.findViewById(R.id.video_width);
                mVideoHeightTextView = (TextView) view.findViewById(R.id.video_height);
                mVideoFrameRateTextView = (TextView) view.findViewById(R.id.video_frame_rate);
                mVideoBitrateTextView = (TextView) view.findViewById(R.id.video_bitrate);

                mAudioCodecTextView = (TextView) view.findViewById(R.id.audio_codec);
                mAudioSampleRateTextView = (TextView) view.findViewById(R.id.audio_sample_rate);
                mAudioChannelsTextView = (TextView) view.findViewById(R.id.audio_channels);

                ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(mView.getContext())
                                .setCancelable(false)
                                .setTitle(R.string.are_you_sure)
                                .setMessage(R.string.delete_video_file)
                                .setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteVideoFile(mVideoFile);
                                            }
                                        })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                });
            }

            public void setVideoFile(final VideoFile videoFile) {
                mView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mView.getContext(), FFmpegPreviewActivity.class);
                        intent.putExtra(
                                FFmpegPreviewActivity.REQUEST_PARAMS_KEY,
                                FFmpegPreviewActivityParams.builder(mView.getContext())
                                        .setVideoFileUri(videoFile.getVideoFile()).build());
                        mView.getContext().startActivity(intent);
                    }
                });

                mVideoFile = videoFile;
                mThumbnailImageView.setBackground(new BitmapDrawable(
                        mView.getContext().getResources(),
                        BitmapFactory.decodeFile(videoFile.getThumbnailFile().getAbsolutePath())));
                mThumbnailImageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mView.getContext(), ThumbnailActivity.class);
                        intent.setData(Uri.fromFile(mVideoFile.getThumbnailFile()));
                        mView.getContext().startActivity(intent);
                    }
                });
                mThumbnailFileSizeTextView.setText(
                        Util.getHumanReadableByteCount(videoFile.getThumbnailFile().length(), true));
                mVideoFileDateTextView.setText(Util.getHumanReadableDate(
                        videoFile.getVideoFile().lastModified()));
                mVideoFileSizeTextView.setText(Util.getHumanReadableByteCount(
                        videoFile.getVideoFile().length(), true));
                String[] parts = videoFile.getVideoFile().getName().split("\\.");
                mVideoFileFormatTextView.setText(parts[1]);

                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                try {
                    metadataRetriever.setDataSource(videoFile.getVideoFile().getAbsolutePath());
                    mVideoBitrateTextView.setText(Util.getHumanReadableBitrate(
                            Integer.parseInt(metadataRetriever.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_BITRATE)), true));
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error extracting video bitrate", e);
                    mVideoBitrateTextView.setText("");
                } finally {
                    metadataRetriever.release();
                }

                MediaExtractor mediaExtractor = new MediaExtractor();
                mVideoCodecTextView.setText(R.string.unknown);
                mAudioCodecTextView.setText(R.string.unknown);
                try {
                    mediaExtractor.setDataSource(videoFile.getVideoFile().getAbsolutePath());
                    for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                        MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                        String mimeType = mediaFormat.getString(KEY_MIME);
                        switch (mimeType) {
                            case MIMETYPE_VIDEO_AVC:
                            case MIMETYPE_VIDEO_DOLBY_VISION:
                            case MIMETYPE_VIDEO_H263:
                            case MIMETYPE_VIDEO_HEVC:
                            case MIMETYPE_VIDEO_MPEG2:
                            case MIMETYPE_VIDEO_MPEG4:
                            case MIMETYPE_VIDEO_RAW:
                            case MIMETYPE_VIDEO_VP8:
                            case MIMETYPE_VIDEO_VP9:
                                mVideoLengthTextView.setText(Util.getHumanReadableDuration(
                                        mediaFormat.getLong(MediaFormat.KEY_DURATION)));
                                mVideoCodecTextView.setText(Util.getMimetype(mimeType));
                                mVideoWidthTextView.setText(String.format(Locale.US, "%d px",
                                        mediaFormat.getInteger(MediaFormat.KEY_WIDTH)));
                                mVideoHeightTextView.setText(String.format(Locale.US, "%d px",
                                        mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)));
                                mVideoFrameRateTextView.setText(String.format(Locale.US, "%d fps",
                                        mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)));
                                break;
                            case MIMETYPE_AUDIO_AAC:
                            case MIMETYPE_AUDIO_AC3:
                            case MIMETYPE_AUDIO_AMR_NB:
                            case MIMETYPE_AUDIO_AMR_WB:
                            case MIMETYPE_AUDIO_EAC3:
                            case MIMETYPE_AUDIO_FLAC:
                            case MIMETYPE_AUDIO_G711_ALAW:
                            case MIMETYPE_AUDIO_G711_MLAW:
                            case MIMETYPE_AUDIO_MPEG:
                            case MIMETYPE_AUDIO_MSGSM:
                            case MIMETYPE_AUDIO_OPUS:
                            case MIMETYPE_AUDIO_QCELP:
                            case MIMETYPE_AUDIO_RAW:
                            case MIMETYPE_AUDIO_VORBIS:
                                mAudioCodecTextView.setText(Util.getMimetype(mimeType));
                                mAudioSampleRateTextView.setText(String.format(Locale.US, "%d Hz",
                                        mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)));
                                mAudioChannelsTextView.setText(String.format(Locale.US, "%d",
                                        mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)));
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error extracting media metadata", e);
                    mVideoLengthTextView.setText("");
                    mVideoCodecTextView.setText("");
                    mVideoWidthTextView.setText("");
                    mVideoHeightTextView.setText("");
                    mVideoFrameRateTextView.setText("");

                    mAudioCodecTextView.setText("");
                    mAudioSampleRateTextView.setText("");
                    mAudioChannelsTextView.setText("");
                } finally {
                    mediaExtractor.release();
                }
            }
        }
    }


    private void uploadFile(
            File      file,
            String    user_id1,
            String    title1,
            String    description1,
            String    type1,
            String    latitute1,
            String    longitute1,
            String    counry_id1,
            String    state_id1,
            String    city_id1,
            String    view_status1,
            String    can_likes1,
            String    can_comment1,
            String    category1,
            String    can_shared1,
            String    audio_id1 ) {

        RequestBody requestBody = RequestBody.create(MediaType.parse("video/mp4"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("video_clip", file.getName(), requestBody);

        RequestBody requestBody1 = RequestBody.create(MediaType.parse("image/jpeg"), fileTumb);
        MultipartBody.Part fileToUpload1 = MultipartBody.Part.createFormData("thumbfile", fileTumb.getName(), requestBody1);



        RequestBody user_id = RequestBody.create(
                MediaType.parse("text/plain"),
                user_id1);
        RequestBody title = RequestBody.create(
                MediaType.parse("text/plain"),
                title1);
        RequestBody description = RequestBody.create(
                MediaType.parse("text/plain"),
                description1);

        RequestBody type = RequestBody.create(
                MediaType.parse("text/plain"),
                type1);

        RequestBody latitute = RequestBody.create(
                MediaType.parse("text/plain"),
                latitute1);
        RequestBody longitute = RequestBody.create(
                MediaType.parse("text/plain"),
                longitute1);
        RequestBody counry_id = RequestBody.create(
                MediaType.parse("text/plain"),
                counry_id1);
        RequestBody state_id = RequestBody.create(
                MediaType.parse("text/plain"),
                state_id1);

        RequestBody city_id = RequestBody.create(
                MediaType.parse("text/plain"),
                city_id1);
        RequestBody view_status = RequestBody.create(
                MediaType.parse("text/plain"),
                view_status1);
        RequestBody can_likes = RequestBody.create(
                MediaType.parse("text/plain"),
                can_likes1);
        RequestBody can_comment = RequestBody.create(
                MediaType.parse("text/plain"),
                can_comment1);

        RequestBody category = RequestBody.create(
                MediaType.parse("text/plain"),
                category1);
        RequestBody can_shared = RequestBody.create(
                MediaType.parse("text/plain"),
                can_shared1);
        RequestBody audio_id = RequestBody.create(
                MediaType.parse("text/plain"),
                audio_id1);


        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("please wait...");
        dialog.show();

        retrofitInstance
                .getAPI()
                .Videoupload(
                user_id,
                fileToUpload,
                fileToUpload1,
                title,
                description,
                type,
                latitute,
                longitute,
                counry_id,
                state_id,
                city_id,
                view_status,
                can_likes,
                can_comment,
                category,
                can_shared,
                audio_id
        ).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                String data = response.body().toString();
                Log.e("data.. ", data) ;
                 dialog.dismiss();
                if (data != null && data!= null) {

                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        String status = jsonObject.getString("status");

                        if (status.equalsIgnoreCase("1")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("response");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjecjt1 = jsonArray.getJSONObject(i);
                                String catId = jsonObjecjt1.getString("id");
                                String category_name = jsonObjecjt1.getString("name");
                                Log.e("name ",  "onResponse: " + category_name );


                            }


                        }

                        //   loginView.postLoginCallBack(status,data,1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "something wrong please try again", Toast.LENGTH_LONG).show();


                        //  loginView.postLoginCallBack("0","something wrong please try again ",0);
                    }

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                 dialog.dismiss();
                Log.e("error", "onFailure: " +  t.toString() );
                Toast.makeText(getActivity(), "something wrong please try again", Toast.LENGTH_LONG).show();

//                        Log.e("error",t.toString());
//                        loginView.postLoginCallBack("0","something wrong please try again ",0);
            }
        });

//        call.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//
//                Log.e("response", "Getting response from server : " + response.raw().toString());
//
//                if (response.isSuccessful()) {
//                    dialog.dismiss();
//                    Log.d("response", "Getting response from server : " + response.body().toString());
//                    String getResponse = response.body().toString();
//                    try {
//                        JSONObject jsonObject = new JSONObject(getResponse);
//                        String message = jsonObject.getString("status");
//                        String responseMessage = jsonObject.getString("response");
//
//                        if (message.equals("1")) {
//                            dialog.dismiss();
//
//                            Toast.makeText(getActivity(), responseMessage,Toast.LENGTH_LONG).show();
//
//
//
//
//                        } else {
//                            Toast.makeText(getActivity(), responseMessage,Toast.LENGTH_LONG).show();
//                            dialog.dismiss();
//                            //  Toasty.error(CreateChannel.this, "Database Error", Toast.LENGTH_LONG, true).show();
//                        }
//                    } catch (JSONException e) {
//                        Log.e("error maeeage ", e.toString());
//                        e.printStackTrace();
//                    }
//
//                    Log.d("Gett", response.body().toString());
//                    Log.d("Gett", response.raw().toString());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                dialog.dismiss();
//                Log.e(" onFailure message .. " , t.toString()) ;
//
//            }
//
//        });


    }



    public void  CategoryDatalist() {
       // dialog.show();
        retrofitInstance
                .getAPI()
                .VideoCategory()
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        String data = response.body().toString();
                        Log.e("data.. ", data) ;
                       // dialog.dismiss();
                        if (data != null && data!= null) {

                            try {
                                JSONObject jsonObject = new JSONObject(data);
                                String status = jsonObject.getString("status");

                                if (status.equalsIgnoreCase("1")) {
                                    JSONArray jsonArray = jsonObject.getJSONArray("response");

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObjecjt1 = jsonArray.getJSONObject(i);
                                        String catId = jsonObjecjt1.getString("id");
                                        String category_name = jsonObjecjt1.getString("name");

                                        Log.e("name ",  "onResponse: " + category_name );


                                    }


                                }

                                //   loginView.postLoginCallBack(status,data,1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), "something wrong please try again", Toast.LENGTH_LONG).show();


                                //  loginView.postLoginCallBack("0","something wrong please try again ",0);
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                       // dialog.dismiss();
                        Toast.makeText(getActivity(), "something wrong please try again", Toast.LENGTH_LONG).show();

//                        Log.e("error",t.toString());
//                        loginView.postLoginCallBack("0","something wrong please try again ",0);
                    }
                });
    }
}