package com.nedzhang.sterlingloganalyzer.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import com.nedzhang.sterlingloganalyzer.LogEntry.LogEntry;
import com.nedzhang.sterlingloganalyzer.entity.OperationEntry;
import com.nedzhang.sterlingloganalyzer.entity.OperationEntry.OperationProperty;
import com.nedzhang.sterlingloganalyzer.entity.OperationThread;
import com.nedzhang.sterlingloganalyzer.entity.OperationTimed;
import com.nedzhang.util.XPathUtil;
import com.nedzhang.util.XmlUtil;

public class ThreadProcessThreeWidget extends VBox {

	private static final OperationProperty[] propertyDisplay = new OperationProperty[] {
			OperationProperty.Thread, OperationProperty.Service,
			OperationProperty.ActClass, OperationProperty.Level,
			OperationProperty.Time, OperationProperty.Duration,
			OperationProperty.StartTime, OperationProperty.EndTime,
			OperationProperty.LogText, OperationProperty.LogPath,
			OperationProperty.PerformanceList };

	@FXML
	private TreeView<OperationEntry> treeviewThreadProcess;

	@FXML
	private WebView viewProperty;

	@FXML
	private TitledPane accordPaneOperationDetail;

	@FXML
	private TitledPane accordPaneSectionLog;

	@FXML
	private Accordion accordionRightViewer;

	@FXML
	private TextArea textAreaSectionLog;

	private final Node rootIcon = new ImageView(new Image(getClass()
			.getResourceAsStream("/log_file.png")));

	private final Node threadIcon = new ImageView(new Image(getClass()
			.getResourceAsStream("/thread.png")));

	private final Node operationIcon = new ImageView(new Image(getClass()
			.getResourceAsStream("/normal_operation.png")));

	private final Node timedOperationIcon = new ImageView(new Image(getClass()
			.getResourceAsStream("/timed_operation.png")));

	private Document performanceDoc = null;

	private final Comparator<OperationTimed> timeDscComparator = new Comparator<OperationTimed>() {
		@Override
		public int compare(final OperationTimed o1, final OperationTimed o2) {
			if (o1 == null) {
				return 1;
			} else if (o2 == null) {
				return -1;
			} else {
				return o2.getDurationInMilli() - o1.getDurationInMilli();
			}
		}
	};

	public ThreadProcessThreeWidget() {

		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"ThreadProcessThreeWidget.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}

		try {

			treeviewThreadProcess
					.getSelectionModel()
					.selectedItemProperty()
					.addListener(
							new ChangeListener<TreeItem<OperationEntry>>() {

								@Override
								public void changed(
										final ObservableValue<? extends TreeItem<OperationEntry>> observable,
										final TreeItem<OperationEntry> oldValue,
										final TreeItem<OperationEntry> newValue) {
									if (newValue != oldValue) {

										createPropertyTable(newValue);

									}

								}
							});

			// initializeFormData();

		} catch (final Exception ex) {
			Logger.getLogger(ThreadProcessThreeWidget.class.getName()).log(
					Level.SEVERE, null, ex);
		}

		ChangeListener<TitledPane> accordionExpandListener = new ChangeListener<TitledPane>() {

			@Override
			public void changed(ObservableValue<? extends TitledPane> ov,
					TitledPane oldVal, TitledPane newVal) {

				if (newVal != oldVal && newVal == accordPaneSectionLog) {

					if (getCanExtractLog()) {
						List<LogEntry> logEntries = getRecursiveLogEntrisForCurrentItem();

						StringBuilder sectionLogBuilder = new StringBuilder();

						for (LogEntry logEntry : logEntries) {
							if (logEntry != null) {
								sectionLogBuilder.append(logEntry.getLogText())
										.append("\n");
							}
						}

						textAreaSectionLog
								.setText(sectionLogBuilder.toString());

					} else {
						textAreaSectionLog.setText("No section log");
					}
				}
			}
		};

		accordionRightViewer.expandedPaneProperty().addListener(
				accordionExpandListener);
	}

