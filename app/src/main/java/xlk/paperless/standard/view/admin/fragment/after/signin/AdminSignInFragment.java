package xlk.paperless.standard.view.admin.fragment.after.signin;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.protobuf.ByteString;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.MyApplication;
import xlk.paperless.standard.view.admin.fragment.after.archive.PdfSignBean;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public class AdminSignInFragment extends BaseFragment implements AdminSignInInterface, View.OnClickListener {

    private AdminSignInPresenter presenter;
    private AdminSignInAdapter signInAdapter;
    private RecyclerView rv_signIn;
    private TextView tv_yd;
    private TextView tv_yqd;
    private TextView tv_wqd;
    private Button btn_delete;
    private Button btn_export_pdf;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_signin, container, false);
        initView(inflate);
        presenter = new AdminSignInPresenter(this);
        presenter.queryAttendPeople();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    protected void reShow() {
        presenter.queryAttendPeople();
    }


    public void initView(View rootView) {
        this.rv_signIn = (RecyclerView) rootView.findViewById(R.id.rv_signIn);
        this.tv_yd = (TextView) rootView.findViewById(R.id.tv_yd);
        this.tv_yqd = (TextView) rootView.findViewById(R.id.tv_yqd);
        this.tv_wqd = (TextView) rootView.findViewById(R.id.tv_wqd);
        this.btn_delete = (Button) rootView.findViewById(R.id.btn_delete);
        this.btn_export_pdf = (Button) rootView.findViewById(R.id.btn_export_pdf);
        btn_delete.setOnClickListener(this);
        btn_export_pdf.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_delete:
                if (signInAdapter == null || signInAdapter.getChecks().isEmpty()) {
                    ToastUtil.show(R.string.please_choose_member);
                    return;
                }
                List<Integer> checks = signInAdapter.getChecks();
                presenter.deleteSignIn(checks);
                break;
            case R.id.btn_export_pdf:
                exportPdf(presenter.getPdfData());
                break;
            default:
                break;
        }
    }

    /**
     * 将签到信息导出成PDF文件到本地
     * @param pdfSignBean 签到数据和会议数据
     */
    private void exportPdf(PdfSignBean pdfSignBean) {
        MyApplication.threadPool.execute(() -> {
            try {
                long l = System.currentTimeMillis();
                InterfaceMeet.pbui_Item_MeetMeetInfo meetInfo = pdfSignBean.getMeetInfo();
                InterfaceRoom.pbui_Item_MeetRoomDetailInfo roomInfo = pdfSignBean.getRoomInfo();
                List<SignInBean> signInBeans = pdfSignBean.getSignInBeans();
                final int size = signInBeans.size();
                LogUtil.i(TAG, "exportPdf signInBeans.size=" + size);
                int signInCount = pdfSignBean.getSignInCount();
                if (meetInfo == null || roomInfo == null) {
                    return;
                }
                FileUtil.createDir(Constant.DIR_EXPORT);
//                File file = new File(Constant.DIR_EXPORT + "签到信息.pdf");
//                if (file.exists()) {
//                    boolean delete = file.delete();
//                    LogUtil.i(TAG, "exportPdf 删除了原文件=" + delete);
//                }

                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(Constant.DIR_EXPORT + "签到信息.pdf"));
                document.open();
                BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                Font boldFont14 = new Font(bfChinese, 14, Font.BOLD);
                Font boldFont16 = new Font(bfChinese, 16, Font.BOLD);

                String top = "会议名称：" + meetInfo.getName().toStringUtf8()
                        + "\n会场：" + meetInfo.getRoomname().toStringUtf8() + "  会场地址：" + roomInfo.getAddr().toStringUtf8()
                        + "\n会议保密：" + (meetInfo.getSecrecy() == 1 ? "是" : "否")
                        + "\n开始时间：" + DateUtil.millisecondFormatDetailedTime(meetInfo.getStartTime() * 1000)
                        + "  结束时间：" + DateUtil.millisecondFormatDetailedTime(meetInfo.getEndTime() * 1000)
                        + "\n应到：" + size + "人  已签到：" + signInCount + "人  未签到：" + (size - signInCount) + "人";
                Paragraph title = new Paragraph(top, boldFont16);
                //设置文字居中 0靠左 1，居中 2，靠右
                title.setAlignment(1);
                //设置段落上空白
                title.setSpacingBefore(5f);
                //设置段落下空白
                title.setSpacingAfter(10f);
                document.add(title);

                // 添加虚线
                Paragraph dottedLine = new Paragraph();
                dottedLine.add(new Chunk(new DottedLineSeparator()));
                //设置段落上空白
                dottedLine.setSpacingBefore(10f);
                //设置段落下空白
                dottedLine.setSpacingAfter(10f);
                document.add(dottedLine);

                //创建一个两列的表格，添加第三个的时候会自动换相应的行数
                PdfPTable pdfPTable = new PdfPTable(2);
                //设置表格宽度比例为100%
                pdfPTable.setWidthPercentage(100);
                //设置表格默认为无边框
                pdfPTable.getDefaultCell().setBorder(0);
                //定义单元格的高度
                final float cellHeight = 100f;
                for (int i = 0; i < size; i++) {
                    SignInBean item = signInBeans.get(i);
                    InterfaceMember.pbui_Item_MemberDetailInfo member = item.getMember();
                    InterfaceSignin.pbui_Item_MeetSignInDetailInfo sign = item.getSign();
                    boolean isNoSign = sign == null;
                    LogUtil.i(TAG, "exportPdf isNoSign=" + isNoSign);
                    String content = "参会人：" + member.getName().toStringUtf8()
                            + "\n签到时间：" + (isNoSign ? "" : DateUtil.millisecondFormatDetailedTime(sign.getUtcseconds() * 1000))
                            + "\n签到状态：" + (isNoSign ? "未签到" : "已签到")
                            + "\n签到方式：" + (isNoSign ? "" : Constant.getMeetSignInTypeName(sign.getSigninType()));
                    Paragraph paragraph = new Paragraph(content, boldFont14);
                    PdfPCell cell_1 = new PdfPCell(paragraph);
                    //设置固定高度
                    cell_1.setFixedHeight(cellHeight);
                    //设置垂直居中
                    cell_1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    pdfPTable.addCell(cell_1);

                    if (!isNoSign) {
                        byte[] bytes = sign.getPsigndata().toByteArray();
                        if (bytes != null && bytes.length > 0) {
                            PdfPCell cell_2 = new PdfPCell();
                            //设置固定高度
                            cell_2.setFixedHeight(cellHeight);
                            Image image = Image.getInstance(bytes);
                            image.scaleAbsolute(100, 50);
                            cell_2.setImage(image);
                            pdfPTable.addCell(cell_2);
                            LogUtil.i(TAG, "exportPdf 有图片签到数据");
                            continue;
                        }
                    }
                    LogUtil.i(TAG, "exportPdf 没有图片签到数据，给一个空白");
                    //没有图片签到数据时，给一个空白
                    Paragraph a = new Paragraph("");
                    PdfPCell cell_2 = new PdfPCell(a);
                    //设置垂直居中
                    cell_2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    //设置水平居中
                    cell_2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    //设置固定高度
                    cell_2.setFixedHeight(cellHeight);
                    pdfPTable.addCell(cell_2);
                }
                document.add(pdfPTable);
                document.close();
                LogUtil.i(TAG, "exportPdf 用时=" + (System.currentTimeMillis() - l));
                showToast();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void showToast() {
        getActivity().runOnUiThread(() -> ToastUtil.show(R.string.export_pdf_successful));
    }

    @Override
    public void update(List<SignInBean> signInBeans, int signInCount) {
        LogUtil.i(TAG, "update signInBeans.size=" + signInBeans.size() + ", signInCount=" + signInCount);
        if (signInAdapter == null) {
            signInAdapter = new AdminSignInAdapter(R.layout.item_admin_signin, signInBeans);
            rv_signIn.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_signIn.setAdapter(signInAdapter);
            signInAdapter.addChildClickViewIds(R.id.item_view_5);
            signInAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    signInAdapter.setSelected(signInBeans.get(position).getMember().getPersonid());
                }
            });
            signInAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
                @Override
                public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                    if (view.getId() == R.id.item_view_5) {
                        LogUtil.i(TAG, "onItemChildClick position=" + position);
                        SignInBean bean = signInBeans.get(position);
                        ByteString picdata = bean.getSign().getPsigndata();
                        showPicDialog(picdata);
                    }
                }
            });
        } else {
            signInAdapter.notifyDataSetChanged();
        }
        tv_yd.setText(getString(R.string.yd_, String.valueOf(signInBeans.size())));
        tv_yqd.setText(getString(R.string.yqd_, String.valueOf(signInCount)));
        tv_wqd.setText(getString(R.string.wqd_, String.valueOf(signInBeans.size() - signInCount)));
    }

    private void showPicDialog(ByteString picdata) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.dialog_show_picture, null);
        ImageView iv = inflate.findViewById(R.id.iv_show_pic);
        iv.setImageBitmap(ConvertUtil.bs2bmp(picdata));
        builder.setView(inflate);
        builder.create().show();
    }
}
