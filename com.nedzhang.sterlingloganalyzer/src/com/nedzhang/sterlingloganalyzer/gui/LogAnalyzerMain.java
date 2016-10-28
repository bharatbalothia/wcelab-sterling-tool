package com.nedzhang.sterlingloganalyzer.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class LogAnalyzerMain extends Application {

	private static final String APP_TITLE = "NZ Sterling Timer Log Analyzer";
	

	@Override
	public void start(final Stage primaryStage) throws Exception {

		final Parent root = FXMLLoader.load(getClass().getResource(
				"/com/nedzhang/sterlingloganalyzer/gui/LogAnalyzerMainForm.fxml"));

		final Scene scene = new Scene(root);

		primaryStage.setScene(scene);

		primaryStage.getIcons().add(
				new Image(this.getClass().getResourceAsStream(
						"/notes_search.png")));

		primaryStage.setTitle(APP_TITLE);

		primaryStage.show();
	}
	
	public static void main(final String[] args) {
		launch(args);
	}
	
	

}
