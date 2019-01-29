package xyz.faizaan.sijpadonatebot;

import com.xandryex.WordReplacer;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for taking in data from XLSX spreadsheet and outputting it into a DOCX spreadsheet.
 *
 * @author Faizaan Datoo
 * @since 1.0.0
 */
public class FileHandler {

    private File spreadsheet, template, outputDir;
    private String fileNameFormat;

    public FileHandler(String fileNameFormat, File spreadsheet, File template, File outputDir) {
        Validate.isTrue(spreadsheet.isFile(), "The location provided for the spreadsheet is not a file!");
        Validate.isTrue(template.isFile(), "The location provided for the template is not a file!");
        Validate.isTrue(outputDir.isDirectory(), "The output directory provided is not a directory!");

        this.fileNameFormat = fileNameFormat;
        this.spreadsheet = spreadsheet;
        this.template = template;
        this.outputDir = outputDir;
    }

    /**
     * Process the files, putting the results in the output directory.
     */
    public void process() throws IOException {
        Map<String, List<String>> spreadsheet = readSpreadsheet();

        for(int i = 0; i < spreadsheet.values().iterator().next().size(); i++)
            modifyOne(spreadsheet, i);

        System.out.println("Done");
    }

    private Map<String, List<String>> readSpreadsheet() throws IOException {
        List<List<String>> rawData = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(spreadsheet);
        Sheet sheet = workbook.getSheetAt(0); // we only deal with the first sheet for now

        for (Row row : sheet) {
            int colIndex = 0;
            for (Cell cell : row) {
                if (colIndex >= rawData.size()) rawData.add(new ArrayList<>());
                List<String> col = rawData.get(colIndex);

                if (cell.getCellType() == CellType.NUMERIC) col.add(String.valueOf(cell.getNumericCellValue()));
                if (cell.getCellType() == CellType.STRING) col.add(String.valueOf(cell.getStringCellValue()));

                colIndex++;
            }
        }

        Map<String, List<String>> formattedData = new HashMap<>();
        for (List<String> column : rawData) {
            List<String> values = column.subList(1, column.size());
            formattedData.put(column.get(0), values);
        }

        return formattedData;
    }

    private void modifyOne(Map<String, List<String>> spreadsheet, int columnNum) throws IOException {
        String fileName = fileNameFormat;

        Pattern p = Pattern.compile("\\[(.*?)]");
        Matcher m = p.matcher(fileNameFormat);
        while (m.find()) {
            String columnName = m.group(1);
            if (!spreadsheet.containsKey(columnName) || spreadsheet.get(columnName).size() < columnNum)
                fileName = fileName.replace(columnName, "undefined");
            fileName = fileName.replace(columnName, spreadsheet.get(columnName).get(columnNum));
        }
        fileName = fileName.replace("[", "").replace("]", "");

        File newFile = new File(outputDir, fileName);
        newFile = Files.copy(template.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING).toFile();

        WordReplacer wordReplacer = new WordReplacer(newFile);
        for (String columnName : spreadsheet.keySet()) {
            String searchTerm = "[" + columnName + "]";
            List<String> col = spreadsheet.get(columnName);
            if(col.size() < columnNum)
                wordReplacer.replaceWordsInText(searchTerm, "undefined");
            else
                wordReplacer.replaceWordsInText(searchTerm, col.get(columnNum));
        }

        wordReplacer.saveAndGetModdedFile(newFile);

    }

}