	public ReadOnlyObjectProperty<TreeItem<OperationEntry>> getSelectedItemProperty() {
		return treeviewThreadProcess.getSelectionModel().selectedItemProperty();
	}

	protected void createPropertyTable(final TreeItem<OperationEntry> newValue) {

		final StringBuilder htmlTableBuilder = new StringBuilder(2000)
				.append("<html><head><style type=\"text/css\">pre { white-space: pre-wrap; /* css-3 */ white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */ white-space: -pre-wrap;      /* Opera 4-6 */ white-space: -o-pre-wrap;    /* Opera 7 */ word-wrap: break-word;       /* Internet Explorer 5.5+ */}</style></head><body><table border=\"1\" style=\"width:100%\">");

		if (newValue != null && newValue.getValue() != null
				&& newValue.getValue().getPropertyMap() != null) {

			final Map<OperationProperty, String> parameterMap = newValue
					.getValue().getPropertyMap();

			if ((!parameterMap.containsKey(OperationProperty.LogPath) || parameterMap
					.get(OperationProperty.LogPath) == null)) {

				final StringBuilder pathBuilder = getLogPath(newValue);

				parameterMap.put(OperationProperty.LogPath,
						pathBuilder.toString());
			}

			if ((!parameterMap.containsKey(OperationProperty.PerformanceList) || parameterMap
					.get(OperationProperty.PerformanceList) == null)
					&& newValue.getChildren() != null
					&& newValue.getChildren().size() > 0) {

				if (newValue.getValue().getChildren() != null
						&& newValue.getValue().getChildren().size() > 0) {

					final List<OperationTimed> performanceHitList = new ArrayList<OperationTimed>(
							newValue.getValue().getChildren().size());

					for (final OperationEntry entry : newValue.getValue()
							.getChildren()) {
						if (entry instanceof OperationTimed) {
							performanceHitList.add((OperationTimed) entry);
						}
					}

					// getChildren().toArray(performanceHitList);

					Collections.sort(performanceHitList, timeDscComparator);

					final StringBuilder perfListBuilder = new StringBuilder(
							1024);

					for (final OperationEntry operationEntry : performanceHitList) {
						perfListBuilder.append(operationEntry).append('\n');
					}

					parameterMap.put(OperationProperty.PerformanceList,
							perfListBuilder.toString());
				}

			}

			for (final OperationProperty key : propertyDisplay) {
				if (parameterMap.containsKey(key)) {
					// System.out.println(key + ": " + parameterMap.get(key));
					htmlTableBuilder.append("<tr>");
					htmlTableBuilder.append("<td>").append(key).append("</td>");
					htmlTableBuilder.append("<td><pre>");
					if (parameterMap.get(key) != null) {
						htmlTableBuilder.append(parameterMap.get(key).replace(
								"<", "&lt;"));
					}
					htmlTableBuilder.append("</pre></td>");
					htmlTableBuilder.append("</tr>");
				}
			}
		} else {
			textAreaSectionLog.setText(null);
		}

		htmlTableBuilder.append("</table></body></html>");
		viewProperty.getEngine().loadContent(htmlTableBuilder.toString());

		accordPaneOperationDetail.setExpanded(true);
		accordPaneSectionLog.setExpanded(false);

	}

	private StringBuilder getLogPath(final TreeItem<OperationEntry> treeItem) {
		final StringBuilder pathBuilder = new StringBuilder();

		List<TreeItem<OperationEntry>> parentList = new ArrayList<TreeItem<OperationEntry>>();

		for (TreeItem<OperationEntry> entry = treeItem; entry != null; entry = entry
				.getParent()) {
			parentList.add(entry);
		}

		StringBuilder prefixBuilder = new StringBuilder();

		for (int i = parentList.size() - 1; i >= 0; i--) {
			if (pathBuilder.length() > 0) {
				pathBuilder.append('\n');
			}
			pathBuilder.append(prefixBuilder);
			pathBuilder.append(parentList.get(i).getValue().toString());
			prefixBuilder.append('.');
		}
		return pathBuilder;
	}

