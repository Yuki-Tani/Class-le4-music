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

public class PlotVoicedSound extends Application{
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
		
		
		double[] pitch = TransformTools.toPitch(wave, sampleRate, frameSize, shiftSize);
		double[] voicedFilter = TransformTools.getVoicedSoundFilter(wave, sampleRate, frameSize, shiftSize);
		double[] filteredWave = TransformTools.applyFilter(wave, voicedFilter);
		
		// チャート作成
		LineChart<Number, Number> chart = ChartTools.makeSimpleChart(
			voicedFilter,
			shiftDuration,
			"voiced",
			"voiced Sound Filter",
			"time (second)",
			"is voice?"
		);
		
		LineChart<Number, Number> chart2 = ChartTools.makeSimpleChart(
			pitch,
			shiftDuration,
			"Pitch",
			"pitch",
			"time (second)",
			"pitch (Hz)"
		);
		
		LineChart<Number, Number> chart3 = ChartTools.makeSimpleChart(
			wave,
			1.0 / sampleRate,
			"wave",
			"original wave",
			"time (second)",
			"power"
		);
		
		LineChart<Number, Number> chart4 = ChartTools.makeSimpleChart(
			filteredWave,
			1.0 / sampleRate,
			"wave",
			"filtered wave",
			"time (second)",
			"power"
			);
		
		final Scene chartScene = new Scene(chart4, 800, 600);

		// ボタン作成
		Button exportButton = new Button("export");
		ExportHandler exportHandler = new ExportHandler(chartScene);
		exportButton.setOnAction(exportHandler);

		// 描画
		
		chart.setMaxHeight(200);
		chart2.setMaxHeight(200);
		chart3.setMaxHeight(200);
		chart4.setMaxHeight(200);
		
		BorderPane pane = new BorderPane();
		BorderPane paneLev1 = new BorderPane();
		pane.setCenter(paneLev1);
		
		pane.setTop(chart);
		paneLev1.setTop(chart3);
		paneLev1.setCenter(chart4);
		paneLev1.setBottom(chart2);
		pane.setBottom(exportButton);

		final Scene scene = new Scene(pane, 800, 1000);

		// ウィンドウ表示
		primaryStage.setScene(scene);
		primaryStage.setTitle(getClass().getName());
		primaryStage.show();
		
	}

}
