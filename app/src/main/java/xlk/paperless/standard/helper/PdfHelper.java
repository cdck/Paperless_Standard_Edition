package xlk.paperless.standard.helper;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.util.FileUtil;

/**
 * @author Created by xlk on 2020/11/12.
 * @desc
 */
public class PdfHelper {
    public static void exportPdf(String fileName) {
        try {
            FileUtil.createDir(Constant.DIR_EXPORT);
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(Constant.DIR_EXPORT + fileName + ".pdf"));
            document.open();
            BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font boldFont14 = new Font(bfChinese, 14, Font.BOLD);
            Font boldFont16 = new Font(bfChinese, 16, Font.BOLD);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
