package xyz.faizaan.sijpadonatebot;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) throws IOException {
//        launch(args);
        String fileNameFormat = "[First name][Last name].docx";
        File spreadsheet = new File("C:\\Users\\fdato\\OneDrive\\Documents\\demo.xlsx");
        File template = new File("C:\\Users\\fdato\\OneDrive\\Documents\\demo.docx");
        File out = new File("C:\\Users\\fdato\\OneDrive\\Documents\\demoOutput");
        
        FileHandler handler = new FileHandler(fileNameFormat, spreadsheet, template, out);
        handler.process();
    }

    @Override
    public void start(Stage primaryStage) {

    }
}
