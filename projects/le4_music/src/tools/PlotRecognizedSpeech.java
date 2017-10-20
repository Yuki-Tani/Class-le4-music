package tools;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

public class PlotRecognizedSpeech extends Application{
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
		
		SpeechRecognizer recognizer = new SpeechRecognizer();
		
		double[] speech = recognizer.toSpeach(wave, sampleRate, frameSize, shiftSize);
		
		// チャート作成
		LineChart<Number, Number> chart = ChartTools.makeSimpleChart(
			speech,
			shiftDuration,
			"vowel",
			"recognized vowel",
			"time (second)",
			"vowel (A_1,I_2,U_3,E_4,O_5)"
		);
		
		LineChart<Number, Number> chart2 = ChartTools.makeSimpleChart(
			wave,
			1.0/sampleRate,
			"wave",
			"original wave",
			"time (second)",
			"power"
		);
		
		BorderPane insidePane = new BorderPane();
		insidePane.setTop(chart);
		insidePane.setCenter(chart2);
		chart.setMaxHeight(300);
		chart2.setMaxHeight(300);
		final Scene chartScene = new Scene(insidePane, 800, 600);

		// ボタン作成
		Button exportButton = new Button("export");
		ExportHandler exportHandler = new ExportHandler(chartScene);
		exportButton.setOnAction(exportHandler);

		// 描画
		
		BorderPane pane = new BorderPane();	
		pane.setCenter(insidePane);
		pane.setBottom(exportButton);

		final Scene scene = new Scene(pane, 800, 800);

		// ウィンドウ表示
		primaryStage.setScene(scene);
		primaryStage.setTitle(getClass().getName());
		primaryStage.show();
		
	}
}
