package tools;

import java.io.File;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.WritableImage;

public class ExportHandler implements EventHandler<ActionEvent>{
	Scene scene;
	String defaultInput = 	"waveform.png";
	String explanation = 	"input file name (.png)";
	String tag = 			"name";
	
	public ExportHandler(Scene scene) {
		this.scene = scene;
	}
	@Override
	public void handle(ActionEvent event) {
		//ファイル名入力ダイアログ
		String name = ChartTools.requestString(explanation, tag, defaultInput);
		if(name.isEmpty()) return;
		
		//画像ファイルの出力
		WritableImage image = scene.snapshot(null);
		try {
		ImageIO.write(
				SwingFXUtils.fromFXImage(image,null),
				"png",
				new File(name)
		);
		}catch(Exception e){
			Alert alert = new Alert(
					AlertType.ERROR,
					"Fail to export!"
			);
			alert.setGraphic(null);
			alert.show();
			return;
		}
		Alert confirm = new Alert(
				AlertType.CONFIRMATION,
				"complete."
		);
		confirm.setGraphic(null);
		confirm.show();
	}
}