package media;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import fox.adds.Out;
import javazoom.jl.decoder.Equalizer.EQFunction;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;


public class Media extends EQFunction {
	private static Thread musicThread, backgThread;
	private static Map<String, File> musicMap = new LinkedHashMap<String, File>();
	private static Map<String, File> soundMap = new LinkedHashMap<String, File>();
	private static Map<String, File> voicesMap = new LinkedHashMap<String, File>();
	private static Map<String, File> backgMap = new LinkedHashMap<String, File>();
	
	private static JavaSoundAudioDevice auDevMusic;
	private static AdvancedPlayer musicPlayer;
	
	private static JavaSoundAudioDevice auDevBackg;
	private static AdvancedPlayer backgPlayer;
	
	private static AdvancedPlayer soundPlayer;
	private static AdvancedPlayer voicePlayer;
	
	private static boolean soundEnabled = true, musicEnabled = true, backgEnabled = true, voiceEnabled = true;
	
	private static float soundVolume;
	private static float musicVolume;
	private static float backgVolume;
	private static float voiceVolume;
	
	private static String lastMusic, lastBackg;
	
	
	public static void addSound(String name, File audioFile) {
//		Out.Print(Media.class, 1, "\nAdd sound '" + audioFile + "' in soundMap with name '" + name + "'...");
		soundMap.put(name, audioFile);
	}
	
	public static void loadSounds(File[] listFiles) {
		for (File file : listFiles) {
			if (file.isFile()) {addSound(file.getName().substring(0, file.getName().length() - 4), file);}
		}
	}
	
	public static void playSound(final String trackName) {
		if (!soundEnabled) return;
		
		if (soundMap.containsKey(trackName)) {
//			Out.Print("\nMedia: sound '" + trackName + "' exist in the soundMap.");
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					JavaSoundAudioDevice auDevSound = new JavaSoundAudioDevice();
					try (InputStream potok = new FileInputStream(soundMap.get(trackName))) {
						auDevSound.setLineGain(soundVolume);
						soundPlayer = new AdvancedPlayer(potok, auDevSound);
						PlaybackListener listener = new PlaybackListener() {
					        @Override public void playbackStarted(PlaybackEvent arg0) {}
					        @Override public void playbackFinished(PlaybackEvent event) {}
					    };
					    soundPlayer.setPlayBackListener(listener);
						if (potok != null) {soundPlayer.play();}
						
//						javafx.scene.media.Media hit = new javafx.scene.media.Media(musicMap.get(trackName).toURI().toString());
//				        musicPlayer = new MediaPlayer(hit);
//				        musicPlayer.setVolume(gVolume);
//				        musicPlayer.play();
					} catch (Exception err) {/* IGNORE AUDIO EXCEPTIONS */
					} finally {
						soundPlayer.close();
						auDevSound.close();
					}
				}
			}).start();
			
