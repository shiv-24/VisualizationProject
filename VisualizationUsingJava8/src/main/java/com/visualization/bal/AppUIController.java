package com.visualization.bal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;

public class AppUIController {

	@FXML
	private Label choseFile;

	@FXML
	private TextField filePath;

	@FXML
	private Button open;

	@FXML
	private Button preview;

	@FXML
	private TableView<ObservableList<StringProperty>> fileContents;

	@FXML
	private ComboBox<String> delimeter;

	Map<String, String> headerDataType = new LinkedHashMap<>();

	final List<String> splitRegex = Arrays.asList(",", "|", ".", " ", "\t");

	public void helper() {
		// TODO Auto-generated method stub
		fileContents.setEditable(false);
		fileContents.setDisable(true);
		preview.setDisable(true);
		List<String> delimeters = Arrays.asList(",", "|", ".", "space", "tab");
		delimeter.setItems(FXCollections.observableList(delimeters));
		delimeter.setValue(delimeters.get(0));
	}

	public void openFile(ActionEvent event) {

		FileChooser fc = new FileChooser();
		// fc.setSelectedExtensionFilter();
		fc.setTitle("Chose File For Parsing");
		fc.getExtensionFilters().addAll(new ExtensionFilter("Comma Seperated Files", "*.csv"),
				new ExtensionFilter("Tab Seperated Files", "*.tsv"), new ExtensionFilter("Text Files", "*.txt"));
		File file = fc.showOpenDialog(null);
		filePath.setText(file == null ? "" : file.getAbsolutePath());
		preview.setDisable(false);
	}

	public void previewFile(ActionEvent event) {
		fileContents.setDisable(false);
		int seperator = delimeter.getSelectionModel().getSelectedIndex();

		try {
			long curTime	=	System.currentTimeMillis();
			System.out.println("Time when it entered into the fuction "+ curTime);
			List<String[]> contents = Files.lines(Paths.get(filePath.getText()))
					.map(name -> name.split(splitRegex.get(seperator))).collect(Collectors.toList());

			setHeaderDataType(contents.get(0), contents.get(1));
			Task<Void> task = new Task<Void>() {

				@Override
				protected Void call() throws Exception {
					String[] headerValue = contents.get(0);
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							for (int column = 0; column < headerValue.length; column++) {
								fileContents.getColumns().add(createColumn(column, headerValue[column]));
							}

						}
					});

					for (int i = 1; i < contents.size(); i++) {
						String[] dataValues = contents.get(i);
						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								for (int colIndex = fileContents.getColumns()
										.size(); colIndex < dataValues.length; colIndex++) {
									fileContents.getColumns().add(createColumn(colIndex, ""));
								}

								ObservableList<StringProperty> data = FXCollections.observableArrayList();
								for (String val : dataValues) {
									data.add(new SimpleStringProperty(val));
								}
								fileContents.getItems().add(data);
							}
						});
					}
					return null;
				}
			};

			Thread thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setHeaderDataType(String[] headers, String[] data) {

		int start = 0;

		for (; start < headers.length; start++) {
			String headerVal = headers[start];
			String val = data[start];
			if (isInteger(val, 10)) {
				headerDataType.put(headerVal, "Integer");
			} else if (isDouble(val, 2)) {
				headerDataType.put(headerVal, "Double");
			} else {
				headerDataType.put(headerVal, "String");
			}

		}

	}

	public void nextScene(ActionEvent event) {

		Iterator<String> ite = headerDataType.keySet().iterator();
		while (ite.hasNext()) {
			String header = ite.next();
			System.out.println("Header Value is " + header);
			System.out.println("Header DataType is " + headerDataType.get(header));
		}
	}

	public static boolean isInteger(String s, int radix) {
		Scanner sc = new Scanner(s.trim());
		try {
		if (!sc.hasNextInt(radix))
			return false;
		// we know it starts with a valid int, now make sure
		// there's nothing left!
		sc.nextInt(radix);
		return !sc.hasNext();
		}catch(Exception e) {
			sc.close();
		}finally {
			sc.close();
		}
		return false;
	}

	public static boolean isDouble(String s, int radix) {
		Scanner sc = new Scanner(s.trim());
		try {
		if (!sc.hasNextDouble())
			return false;
		// we know it starts with a valid int, now make sure
		// there's nothing left!
		sc.nextDouble();
		return !sc.hasNext();
		}catch(Exception e) {
			sc.close();
		}finally {
			sc.close();
		}
		return false;
	}

	private TableColumn<ObservableList<StringProperty>, String> createColumn(final int columnIndex,
			String columnTitle) {
		TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>();
		String title;
		if (columnTitle == null || columnTitle.trim().length() == 0) {
			title = "Column " + (columnIndex + 1);
		} else {
			title = columnTitle;
		}
		column.setText(title);
		column.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<StringProperty>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							CellDataFeatures<ObservableList<StringProperty>, String> cellDataFeatures) {
						ObservableList<StringProperty> values = cellDataFeatures.getValue();
						if (columnIndex >= values.size()) {
							return new SimpleStringProperty("");
						} else {
							return cellDataFeatures.getValue().get(columnIndex);
						}
					}
				});
		return column;
	}

}
