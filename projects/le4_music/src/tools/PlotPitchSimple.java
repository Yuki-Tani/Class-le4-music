package tools;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

public class PlotPitchSimple extends Application{
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// コマンド引数
		final String[] args = getParameters().getRaw().toArray(new String[0]);
		if (args.length < 1) {
			System.out.println("WAVEFILE is not given.");
			Platform.exit();
			return;
		}
		final File wavFile = new File(args[0]);

		// waveファイル読み込み
		final AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
		final double[] wave = Le4MusicUtils.readWaveformMonaural(stream);
		final AudioFormat format = stream.getFormat();
		final double sampleRate = format.getSampleRate();
		stream.close();
		
		final double frameDuration = Le4MusicUtils.frameDuration;
		final double shiftDuration = frameDuration / 8.0;
		final int frameSize = (int) Math.round(frameDuration * sampleRate);
		final int shiftSize = (int) Math.round(shiftDuration * sampleRate);

		/*
		double[] miniWave = new double[(int)sampleRate/2];
		for(int i=0;i<miniWave.length;i++) {
			miniWave[i] = wave[i+(int)sampleRate/2];
		}
		// pitch算出
		double pitch = TransformTools.getPitch(miniWave, sampleRate);
		System.out.println("PITCH :: "+pitch);
		*/
		
		double[] pitch = TransformTools.toPitch(wave, sampleRate, frameSize, shiftSize);
		
		// チャート作成
		String title = ChartTools.requestName("title", "pitch");
		Chart chart = ChartTools.makeSimpleChart(
			pitch,
			shiftDuration,
			"Pitch",
			title,
			"Time (second)",
			"Pitch (Hz)"
		);
		
		final Scene chartScene = new Scene(chart, 800, 600);

		// ボタン作成
		Button exportButton = new Button("export");
		ExportHandler exportHandler = new ExportHandler(chartScene);
		exportButton.setOnAction(exportHandler);

		// 描画
		BorderPane pane = new BorderPane();
		pane.setCenter(chart);
		pane.setBottom(exportButton);

		final Scene scene = new Scene(pane, 800, 600);

		// ウィンドウ表示
		primaryStage.setScene(scene);
		primaryStage.setTitle(getClass().getName());
		primaryStage.show();
		
	}

}
