package test;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class Test extends Application{

	public static void main(String[] args) {
		System.out.println("Hello eclipse oxgen!");
		System.out.println("Hello le4!");
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new Scene(new Group(), 200, 100));
		primaryStage.show();
		Alert alert = new Alert(
				AlertType.ERROR,
				"Fail to export!");
		alert.showAndWait();
		TextInputDialog textIn = new TextInputDialog("default");
		String str = textIn.showAndWait().orElse("");
		new Alert(AlertType.INFORMATION,str).show();
	}

}
