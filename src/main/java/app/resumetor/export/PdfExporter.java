package app.resumetor.export;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Renders DocumentFormatter markup into a clean single-column, ATS-friendly PDF. */
public class PdfExporter implements DocumentExporter {

    private static final float MARGIN = 54f;
    private static final PDRectangle PAGE_SIZE = PDRectangle.LETTER;

    private final PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private PDDocument doc;
    private PDPageContentStream cs;
    private float y;

    @Override
    public void export(String content, File destination) throws IOException {
        try (PDDocument document = build(content)) {
            document.save(destination);
        }
    }

    /** Builds the PDF in memory. Caller must close the returned document. */
    public PDDocument build(String content) throws IOException {
        doc = new PDDocument();
        newPage();
        float width = PAGE_SIZE.getWidth() - 2 * MARGIN;
        boolean inHeader = false;

        for (String rawLine : content.split("\n")) {
            String line = sanitize(rawLine);
            if (line.isBlank()) { inHeader = false; y -= 6; continue; }

            if (line.startsWith("# ")) {
                writeCentered(line.substring(2), bold, 20);
                y -= 2;
                inHeader = true;
            } else if (inHeader) {
                for (String w : wrap(line, regular, 10, width)) writeCentered(w, regular, 10);
            } else if (line.startsWith("## ")) {
                y -= 8;
                ensureSpace(30);
                writeLine(line.substring(3).toUpperCase(), bold, 12, MARGIN);
                cs.setLineWidth(0.7f);
                cs.moveTo(MARGIN, y + 2);
                cs.lineTo(PAGE_SIZE.getWidth() - MARGIN, y + 2);
                cs.stroke();
                y -= 4;
            } else if (line.startsWith("### ")) {
                y -= 3;
                for (String w : wrap(line.substring(4), bold, 11, width)) writeLine(w, bold, 11, MARGIN);
            } else if (line.startsWith("- ")) {
                List<String> wrapped = wrap(line.substring(2), regular, 10.5f, width - 14);
                for (int i = 0; i < wrapped.size(); i++) {
                    if (i == 0) writeLineNoAdvance("-", regular, 10.5f, MARGIN + 2);
                    writeLine(wrapped.get(i), regular, 10.5f, MARGIN + 14);
                }
            } else {
                for (String w : wrap(line, regular, 10.5f, width)) writeLine(w, regular, 10.5f, MARGIN);
            }
        }
        cs.close();
        PDDocument result = doc;
        doc = null;
        return result;
    }

    private void newPage() throws IOException {
        if (cs != null) cs.close();
        PDPage page = new PDPage(PAGE_SIZE);
        doc.addPage(page);
        cs = new PDPageContentStream(doc, page);
        y = PAGE_SIZE.getHeight() - MARGIN;
    }

    private void ensureSpace(float needed) throws IOException {
        if (y - needed < MARGIN) newPage();
    }

    private void writeLine(String text, PDFont font, float size, float x) throws IOException {
        writeLineNoAdvance(text, font, size, x);
        y -= size * 1.35f;
    }

    private void writeLineNoAdvance(String text, PDFont font, float size, float x) throws IOException {
        ensureSpace(size * 1.35f);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y - size);
        cs.showText(text);
        cs.endText();
    }

    private void writeCentered(String text, PDFont font, float size) throws IOException {
        float w = font.getStringWidth(text) / 1000 * size;
        writeLine(text, font, size, (PAGE_SIZE.getWidth() - w) / 2);
    }

    private List<String> wrap(String text, PDFont font, float size, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (font.getStringWidth(candidate) / 1000 * size > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    /** Standard-14 fonts only support WinAnsi; normalize common unicode punctuation. */
    private static String sanitize(String s) {
        return s.replace('\u2013', '-').replace('\u2014', '-').replace('\u2022', '-')
                .replace('\u2018', '\'').replace('\u2019', '\'')
                .replace('\u201C', '"').replace('\u201D', '"')
                .replace("\u2026", "...").replace('\t', ' ')
                .replaceAll("[^\\x20-\\x7E]", "")
                .replace("**", "");
    }
}
