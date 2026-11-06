package app.resumetor.export;

import java.io.File;
import java.io.IOException;

public interface DocumentExporter {
    void export(String content, File destination) throws IOException;
}
