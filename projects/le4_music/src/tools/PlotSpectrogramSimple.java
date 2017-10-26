package tools;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.LineChartWithSpectrogram;

public class PlotSpectrogramSimple extends Application {

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
		final double[] waveform = Le4MusicUtils.readWaveformMonaural(stream);
		final AudioFormat format = stream.getFormat();
		final double sampleRate = format.getSampleRate();
		stream.close();

		final double frameDuration = Le4MusicUtils.frameDuration;
		final double shiftDuration = frameDuration / 8.0;

		// 窓関数のフレーム長 // s * 1/s
		final int frameSize = (int) Math.round(frameDuration * sampleRate);
		// 何要素分シフトするか
		final int shiftSize = (int) Math.round(shiftDuration * sampleRate);

		// 短時間フーリエ変換をする
		final double[][] specLog = TransformTools.toSpectrogram(waveform, frameSize, shiftSize);
		
		// チャートに名前をつける
		String title = ChartTools.requestName("series","spectrogram");
		
		
		LineChartWithSpectrogram<Number,Number> chart = makeSpectrogramChart(
				specLog,
				frameDuration,
				shiftDuration,
				sampleRate,
				title,
				"time (second)",
				"frequency (Hz)"
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
	
	public static LineChartWithSpectrogram<Number, Number> makeSpectrogramChart(
			double[][] spectrogramData,
			double frameDuration,
			double shiftDuration,
			double sampleRate,
			String title,
			String xLabel,
			String yLabel)
	{
		// 窓関数のフレーム長 // s * 1/s
		final int frameSize = (int) Math.round(frameDuration * sampleRate);
		// 何要素分シフトするか
		final int shiftSize = (int) Math.round(shiftDuration * sampleRate);

		// 2^pとなる配列サイズを求める
		final int fftSize = 1 << Le4MusicUtils.nextPow2(frameSize);
		final int fftHalfSize = (fftSize >> 1) + 1;
		
		// x軸
		final NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel("Time (Second)");
		xAxis.setLowerBound(0.0);
		xAxis.setUpperBound(spectrogramData.length * shiftDuration);
		// y軸
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Frequency (Hz)");
		yAxis.setLowerBound(0.0);
		yAxis.setUpperBound(sampleRate * 0.5);
		yAxis.setTickUnit(sampleRate * 0.05);
		yAxis.setAutoRanging(false);
		
		// chチャート作成
		final LineChartWithSpectrogram<Number, Number> chart = new LineChartWithSpectrogram<>(xAxis, yAxis);
		chart.setParameters(spectrogramData.length, fftHalfSize, sampleRate * 0.5);
		chart.setTitle(title);
		Arrays.stream(spectrogramData).forEach(chart::addSpecLog);
		chart.setCreateSymbols(false);
		chart.setLegendVisible(false);
		
		return chart;
	}

}
