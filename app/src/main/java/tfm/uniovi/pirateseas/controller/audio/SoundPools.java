package tfm.uniovi.pirateseas.controller.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multi SoundPool to prevent memory error.
 */
public class SoundPools {
	private static final String TAG = "SoundPools";

	private static final int MAX_STREAMS_PER_POOL = 15;

	private List<SoundPoolContainer> containers;

	/**
	 * Constructor
	 */
	public SoundPools() {
		containers = Collections
				.synchronizedList(new ArrayList<SoundPoolContainer>());
	}

	/**
	 * Method to register a new sound resource on the available pool
	 * @param context
	 * @param soundId New sound resource
	 * @param id Sound tag id
	 */
	public void loadSound(Context context, String soundId, int id) {
		Log.d(TAG, "SouldPools load sound " + soundId);
		try {
			for (SoundPoolContainer container : containers) {
				if (container.contains(soundId)) {
					return;
				}
			}
			for (SoundPoolContainer container : containers) {
				if (!container.isFull()) {
					container.load(context, soundId, id);
					return;
				}
			}
			SoundPoolContainer container = new SoundPoolContainer();
			containers.add(container);
			container.load(context, soundId ,id);
		} catch (Exception e) {
			Log.w(TAG, "Load sound error", e);
		}
	}

	/**
	 * Play the selected sound
	 * @param context The Context
	 * @param soundId   Sound resource
	 * @param id	Sound tag id
	 */
	void playSound(Context context, String soundId, Integer id) {
		Log.d(TAG, "SouldPools play sound " + soundId);
		try {
			for (SoundPoolContainer container : containers) {
				if (container.contains(soundId)) {
					container.play(context, soundId, id);
					return;
				}
			}
			for (SoundPoolContainer container : containers) {
				if (!container.isFull()) {
					container.play(context, soundId, id);
					return;
				}
			}
			SoundPoolContainer container = new SoundPoolContainer();
			containers.add(container);

			container.play(context, soundId, id);
		} catch (Exception e) {
			Log.w(TAG, "Play sound error for id:" + soundId, e);
		}
	}

	/**
	 * Pause the current sound
	 */
	public void onPause() {
		for (SoundPoolContainer container : containers) {
			container.onPause();
		}
	}

	/**
	 * Resume the current sound
	 */
	public void onResume() {
		for (SoundPoolContainer container : containers) {
			container.onResume();
		}
	}

	/**
	 * Sound pool inner class
	 */
	private static class SoundPoolContainer {
		SoundPool soundPool;
		Map<String, Integer> soundMap;
		AtomicInteger size;

		@SuppressLint("NewApi")
		/**
		 * Constructor
		 */
		public SoundPoolContainer() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder()
						.setLegacyStreamType(android.media.AudioManager.STREAM_MUSIC)
						.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA);
				SoundPool.Builder spBuilder = new SoundPool.Builder().setMaxStreams(MAX_STREAMS_PER_POOL)
						.setAudioAttributes(attrBuilder.build());
				this.soundPool = spBuilder.build();
			} else {
				this.soundPool = new SoundPool(MAX_STREAMS_PER_POOL, android.media.AudioManager.STREAM_MUSIC, 0);
			}
			this.soundMap = new ConcurrentHashMap<String, Integer>(
					MAX_STREAMS_PER_POOL);
			this.size = new AtomicInteger(0);
		}

		/**
		 * Load the selected sound resource into the sound pool
		 * @param context
		 * @param soundId Sound resource
		 * @param id	Sound tag id
		 */
		public void load(Context context, String soundId, int id) {
			try {
				if(soundPool != null){
					this.size.incrementAndGet();
					soundMap.put(soundId, soundPool.load(context, id, 1));
				}
			} catch (Exception e) {
				this.size.decrementAndGet();
				Log.w(TAG, "Load sound error" + e.getMessage());
			}
		}

		/**
		 * Play the selected sound
		 * @param context The context
		 * @param sound	Sound tag id
		 * @param id	Sound resource id
		 */
		void play(Context context, String sound, Integer id) {
			android.media.AudioManager audioManager = (android.media.AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			final int streamVolume = audioManager
					.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
			Integer soundId = soundMap.get(sound);
			if (soundId == null) {
				soundPool
						.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
							@Override
							public void onLoadComplete(SoundPool soundPool,
									int sampleId, int status) {
								soundPool.play(sampleId, streamVolume,
										streamVolume, 1, 0, 1f);
							}
						});
				this.size.incrementAndGet();
				soundPool.load(context,id, 1);
			} else {
				try {
					soundPool.play(soundId, streamVolume, streamVolume, 1, 0,
							1f);
				} catch (Exception e) {
					Log.w(TAG, "Play sound error", e);
				}
			}
		}

		/**
		 * Check if the pool contains the selected sound
		 * @param id the sound id
		 * @return whether the sound is registered on the soundMap or not
		 */
		boolean contains(String id) {
			return soundMap.containsKey(id);
		}

		/**
		 * Checks if the pool is full
		 * @return Whether the sound poll is full or not
		 */
		@SuppressWarnings("BooleanMethodIsAlwaysInverted")
		boolean isFull() {
			return this.size.get() >= MAX_STREAMS_PER_POOL;
		}

		/**
		 * Pause the current sound
		 */
		public void onPause() {
			try {
				soundPool.autoPause();
			} catch (Exception e) {
				Log.w(TAG, "Pause SoundPool error", e);
			}
		}

		/**
		 * Resume the current sound
		 */
		public void onResume() {
			try {
				soundPool.autoResume();
			} catch (Exception e) {
				Log.w(TAG, "Resume SoundPool error", e);
			}
		}
	}

}
