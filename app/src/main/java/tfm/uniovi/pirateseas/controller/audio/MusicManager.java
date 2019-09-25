package tfm.uniovi.pirateseas.controller.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * MusicManager class to handler the MediaPlayer object
 */
@SuppressLint("UseSparseArrays")
public class MusicManager{
	private static final String TAG = "MusicManager";
	
	// Sound Ids
	public final static int SOUND_SHOT_FIRED = 0x00;
	public final static int SOUND_SHOT_HIT = 0x01;
	public final static int SOUND_SHOT_MISSED = 0x02;
	public final static int SOUND_SHOT_RELOADING = 0x03;
	public final static int SOUND_SHOT_EXPLOSION = 0x04;
	public final static int SOUND_WEATHER_FOG = 0x05;
	public final static int SOUND_WEATHER_STORM = 0x06;
	public final static int SOUND_WEATHER_MAELSTROM = 0x07;
	public final static int SOUND_ENEMY_APPEAR = 0x08;
	public final static int SOUND_GOLD_GAINED = 0x09;
	public final static int SOUND_GOLD_SPENT = 0x0A;
	public final static int SOUND_XP_GAINED = 0x0B;
	public final static int MUSIC_GAME_PAUSED = 0x0C;
	public final static int MUSIC_BATTLE = 0x0D;
	public final static int MUSIC_ISLAND = 0x0E;
	public final static int MUSIC_GAME_MENU = 0x0F;
    public static final int MUSIC_GAME_OVER = 0x10;

    // Sounds
	private HashMap<Integer,Integer> mSoundKeys;
	private SoundPools mSoundPools;
	private AudioManager  mAudioManager;
	private Context mContext;

	// Music
	private MediaPlayer mBackgroundMusic;
	private MediaPlayerState mState;
	private Integer activeSongResource;
	
	private static MusicManager mInstance = null;

	private static final MediaPlayerState[] ANY_VALID_STATE = {MediaPlayerState.IDLE, MediaPlayerState.END, MediaPlayerState.ERROR, MediaPlayerState.INITIALIZED, MediaPlayerState.PREPARING, MediaPlayerState.PREPARED, MediaPlayerState.STARTED, MediaPlayerState.STOPPED, MediaPlayerState.PAUSED, MediaPlayerState.COMPLETED};
	private static final MediaPlayerState[] RESET_VALID_STATES = {MediaPlayerState.IDLE, MediaPlayerState.ERROR, MediaPlayerState.INITIALIZED, MediaPlayerState.PREPARED, MediaPlayerState.STARTED, MediaPlayerState.STOPPED, MediaPlayerState.PAUSED, MediaPlayerState.COMPLETED};
	private static final MediaPlayerState[] OPTIONS_VALID_STATES = {MediaPlayerState.IDLE, MediaPlayerState.INITIALIZED, MediaPlayerState.PREPARED, MediaPlayerState.STARTED, MediaPlayerState.STOPPED, MediaPlayerState.PAUSED, MediaPlayerState.COMPLETED};
	private static final MediaPlayerState[] STOP_VALID_STATES = {MediaPlayerState.PREPARED, MediaPlayerState.STARTED, MediaPlayerState.STOPPED, MediaPlayerState.PAUSED, MediaPlayerState.COMPLETED};
	private static final MediaPlayerState[] START_VALID_STATES = {MediaPlayerState.PREPARED, MediaPlayerState.STARTED, MediaPlayerState.PAUSED, MediaPlayerState.COMPLETED};
	private static final MediaPlayerState[] PAUSE_VALID_STATES = {MediaPlayerState.STARTED, MediaPlayerState.PAUSED, MediaPlayerState.COMPLETED};
	private static final MediaPlayerState[] ASYNC_PREP_VALID_STATES = {MediaPlayerState.INITIALIZED, MediaPlayerState.STOPPED};
	private static final MediaPlayerState[] SET_SOURCE_VALID_STATE = {MediaPlayerState.IDLE};

