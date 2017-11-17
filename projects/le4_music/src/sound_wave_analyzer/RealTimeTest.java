package sound_wave_analyzer;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioSystem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jp.ac.kyoto_u.kuis.le4music.Player;
import jp.ac.kyoto_u.kuis.le4music.Recorder;

public class RealTimeTest extends Application{

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		/*
		FlowPane pane = new FlowPane();
		
		Info[] mixer = AudioSystem.getMixerInfo();
		for(Info info : mixer) {
			pane.getChildren().add(new Label(info.toString()));
		}
		Scene scene = new Scene(pane,400,400);
		primaryStage.setScene(scene);
		primaryStage.show();
		*/
		
		BorderPane pane = new BorderPane();
		ListView<String> view1 = new ListView<>();
		ListView<String> view2 = new ListView<>();
		
		pane.setLeft(view1);
		pane.setRight(view2);
		
		view1.getItems().add("player");
		view2.getItems().add("recorder");
		
		Arrays.stream(AudioSystem.getMixerInfo()).forEach(t -> System.out.println(">>>"+t));
		
	
		Recorder recorder = Recorder.builder()
                .mixer(AudioSystem.getMixerInfo()[0])
                .daemon()
                .build();
		 recorder.addAudioFrameListener((frame, position) -> {
			 final double rms = Arrays.stream(frame).map(x -> x * x).average().orElse(0.0);
			 final double logRms = 20.0 * Math.log10(rms);
			 final double posInSec = position / recorder.getSampleRate();
			 System.out.printf("[rec]Pos %d (%.2f sec), RMS %f dB (frame %d)%n", position, posInSec, logRms, frame.length);
			 view2.getItems().add(posInSec+": "+logRms);
		 });
		
		 File file = new File("test.wav");
		 Player player = Player.builder(file)
		                       .mixer(AudioSystem.getMixerInfo()[0])
		                       .daemon()
		                       .build();
		 player.addAudioFrameListener((frame, position) -> {
		   final double rms = Arrays.stream(frame).map(x -> x * x).average().orElse(0.0);
		   final double logRms = 20.0 * Math.log10(rms);
		   final double posInSec = position / player.getSampleRate();
		   if(posInSec > 9.99) recorder.stop();
		   System.out.printf("[play]Pos %d (%.3f s), RMS %f dB (frame %d)%n", position, posInSec, logRms, frame.length);
		   view1.getItems().add(posInSec+": "+logRms);
		 });
		 
		Thread.sleep(5000);
		 
		recorder.start();
		player.start();
		 
		Scene scene = new Scene(pane,600,600);
		primaryStage.setScene(scene);
		primaryStage.show();
		 
	}

}
