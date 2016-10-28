package com.nedzhang.sterlingloganalyzer.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.nedzhang.sterlingloganalyzer.entity.OperationThread;
import com.nedzhang.sterlingloganalyzer.parser.LogFileParser;
import com.nedzhang.sterlingloganalyzer.parser.LogLineParser;
import com.nedzhang.sterlingloganalyzer.parser.ParserManager;
import com.nedzhang.util.XmlUtil;

public class LogAnalyzerMainForm extends Parent {

	private static Logger logger = Logger.getLogger(LogAnalyzerMainForm.class.getName());
	
	private static final String APP_TITLE = "NZ Sterling Timer Log Analyzer";

	@FXML
	private ThreadProcessThreeWidget processTreeWidget;
	
	@FXML 
	private VBox panelProcessWait;	

	@FXML
	private ToggleGroup toggleGroupParser;

	@FXML
	private Menu menuParser;
	
	@FXML
	private CheckMenuItem checkParseFilterTimer;
	
	@FXML
	private CheckMenuItem checkParseFilterSqldebug;
	
	@FXML
	private CheckMenuItem checkParseFilterDebug;
	
	@FXML
	private CheckMenuItem checkParseFilterVerbose;
	

	private boolean menuParserLoaded = false;

	// @FXML
	// private MenuItem menuExtractLog;

	private File lastFileOpenDir = null;

	// private String parserClass;

	private LogLineParser logLineParser;

	private final ParserManager parserManager;

	public LogAnalyzerMainForm() throws XPathExpressionException {
		
		super();

		parserManager = ParserManager.getInstance();
		

	}

	private Stage getStage() {
		return (Stage) processTreeWidget.getParent().getScene().getWindow();
	}

	@FXML
	private void onMenuClose(final ActionEvent event) {

		getStage().close();
	}

	@FXML
	private void setParser(final ActionEvent event) throws Exception {

		if (!menuParserLoaded) {
			loadMenuParser();
		}
		
		RadioMenuItem selectedParserItem = (RadioMenuItem) toggleGroupParser
				.getSelectedToggle();

		String menuText = selectedParserItem.getText();

		// if ("AEO Sterling Parser".equals(menuText)) {
		// parserClass =
		// "com.nedzhang.sterlingloganalyzer.customLineParser.AEOLoglineParser2";
		// } else if ("AEO JBoss Parser".equals(menuText)) {
		// parserClass =
		// "com.nedzhang.sterlingloganalyzer.customLineParser.AEOJBossLoglineParser2";
		// } else {
		// throw new
		// Exception("Unknown radio button selected for parser class");
		// }

		logLineParser = parserManager.getParser(menuText);
	}

	// @FXML
	// private void onMenuExtractLog(final ActionEvent event) {
	// List<LogEntry> logEntries =
	// processTreeWidget.getRecursiveLogEntrisForCurrentItem();
	//
	// for (LogEntry logEntry : logEntries) {
	// System.out.println(logEntry.getLogText());
	// }
	// }
	

	@FXML
	private void onFileMenuShowing(final Event event) {
		// menuExtractLog.setDisable(!processTreeWidget.getCanExtractLog());
	}

	@FXML
	private void onMenuParserShowing(final Event event) {

		if (!menuParserLoaded) {
			loadMenuParser();
		}

	}

	private void loadMenuParser() {
		
		menuParser.getItems().clear();

		for (String parserKey : parserManager.getParserNames()) {
			RadioMenuItem item = new RadioMenuItem(parserKey);
			item.setToggleGroup(toggleGroupParser);
			// Set the event handler for changing parser
			item.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					try {
						setParser(event);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Failed to set parser.", e);
						e.printStackTrace();
					}
				}
			});

			boolean selected = Boolean.parseBoolean(parserManager
					.getParser(parserKey).getProperty("default"));

			item.setSelected(selected);

			menuParser.getItems().add(item);
		}
		menuParserLoaded = true;
	}

	@FXML
	private void onMenuOpen(final ActionEvent event) throws IOException,
			Exception {
		// System.out.println("User wants to open a file");

		final FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Open Resource File");
		fileChooser.setInitialDirectory(lastFileOpenDir);

		final File fileToOpen = fileChooser.showOpenDialog(getStage());

		if (fileToOpen != null && fileToOpen.exists()) {
			lastFileOpenDir = fileToOpen.getParentFile();

			final FileInputStream inputStream = new FileInputStream(fileToOpen);

			// Check if parsers are loaded. If not, load the default parser
			if (logLineParser == null) {
				setParser(null);
			}
			
			panelProcessWait.setVisible(true);
			 
			Task<List<OperationThread>> parseLogFileTask = new Task<List<OperationThread>>() {
				@Override
				protected List<OperationThread> call() throws Exception {
					
					
					try {
						final List<OperationThread> threadsLogged = parseLogFile(inputStream);
						
						return threadsLogged;
						
					}
					finally {
						panelProcessWait.setVisible(false);
					}
				}
			};
			
			parseLogFileTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@SuppressWarnings("unchecked")
				@Override
				public void handle(WorkerStateEvent event) {
					
					final List<OperationThread> threadsLogged = (List<OperationThread>) event.getSource().getValue();

					processTreeWidget.loadProcess(fileToOpen.getName(), threadsLogged);
					
					
				}
			});
			
			
			Thread taskThread = new Thread(parseLogFileTask);
			
			taskThread.setDaemon(true);
			
			taskThread.start();			

			getStage().setTitle(APP_TITLE + " - " + fileToOpen.getName());

		}
	}

	private List<OperationThread> parseLogFile(final FileInputStream inputStream) throws IOException, Exception {
		final LogFileParser logReader = new LogFileParser(logLineParser);

		final List<OperationThread> threadsLogged = logReader
				.readLog(inputStream, checkParseFilterTimer.isSelected(), 
						checkParseFilterSqldebug.isSelected(),
						checkParseFilterDebug.isSelected(),
						checkParseFilterVerbose.isSelected());

		return threadsLogged;
//		processTreeWidget.loadProcess(fileToOpen.getName(), threadsLogged);
	}


	@FXML
	private void onMenuCreatePerfXml(final Event event) {
		
		
		try {
			Document perfXmlDoc = processTreeWidget.createPerfXml();
			
			XmlUtil.writeXmlToFile(perfXmlDoc, "/home/nzhang/project/eci/temp/performance_report.xml");
			
		} catch (IllegalArgumentException | ParserConfigurationException
				| SAXException | IOException | XPathExpressionException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
		}
		
		System.out.println("you clicked create perf xml!");

	}

}
