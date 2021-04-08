package xlk.paperless.standard.util;


import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePerson;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;
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
import xlk.paperless.standard.view.App;
import xlk.paperless.standard.view.admin.fragment.after.signin.SignInBean;
import xlk.paperless.standard.view.admin.fragment.pre.bind.ReadJxlBean;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleBean;

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
        String s = DateUtil.nowDate();
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
        FileUtil.createDir(Constant.DIR_EXPORT);
        String fileName = "参会人投票-选举详情";
        //1.创建Excel文件
        File file = createXlsFile(Constant.DIR_EXPORT + fileName);
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
        FileUtil.createDir(Constant.DIR_EXPORT);
        //1.创建Excel文件
        File file = createXlsFile(Constant.DIR_EXPORT + fileName);
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
                    //列数
                    switch (c) {
                        // 投票内容
                        case 0:
                            builder.setContent(s2b(contents));
                            break;
                        // 是否记名
                        case 1:
                            int mode = Integer.parseInt(contents);
                            builder.setMode(mode);
                            break;
                        // 选项总数
                        case 2:
                            total = Integer.parseInt(contents);
                            break;
                        // 答案数量
                        case 3:
                            answer = Integer.parseInt(contents);
                            LogUtil.d(TAG, "readVoteXls -->选项总数：" + total + ", 答案数量：" + answer);
                            if (total != 0) {
                                //多选
                                if (answer == 0) {
                                    builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_MANY_VALUE);
                                    //单选
                                } else if (answer == 1) {
                                    builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE_VALUE);
                                } else if (total == 5) {
                                    //5选2
                                    if (answer == 2) {
                                        builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_5_VALUE);
                                        //5选3
                                    } else if (answer == 3) {
                                        builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_3_5_VALUE);
                                        //5选4
                                    } else if (answer == 4) {
                                        builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_4_5_VALUE);
                                    }
                                } else if (total == 3) {
                                    //3选2
                                    if (answer == 2) {
                                        builder.setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_3_VALUE);
                                    }
                                }
                            }
                            break;
                        // 选项1/2/3/4/5
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            LogUtil.d(TAG, "readVoteXls -->添加选项：" + contents);
                            if (!contents.isEmpty()) {
                                selectcount++;
                                all.add(s2b(contents));
                            }
                            break;
                        default:
                            break;
                    }
                }
                builder.setSelectcount(selectcount);
                //所有选项
                builder.addAllText(all);
                InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
                temps.add(build);
            }
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
        FileUtil.createDir(Constant.DIR_EXPORT);
        //1.创建Excel文件
        File file = createXlsFile(Constant.DIR_EXPORT + "常用参会人");
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

    /**
     * 读取表格内容生成参会人数据
     *
     * @param filePath 表格文件路径
     */
    public static List<InterfaceMember.pbui_Item_MemberDetailInfo> readMemberInfo(String filePath) {
        List<InterfaceMember.pbui_Item_MemberDetailInfo> temps = new ArrayList<>();
        try {
            InputStream is = new FileInputStream(filePath);
            InterfaceMember.pbui_Item_MemberDetailInfo.Builder builder = InterfaceMember.pbui_Item_MemberDetailInfo.newBuilder();
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
//                        case 7:// 人员ID
//                            builder.setPersonid(Integer.parseInt(contents));
//                            break;
                    }
                }
                InterfaceMember.pbui_Item_MemberDetailInfo build = builder.build();
                temps.add(build);
            }
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
     * 归档会议参会人信息
     *
     * @param devSeatInfos 参会人信息（包括会议身份）
     */
    public static boolean exportMemberInfo(List<MemberRoleBean> devSeatInfos) {
        FileUtil.createDir(Constant.DIR_ARCHIVE_TEMP);
        //1.创建Excel文件
        File file = createXlsFile(Constant.DIR_ARCHIVE_TEMP + "会议参会人");
        try {
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建Sheet
            WritableSheet ws = workbook.createSheet("参会人员", 0);
            //4.创建单元格
            Label label;
            //配置单元格样式
            WritableCellFormat wc = new WritableCellFormat();
            // 设置居中
            wc.setAlignment(Alignment.CENTRE);
            // 设置边框线
            wc.setBorder(Border.ALL, BorderLineStyle.THIN);
            // 设置单元格的背景颜色
            wc.setBackground(Colour.WHITE);

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
            label = new Label(8, 0, "角色", wc);
            ws.addCell(label);
            for (int i = 0; i < devSeatInfos.size(); i++) {
                MemberRoleBean memberRoleBean = devSeatInfos.get(i);
                InterfaceMember.pbui_Item_MemberDetailInfo info = memberRoleBean.getMember();
                InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seat = memberRoleBean.getSeat();
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
                //身份
                String memberRoleName = "";
                if (seat != null) {
                    int role = seat.getRole();
                    memberRoleName = Constant.getMemberRoleName(App.applicationContext, role);
                }
                label = new Label(8, i + 1, memberRoleName, wc);
                ws.addCell(label);
            }
            //6.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //7.最后一步，关闭工作簿
            workbook.close();
            return true;
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 归档会议签到信息
     *
     * @param signInData 签到信息
     */
    public static boolean exportArchiveSignIn(List<SignInBean> signInData) {
        FileUtil.createDir(Constant.DIR_ARCHIVE_TEMP);
        //1.创建Excel文件
        File file = createXlsFile(Constant.DIR_ARCHIVE_TEMP + "会议签到信息");
        try {
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建Sheet
            WritableSheet ws = workbook.createSheet("会议签到信息", 0);
            //4.创建单元格
            Label label;
            //配置单元格样式
            WritableCellFormat wc = new WritableCellFormat();
            // 设置居中
            wc.setAlignment(Alignment.CENTRE);
            // 设置边框线
            wc.setBorder(Border.ALL, BorderLineStyle.THIN);
            // 设置单元格的背景颜色
            wc.setBackground(Colour.WHITE);

            label = new Label(0, 0, "人员ID", wc);
            ws.addCell(label);
            label = new Label(1, 0, "姓名", wc);
            ws.addCell(label);
            label = new Label(2, 0, "签到时间", wc);
            ws.addCell(label);
            label = new Label(3, 0, "签到状态", wc);
            ws.addCell(label);
            label = new Label(4, 0, "签到方式", wc);
            ws.addCell(label);
            for (int i = 0; i < signInData.size(); i++) {
                SignInBean signInBean = signInData.get(i);
                InterfaceMember.pbui_Item_MemberDetailInfo info = signInBean.getMember();
                InterfaceSignin.pbui_Item_MeetSignInDetailInfo sign = signInBean.getSign();
                //人员ID
                String id = String.valueOf(info.getPersonid());
                label = new Label(0, i + 1, id, wc);
                ws.addCell(label);
                //姓名
                String name = info.getName().toStringUtf8();
                label = new Label(1, i + 1, name, wc);
                ws.addCell(label);
                //签到时间
                String time = "";
                if (sign != null) {
                    time = DateUtil.millisecondFormatDetailedTime(sign.getUtcseconds() * 1000);
                }
                label = new Label(2, i + 1, time, wc);
                ws.addCell(label);
                //签到状态
                String signState = "未签到";
                if (sign != null) {
                    signState = "已签到";
                }
                label = new Label(3, i + 1, signState, wc);
                ws.addCell(label);
                //签到方式
                String signTypeString = "";
                if (sign != null) {
                    signTypeString = Constant.getMeetSignInTypeName(sign.getSigninType());
                }
                label = new Label(4, i + 1, signTypeString, wc);
                ws.addCell(label);
            }
            //6.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //7.最后一步，关闭工作簿
            workbook.close();
            return true;
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 归档投票或选举
     *
     * @param voteData 投票/选举信息
     * @param size     参会人总数
     * @param isVote   =true投票，=false选举
     */
    public static boolean exportArchiveVote(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteData, int size, boolean isVote) {
        FileUtil.createDir(Constant.DIR_ARCHIVE_TEMP);
        //1.创建Excel文件
        File file = createXlsFile(Constant.DIR_ARCHIVE_TEMP + (isVote ? "投票结果导出" : "选举结果导出"));
        try {
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建Sheet
            WritableSheet ws = workbook.createSheet("投票信息", 0);
            //4.创建单元格
            Label label;
            //配置单元格样式
            WritableCellFormat wc = new WritableCellFormat();
            // 设置居中
            wc.setAlignment(Alignment.CENTRE);
            // 设置边框线
            wc.setBorder(Border.ALL, BorderLineStyle.THIN);
            // 设置单元格的背景颜色
            wc.setBackground(Colour.WHITE);

            label = new Label(0, 0, "投票ID", wc);
            ws.addCell(label);
            label = new Label(1, 0, "投票内容", wc);
            ws.addCell(label);
            label = new Label(2, 0, "投票类型", wc);
            ws.addCell(label);
            label = new Label(3, 0, "投票匿名", wc);
            ws.addCell(label);
            label = new Label(4, 0, "投票时长（秒）", wc);
            ws.addCell(label);
            label = new Label(5, 0, "投票状态", wc);
            ws.addCell(label);
            label = new Label(6, 0, "投票单多选择", wc);
            ws.addCell(label);
            label = new Label(7, 0, "投票选项数", wc);
            ws.addCell(label);
            label = new Label(8, 0, "选项一", wc);
            ws.addCell(label);
            label = new Label(9, 0, "投票数", wc);
            ws.addCell(label);
            label = new Label(10, 0, "选项二", wc);
            ws.addCell(label);
            label = new Label(11, 0, "投票数", wc);
            ws.addCell(label);
            label = new Label(12, 0, "选项三", wc);
            ws.addCell(label);
            label = new Label(13, 0, "投票数", wc);
            ws.addCell(label);
            if (isVote) {
                label = new Label(14, 0, "参会人总数", wc);
                ws.addCell(label);
            } else {
                label = new Label(14, 0, "选项四", wc);
                ws.addCell(label);
                label = new Label(15, 0, "投票数", wc);
                ws.addCell(label);
                label = new Label(16, 0, "选项五", wc);
                ws.addCell(label);
                label = new Label(17, 0, "投票数", wc);
                ws.addCell(label);
                label = new Label(18, 0, "参会人总数", wc);
                ws.addCell(label);
            }
            for (int i = 0; i < voteData.size(); i++) {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo info = voteData.get(i);
                //选项内容
                List<InterfaceVote.pbui_SubItem_VoteItemInfo> answers = info.getItemList();
                //投票ID
                String id = String.valueOf(info.getVoteid());
                label = new Label(0, i + 1, id, wc);
                ws.addCell(label);
                //投票内容
                String voteContent = info.getContent().toStringUtf8();
                label = new Label(1, i + 1, voteContent, wc);
                ws.addCell(label);
                //投票类型
                String voteMainType = isVote ? "投票" : "选举";
                label = new Label(2, i + 1, voteMainType, wc);
                ws.addCell(label);
                //投票匿名
                String voteMode = info.getMode() == 1 ? "记名" : "匿名";
                label = new Label(3, i + 1, voteMode, wc);
                ws.addCell(label);
                //投票时长
                String timeOut = String.valueOf(info.getTimeouts());
                label = new Label(4, i + 1, timeOut, wc);
                ws.addCell(label);
                //投票状态
                String voteState = "";
                switch (info.getVotestate()) {
                    case InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE:
                        voteState = "未发起";
                        break;
                    case InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE:
                        voteState = "正在进行";
                        break;
                    case InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_endvote_VALUE:
                        voteState = "已经结束";
                        break;
                    default:
                        break;
                }
                label = new Label(5, i + 1, voteState, wc);
                ws.addCell(label);
                //投票选项类型
                String voteType = "";
                switch (info.getType()) {
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_MANY_VALUE:
                        voteType = "多选";
                        break;
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE_VALUE:
                        voteType = "单选";
                        break;
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_4_5_VALUE:
                        voteType = "5选4";
                        break;
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_3_5_VALUE:
                        voteType = "5选3";
                        break;
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_5_VALUE:
                        voteType = "5选2";
                        break;
                    case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_3_VALUE:
                        voteType = "3选2";
                        break;
                    default:
                        break;
                }
                label = new Label(6, i + 1, voteType, wc);
                ws.addCell(label);
                //投票选项数
                String selCount = String.valueOf(info.getSelectcount());
                label = new Label(7, i + 1, selCount, wc);
                ws.addCell(label);
                //获取有选项的个数
                int answerCount = answers.size();

                label = new Label(8, i + 1, answerCount > 0 ? answers.get(0).getText().toStringUtf8() : "", wc);
                ws.addCell(label);
                label = new Label(9, i + 1, answerCount > 0 ? String.valueOf(answers.get(0).getSelcnt()) : String.valueOf(0), wc);
                ws.addCell(label);
                label = new Label(10, i + 1, answerCount > 1 ? answers.get(1).getText().toStringUtf8() : "", wc);
                ws.addCell(label);
                label = new Label(11, i + 1, answerCount > 1 ? String.valueOf(answers.get(1).getSelcnt()) : String.valueOf(0), wc);
                ws.addCell(label);
                label = new Label(12, i + 1, answerCount > 2 ? answers.get(2).getText().toStringUtf8() : "", wc);
                ws.addCell(label);
                label = new Label(13, i + 1, answerCount > 2 ? String.valueOf(answers.get(2).getSelcnt()) : String.valueOf(0), wc);
                ws.addCell(label);
                if (!isVote) {
                    label = new Label(14, i + 1, answerCount > 3 ? answers.get(3).getText().toStringUtf8() : "", wc);
                    ws.addCell(label);
                    label = new Label(15, i + 1, answerCount > 3 ? String.valueOf(answers.get(3).getSelcnt()) : String.valueOf(0), wc);
                    ws.addCell(label);
                    label = new Label(16, i + 1, answerCount > 4 ? answers.get(4).getText().toStringUtf8() : "", wc);
                    ws.addCell(label);
                    label = new Label(17, i + 1, answerCount > 4 ? String.valueOf(answers.get(4).getSelcnt()) : String.valueOf(0), wc);
                    ws.addCell(label);
                    //参会人总数
                    label = new Label(18, i + 1, String.valueOf(size), wc);
                    ws.addCell(label);
                } else {
                    //参会人总数
                    label = new Label(14, i + 1, String.valueOf(size), wc);
                    ws.addCell(label);
                }
            }
            //6.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //7.最后一步，关闭工作簿
            workbook.close();
            return true;
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 读取坐席表格内容
     * @param filePath 表格文件路径
     */
    public static List<ReadJxlBean> readSeatInfo(String filePath) {
        List<ReadJxlBean> readJxlBeans = new ArrayList<>();
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
                ReadJxlBean readJxlBean = new ReadJxlBean();
                for (int c = 0; c < columns; c++) {
                    Cell cell = sheet.getCell(c, r);
                    String contents = cell.getContents();
                    switch (c) {//列数
                        case 0:// 人员姓名
                            readJxlBean.setMemberName(contents);
                            break;
                        case 1:// 坐席ID
                            try {
                                int devId = contents.isEmpty() ? 0 : Integer.valueOf(contents);
                                readJxlBean.setDevId(devId);
                            }catch (Exception e){

                            }
                            break;
                        case 2:// 坐席名称
                            readJxlBean.setDevName(contents);
                            break;
                    }
                }
                readJxlBeans.add(readJxlBean);
            }
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return readJxlBeans;
    }

    /**
     * 导出座位绑定信息
     *
     * @param devSeatInfos 参会人和席位信息
     */
    public static boolean exportSeatInfo(List<MemberRoleBean> devSeatInfos) {
        FileUtil.createDir(Constant.DIR_EXPORT);
        //1.创建Excel文件
        File file = createXlsFile(Constant.DIR_EXPORT + "坐席表");
        try {
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建Sheet
            WritableSheet ws = workbook.createSheet("坐席表", 0);
            //4.创建单元格
            Label label;
            //配置单元格样式
            WritableCellFormat wc = new WritableCellFormat();
            // 设置居中
            wc.setAlignment(Alignment.CENTRE);
            // 设置边框线
            wc.setBorder(Border.ALL, BorderLineStyle.THIN);
            // 设置单元格的背景颜色
            wc.setBackground(Colour.WHITE);

            label = new Label(0, 0, "人员姓名", wc);
            ws.addCell(label);
            label = new Label(1, 0, "坐席ID", wc);
            ws.addCell(label);
            label = new Label(2, 0, "坐席名称", wc);
            ws.addCell(label);
            for (int i = 0; i < devSeatInfos.size(); i++) {
                MemberRoleBean bean = devSeatInfos.get(i);
                InterfaceMember.pbui_Item_MemberDetailInfo member = bean.getMember();
                InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seat = bean.getSeat();
                //人员姓名
                label = new Label(0, i + 1, member.getName().toStringUtf8(), wc);
                ws.addCell(label);
                //坐席ID
                label = new Label(1, i + 1, ((seat != null && seat.getMemberid() != 0) ? String.valueOf(seat.getDevid()) : ""), wc);
                ws.addCell(label);
                //坐席名称
                label = new Label(2, i + 1, ((seat != null && seat.getMemberid() != 0) ? seat.getDevname().toStringUtf8() : ""), wc);
                ws.addCell(label);
            }
            //6.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //7.最后一步，关闭工作簿
            workbook.close();
            return true;
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
        return false;
    }
}
