package xyz.faizaan.sijpadonatebot;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main extends Application {

    public Label templateLbl;
    public Label spreadsheetLbl;
    public Label outputLbl;
    public Label statusLbl;
    public DatePicker datePicker;
    public ProgressBar progress;

    private Stage primaryStage;
    private File spreadsheet, template, outputDir;
    private File userPreferredDirectory = new File(System.getProperty("user.home"));

    public static void main(String[] args) throws Exception {
        launch(args);
        /* String fileNameFormat = "[First name][Last name].docx";
        File spreadsheet = new File("C:\\Users\\fdato\\OneDrive\\Documents\\demo.xlsx");
        File template = new File("C:\\Users\\fdato\\OneDrive\\Documents\\demo.docx");
        File out = new File("C:\\Users\\fdato\\OneDrive\\Documents\\demoOutput");

        FileHandler handler = new FileHandler(fileNameFormat, spreadsheet, template, out);
        handler.process(); */

//        File spreadsheet = new File("C:\\Users\\fdato\\OneDrive\\Desktop\\SIJPA Letter Generator\\Specifications\\Donation Letter Generation List.xlsx");
//        ExcelHandler handler = new ExcelHandler(spreadsheet);
//        System.out.println(handler.getDonorInfo("Hussein Albumohammed").toString());
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
        File f = chooser.showOpenDialog(primaryStage);
        if (f == null) return; // they chose to cancel
        this.template = f;

        this.userPreferredDirectory = this.template.getParentFile();

        templateLbl.setText(this.template.getName());
    }

    public void btnSelectSpreadsheet(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select the spreadsheet");
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Excel document", "*.xlsx"));
        chooser.setInitialDirectory(userPreferredDirectory);
        File f = chooser.showOpenDialog(primaryStage);
        if (f == null) return; // they chose to cancel
        this.spreadsheet = f;

        this.userPreferredDirectory = this.spreadsheet.getParentFile();

        spreadsheetLbl.setText(this.spreadsheet.getName());
    }

    public void btnSelectOutput(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select the output directory");
        chooser.setInitialDirectory(userPreferredDirectory);
        File f = chooser.showDialog(primaryStage);
        if (f == null) return; // they chose to cancel
        this.outputDir = f;
        this.userPreferredDirectory = this.outputDir.getParentFile();

        outputLbl.setText(this.outputDir.getName());
    }

    public void btnGenerate(ActionEvent actionEvent) {
        if (datePicker.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You must provide a letter date!", ButtonType.CLOSE);
            alert.show();
            return;
        }

        FileHandler handler = new FileHandler(datePicker.getValue(), spreadsheet, template, outputDir);

        statusLbl.setText("Working... this could take several minutes.");
        statusLbl.setFont(new Font("System", 14));

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                ExcelHandler excelHandler = new ExcelHandler(spreadsheet);
                List<String> names = excelHandler.getAllNames();

                int fails = 0;

                int index = 0;
                for (String name : names) {
                    if(!handler.modifyOne(excelHandler, name)) fails++;
                    index++;

                    updateProgress(index, names.size());
                    System.out.println("Finished " + index + "/" + names.size());
                }

                System.out.println("Done");

                return fails;
            }
        };

        task.setOnSucceeded(event -> {
            try {
                if(task.get() == 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Your files have been generated!");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Your files were generated, but " + task.get() + " rows failed to yield valid data. Check to make sure" +
                            "there are no errors in your spreadsheet, such as trailing whitespace in the names.");
                    alert.show();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            statusLbl.setText("Done.");
            progress.setVisible(false);
        });

        task.setOnFailed(event -> {
            progress.setVisible(false);
            Throwable e = task.getException();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Generation error");
            alert.setHeaderText("An error occurred while generating your files :(");
            alert.setContentText(e.getMessage());
            if (e instanceof NullPointerException)
                alert.setContentText("Please select all of the necessary files.");

            statusLbl.setText("Failed.");

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
        });

        progress.progressProperty().bind(task.progressProperty());
        progress.setVisible(true);

        Thread t = new Thread(task);
        t.start();

    }

}
