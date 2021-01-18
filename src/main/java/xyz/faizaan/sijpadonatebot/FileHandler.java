package xyz.faizaan.sijpadonatebot;

import com.xandryex.WordReplacer;
import org.apache.commons.lang3.Validate;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private LocalDate letterDateFormat;

    public FileHandler(LocalDate letterDateFormat, File spreadsheet, File template, File outputDir) {
        Validate.isTrue(spreadsheet.isFile(), "The location provided for the spreadsheet is not a file!");
        Validate.isTrue(template.isFile(), "The location provided for the template is not a file!");
        Validate.isTrue(outputDir.isDirectory(), "The output directory provided is not a directory!");

        this.letterDateFormat = letterDateFormat;
        this.spreadsheet = spreadsheet;
        this.template = template;
        this.outputDir = outputDir;
    }

    /**
     * Process the files, putting the results in the output directory.
     */

    public boolean modifyOne(ExcelHandler handler, String name) throws IOException, XmlException {
        Donor donor = handler.getDonorInfo(name);
        if(donor == null) return false; //skipping

        String fileName = donor.firstName + " " + donor.lastName + ".docx";

        File newFile = new File(outputDir, fileName);
        newFile = Files.copy(template.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING).toFile();

        WordReplacer wordReplacer = new WordReplacer(newFile);
        wordReplacer.replaceWordsInText("[LetterDate]", letterDateFormat.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        wordReplacer.replaceWordsInText("[LetterYear]", letterDateFormat.format(DateTimeFormatter.ofPattern("yyyy")));
        wordReplacer.replaceWordsInText("[First Name]", donor.firstName);
        wordReplacer.replaceWordsInText("[First Name]", donor.firstName);
        wordReplacer.replaceWordsInText("[First Name]", donor.firstName);
        wordReplacer.replaceWordsInText("[Last Name]", donor.lastName);
        wordReplacer.replaceWordsInText("[Address]", donor.address);
        wordReplacer.replaceWordsInText("[City]", donor.city);
        wordReplacer.replaceWordsInText("[State]", donor.state);
        wordReplacer.replaceWordsInText("[Zip Code]", donor.zipCode);

        Transaction transaction1 = donor.transactions.get(0);
        wordReplacer.replaceWordsInTables("[Date1]", transaction1.date);
        wordReplacer.replaceWordsInTables("[Amount1]", escapeAllDollars(transaction1.amount));
        wordReplacer.replaceWordsInTables("[Type1]", transaction1.type);
        wordReplacer.replaceWordsInTables("[Chknum1]", transaction1.checkNum);
        wordReplacer.replaceWordsInTables("[Desc1]", transaction1.subcategory.isEmpty() ? escapeAllDollars(transaction1.category) : escapeAllDollars(transaction1.subcategory));

        wordReplacer.saveAndGetModdedFile(newFile);

        // table transactions
        FileInputStream stream = new FileInputStream(newFile);
        XWPFDocument doc = new XWPFDocument(stream);
        XWPFTable table = doc.getTableArray(1);

        //insert new row, which is a copy of row 2, as new row 3:
        for(int i = 0; i < donor.transactions.size() - 1; i++) {
            int rowId = i + 2;

            XWPFTableRow oldRow = table.getRow(1);
            CTRow ctrow = CTRow.Factory.parse(oldRow.getCtRow().newInputStream());
            XWPFTableRow newRow = new XWPFTableRow(ctrow, table);

            int cellId = 0;
            Transaction trans = donor.transactions.get(rowId - 1);
            for (XWPFTableCell cell : newRow.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    for (XWPFRun run : paragraph.getRuns()) {
                        cellId++;
                        if(cellId == 1) run.setText(trans.date, 0);
                        if(cellId == 2) run.setText(trans.amount, 0);
                        if(cellId == 3) run.setText(trans.type, 0);
                        if(cellId == 4) run.setText(trans.subcategory.isEmpty() ? trans.category : trans.subcategory, 0);
                    }
                }
            }

            table.addRow(newRow, rowId);
        }

        doc.write(new FileOutputStream(newFile));
        doc.close();

        return true;
    }

    private String escapeAllDollars(String in) {
        return in.replace("$", "\\$");
    }

}
