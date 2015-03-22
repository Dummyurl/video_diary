package com.mti.videodiary.fragment;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gc.materialdesign.views.ButtonFloat;
import com.mti.videodiary.activity.BaseActivity;
import com.mti.videodiary.activity.CreateVideoNoteActivity;
import com.mti.videodiary.activity.MenuActivity;
import com.mti.videodiary.adapter.VideoAdapter;
import com.mti.videodiary.application.VideoDiaryApplication;
import com.mti.videodiary.data.DataBaseManager;
import com.mti.videodiary.data.dao.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import mti.com.videodiary.R;

import static android.view.View.OnClickListener;

/**
 * Created by Taras Matolinets on 23.02.15.
 */
public class VideoFragment extends BaseFragment implements OnClickListener, SearchView.OnQueryTextListener {
    private static final int REQUEST_VIDEO_CAPTURE = 101;

    public static final String KEY_POSITION = "com.mti.position.key";
    public static final String FILE_FORMAT = ".mp4";
    public static final String UPDATE_ADAPTER_INTENT = "com.mti.video.dairy.update.adapter";
    public static String VIDEO_FILE_NAME = File.separator + "video-dairy" + FILE_FORMAT;
    public static final String KEY_VIDEO_PATH = "com.mti.video-dairy.key-video-file-path";

    private View mView;
    private RecyclerView mRecyclerView;
    private VideoAdapter mAdapter;
    private StaggeredGridLayoutManager mLayoutManager;
    private ButtonFloat mButtonFloat;
    private ImageView mIvCameraOff;
    private TextView mTvNoRecords;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataBaseManager.init(getActivity());

        setHasOptionsMenu(true);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver,
                new IntentFilter(UPDATE_ADAPTER_INTENT));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showEmptyView();
        }
    };


    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_video, container, false);

        initViews();
        setupRecycleView();
        initListeners();
        showEmptyView();

        checkCamera();

        return mView;
    }

    private void showEmptyView() {
        final List<Video> listVideos = DataBaseManager.getInstance().getAllVideosList();

        if (listVideos.isEmpty()) {
            mIvCameraOff.setVisibility(View.VISIBLE);
            mTvNoRecords.setVisibility(View.VISIBLE);

            YoYo.AnimationComposer personalAnim = YoYo.with(Techniques.ZoomIn);
            personalAnim.duration(DURATION);
            personalAnim.playOn(mIvCameraOff);
            personalAnim.playOn(mTvNoRecords);

        } else {
            mIvCameraOff.setVisibility(View.GONE);
            mTvNoRecords.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        mIvCameraOff = (ImageView) mView.findViewById(R.id.ivCameraOff);
        mTvNoRecords = (TextView) mView.findViewById(R.id.tvNoRecords);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.videoRecycleView);

        mButtonFloat = (ButtonFloat) mView.findViewById(R.id.buttonFloat);
    }

    private void initListeners() {
        mButtonFloat.setOnClickListener(this);
    }

    private void setupRecycleView() {
        final List<Video> listVideos = DataBaseManager.getInstance().getAllVideosList();

        mRecyclerView.setHasFixedSize(true);

        Display display = ((WindowManager) getActivity().getSystemService(MenuActivity.WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getOrientation() == Surface.ROTATION_90)
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        else
            mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        mAdapter = new VideoAdapter(getActivity(), listVideos);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void checkCamera() {
        if (!hasCamera())
            mButtonFloat.setEnabled(false);
        else
            Crouton.makeText(getActivity(), R.string.fragment_broken_camera_warning, Style.ALERT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mLayoutManager.setSpanCount(2);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager.setSpanCount(1);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_action_bar_video, menu);

        SearchManager manager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();

        search.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));
        search.setOnQueryTextListener(this);
    }

    private boolean hasCamera() {
        if (getActivity().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonFloat:
                final File mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + BaseActivity.APPLICATION_DIRECTORY + File.separator + BaseActivity.VIDEO_DIR + VIDEO_FILE_NAME);

                Uri fileUri = Uri.fromFile(mediaFile);

                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

                startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_VIDEO_CAPTURE:
                if (resultCode == getActivity().RESULT_OK) {
                    startVideoActivity(data);
                } else if (resultCode == getActivity().RESULT_CANCELED) {
                    Log.w(VideoDiaryApplication.TAG, "Video recording cancelled.");
                } else {
                    Log.e(VideoDiaryApplication.TAG, "Failed to record video");
                }
                break;

            case MenuActivity.UPDATE_VIDEO_ADAPTER:
                List<Video> listVideo = DataBaseManager.getInstance().getAllVideosList();

                mAdapter.setListVideos(listVideo);
                mAdapter.notifyDataSetChanged();

                showEmptyView();
                break;
        }
    }

    private void startVideoActivity(Intent data) {
        final Uri videoUri = data.getData();
        String videoFilePath = videoUri.getPath();

        Intent intent = new Intent(getActivity(), CreateVideoNoteActivity.class);
        intent.putExtra(KEY_VIDEO_PATH, videoFilePath);

        startActivityForResult(intent, MenuActivity.UPDATE_VIDEO_ADAPTER);

        Log.i(VideoDiaryApplication.TAG, "Video has been saved to: " + data.getData());
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        List<Video> listVideo = DataBaseManager.getInstance().getAllVideosList();

        ArrayList<Video> searchVideoList = new ArrayList<Video>();
        for (Video v : listVideo) {
            if (v.getTitle().contains(s))
                searchVideoList.add(v);
        }

        if (!searchVideoList.isEmpty())
            mAdapter.setListVideos(searchVideoList);

        mAdapter.notifyDataSetChanged();

        return true;
    }
}
