package sound_wave_analyzer;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.LineChartWithSpectrogram;
import tools.ChartTools;
import tools.PlotSpectrogramSimple;
import tools.SpeechRecognizer;
import tools.TransformTools;

public class SoundWaveAnalyzer extends Application{
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
		
		String fileName = args[0];
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
		
		
		double[][] spectrogram = TransformTools.toSpectrogram(wave, frameSize, shiftSize);
		double[] pitch = TransformTools.toPitch(wave, sampleRate, frameSize, shiftSize);
		double[] speech = recognizer.toSpeach(wave, sampleRate, frameSize, shiftSize);
		
		// チャート作成
		LineChart<Number, Number> waveChart = ChartTools.makeSimpleChart(
				wave,
				1.0/sampleRate,
				"original wave",
				"time (second)",
				"power"
		);
		
		LineChartWithSpectrogram<Number,Number> spectrogramChart 
			= PlotSpectrogramSimple.makeSpectrogramChart(
				spectrogram, 
				frameDuration, 
				shiftDuration, 
				sampleRate, 
				"spectrogram", 
				"time (second)",
				"frequency (Hz)"
		);
		ChartTools.setYAxisDetails(spectrogramChart, sampleRate*0.5, sampleRate*0.1);
		
		LineChart<Number, Number> pitchChart = ChartTools.makeSimpleChart(
				pitch,
				shiftDuration,
				"pitch",
				"time (second)",
				"frequency (Hz)"
		);
		ChartTools.setYAxisDetails(pitchChart, 1500,150);
		
		LineChart<Number, Number> speechChart = ChartTools.makeSimpleChart(
				speech,
				shiftDuration,
				"speech",
				"time (second)",
				"vowel (A1,I2,U3,E4,O5)"
		);
		ChartTools.setYAxisDetails(speechChart, 5.0, 1.0);
		
		
		waveChart.setMaxHeight(200);
		pitchChart.setMaxHeight(200);
		speechChart.setMaxHeight(200);
		
		// イベントの設計
		ChartTools.ChartXYAction action = new ChartTools.ChartXYAction(waveChart);
		waveChart.lookup(".chart-plot-background").setOnMouseClicked(action);
		
/*		
		BorderPane insidePane = new BorderPane();
		insidePane.setTop(chart);
		insidePane.setCenter(chart2);
		chart.setMaxHeight(300);
		chart2.setMaxHeight(300);
		

		// ボタン作成
		Button exportButton = new Button("export");
		ExportHandler exportHandler = new ExportHandler(chartScene);
		exportButton.setOnAction(exportHandler);
*/		
		// レイアウトの作成
		BorderPane mainPane = new BorderPane();
		BorderPane chartPane = new BorderPane();
		BorderPane bottomChartPane = new BorderPane();
		BorderPane informationPane = new BorderPane();
		BorderPane bottomInformationPane = new BorderPane();
		
		mainPane.setCenter(chartPane);
		mainPane.setRight(informationPane);
		chartPane.setBottom(bottomChartPane);
		informationPane.setBottom(bottomInformationPane);
		
		// コンポーネントの設置
		chartPane.setTop(waveChart);
		chartPane.setCenter(spectrogramChart);
		bottomChartPane.setCenter(pitchChart);
		bottomChartPane.setBottom(speechChart);

		final Scene scene = new Scene(mainPane, 800, 800);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Sound Wave Analyzer ["+fileName+"]");
		primaryStage.show();
		
	}
}
