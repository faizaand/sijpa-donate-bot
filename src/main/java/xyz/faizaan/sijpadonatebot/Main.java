package xyz.faizaan.sijpadonatebot;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main extends Application {

    public Label templateLbl;
    public Label spreadsheetLbl;
    public Label outputLbl;
    public TextField nameField;
    public Label statusLbl;

    private Stage primaryStage;
    private File spreadsheet, template, outputDir;
    private File userPreferredDirectory = new File(System.getProperty("user.home"));

    public static void main(String[] args) throws IOException {
        launch(args);
        /* String fileNameFormat = "[First name][Last name].docx";
        File spreadsheet = new File("C:\\Users\\fdato\\OneDrive\\Documents\\demo.xlsx");
        File template = new File("C:\\Users\\fdato\\OneDrive\\Documents\\demo.docx");
        File out = new File("C:\\Users\\fdato\\OneDrive\\Documents\\demoOutput");
        
        FileHandler handler = new FileHandler(fileNameFormat, spreadsheet, template, out);
        handler.process(); */
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Donation \"Bot\"");
        primaryStage.setWidth(700);
        primaryStage.setHeight(400);

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("scenes/Main.fxml"));

        Scene scene = new Scene(root, 640, 300);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public void btnSelectTemplate(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select the template");
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Word document", "*.docx"));
        chooser.setInitialDirectory(userPreferredDirectory);
        this.template = chooser.showOpenDialog(primaryStage);
        if(this.template == null) return; // they chose to cancel

        this.userPreferredDirectory = this.template.getParentFile();

        templateLbl.setText(this.template.getName());
    }

    public void btnSelectSpreadsheet(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select the spreadsheet");
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Excel document", "*.xlsx"));
        chooser.setInitialDirectory(userPreferredDirectory);
        this.spreadsheet = chooser.showOpenDialog(primaryStage);
        if(this.spreadsheet == null) return; // they chose to cancel

        this.userPreferredDirectory = this.spreadsheet.getParentFile();

        spreadsheetLbl.setText(this.spreadsheet.getName());
    }

    public void btnSelectOutput(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select the output directory");
        chooser.setInitialDirectory(userPreferredDirectory);
        this.outputDir = chooser.showDialog(primaryStage);
        if(this.outputDir == null) return; // they chose to cancel
        this.userPreferredDirectory = this.outputDir.getParentFile();

        outputLbl.setText(this.outputDir.getName());
    }

    public void btnGenerate(ActionEvent actionEvent) {
        if(nameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You must provide a file name format!", ButtonType.CLOSE);
            alert.show();
            return;
        }

        this.statusLbl.setText("Working...");
        this.statusLbl.setVisible(true);

        try {
            FileHandler handler = new FileHandler(nameField.getText(), spreadsheet, template, outputDir);
            handler.process();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Your files have been generated!", ButtonType.CLOSE);
            alert.show();
            this.statusLbl.setText("Ready.");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Generation error");
            alert.setHeaderText("An error occurred while generating your files :(");
            alert.setContentText(e.getMessage());
            if(e instanceof NullPointerException) alert.setContentText("Please select all of the necessary files.");

            this.statusLbl.setText("Failed.");

            // Create expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        }
    }

}