	public void loadProcess(final String fileName,
			final List<? extends OperationEntry> threads) {

		OperationEntry rootOpEntry = new OperationEntry(null) {
			@Override
			public String toString() {
				return fileName;
			}
		};

		for (OperationEntry operationEntry : threads) {
			rootOpEntry.getChildren().add(operationEntry);

		}

		treeviewThreadProcess.setRoot(new TreeItem<OperationEntry>(rootOpEntry,
				rootIcon));

		performanceDoc = null;
		loadProcess(treeviewThreadProcess.getRoot(), threads);

	}

	private void loadProcess(final TreeItem<OperationEntry> parentTreeNode,
			final List<? extends OperationEntry> operations) {

		// TreeItem parentTreeNode = treeviewThreadProcess.getRoot();
		for (final OperationEntry op : operations) {

			Node icon;

			if (op instanceof OperationThread) {
				icon = threadIcon;
			} else if (op instanceof OperationTimed) {
				icon = timedOperationIcon;
			} else if (op instanceof OperationEntry) {
				icon = operationIcon;
			} else {
				icon = rootIcon;
			}

			final TreeItem<OperationEntry> treeNodeItem = new TreeItem<OperationEntry>(
					op, icon);

			parentTreeNode.getChildren().add(treeNodeItem);

			if (op.getChildren() != null && op.getChildren().size() > 0) {
				loadProcess(treeNodeItem, op.getChildren());
			}

		}

	}

	public boolean getCanExtractLog() {

		TreeItem<OperationEntry> selectedItem = treeviewThreadProcess
				.getSelectionModel().getSelectedItem();

		return selectedItem != null && selectedItem.getValue() != null;
		// && (selectedItem.getValue() instanceof OperationTimed ||(
		// selectedItem.getValue().getChildren() != null
		// && selectedItem.getValue().getChildren().size() > 0));

	}

	public List<LogEntry> getRecursiveLogEntrisForCurrentItem() {

		if (getCanExtractLog()) {

			TreeItem<OperationEntry> selectedItem = treeviewThreadProcess
					.getSelectionModel().getSelectedItem();

			OperationEntry operation = selectedItem.getValue();

			List<LogEntry> logEntries = new ArrayList<LogEntry>();

			getOperationLog(logEntries, operation);

			return logEntries;

		} else {
			return null;
		}
	}

	private void getOperationLog(List<LogEntry> logEntries,
			OperationEntry operation) {

		logEntries.add(operation.getLog());

		if (operation.getChildren() != null
				&& operation.getChildren().size() > 0) {
			for (int i = 0; i < operation.getChildren().size(); i++) {
				OperationEntry childOp = operation.getChildren().get(i);
				getOperationLog(logEntries, childOp);
			}
		}

		if (operation instanceof OperationTimed) {
			LogEntry endLogEntry = ((OperationTimed) operation)
					.getEndLogEntry();
			if (endLogEntry != null) {
				logEntries.add(endLogEntry);
			}
		}
	}

	public Document createPerfXml() throws IllegalArgumentException,
			ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		if (performanceDoc == null) {
			performanceDoc = createPerfDoc(treeviewThreadProcess.getRoot());
		}

