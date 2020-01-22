import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

public class ReturnPDValues {
    public PDPageContentStream contentStream;
    public PDPage page;

    /* Constructor */
    public ReturnPDValues(PDPageContentStream contentStream, PDPage page) {
        this.contentStream = contentStream;
        this.page = page;
    }
}