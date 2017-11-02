package test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Test extends Application{

	public static void main(String[] args) {
		System.out.println("Hello eclipse oxgen!");
		System.out.println("Hello le4!");
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		//stage test
		
		BorderPane pane = new BorderPane();
		
		Label text = new Label();
		
		
		String place = "children/test.txt"; // res を build-pathに含める！
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(place);
			if(stream == null) System.out.println("null.");
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			text.setText(in.readLine());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		pane.setCenter(text);
		primaryStage.setScene(new Scene(pane, 200, 100));
		
		// alert test
		Alert alert = new Alert(
				AlertType.ERROR,
				"error test");
		alert.showAndWait();
		
		// text input dialog test
		TextInputDialog textIn = new TextInputDialog("default");
		String str = textIn.showAndWait().orElse("");
		new Alert(AlertType.INFORMATION,str).show();
		
		primaryStage.show();
	}

}