		return performanceDoc;
	}

	private Document createPerfDoc(TreeItem<OperationEntry> rootItem)
			throws IllegalArgumentException, ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {

		Document perfDoc = XmlUtil.getDocument("<PerformanceReport/>");

		Element rootElement = (Element) XmlUtil.getChildNodeByName(perfDoc, "PerformanceReport", false);
		
		loadTreeIntoDoc(rootItem.getValue(), rootElement);

		return perfDoc;
	}

	private void loadTreeIntoDoc(OperationEntry entry,
			Element perfDocParentElement) throws XPathExpressionException {

		Element timedOpElement = null;

		if (entry instanceof OperationTimed) {

			OperationTimed timerEntry =  ((OperationTimed) entry);
			
			String target = timerEntry.getTarget();
			
			timedOpElement = getTimeOpElement(perfDocParentElement, target);
			
			int duration = timerEntry.getDurationInMilli();
			
			int count = Integer.valueOf(XmlUtil.getAttribute(timedOpElement, "count", "0"));
			
			double average = Double.valueOf(XmlUtil.getAttribute(timedOpElement, "average", "0"));
			
			int min = Integer.valueOf(XmlUtil.getAttribute(timedOpElement, "min", "99999999"));
			
			int max = Integer.valueOf(XmlUtil.getAttribute(timedOpElement, "max", "0"));
			
			if (duration > max) {
				timedOpElement.setAttribute("max", String.valueOf(duration));
			}
			
			if (duration < min) {
				timedOpElement.setAttribute("min", String.valueOf(duration));
			}
			
			double newAverage = 0.00 + (count * average + duration) / (count + 1);
			
			timedOpElement.setAttribute("average", String.format("%.3f", newAverage));
			
			timedOpElement.setAttribute("count", String.valueOf(count+1));
			
//			createInstanceElement(perfDocParentElement, timedOpElement,
//					timerEntry, duration);
			
			
		} else {
			// For OperationThread and OperationEntry just keep going
			timedOpElement = perfDocParentElement;
		}

		List<OperationEntry> childEntries = entry.getChildren();
		if (childEntries != null && childEntries.size() > 0) {
			for (int i=0; i<childEntries.size(); i++) {
				loadTreeIntoDoc(childEntries.get(i), timedOpElement);
			}
		}
	}

	private void createInstanceElement(Element perfDocParentElement,
			Element timedOpElement, OperationTimed timerEntry, int duration) {
		
		Element instanceElement = perfDocParentElement.getOwnerDocument().createElement("Instance");
		
		instanceElement.setAttribute("thread", timerEntry.getThread());
		instanceElement.setAttribute("action", timerEntry.toString());
		instanceElement.setAttribute("duration", String.valueOf(duration));
		
		XmlUtil.getChildNodeByName(timedOpElement, "Instances", true).appendChild(instanceElement);
	}

	private Element getTimeOpElement(Element perfDocParentElement, String target) {
		
		org.w3c.dom.Node timeOpCollectionNode = XmlUtil.getChildNodeByName(perfDocParentElement, "TimeOps", true);
		
		String timedOpElementName = createTimedOpElementName(target);
		
		Element timedOpElement = (Element) XmlUtil.getChildNodeByName(timeOpCollectionNode, timedOpElementName, true);;
		
		return timedOpElement;
	}
	
	private String createTimedOpElementName(final String target) {
		
		
		if (target == null || target.length() == 0) {
			return target;
		} else {
			StringBuilder elementNameBuilder = new StringBuilder(target);
			for (int i=0; i<elementNameBuilder.length(); i++) {
				char charToCheck = elementNameBuilder.charAt(i);
				if (charToCheck == '.') {
					elementNameBuilder.setCharAt(i, '-');
				}  else if (charToCheck<'0' || (charToCheck > '9' && charToCheck <'A') || (charToCheck >'Z' && charToCheck < 'a') || charToCheck > 'z') {
					elementNameBuilder.setCharAt(i, '_');
				}
			}
			
			return elementNameBuilder.toString();
		}
	}

	private Element getTimeOpElement_Old(Element perfDocParentElement, String target)
			throws XPathExpressionException {
		Element timedOpElement;
		XPathExpression xpathTimerWithTarget = XPathUtil
				.getXPathExpression(
						"ThreadProcessThreeWidget.com.nedzhang.sterlingloganalyzer.gui",
						"TimedOps/TimedOp[@operation='" + XmlUtil.escapeXML(target) + "']");
		
		timedOpElement = (Element) xpathTimerWithTarget.evaluate(perfDocParentElement, XPathConstants.NODE);
		
//			perfElement = (Element) XmlUtil.getChildNodeByName(
//					perfDocParentElement, "OperationTimed", true);
		
		if (timedOpElement == null) {
			timedOpElement = perfDocParentElement.getOwnerDocument().createElement("TimedOp");
			XmlUtil.getChildNodeByName(perfDocParentElement, "TimedOps", true).appendChild(timedOpElement);
			timedOpElement.setAttribute("operation", target);
		}
		return timedOpElement;
	}
}
