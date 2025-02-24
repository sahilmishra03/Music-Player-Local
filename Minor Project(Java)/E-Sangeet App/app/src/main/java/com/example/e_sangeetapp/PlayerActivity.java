package com.example.e_sangeetapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    ImageButton btnPlay, btnNext, btnPrev, btnFF, btnFR;
    TextView txtSongName, txtStart, txtStop;
    SeekBar seekBar;
    ImageView imageView;
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Handler seekBarHandler = new Handler();
    Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Now Playing");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        initializeMediaPlayer();

        btnPlay.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNextSong());
        btnPrev.setOnClickListener(v -> playPreviousSong());
        btnFF.setOnClickListener(v -> fastForward());
        btnFR.setOnClickListener(v -> rewind());

        mediaPlayer.setOnCompletionListener(mp -> playNextSong());
    }

    private void initializeViews() {
        btnPrev = findViewById(R.id.btnprev);
        btnNext = findViewById(R.id.btnnext);
        btnPlay = findViewById(R.id.playbtn);
        btnFF = findViewById(R.id.btnff);
        btnFR = findViewById(R.id.btnfr);
        txtSongName = findViewById(R.id.txtsn);
        txtStart = findViewById(R.id.txtsstart);
        txtStop = findViewById(R.id.txtsstop);
        seekBar = findViewById(R.id.seekbar);
        imageView = findViewById(R.id.imgView);

        txtSongName.setSelected(true); // Enable marquee effect
    }

    private void initializeMediaPlayer() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        if (mySongs == null || mySongs.isEmpty()) return;

        position = bundle.getInt("pos", 0);

        playSong();

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    txtStart.setText(createTime(mediaPlayer.getCurrentPosition()));
                    seekBarHandler.postDelayed(this, 1000);
                }
            }
        };
        seekBarHandler.post(updateSeekBar);

        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.main), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(ContextCompat.getColor(this, R.color.main), PorterDuff.Mode.SRC_IN);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHandler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarHandler.post(updateSeekBar);
            }
        });
    }

    private void playSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Uri uri = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        if (mediaPlayer != null) {
            mediaPlayer.start();

            txtSongName.setText(mySongs.get(position).getName());
            txtStop.setText(createTime(mediaPlayer.getDuration()));
            btnPlay.setBackgroundResource(R.drawable.baseline_pause_24);
            startAnimation(imageView);

            seekBar.setMax(mediaPlayer.getDuration());
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                btnPlay.setBackgroundResource(R.drawable.baseline_play_arrow_24);
                mediaPlayer.pause();
            } else {
                btnPlay.setBackgroundResource(R.drawable.baseline_pause_24);
                mediaPlayer.start();
            }
        }
    }

    private void playNextSong() {
        position = (position + 1) % mySongs.size();
        playSong();
    }

    private void playPreviousSong() {
        position = (position - 1 < 0) ? (mySongs.size() - 1) : (position - 1);
        playSong();
    }

    private void fastForward() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int newTime = mediaPlayer.getCurrentPosition() + 10000; // 10 seconds
            mediaPlayer.seekTo(Math.min(newTime, mediaPlayer.getDuration()));
        }
    }

    private void rewind() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int newTime = mediaPlayer.getCurrentPosition() - 10000; // 10 seconds
            mediaPlayer.seekTo(Math.max(newTime, 0));
        }
    }

    public void startAnimation(View view) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        rotation.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotation);
        animatorSet.start();
    }

    public String createTime(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        return String.format("%d:%02d", min, sec);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        seekBarHandler.removeCallbacks(updateSeekBar);
        super.onDestroy();
    }
}
