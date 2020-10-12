package xlk.paperless.standard.util;


import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfacePerson;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.bean.SubmitMember;
import xlk.paperless.standard.data.exportbean.ExportSubmitMember;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author xlk
 * @date 2020/4/3
 * @desc 将信息导出成xls表格
 * WritableSheet.mergeCells(0, 0, 0, 1);//合并单元格，
 * 第一个参数：要合并的单元格最左上角的列号，
 * 第二个参数：要合并的单元格最左上角的行号，
 * 第三个参数：要合并的单元格最右角的列号，
 * 第四个参数：要合并的单元格最右下角的行号，
 * new Label(column c, row r, String cont, CellFormat st);
 * 第一个参数：列
 * 第二个参数：行
 * 第三个参数：内容
 * 第四个参数：单元格格式
 */
public class JxlUtil {
    private static final String TAG = "JxlUtil-->";

    private static File createXlsFile(String fileName) {
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
    public static void exportSubmitMember(ExportSubmitMember info) {
        FileUtil.createDir(Constant.dir_export);
        String fileName = "参会人投票-选举详情";
        //1.创建Excel文件
        File file = createXlsFile(Constant.dir_export + fileName);
        try {
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建Sheet
            WritableSheet ws = workbook.createSheet(fileName, 0);
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

            //创建表格的时间
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
            ToastUtil.show(R.string.export_successful);
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出投票/选举内容
     *
     * @param votes    投票信息
     * @param fileName 文件名（不需要后缀）
     * @param content  内容描述
     */
    public static void exportVoteInfo(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> votes, String fileName, String content) {
        FileUtil.createDir(Constant.dir_export);
        //1.创建Excel文件
        File file = createXlsFile(Constant.dir_export + fileName);
        try {
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建Sheet
            WritableSheet ws = workbook.createSheet(fileName, 0);
            //4.创建单元格
            Label label;
            //配置单元格样式
            WritableCellFormat wc = new WritableCellFormat();
            wc.setAlignment(Alignment.CENTRE); // 设置居中
            wc.setBorder(Border.ALL, BorderLineStyle.THIN); // 设置边框线
            wc.setBackground(Colour.WHITE); // 设置单元格的背景颜色

            label = new Label(0, 0, content, wc);
            ws.addCell(label);
            label = new Label(1, 0, "是否记名[1：是 0：否]", wc);
            ws.addCell(label);
            label = new Label(2, 0, "选项总数量", wc);
            ws.addCell(label);
            label = new Label(3, 0, "答案数量[0：表示任意]", wc);
            ws.addCell(label);
            label = new Label(4, 0, "选项1", wc);
            ws.addCell(label);
            label = new Label(5, 0, "选项2", wc);
            ws.addCell(label);
            label = new Label(6, 0, "选项3", wc);
            ws.addCell(label);
            label = new Label(7, 0, "选项4", wc);
            ws.addCell(label);
            label = new Label(8, 0, "选项5", wc);
            ws.addCell(label);
            for (int i = 0; i < votes.size(); i++) {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo info = votes.get(i);
                //投票内容
                String cont = info.getContent().toStringUtf8().trim();
                label = new Label(0, i + 1, cont, wc);
                ws.addCell(label);
                //是否记名
                int mode = info.getMode();
                label = new Label(1, i + 1, mode + "", wc);
                ws.addCell(label);
                //选项总数量
                int selectcount = info.getSelectcount();
                label = new Label(2, i + 1, selectcount + "", wc);
                ws.addCell(label);
                //答案数量
                int type = info.getType();
                switch (type) {
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE_VALUE://单选
                        label = new Label(3, i + 1, 1 + "", wc);
                        break;
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_4_5_VALUE://5选4
                        label = new Label(3, i + 1, 4 + "", wc);
                        break;
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_3_5_VALUE:
                        label = new Label(3, i + 1, 3 + "", wc);
                        break;
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_5_VALUE:
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_3_VALUE:
                        label = new Label(3, i + 1, 2 + "", wc);
                        break;
                    default:
                        label = new Label(3, i + 1, 0 + "", wc);
                        break;
                }
                ws.addCell(label);
                List<InterfaceVote.pbui_SubItem_VoteItemInfo> itemList = info.getItemList();
                for (int j = 0; j < itemList.size(); j++) {
                    InterfaceVote.pbui_SubItem_VoteItemInfo item = itemList.get(j);
                    String trim = item.getText().toStringUtf8().trim();
                    label = new Label(4 + j, i + 1, trim, wc);
                    ws.addCell(label);
                }
            }
            //6.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //7.最后一步，关闭工作簿
            workbook.close();
            ToastUtil.show(R.string.export_successful);
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将xls文件解析成投票
     *
     * @param filePath xls文件路径
     * @param mainType 投票/选举
     * @see InterfaceMacro.Pb_MeetVoteType
     */
    public static List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> readVoteXls(String filePath, int mainType) {
        File file = new File(filePath);
        if (!file.exists()) {
            LogUtil.e(TAG, "readVoteXls 没有找到该文件：" + filePath);
            return null;
        }
        List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> temps = new ArrayList<>();
        InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
        try {
            InputStream is = new FileInputStream(filePath);
            //使用jxl
            Workbook rwb = Workbook.getWorkbook(is);
            //有多少张表
            Sheet[] sheets = rwb.getSheets();
//            for (int i = 0; i < sheets.length; i++) {
            Sheet sheet = sheets[0];
            //有多少列
            int columns = sheet.getColumns();
            //有多少行
            int rows = sheet.getRows();
            int total = 0, answer = 0;
            //r=1 过滤掉第一行的标题
            for (int r = 1; r < rows; r++) {
                builder.setMaintype(mainType);
                List<ByteString> all = new ArrayList<>();
                int selectcount = 0;
                for (int c = 0; c < columns; c++) {
                    Cell cell = sheet.getCell(c, r);
                    String contents = cell.getContents();
                    switch (c) {//列数
                        case 0:// 投票内容
                            builder.setContent(s2b(contents));
                            break;
                        case 1:// 是否记名
                            int mode = Integer.parseInt(contents);
                            builder.setMode(mode);
                            break;
                        case 2:// 选项总数
                            total = Integer.parseInt(contents);
                            break;
                        case 3:// 答案数量
                            answer = Integer.parseInt(contents);
                            LogUtil.d(TAG, "readVoteXls -->选项总数：" + total + ", 答案数量：" + answer);
                            if (total != 0) {
                                if (answer == 0) {//多选
                                    builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_MANY_VALUE);
                                } else if (answer == 1) {//单选
                                    builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE_VALUE);
                                } else if (total == 5) {
                                    if (answer == 2) {//5选2
                                        builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_5_VALUE);
                                    } else if (answer == 3) {//5选3
                                        builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_3_5_VALUE);
                                    } else if (answer == 4) {//5选4
                                        builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_4_5_VALUE);
                                    }
                                } else if (total == 3) {
                                    if (answer == 2) {//3选2
                                        builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_3_VALUE);
                                    }
                                }
                            }
                            break;
                        case 4:// 选项1
                        case 5:// 选项2
                        case 6:// 选项3
                        case 7:// 选项4
                        case 8:// 选项5
                            LogUtil.d(TAG, "readVoteXls -->添加答案：" + contents);
                            if (!contents.isEmpty()) {
                                selectcount++;
                                all.add(s2b(contents));
                            }
                            break;
                    }
                }
                builder.setSelectcount(selectcount);
                builder.addAllText(all);//答案
                InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
                temps.add(build);
            }
//            }
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return temps;
    }

