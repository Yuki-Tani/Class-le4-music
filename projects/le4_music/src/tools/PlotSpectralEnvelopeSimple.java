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

public class PlotSpectralEnvelopeSimple extends Application{
		
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
		
		final double[] spectrum = TransformTools.toSpectrum(waveform);
		final double[] specEnv = TransformTools.toEnvelope(spectrum);
		final double[] specLog = TransformTools.toDB(spectrum);
		final double[] specEnvLog = TransformTools.toDB(specEnv);
		
		/*
		for(int i=0; i<spectrum.length; i++) {
			System.out.println(i + " : "+ (1.0*specEnvLog[i]/specLog[i]));
		}
		*/
		
		// チャート作成
		String title = ChartTools.requestName("title", "spectrum");
		LineChart<Number, Number> chart = ChartTools.makeSimpleChart(
			specLog,
			TransformTools.specScaleX(waveform, sampleRate),
			"spectrum",
			title,
			"Frequency (Hz)",
			"Amplitude (dB)"
		);
		
		ChartTools.addGraph(
				specEnvLog,
				TransformTools.specScaleX(waveform, sampleRate),
				"envelope",
				chart);

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