	/**
	 * Method to get the MusicInstance to start music tracks
	 * @param context Context
	 * @param backgroundMusicId Music resource id
	 * @return MusicManager instance
	 */
	public static MusicManager getInstance(Context context,
			int backgroundMusicId) {
		if (mInstance == null) {
			mInstance = new MusicManager();
		}
		if (mInstance.mBackgroundMusic == null || Arrays.asList(OPTIONS_VALID_STATES).contains(mInstance.mState) && !mInstance.mBackgroundMusic.isPlaying()){
			mInstance.initSounds(context, backgroundMusicId);
		}
		return mInstance;
	}

	/**
	 * Method to get the MusicInstance used for register music tracks and sounds
	 * @param context Context
	 * @return MusicManager instance
	 */
	public static MusicManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new MusicManager();
		}
		if (mInstance.mAudioManager == null){
			mInstance.init(context);
		}
		return mInstance;
	}

	/**
	 * Method to get the MusicInstance used to manage the music of the Activities
	 * @return MusicManager instance
	 */
	public static MusicManager getInstance() {
		synchronized (MusicManager.class) {
			if (mInstance == null) {
				mInstance = new MusicManager();
			}
			return mInstance;
		}
	}

	/**
	 * Empty constructor
	 */
	private MusicManager(){}

	/**
	 * Initialization method for the MusicManager instance
	 * @param context Context
	 */
	private void init(Context context) {
		mSoundPools = new SoundPools();
		mSoundKeys = new HashMap<>();
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}


	/**
	 * Starts the selected song as background music
 	 * @param context Context
	 * @param backgroundMusicId Music resource id
	 */
	private void initSounds(Context context, int backgroundMusicId) {
		this.mContext = context;
		this.activeSongResource = mSoundKeys.get(backgroundMusicId);
		if(activeSongResource == null)
			initSounds(context, backgroundMusicId);
		
		if(mAudioManager == null)
			init(mContext);
		
		mBackgroundMusic = MediaPlayer.create(context, activeSongResource);
		setState(MediaPlayerState.PREPARED);
		mBackgroundMusic.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				setState(MediaPlayerState.PREPARED);
				mp.start();
				setState(MediaPlayerState.STARTED);
			}
		});
		mBackgroundMusic.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
				setState(MediaPlayerState.ERROR);
				resetPlayer();
				return true;
			}
		});

		float dv = getDeviceVolume();
		if(Arrays.asList(OPTIONS_VALID_STATES).contains(mInstance.mState)) {
			mBackgroundMusic.setLooping(true);
			mBackgroundMusic.setVolume(dv, dv);
		}
	}

	/**
	 * Register a new sound resource
	 * @param soundId Sound id
	 * @param soundResource Sound resource
	 */
	public void registerSound(int soundId, int soundResource) {
		mSoundKeys.put(soundId, soundResource);
		mSoundPools.loadSound(mContext, String.valueOf(soundId), soundResource);
	}

	/**
	 * Starts playing the last selected song
	 */
	public void playBackgroundMusic() {
		try{
			if (mBackgroundMusic == null){
				mInstance.initSounds(mContext, MUSIC_GAME_MENU);
			}
			if(mBackgroundMusic!= null && Arrays.asList(OPTIONS_VALID_STATES).contains(mInstance.mState) && !mBackgroundMusic.isPlaying()){
					try {
						if(Arrays.asList(START_VALID_STATES).contains(mInstance.mState)) {
							mBackgroundMusic.start();
							setState(MediaPlayerState.STARTED);
						}
					} catch (IllegalStateException e){
						Log.e(TAG, e.getMessage());
					}
			}
    	}catch(IllegalStateException e){
    		Log.e(TAG, e.getMessage());
    	}
	}

	/**
	 * Pauses the selected song
	 */
	public void pauseBackgroundMusic(){
		if(mBackgroundMusic!= null && Arrays.asList(OPTIONS_VALID_STATES).contains(mInstance.mState) && mBackgroundMusic.isPlaying() && Arrays.asList(PAUSE_VALID_STATES).contains(mInstance.mState)){
			mBackgroundMusic.pause();
			setState(MediaPlayerState.PAUSED);
		}
	}

	public void changeSong(@NonNull Context context, int newSongResource) {
		activeSongResource = mSoundKeys.get(newSongResource);
		if(activeSongResource == null)
			return;
		mBackgroundMusic.reset();
		setState(MediaPlayerState.IDLE);
		mBackgroundMusic = MediaPlayer.create(context, activeSongResource);
		setState(MediaPlayerState.PREPARED);
		mBackgroundMusic.start();
		setState(MediaPlayerState.STARTED);
	}

	/**
	 * Stops the selected song
	 */
	public void stopBackgroundMusic(){
		if(mBackgroundMusic!= null && Arrays.asList(OPTIONS_VALID_STATES).contains(mInstance.mState) && mBackgroundMusic.isPlaying() && Arrays.asList(STOP_VALID_STATES).contains(mInstance.mState)){
			mBackgroundMusic.stop();
			setState(MediaPlayerState.STOPPED);
			mBackgroundMusic.prepareAsync();
			setState(MediaPlayerState.PREPARING);
		}
	}

	/**
	 * Resets the MusicManager state to the initial one
	 */
	public void resetPlayer(){
		if(mBackgroundMusic!=null){
			try {
				if(Arrays.asList(RESET_VALID_STATES).contains(mInstance.mState)) {
					mBackgroundMusic.reset();
					setState(MediaPlayerState.IDLE);
					mBackgroundMusic.setDataSource(mContext, Uri.parse("android.resource://tfm.uniovi.pirateseas/raw/" + mContext.getResources().getResourceName(activeSongResource)));
					setState(MediaPlayerState.INITIALIZED);
					mBackgroundMusic.prepareAsync();
					setState(MediaPlayerState.PREPARING);
				}
			} catch(IllegalStateException e){
				Log.e(TAG, "MusicManager has not the resources yet/anymore");
			} catch (IOException e) {
				Log.e(TAG, "File path not found");
			}
		}
	}

	/**
	 * Plays the selected sounds resource once
	 * @param index Sound resource id
	 */
	public void playSound (int index) {
		Integer resourceId = mSoundKeys.get(index);
		mSoundPools.playSound(mContext, String.valueOf(index), resourceId);
	}

	/**
	 * Return the device music volume
	 * @return Device music volume
	 */
	public float getDeviceVolume(){
		float mCurrVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		return (mCurrVolume / mMaxVolume) * 100;
	}

	/**
	 * Set the device volume with 'volumeValue'
	 * @param volumeValue New device volume value
	 */
	public void setDeviceVolume(float volumeValue){		
		float mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int streamedVolume = (int) ((volumeValue * mMaxVolume) / 100);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamedVolume, 0);
	}

	/**
	 * Release the MusicManager resources
	 */
	public void releaseResources(){
		if(mBackgroundMusic!=null && Arrays.asList(ANY_VALID_STATE).contains(mInstance.mState) && !mBackgroundMusic.isPlaying()){
			mBackgroundMusic.release();
			setState(MediaPlayerState.END);
			mSoundKeys.clear();
		}
	}

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isPlaying() {
		if(mBackgroundMusic == null || !Arrays.asList(OPTIONS_VALID_STATES).contains(mInstance.mState))
			return false;
		return mBackgroundMusic.isPlaying();
    }

    public boolean isLoaded() {
		return mBackgroundMusic != null;
	}

	public boolean hasRegisteredSounds() {
		return mSoundKeys!=null && !mSoundKeys.isEmpty();
	}

	private enum MediaPlayerState {
		IDLE,
		END,
		ERROR,
		INITIALIZED,
		PREPARING,
		PREPARED,
		STARTED,
		STOPPED,
		PAUSED,
		COMPLETED
	}

	public MediaPlayerState getState() {
		return mState;
	}

	private void setState(MediaPlayerState state){
		Log.d(TAG, "MediaState: " + this.mState + " > " + state);
		this.mState = state;
	}


}