    /**
     * 将常用人员导出到Excel表格
     *
     * @param memberInfos 常用人员集合
     */
    public static void exportMember(List<InterfacePerson.pbui_Item_PersonDetailInfo> memberInfos) {
        {
            FileUtil.createDir(Constant.dir_export);
            //1.创建Excel文件
            File file = createXlsFile(Constant.dir_export + "常用参会人");
            try {
                file.createNewFile();
                //2.创建工作簿
                WritableWorkbook workbook = Workbook.createWorkbook(file);
                //3.创建Sheet
                WritableSheet ws = workbook.createSheet("常用人员", 0);
                //4.创建单元格
                Label label;
                //配置单元格样式
                WritableCellFormat wc = new WritableCellFormat();
                wc.setAlignment(Alignment.CENTRE); // 设置居中
                wc.setBorder(Border.ALL, BorderLineStyle.THIN); // 设置边框线
                wc.setBackground(Colour.WHITE); // 设置单元格的背景颜色

                label = new Label(0, 0, "人员姓名", wc);
                ws.addCell(label);
                label = new Label(1, 0, "单位", wc);
                ws.addCell(label);
                label = new Label(2, 0, "职位", wc);
                ws.addCell(label);
                label = new Label(3, 0, "备注", wc);
                ws.addCell(label);
                label = new Label(4, 0, "手机", wc);
                ws.addCell(label);
                label = new Label(5, 0, "邮箱", wc);
                ws.addCell(label);
                label = new Label(6, 0, "签到密码", wc);
                ws.addCell(label);
                label = new Label(7, 0, "人员ID", wc);
                ws.addCell(label);
                for (int i = 0; i < memberInfos.size(); i++) {
                    InterfacePerson.pbui_Item_PersonDetailInfo info = memberInfos.get(i);
                    //人员姓名
                    String name = info.getName().toStringUtf8();
                    label = new Label(0, i + 1, name, wc);
                    ws.addCell(label);
                    //单位
                    String company = info.getCompany().toStringUtf8();
                    label = new Label(1, i + 1, company, wc);
                    ws.addCell(label);
                    //职位
                    String job = info.getJob().toStringUtf8();
                    label = new Label(2, i + 1, job, wc);
                    ws.addCell(label);
                    //备注
                    String comment = info.getComment().toStringUtf8();
                    label = new Label(3, i + 1, comment, wc);
                    ws.addCell(label);
                    //手机
                    String phone = info.getPhone().toStringUtf8();
                    label = new Label(4, i + 1, phone, wc);
                    ws.addCell(label);
                    //邮箱
                    String email = info.getEmail().toStringUtf8();
                    label = new Label(5, i + 1, email, wc);
                    ws.addCell(label);
                    //签到密码
                    String pwd = info.getPassword().toStringUtf8();
                    label = new Label(6, i + 1, pwd, wc);
                    ws.addCell(label);
                    //人员id
                    int id = info.getPersonid();
                    label = new Label(7, i + 1, id + "", wc);
                    ws.addCell(label);
                }
                //6.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
                workbook.write();
                //7.最后一步，关闭工作簿
                workbook.close();
                ToastUtil.show(R.string.export_successful);
            } catch (IOException | WriteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从表格中读取常用参会人信息
     */
    public static List<InterfacePerson.pbui_Item_PersonDetailInfo> readMemberXls(String filePath) {
        List<InterfacePerson.pbui_Item_PersonDetailInfo> memberInofs = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            LogUtil.e(TAG, "readMemberXls 没有找到该文件：" + filePath);
            return memberInofs;
        }
        InterfacePerson.pbui_Item_PersonDetailInfo.Builder builder = InterfacePerson.pbui_Item_PersonDetailInfo.newBuilder();
        try {
            InputStream is = new FileInputStream(filePath);
            //使用jxl
            Workbook rwb = Workbook.getWorkbook(is);
            //有多少张表
            Sheet[] sheets = rwb.getSheets();
            //获取表格
            Sheet sheet = sheets[0];
            //有多少列
            int columns = sheet.getColumns();
            //有多少行
            int rows = sheet.getRows();
            //r=1 过滤掉第一行的标题
            for (int r = 1; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    Cell cell = sheet.getCell(c, r);
                    String contents = cell.getContents();
                    switch (c) {//列数
                        case 0:// 人员姓名
                            builder.setName(s2b(contents));
                            break;
                        case 1:// 单位
                            builder.setCompany(s2b(contents));
                            break;
                        case 2:// 职位
                            builder.setJob(s2b(contents));
                            break;
                        case 3:// 备注
                            builder.setComment(s2b(contents));
                            break;
                        case 4:// 手机
                            builder.setPhone(s2b(contents));
                            break;
                        case 5:// 邮箱
                            builder.setEmail(s2b(contents));
                            break;
                        case 6:// 签到密码
                            builder.setPassword(s2b(contents));
                            break;
                        case 7:// 人员ID
                            builder.setPersonid(Integer.parseInt(contents));
                            break;
                    }
                }
                InterfacePerson.pbui_Item_PersonDetailInfo build = builder.build();
                memberInofs.add(build);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return memberInofs;
    }
}
