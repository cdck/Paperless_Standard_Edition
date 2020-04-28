package xlk.paperless.standard.util;


import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.bean.SubmitMember;
import xlk.paperless.standard.data.exportbean.ExportSubmitMember;

/**
 * @author xlk
 * @date 2020/4/3
 * @Description:
 */
public class JxlUtil {
    private static final String TAG = "JxlUtil-->";


    public static File createXlsFile(String fileName) {
        File file = new File(fileName + ".xls");
        String s = DateUtil.nowDate(System.currentTimeMillis());
        if (file.exists()) {
            return createXlsFile(fileName + "-" + s);
        } else {
            return file;
        }
    }

    /**
     * WritableSheet.mergeCells(0, 0, 0, 1);//合并单元格，
     * 第一个参数：要合并的单元格最左上角的列号，
     * 第二个参数：要合并的单元格最左上角的行号，
     * 第三个参数：要合并的单元格最右角的列号，
     * 第四个参数：要合并的单元格最右下角的行号，
     * new Label(0, 1, "序号");
     * 第一个参数：列
     * 第二个参数：行
     * 第三个参数：内容
     */
    public static void exportSubmitMember(ExportSubmitMember info, Context context) {
        FileUtil.createDir(Constant.export_dir);
        String fileName = "参会人投票-选举详情";
        //1.创建Excel文件
        File file = createXlsFile(Constant.export_dir + fileName);
        try {
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建Sheet
            WritableSheet ws = workbook.createSheet("参会人投票-选举详情", 0);
            //4.创建单元格
            Label label;

            WritableCellFormat wc = new WritableCellFormat();
            wc.setAlignment(Alignment.CENTRE); // 设置居中
            wc.setBorder(Border.ALL, BorderLineStyle.THIN); // 设置边框线
            wc.setBackground(Colour.WHITE); // 设置单元格的背景颜色
            //5.编辑单元格
            //合并单元格作为标题
            ws.mergeCells(0, 0, 2, 1);
            label = new Label(0, 0, "人员统计详情", wc);
            ws.addCell(label);

            ws.mergeCells(0, 2, 2, 2);
            label = new Label(0, 2, info.getCreateTime(), wc);
            ws.addCell(label);

            ws.mergeCells(0, 3, 2, 3);
            label = new Label(0, 3, "标题：" + info.getTitle(), wc);
            ws.addCell(label);

            ws.mergeCells(0, 4, 2, 4);
            label = new Label(0, 4, info.getYd() + info.getSd() + info.getYt() + info.getWt(), wc);
            ws.addCell(label);

            label = new Label(0, 5, "序号", wc);
            ws.addCell(label);
            label = new Label(1, 5, "参会人-提交人-姓名", wc);
            ws.addCell(label);
            label = new Label(2, 5, "选择的项", wc);
            ws.addCell(label);
            List<SubmitMember> submitMembers = info.getSubmitMembers();
            for (int i = 0; i < submitMembers.size(); i++) {
                int number = i + 1;
                label = new Label(0, 5 + number, String.valueOf(number), wc);
                ws.addCell(label);
                label = new Label(1, 5 + number, submitMembers.get(i).getMemberInfo().getMembername().toStringUtf8(), wc);
                ws.addCell(label);
                label = new Label(2, 5 + number, submitMembers.get(i).getAnswer(), wc);
                ws.addCell(label);
            }
            //6.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //7.最后一步，关闭工作簿
            workbook.close();
            ToastUtil.show(context, R.string.export_successful);
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }
}
