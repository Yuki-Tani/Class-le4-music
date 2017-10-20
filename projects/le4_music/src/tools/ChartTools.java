package tools;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextInputDialog;

public class ChartTools {

	public static String requestName(
			String whatName, 
			String defaultName) 
	{
		String explanation = "input " + whatName + " name";
		String tag = "name";
		String name = requestString(explanation, tag, defaultName);
		return name;
	}

	public static String requestString(
			String explanation,
			String tag,
			String defaultInput)
	{
		TextInputDialog dialog = new TextInputDialog(defaultInput);
		dialog.setGraphic(null);
		dialog.setHeaderText(explanation);
		dialog.setContentText(tag);
		String input = dialog.showAndWait().orElse("");
		return input;
	}

	/*
	 * データからチャートを作成する
	 */
	public static LineChart<Number, Number> makeSimpleChart(
			double[] plotData,
			double xScale,
			String seriesName,
			String title,
			String xLabel,
			String yLable)
	{
		// データ系列を作成
		final ObservableList<XYChart.Data<Number, Number>> data = 
				IntStream.range(0, plotData.length)
				.mapToObj(i -> new XYChart.Data<Number, Number>(i * xScale, plotData[i]))
				.collect(Collectors.toCollection(FXCollections::observableArrayList));

		// データ系列に名前をつける
		final XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(seriesName);
		series.setData(data);

		// 軸を作成
		final NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel(xLabel);
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel(yLable);

		// チャート作成
		final LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
		chart.setTitle(title);
		chart.setCreateSymbols(false);
		chart.getData().add(series);
		return chart;
	}
	
	/*
	 * チャートに新しいデータ系列を追加する
	 */
	public static void addGraph(
			double[] plotData,
			double xScale,
			String seriesName,
			LineChart<Number, Number> chart)
	{
		// データ系列を作成
		final ObservableList<XYChart.Data<Number, Number>> data = 
				IntStream.range(0, plotData.length)
				.mapToObj(i -> new XYChart.Data<Number, Number>(i * xScale, plotData[i]))
				.collect(Collectors.toCollection(FXCollections::observableArrayList));

		// データ系列に名前をつける
		final XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(seriesName);
		series.setData(data);
		
		chart.getData().add(series);
	}
}