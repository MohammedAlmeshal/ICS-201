package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class Main extends Application implements Initializable, EventHandler<ActionEvent> {

	@FXML
	Button Next = new Button();
	@FXML
	Button Back;
	@FXML
	Button CreatePDF;
	@FXML
	Button SendEmail;
	@FXML
	TextArea MessageField = new TextArea();
	@FXML
	TextField from;
	@FXML
	TextField Subject;
	@FXML
	PasswordField pswrd;
	@FXML
	Label LoadStatus;
	@FXML
	Button LoadContact;
	@FXML
	MenuItem SaveContact;
	@FXML
	MenuItem LoadTemplate;
	@FXML
	MenuItem SaveTemplate = new MenuItem();

	@FXML
	MenuButton TagsMenuButton = new MenuButton();

	@FXML
	static ArrayList<MenuItem> MenuItems = new ArrayList<MenuItem>();
	@FXML
	TableView<String[]> Table1 = new TableView();
	@FXML
	ArrayList<TableColumn> Colums = new ArrayList<TableColumn>();

	static ArrayList<String> info = new ArrayList<String>();
	static String[][] infoArray;
	static ArrayList<Contact> ContactList = new ArrayList<Contact>();
	static ArrayList<String> messages = new ArrayList<String>();
	static ObservableList<String[]> data = FXCollections.observableArrayList();
	static String[] senderInfo = new String[3];

	static Boolean Loaded = false;
	static int EmailIndex = -1;

	public void start(Stage primaryStage) {
		try {
			// stage setup using FXML file
			AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("Sample.fxml"));
			Scene scene = new Scene(root, 650, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			// CreatePDF.setDisable(true);

			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);

	}

	@Override
	@FXML
	// Handle method takes all actions and behaves accordingly
	public void handle(ActionEvent event) {

		// Loading contacts Button
		if (event.getSource() == LoadContact) {

			// clearing the lists of past contacts info if there is any
			if (Loaded == true) {
				MenuItems.clear();
				ContactList.clear();
				info.clear();
				messages.clear();
				Colums.clear();
				Arrays.fill(infoArray, null);
				data.clear();
				EmailIndex = -1;
			}
			// opening the contact file and extract content in Read method
			File file = OpenFileChooser();
			try {
				Loaded = ReadContactFile(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (Loaded) {
				// inform that there is contact loaded
				LoadStatus.setText("Contact Loaded ");
				// Enable Buttons that needed contact info to function
				TagsMenuButton.setDisable(false);
				Next.setDisable(false);
				// empty message filed
				MessageField.setText("");
				// inform contact loaded via alert
				InfoAlert(2);
			}
		}

		// if next button is hit
		else if (event.getSource() == Next) {
			// fire an error alert if required filed are empty
			if (MessageField.getText().isEmpty() && Subject.getText().isEmpty()) {
				ErrorAlert(4);
				return;
			} else if (from.getText().isEmpty()) {
				ErrorAlert(5);
				return;
			} else if (pswrd.getText().isEmpty()) {
				ErrorAlert(6);
				return;
			}

			// clear past contact info if any
			Arrays.fill(senderInfo, null);
			// method call to generate the custom messages
			GenerateMessages();
			// this array is to store sender info for next stage
			senderInfo[0] = from.getText();
			senderInfo[1] = pswrd.getText();
			senderInfo[2] = Subject.getText();
			// showing the second stage
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Second.fxml"));
			Parent root1;
			try {
				root1 = (Parent) fxmlLoader.load();

				Stage stage = new Stage();
				stage.setTitle("");
				stage.setScene(new Scene(root1));

				stage.show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// if send email button was hit
		else if (event.getSource() == SendEmail) {

			boolean successful = true;
			// loop to the number of messages available and send each costume
			// message

			for (int i = 0; i < messages.size(); i++) {

				try {
					// usage of provided email class
					SendEmailOffice365 Sender = new SendEmailOffice365(senderInfo[0], senderInfo[1],
							ContactList.get(i).getInfo().get(EmailIndex), senderInfo[2], messages.get(i));

					// check if email was sent or not
					if (!(Sender.sendEmail()))
						successful = false;

				} catch (Exception e) {

					// System.out.println(e);
				}

			}
			// inform user of email status
			if (successful)
				InfoAlert(1);
			else
				ErrorAlert(1);

		}

		else if (event.getSource() == CreatePDF) {

			// opening save dialog to save the PDF file in

			File file = SaveFileChooser();

			try {

				OutputStream FileStram = new FileOutputStream(file.getAbsoluteFile());

				// keep the following 3 lines unchanged

				Document document = new Document();
				PdfWriter.getInstance(document, FileStram);
				document.open();

				// generate pages to the number of contact list

				for (int i = 0; i < ContactList.size(); i++) {

					document.add(new Paragraph(messages.get(i)));
					// go to new page
					document.newPage();
				}
				document.close();
				FileStram.close();

				InfoAlert(3);

			} catch (Exception e) {

				System.out.println(e);
				// e.printStackTrace();

			}

		}
		
		else if (event.getSource() == LoadTemplate) {
			// open file open dialog
			File file = OpenFileChooser();

			Scanner sc = null;
			try {
				sc = new Scanner(new FileInputStream(file.getAbsolutePath()));
			// reading file content 
				while (sc.hasNextLine()) {
					MessageField.appendText(sc.nextLine() + "\n");
				}
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}

		}

		else if (event.getSource() == SaveTemplate) {
		// open save dialog
			File savedFile = SaveFileChooser();
		// write message file content - template - into a text file 
			try {
				
				PrintWriter Pwrite = new PrintWriter(new FileOutputStream(savedFile.getAbsolutePath()));
				Pwrite.print(MessageField.getText());
				Pwrite.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// feature of adding placeholder to message from the tags menu
		for (int i = 0; i < MenuItems.size(); i++) {
			final Integer j = i;
			MenuItems.get(i).setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {

					MessageField.appendText(MenuItems.get(j).getText());

				}
			});
		}

	}
	 // a method to open file explorer 
	private File SaveFileChooser() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save file");

		File savedFile = fileChooser.showSaveDialog(null);
		return savedFile;

	}
	 // a method to open location explorer 
	private File OpenFileChooser() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open file");
		File file = fileChooser.showOpenDialog(null);

		return file;

	}
	// this method reads the contact file and save its conent as 1-tags 2-contact info
	private boolean ReadContactFile(File file) throws FileNotFoundException {

		boolean flag = true;
		// getting information of the first row of the file "tags"
		Scanner sc = new Scanner(new FileInputStream(file.getAbsolutePath()));
		String tagsLine = sc.nextLine();
		StringTokenizer st = new StringTokenizer(tagsLine, ",");
		StringTokenizer st2 = new StringTokenizer(tagsLine, ",");
		int i = 0;
	// check if tags are valid
		while (st.hasMoreTokens()) {
			String Token = st.nextToken();

			if (Token.contentEquals("[[EMAIL_ADDRESS]]") || Token.contentEquals("[[EMAIL]]"))
				EmailIndex = i;

			if (!(Token.substring(0, 2).equals("[[")
					&& Token.substring(Token.length() - 2, Token.length()).equals("]]")))
				flag = false;

			i++;
		}
		// fire error if any invalid tag is found
		if (EmailIndex == -1) {
			ErrorAlert(3);
			return false;
		}

		else if (flag == false) {
			ErrorAlert(2);
			return false;
		}
		
		
		int x = 0;
		// clear past menu items if any
		TagsMenuButton.getItems().clear();
		
		// Populating the MenuButton with MenuItems that represent the tags
		while (st2.hasMoreTokens()) {
			String Token = st2.nextToken();

			if (x == EmailIndex)
				continue;
			MenuItems.add(x, new MenuItem(Token));
			TagsMenuButton.getItems().add(MenuItems.get(x));
			x++;
		}

		int j = 0;
		// reading conrtacts
		while (sc.hasNextLine()) {

			// adding to list "info" - which represent the information of each
			// contact- to each contact in "contactList"

			StringTokenizer st1 = new StringTokenizer(sc.nextLine(), ",");
			info = new ArrayList<String>();
			int y = 0;
			while (st1.hasMoreTokens()) {
				info.add(y, st1.nextToken());
				y++;
			}
			ContactList.add(j, new Contact(info));
			j++;

		}

		sc.close();
		// copying indo to a 2d array for use in table view 
		infoArray = new String[ContactList.size()][info.size()];
		for (int m = 0; m < ContactList.size(); m++) {
			for (int n = 0; n < info.size(); n++) {
				infoArray[m][n] = ContactList.get(m).getInfo().get(n);
			}
		}
		data.addAll(Arrays.asList(infoArray));

		return flag;

	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// set default buttons status 
		TagsMenuButton.setDisable(true);
		Next.setDisable(true);
		BooleanBinding booleanBind = MessageField.textProperty().isEmpty();
		SaveTemplate.disableProperty().bind(booleanBind);

			// populating tableview 
		for (int i = 0; i < MenuItems.size(); i++) {

			TableColumn columnTitle = new TableColumn(MenuItems.get(i).getText());
			final int columnNo = i;

			columnTitle.setCellValueFactory(
					new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
						@Override
						public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> param) {
							return new SimpleStringProperty((param.getValue()[columnNo]));
						}
					});

			columnTitle.setResizable(true);

			Table1.getColumns().add(columnTitle);

		}
		Table1.setItems(data);
	}
	 // the method that generate the custom messages 
	public ArrayList<String> GenerateMessages() {

		String message = null;

		for (int i = 0; i < ContactList.size(); i++) {
			message = MessageField.getText();
			for (int j = 0; j < MenuItems.size(); j++) {
				// Usage if replace method to change tags with specified info 
				message = message.replace(MenuItems.get(j).getText(), ContactList.get(i).getInfo().get(j));

			}
			messages.add(message);
		}

		return messages;

	}

	
	// Bunting all info alerts in one method for readability 
	public void InfoAlert(int i) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setHeaderText("Information Alert");
		alert.setTitle("Success");

		switch (i) {
		case 1:
			alert.setContentText("Emails sent !");
			break;
		case 2:
			alert.setContentText("Contact file is successfully loaded!");
			break;
		case 3:
			alert.setContentText("PDF file is successfully created!");
			break;
		}
		alert.show();

	}
	// Bunting all error alerts in one method for readability 
	public void ErrorAlert(int i) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Failed");
		alert.setHeaderText("Error");

		alert.show();
		switch (i) {
		case 1:
			alert.setContentText("Emails did not go through!");
			break;
		case 2:
			alert.setContentText("Some tag is missing square brackets ''[[ ]]'' ");
			break;
		case 3:
			alert.setContentText(" No tags for [[EMAIL_ADDRESS]] or [[EMAIL]] was found ");
			break;
		case 4:
			alert.setContentText("Message Field and subject can not be both embty! ");
			break;
		case 5:
			alert.setContentText("From Field can not be embty! ");
			break;
		case 6:
			alert.setContentText("Password Field can not be embty! ");
			break;
		}
		alert.show();

	}

}
