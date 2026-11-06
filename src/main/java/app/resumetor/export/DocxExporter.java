package app.resumetor.export;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DocxExporter implements DocumentExporter {
    
    @Override
    public void export(String content, File destination) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(destination)) {
             
            String[] lines = content.split("\n");
            boolean inHeader = false;

            for (String rawLine : lines) {
                String line = rawLine.replace("**", "");
                if (line.isBlank()) { inHeader = false; continue; }

                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setSpacingAfter(60);
                XWPFRun run = paragraph.createRun();
                run.setFontFamily("Arial");
                run.setFontSize(11);

                if (line.startsWith("# ")) {
                    inHeader = true;
                    paragraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
                    run.setBold(true);
                    run.setFontSize(20);
                    run.setText(line.substring(2));
                } else if (inHeader) {
                    paragraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
                    run.setFontSize(10);
                    run.setText(line);
                } else if (line.startsWith("## ")) {
                    paragraph.setSpacingBefore(200);
                    paragraph.setBorderBottom(org.apache.poi.xwpf.usermodel.Borders.SINGLE);
                    run.setBold(true);
                    run.setFontSize(12);
                    run.setText(line.substring(3).toUpperCase());
                } else if (line.startsWith("### ")) {
                    paragraph.setSpacingBefore(80);
                    run.setBold(true);
                    run.setText(line.substring(4));
                } else if (line.startsWith("- ")) {
                    paragraph.setIndentationLeft(280);
                    paragraph.setIndentationHanging(200);
                    run.setText("•  " + line.substring(2));
                } else {
                    run.setText(line);
                }
            }
            
            document.write(out);
        }
    }
}