//			Out.Print("\nMedia: sound '" + trackName + "' playing now...");
		} else {
			Out.Print(Media.class, 3, "\nMedia: sound '" + trackName + "' is NOT exist in the soundMap");
			for (String sName : soundMap.keySet()) {
				System.out.println(sName + " (" + soundMap.get(sName) + ")");
			}
		}
	}
	
	
	public static void addMusic(String name, File audioFile) {musicMap.put(name, audioFile);}

	public static void loadMusics(File[] listFiles) {
		for (File file : listFiles) {
			addMusic(file.getName().substring(0, file.getName().length() - 4), file);
		}
	}
	
	public static void playMusic(final String trackName, Boolean rep) {
		if (!musicEnabled || trackName == null) {return;}
		lastMusic = trackName;
		
//		System.out.println("INCOME MUSIC: '" + trackName + "'");
		if (musicMap.containsKey(trackName)) {
			if (musicThread != null) {
				if (!musicThread.isInterrupted()) {stopMusic();}		    
			
				while (musicThread.isAlive()) {
					try {musicThread.join();} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
			
			musicThread = new Thread(new Runnable() {
				@Override
				public void run() {
//					System.out.println("NEXT MUSIC: '" + trackName + "'");
					if (auDevMusic != null && auDevMusic.isOpen()) {auDevMusic.close();}
					
					auDevMusic = new JavaSoundAudioDevice();
					try (InputStream potok = new FileInputStream(musicMap.get(trackName))) {
						auDevMusic.setLineGain(musicVolume);
						musicPlayer = new AdvancedPlayer(potok, auDevMusic);
						PlaybackListener listener = new PlaybackListener() {
					        @Override
					        public void playbackStarted(PlaybackEvent arg0) {
					        	System.out.println("Playback started..");
					        }

					        @Override
					        public void playbackFinished(PlaybackEvent event) {
					        	System.out.println("Playback finished..");
					        }
					    };
					    musicPlayer.setPlayBackListener(listener);					    
						musicPlayer.play(); 
					} catch (Exception err){err.printStackTrace();
					} finally {
						musicPlayer.close();	
						auDevMusic.close();
					}
					
//					System.out.println("CLOSING PREVIOUS MUS...");
				}
			});
			musicThread.start();		
		
			Out.Print("Media: music: the '" + trackName + "' exist into musicMap and play now...");
		} else {
			Out.Print("\nMedia: music: music '" + trackName + "' is NOT exist in the musicMap");
			for (String musName : musicMap.keySet()) {
				System.out.println(musName + " (" + musicMap.get(musName) + ")");
			}
			throw new RuntimeException("Media: music: music '" + trackName + "' is NOT exist in the musicMap");
		}
	}
	
	public static void stopMusic() {
		if (musicPlayer == null) {return;}
		
		try{musicPlayer.stop();} catch(Exception a) {/* IGNORE STOPPED ALREADY */}
		try{musicPlayer.close();} catch(Exception a) {/* IGNORE CLOSED ALREADY */}
		musicThread.interrupt();
	}
	
	
	public static void addBackg(String name, File audioFile) {backgMap.put(name, audioFile);}
	
	public static void playBackg(final String trackName) {
		if (!backgEnabled || trackName == null) {return;}
		lastBackg = trackName;

		if (backgMap.containsKey(trackName)) {
			if (backgThread != null) {
				if (!backgThread.isInterrupted()) {stopBackg();}		    
			
				while (backgThread.isAlive()) {
					try {backgThread.join();} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
			
			backgThread = new Thread(new Runnable() {
				@Override
				public void run() {
//					System.out.println("NEXT BACKG: '" + trackName + "'");
					if (auDevBackg != null && auDevBackg.isOpen()) {auDevBackg.close();}
					
					auDevBackg = new JavaSoundAudioDevice();					
					try (InputStream potok = new FileInputStream(backgMap.get(trackName))) {
						auDevBackg.setLineGain(backgVolume);
						backgPlayer = new AdvancedPlayer(potok, auDevBackg);
						PlaybackListener listener = new PlaybackListener() {
					        @Override
					        public void playbackStarted(PlaybackEvent arg0) {
					        	System.out.println("JavaSoundAudioDevice auDevBackg started..");
					        }

					        @Override
					        public void playbackFinished(PlaybackEvent event) {
					        	System.out.println("JavaSoundAudioDevice auDevBackg finished..");
					        }
					    };
					    backgPlayer.setPlayBackListener(listener);					    
						backgPlayer.play();
					} catch (Exception err){err.printStackTrace();
					} finally {
						backgPlayer.close();	
						auDevBackg.close();
					}
					
//					System.out.println("CLOSING PREVIOUS MUS...");
				}
			});
			backgThread.start();		
		
			Out.Print("Media: backg: the '" + trackName + "' exist into backgMap and play now...");
		} else {Out.Print("Media: backg: '" + trackName + "' is NOT exist in the backgMap");}
	}

	public static void loadBackgs(File[] listFiles) {
		for (File file : listFiles) {
			addBackg(file.getName().substring(0, file.getName().length() - 4), file);
		}
	}

	public static void stopBackg() {
		if (backgPlayer == null) {return;}

		try{backgPlayer.stop();} catch(Exception a) {/* IGNORE STOPPED ALREADY */}
		try{backgPlayer.close();} catch(Exception a) {/* IGNORE CLOSED ALREADY */}
		backgThread.interrupt();
	}
	
	
	public static void addVoice(String name, File audioFile) {voicesMap.put(name, audioFile);}
	
	public static void playVoice(final String trackName) {
		if (!voiceEnabled || trackName == null) {return;}
		
		if (voicesMap.containsKey(trackName)) {
	        new Thread(new Runnable() {
				@Override
				public void run() {
					JavaSoundAudioDevice auDev = new JavaSoundAudioDevice();
					try (InputStream potok = new FileInputStream(voicesMap.get(trackName))) {
						voicePlayer = new AdvancedPlayer(potok, auDev);
						 
						 if (auDev instanceof JavaSoundAudioDevice) {
								JavaSoundAudioDevice jsAudio = (JavaSoundAudioDevice) auDev;
							    jsAudio.setLineGain(voiceVolume);
						 }
						 
						 voicePlayer.play();
					} catch (Exception err) {err.printStackTrace();
					} finally {
						voicePlayer.close();
						auDev.close();
					}
				}
			}).start();
		
			Out.Print("Media: voice: the '" + trackName + "' exist into voiceMap and play now...");
		} else {
			Out.Print("\nMedia: voice '" + trackName + "' is NOT exist in the voiceMap");
			for (String vName : voicesMap.keySet()) {
				System.out.println(vName + " (" + voicesMap.get(vName) + ")");
			}
		}
	}
	
	public static void loadVoices(File[] listFiles) {
		for (File file : listFiles) {
			addVoice(file.getName().substring(0, file.getName().length() - 4), file);
		}
	}
	
	
	public static void setSoundEnabled(Boolean _soundEnabled) {
		soundEnabled = _soundEnabled;
		if (!soundEnabled && soundPlayer != null) {
			try{soundPlayer.stop();} catch(Exception a) {/* IGNORE STOPPED ALREADY */}
		}
	}
	public static Boolean getSoundEnabled() {return soundEnabled;}

	public static void setMusicEnabled(Boolean _musicEnabled) {
		musicEnabled = _musicEnabled;		
		if (!musicEnabled && musicPlayer != null) {stopMusic();
		} else {playMusic(lastMusic, true);}
	}
	public static Boolean getMusicEnabled() {return musicEnabled;}

	public static void setBackgEnabled(Boolean _backgEnabled) {
		backgEnabled = _backgEnabled;		
		if (!backgEnabled && backgPlayer != null) {stopBackg();
		} else {playBackg(lastBackg);}
	}
	
	public static void setVoiceEnabled(Boolean _voiceEnabled) {
		voiceEnabled = _voiceEnabled;		
		if (!voiceEnabled && voicePlayer != null) {
			try{voicePlayer.stop();} catch(Exception a) {/* IGNORE STOPPED ALREADY */}
		}
	}
	
	
	public static void setMusicVolume(float d) {
		musicVolume = (float) (Math.log(d) / Math.log(2) * 6.0f);
		if (auDevMusic != null) {
			auDevMusic.setLineGain(musicVolume);
		}
	}
	public static void setBackgVolume(float d) {
		backgVolume = (float) (Math.log(d) / Math.log(2) * 6.0f);
		if (auDevBackg != null) {
			auDevBackg.setLineGain(backgVolume);
		} else {System.out.println("Громкость бэкграунда не может быть выставлена пока auDevBackg = NULL, but backgVolume setts up to " + backgVolume);}
	}
	public static void setSoundVolume(float d) {
		soundVolume = (float) (Math.log(d) / Math.log(2) * 6.0f);
	}
	public static void setVoiceVolume(float d) {
		voiceVolume = (float) (Math.log(d) / Math.log(2) * 6.0f);
	}
	
	public static float getMusicVolume() {return musicVolume;}
	public static float getBackgVolume() {return backgVolume;}
	public static float getSoundVolume() {return soundVolume;}
	public static float getVoiceVolume() {return voiceVolume;}
	
	public static float getGlobalMusVolume(int band) {return (float) (Math.log(musicVolume) / Math.log(2) * 6.0f);}
}