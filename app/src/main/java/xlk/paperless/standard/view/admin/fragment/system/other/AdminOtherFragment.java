package xlk.paperless.standard.view.admin.fragment.system.other;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.UriUtils;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.io.File;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.File3Adapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.helper.AfterTextWatcher;
import xlk.paperless.standard.ui.ColorPickerDialog;
import xlk.paperless.standard.ui.InterfaceDragView;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.view.admin.fragment.system.seat.BgPictureAdapter;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/9/22.
 * @desc
 */
public class AdminOtherFragment extends BaseFragment implements AdminOtherInterface, View.OnClickListener {
    private EditText edt_url;
    private Button btn_url_submit;
    private Button btn_update;
    private EditText edt_company;
    private Button btn_company;
    private EditText edt_file;
    private Button btn_file_modify;
    private EditText edt_old_pwd;
    private EditText edt_new_pwd;
    private EditText edt_confirm_pwd;
    private Button btn_submit_modify;
    private Button btn_change_home;
    private Button btn_change_subbg;
    private Button btn_change_pro;
    private Button btn_change_bulletin;
    private AdminOtherPresenter presenter;
    private PopupWindow releaseFilePop;
    private File3Adapter releaseFileAdapter;
    private RecyclerView rv_release_file;
    private PopupWindow upGradeFilePop;
    private RecyclerView rv_upGrade_file;
    private File3Adapter upGradeFileAdapter;
    private PopupWindow interfacePop;
    private InterfaceDragView dragView;
    private Spinner pop_sp_bold;
    private Spinner pop_sp_show;
    private Spinner pop_sp_font;
    private Spinner pop_sp_align;
    private EditText pop_edt_width;
    private EditText pop_edt_height;
    private EditText pop_edt_text_size;
    private ImageView pop_iv_text_color;
    /**
     * 背景图片文件
     */
    private PopupWindow bgFilePop;
    private RecyclerView rv_pic;
    private BgPictureAdapter pictureAdapter;
    private final int REQUEST_CODE_RELEASE_FILE = 0;
    private final int REQUEST_CODE_UPDATE_FILE = 1;
    private final int REQUEST_CODE_UPLOAD_FILE = 2;
    private final int POP_TAG_MAIN = 1;
    private final int POP_TAG_PROJECTIVE = 2;
    private final int POP_TAG_NOTICE = 3;
    private int CURRENT_POP_TAG;
    private LinearLayout ll_size;
    private LinearLayout ll_sp;
    private PopupWindow urlPop;
    private RecyclerView rv_pop_url;
    private UrlAdapter urlAdapter;
    private PopupWindow subViewPop;
    private ImageView iv_subview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_other, container, false);
        initView(inflate);
        presenter = new AdminOtherPresenter(getContext(), this);
        reShow();
        return inflate;
    }

    @Override
    protected void reShow() {
        presenter.queryAdmin();
        presenter.webQuery();
        presenter.queryCompany();
        presenter.queryReleaseFile();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    private void initView(View inflate) {
        edt_url = (EditText) inflate.findViewById(R.id.edt_url);
        edt_url.setKeyListener(null);
        btn_url_submit = (Button) inflate.findViewById(R.id.btn_url_submit);
        btn_update = (Button) inflate.findViewById(R.id.btn_update);
        edt_company = (EditText) inflate.findViewById(R.id.edt_company);
        btn_company = (Button) inflate.findViewById(R.id.btn_company);
        edt_file = (EditText) inflate.findViewById(R.id.edt_file);
        btn_file_modify = (Button) inflate.findViewById(R.id.btn_file_modify);
        edt_old_pwd = (EditText) inflate.findViewById(R.id.edt_old_pwd);
        edt_new_pwd = (EditText) inflate.findViewById(R.id.edt_new_pwd);
        edt_confirm_pwd = (EditText) inflate.findViewById(R.id.edt_confirm_pwd);
        btn_submit_modify = (Button) inflate.findViewById(R.id.btn_submit_modify);
        btn_change_home = (Button) inflate.findViewById(R.id.btn_change_home);
        btn_change_subbg = (Button) inflate.findViewById(R.id.btn_change_subbg);
        btn_change_pro = (Button) inflate.findViewById(R.id.btn_change_pro);
        btn_change_bulletin = (Button) inflate.findViewById(R.id.btn_change_bulletin);

        btn_url_submit.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        btn_company.setOnClickListener(this);
        btn_file_modify.setOnClickListener(this);
        btn_submit_modify.setOnClickListener(this);
        btn_change_home.setOnClickListener(this);
        btn_change_subbg.setOnClickListener(this);
        btn_change_pro.setOnClickListener(this);
        btn_change_bulletin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_url_submit:
                showUrlPop();
                break;
            case R.id.btn_update:
                presenter.queryUpdateFile();
                showUpGradeFilePop();
                break;
            //修改公司名称
            case R.id.btn_company: {
                String company = edt_company.getText().toString().trim();
                if (TextUtils.isEmpty(company)) {
                    ToastUtil.show(R.string.conent_isEmpty);
                    break;
                }
                presenter.modifyCompany(company);
                break;
            }
            //修改会议发布文件
            case R.id.btn_file_modify:
                presenter.queryReleaseFile();
                showReleaseFilePop();
                break;
            //修改管理员密码
            case R.id.btn_submit_modify:
                modifyAdminPassword();
                break;
            case R.id.btn_change_home:
                CURRENT_POP_TAG = POP_TAG_MAIN;
                presenter.queryInterFaceConfiguration();
                showInterfacePop(CURRENT_POP_TAG);
                break;
            case R.id.btn_change_subbg: {
                presenter.queryInterFaceConfiguration();
                showSubViewPop();
                break;
            }
            case R.id.btn_change_pro:
                CURRENT_POP_TAG = POP_TAG_PROJECTIVE;
                presenter.queryInterFaceConfiguration();
                showInterfacePop(CURRENT_POP_TAG);
                break;
            case R.id.btn_change_bulletin:
                CURRENT_POP_TAG = POP_TAG_NOTICE;
                presenter.queryInterFaceConfiguration();
                showInterfacePop(CURRENT_POP_TAG);
                break;
            default:
                break;
        }
    }

    private void showSubViewPop() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_subview_interface, null);
        subViewPop = new PopupWindow(inflate, Values.screen_width * 3 / 4, Values.screen_height * 3 / 4);
        subViewPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        subViewPop.setTouchable(true);
        // true:设置触摸外面时消失
        subViewPop.setOutsideTouchable(true);
        subViewPop.setFocusable(true);
        subViewPop.setAnimationStyle(R.style.pop_Animation);
        subViewPop.showAtLocation(btn_change_subbg, Gravity.CENTER, 0, 0);
        iv_subview = inflate.findViewById(R.id.iv_subview);
        inflate.findViewById(R.id.btn_change).setOnClickListener(v -> {
            showBgFilePop(4,false);
        });

    }

    private void showUrlPop() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_url, null);
        urlPop = new PopupWindow(inflate, Values.half_width, Values.half_height);
        urlPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        urlPop.setTouchable(true);
        // true:设置触摸外面时消失
        urlPop.setOutsideTouchable(true);
        urlPop.setFocusable(true);
        urlPop.setAnimationStyle(R.style.pop_Animation);
        urlPop.showAtLocation(btn_url_submit, Gravity.CENTER, 0, 0);
        rv_pop_url = inflate.findViewById(R.id.rv_pop_url);
        EditText edt_pop_name = inflate.findViewById(R.id.edt_pop_name);
        EditText edt_pop_url = inflate.findViewById(R.id.edt_pop_url);
        urlAdapter = new UrlAdapter(R.layout.item_other_url, presenter.urlLists);
        rv_pop_url.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_pop_url.setAdapter(urlAdapter);
        urlAdapter.setOnItemClickListener((adapter, view, position) -> {
            InterfaceBase.pbui_Item_UrlDetailInfo item = presenter.urlLists.get(position);
            urlAdapter.setSelect(item.getId());
            edt_pop_name.setText(item.getName().toStringUtf8());
            edt_pop_url.setText(item.getAddr().toStringUtf8());
        });
        inflate.findViewById(R.id.btn_pop_add).setOnClickListener(v -> {
            String name = edt_pop_name.getText().toString().trim();
            String addr = edt_pop_url.getText().toString().trim();
            if (name.isEmpty() || addr.isEmpty()) {
                ToastUtil.show(R.string.please_enter_url);
                return;
            }
            InterfaceBase.pbui_Item_UrlDetailInfo build = InterfaceBase.pbui_Item_UrlDetailInfo.newBuilder()
                    .setName(s2b(name))
                    .setAddr(s2b(addr))
                    .build();
            jni.addUrl(build);
        });
        inflate.findViewById(R.id.btn_pop_modify).setOnClickListener(v -> {
            int selectedId = urlAdapter.getSelectedId();
            if (selectedId == -1) {
                ToastUtil.show(R.string.please_choose_url_first);
                return;
            }
            String name = edt_pop_name.getText().toString().trim();
            String addr = edt_pop_url.getText().toString().trim();
            if (name.isEmpty() || addr.isEmpty()) {
                ToastUtil.show(R.string.please_enter_url);
                return;
            }
            InterfaceBase.pbui_Item_UrlDetailInfo build = InterfaceBase.pbui_Item_UrlDetailInfo.newBuilder()
                    .setId(selectedId)
                    .setName(s2b(name))
                    .setAddr(s2b(addr))
                    .build();
            jni.modifyUrl(build);
        });
        inflate.findViewById(R.id.btn_pop_delete).setOnClickListener(v -> {
            InterfaceBase.pbui_Item_UrlDetailInfo selectUrl = urlAdapter.getSelectUrl();
            if (selectUrl == null) {
                ToastUtil.show(R.string.please_choose_url_first);
                return;
            }
            jni.delUrl(selectUrl);
        });
        inflate.findViewById(R.id.btn_pop_close).setOnClickListener(v -> {
            urlPop.dismiss();
        });
    }

    @Override
    public void updateUrl() {
        if (!presenter.urlLists.isEmpty()) {
            edt_url.setText(presenter.urlLists.get(0).getAddr().toStringUtf8());
        }
        if (urlPop != null && urlPop.isShowing()) {
            urlAdapter.notifyDataSetChanged();
        }
    }

    private void showInterfacePop(int tag) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_interface, null);
        interfacePop = new PopupWindow(inflate, Values.screen_width * 3 / 4, Values.screen_height * 3 / 4);
        interfacePop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        interfacePop.setTouchable(true);
        // true:设置触摸外面时消失
        interfacePop.setOutsideTouchable(true);
        interfacePop.setFocusable(true);
        interfacePop.setAnimationStyle(R.style.pop_Animation);
        interfacePop.showAtLocation(btn_change_home, Gravity.CENTER, 0, 0);
        dragView = inflate.findViewById(R.id.interface_view);
        ll_size = inflate.findViewById(R.id.ll_size);
        ll_sp = inflate.findViewById(R.id.ll_sp);
        pop_edt_width = inflate.findViewById(R.id.edt_width);
        pop_edt_height = inflate.findViewById(R.id.edt_height);
        pop_edt_text_size = inflate.findViewById(R.id.edt_text_size);
        pop_iv_text_color = inflate.findViewById(R.id.iv_text_color);
        CheckBox cb_all = inflate.findViewById(R.id.cb_all);
//        cb_all.setVisibility(tag == POP_TAG_MAIN ? View.GONE : View.VISIBLE);
        pop_sp_bold = inflate.findViewById(R.id.sp_bold);
        pop_sp_show = inflate.findViewById(R.id.sp_show);
        pop_sp_font = inflate.findViewById(R.id.sp_font);
        pop_sp_align = inflate.findViewById(R.id.sp_align);
        pop_sp_bold.setOnItemSelectedListener(mainSpSelectedListener);
        pop_sp_show.setOnItemSelectedListener(mainSpSelectedListener);
        pop_sp_font.setOnItemSelectedListener(mainSpSelectedListener);
        pop_sp_align.setOnItemSelectedListener(mainSpSelectedListener);
        dragView.post(() -> {
            switch (tag) {
                case POP_TAG_MAIN:
                    dragView.initInterfaceView(presenter.mainInterfaceBeans);
                    break;
                case POP_TAG_PROJECTIVE:
                    dragView.initInterfaceView(presenter.projectiveInterfaceBeans);
                    break;
                case POP_TAG_NOTICE:
                    dragView.initInterfaceView(presenter.noticeInterfaceBeans);
                    break;
                default:
                    break;
            }
        });
        dragView.setViewClickListener(new InterfaceDragView.ViewClickListener() {
            @Override
            public void onClick(MainInterfaceBean data, int width, int height) {
                LogUtil.d(TAG, "onClick 选中的view信息=" + data.toString());
                pop_edt_width.setText(String.valueOf(width));
                pop_edt_height.setText(String.valueOf(height));
                pop_edt_text_size.setText(String.valueOf(data.getFontSize()));
                pop_iv_text_color.setBackgroundColor(data.getColor());
                int fontFlag = data.getFontFlag();
                boolean isBold = (fontFlag & InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD_VALUE)
                        == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD_VALUE;
                pop_sp_bold.setSelection(isBold ? 1 : 0);
                int flag = data.getFlag();
                boolean isShow = (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE)
                        == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
                pop_sp_show.setSelection(isShow ? 1 : 0);
                int position = getSpinnerFontPositionByName(data.getFontName());
                pop_sp_font.setSelection(position);
                pop_sp_align.setSelection(getSpinnerAlignPosition(data.getAlign()));
            }

            @Override
            public void onHide(boolean hide) {
                ll_size.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
                ll_sp.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
            }
        });
        cb_all.setOnClickListener(v -> {
            boolean checked = cb_all.isChecked();
            cb_all.setChecked(checked);
            dragView.showAll(CURRENT_POP_TAG, checked);
        });
        //字体颜色
        pop_iv_text_color.setOnClickListener(v -> {
            new ColorPickerDialog(getContext(), color -> getActivity().runOnUiThread(() -> {
                dragView.updateTextColor(color);
                pop_iv_text_color.setBackgroundColor(color);
            }), Color.BLACK).show();
        });
        //宽
        pop_edt_width.addTextChangedListener(new AfterTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        int width = Integer.parseInt(s.toString().trim());
                        LogUtil.i(TAG, "afterTextChanged 宽改变=" + width);
                        dragView.updateViewWidth(width);
                    });
                }
            }
        });
        //高
        pop_edt_height.addTextChangedListener(new AfterTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        int height = Integer.parseInt(s.toString().trim());
                        LogUtil.i(TAG, "afterTextChanged 高改变=" + height);
                        dragView.updateViewHeight(height);
                    });
                }
            }
        });
        //字体大小
        pop_edt_text_size.addTextChangedListener(new AfterTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        int size = Integer.parseInt(s.toString().trim());
                        LogUtil.i(TAG, "afterTextChanged 字体大小改变=" + size);
                        dragView.updateTextSize(size);
                    });
                }
            }
        });
        //背景图
        inflate.findViewById(R.id.btn_bg).setOnClickListener(v -> {
            showBgFilePop(tag, false);
        });
        //logo图
        inflate.findViewById(R.id.btn_logo).setOnClickListener(v -> {
            showBgFilePop(tag, true);
        });
        //取消Logo
        inflate.findViewById(R.id.btn_cancel_logo).setOnClickListener(v -> {
            switch (tag) {
                case POP_TAG_MAIN:
                    presenter.saveMainLogo(0);
                    break;
                case POP_TAG_PROJECTIVE:
                    presenter.saveProjectiveLogo(0);
                    break;
                case POP_TAG_NOTICE:
                    presenter.saveNoticeLogo(0);
                    break;
                default:
                    break;
            }
        });
        //保存
        inflate.findViewById(R.id.btn_save).setOnClickListener(v -> {
            List<MainInterfaceBean> data = dragView.getData();
            presenter.saveInterfaceConfig(data);
        });
        //复位
        inflate.findViewById(R.id.btn_reset).setOnClickListener(v -> {
            dragView.reset();
        });
    }

    @Override
    public void updatePictureRv() {
        if (pictureAdapter != null) {
            LogUtil.i(TAG, "updatePictureRv ");
            pictureAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 展示选择背景图
     *
     * @param tag    =1主界面，=2投影界面，=3公告界面,=其它子界面背景图
     * @param isLogo 是否处理登录图片
     */
    private void showBgFilePop(int tag, boolean isLogo) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_bg_picture, null);
        bgFilePop = new PopupWindow(inflate, Values.half_width, Values.half_height);
        bgFilePop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        bgFilePop.setTouchable(true);
        // true:设置触摸外面时消失
        bgFilePop.setOutsideTouchable(true);
        bgFilePop.setFocusable(true);
        bgFilePop.setAnimationStyle(R.style.pop_Animation);
        bgFilePop.showAtLocation(btn_change_home, Gravity.CENTER, 0, 0);
        rv_pic = inflate.findViewById(R.id.rv_pic);
        pictureAdapter = new BgPictureAdapter(R.layout.item_bg_picture, presenter.pictureData);
        rv_pic.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_pic.setAdapter(pictureAdapter);
        pictureAdapter.setOnItemClickListener((adapter, view, position) -> pictureAdapter.setSelected(presenter.pictureData.get(position).getMediaid()));
        inflate.findViewById(R.id.btn_increase).setOnClickListener(v -> chooseLocalFile(REQUEST_CODE_UPLOAD_FILE));
        inflate.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selectPic = pictureAdapter.getSelectPic();
            if (selectPic != null) {
                jni.deleteMeetDirFile(0, selectPic);
            } else {
                ToastUtil.show(R.string.please_choose_file_first);
            }
        });
        inflate.findViewById(R.id.btn_determine).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selectPic = pictureAdapter.getSelectPic();
            if (selectPic != null) {
                int mediaid = selectPic.getMediaid();
                switch (tag) {
                    case POP_TAG_MAIN:
                        if (isLogo) {
                            presenter.saveMainLogo(mediaid);
                        } else {
                            presenter.saveMainBg(mediaid);
                        }
                        break;
                    case POP_TAG_PROJECTIVE:
                        if (isLogo) {
                            presenter.saveProjectiveLogo(mediaid);
                        } else {
                            presenter.saveProjectiveBg(mediaid);
                        }
                        break;
                    case POP_TAG_NOTICE:
                        if (isLogo) {
                            presenter.saveNoticeLogo(mediaid);
                        } else {
                            presenter.saveNoticeBg(mediaid);
                        }
                        break;
                    default:
                        presenter.saveSubViewBg(mediaid);
                        break;
                }
                bgFilePop.dismiss();
            } else {
                ToastUtil.show(R.string.please_choose_file_first);
            }
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> bgFilePop.dismiss());
    }

    private OnItemSelectedListener mainSpSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.sp_bold: {
                    dragView.updateTextBold(position);
                    break;
                }
                case R.id.sp_show: {
                    dragView.updateViewShow(position);
                    break;
                }
                case R.id.sp_font: {
                    String fontName = (String) pop_sp_font.getSelectedItem();
                    dragView.updateTextFont(fontName);
                    break;
                }
                case R.id.sp_align: {
                    dragView.updateTextAlign(position);
                    break;
                }
                default:
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private int getSpinnerAlignPosition(int align) {
        switch (align) {
            //左对齐
            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT_VALUE:
                return 1;
            //右对齐
            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT_VALUE:
                return 2;
            //居中对齐
            default:
                return 0;
        }
    }

    /**
     * 根据字体名称获取Spinner索引
     *
     * @param fontName 字体名称 “宋体”..
     */
    private int getSpinnerFontPositionByName(String fontName) {
        switch (fontName) {
            case "黑体":
                return 1;
            case "楷体":
                return 2;
            case "隶书":
                return 3;
            case "微软雅黑":
                return 4;
            case "小楷":
                return 5;
            default:
                return 0;
        }
    }

    private void showReleaseFilePop() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_choose_file, null);
        releaseFilePop = new PopupWindow(inflate, Values.half_width, Values.half_height);
        releaseFilePop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        releaseFilePop.setTouchable(true);
        // true:设置触摸外面时消失
        releaseFilePop.setOutsideTouchable(true);
        releaseFilePop.setFocusable(true);
        releaseFilePop.setAnimationStyle(R.style.pop_Animation);
        releaseFilePop.showAtLocation(btn_update, Gravity.CENTER, 0, 0);
        rv_release_file = inflate.findViewById(R.id.rv_file);
        releaseFileAdapter = new File3Adapter(R.layout.item_file_3, presenter.releaseFileData);
        rv_release_file.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_release_file.setAdapter(releaseFileAdapter);
        releaseFileAdapter.setOnItemClickListener((adapter, view, position) -> {
            int mediaId;
            mediaId = presenter.releaseFileData.get(position).getMediaid();
            releaseFileAdapter.setSelectedId(mediaId);
        });
        inflate.findViewById(R.id.btn_increase).setOnClickListener(v -> {
            chooseLocalFile(REQUEST_CODE_RELEASE_FILE);
        });
        inflate.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selectedFile = releaseFileAdapter.getSelectedFile();
            if (selectedFile != null) {
                jni.deleteMeetDirFile(0, selectedFile);
            } else {
                ToastUtil.show(R.string.please_choose_file_first);
            }
        });
        inflate.findViewById(R.id.btn_determine).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selectedFile = releaseFileAdapter.getSelectedFile();
            if (selectedFile != null) {
                presenter.saveReleaseFile(selectedFile.getMediaid());
                releaseFilePop.dismiss();
            } else {
                ToastUtil.show(R.string.please_choose_file_first);
            }
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            releaseFilePop.dismiss();
        });
        inflate.findViewById(R.id.btn_cancel_release).setOnClickListener(v -> {
            presenter.saveReleaseFile(0);
            releaseFilePop.dismiss();
        });
    }

    private void showUpGradeFilePop() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_choose_file, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showUpGradeFilePop fragment的大小 width=" + width + ",height=" + height);
        upGradeFilePop = new PopupWindow(inflate, Values.half_width, Values.half_height);
        upGradeFilePop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        upGradeFilePop.setTouchable(true);
        // true:设置触摸外面时消失
        upGradeFilePop.setOutsideTouchable(true);
        upGradeFilePop.setFocusable(true);
        upGradeFilePop.setAnimationStyle(R.style.pop_Animation);
        upGradeFilePop.showAtLocation(btn_update, Gravity.CENTER, 0, 0);
        rv_upGrade_file = inflate.findViewById(R.id.rv_file);
        inflate.findViewById(R.id.btn_cancel_release).setVisibility(View.GONE);
        upGradeFileAdapter = new File3Adapter(R.layout.item_file_3, presenter.updateFileData);
        rv_upGrade_file.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_upGrade_file.setAdapter(upGradeFileAdapter);
        upGradeFileAdapter.setOnItemClickListener((adapter, view, position) -> {
            int mediaId;
            mediaId = presenter.updateFileData.get(position).getMediaid();
            upGradeFileAdapter.setSelectedId(mediaId);
        });
        inflate.findViewById(R.id.btn_increase).setOnClickListener(v -> {
            chooseLocalFile(REQUEST_CODE_UPDATE_FILE);
        });
        inflate.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selectedFile = upGradeFileAdapter.getSelectedFile();
            if (selectedFile != null) {
                jni.deleteMeetDirFile(0, selectedFile);
            } else {
                ToastUtil.show(R.string.please_choose_file_first);
            }
        });
        inflate.findViewById(R.id.btn_determine).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selectedFile = upGradeFileAdapter.getSelectedFile();
            if (selectedFile != null) {
                jni.updateDevice(selectedFile.getMediaid());
            } else {
                ToastUtil.show(R.string.please_choose_file_first);
            }
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            upGradeFilePop.dismiss();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            File file = UriUtils.uri2File(uri);
            if (file == null) {
                LogUtil.e(TAG, "onActivityResult 获取文件路径失败 uri=" + uri);
                return;
            }

            if (requestCode == REQUEST_CODE_RELEASE_FILE) {
                if (FileUtil.isAudioAndVideoFile(file.getName())) {
                    jni.uploadFile(0, 0, InterfaceMacro.Pb_MeetFileAttrib.Pb_MEETFILE_ATTRIB_PUBLISH_VALUE,
                            file.getName(), file.getAbsolutePath(), 0, Constant.UPLOAD_PUBLISH_FILE);
                } else {
                    ToastUtil.show(R.string.please_choose_video_file);
                }
            } else if (requestCode == REQUEST_CODE_UPDATE_FILE) {
                if (file.getName().endsWith(".upz")) {
                    jni.uploadFile(0, 0, InterfaceMacro.Pb_MeetFileAttrib.Pb_MEETFILE_ATTRIB_DEVICEUPDATE_VALUE,
                            file.getName(), file.getAbsolutePath(), 0, Constant.UPLOAD_UPGRADE_FILE);
                } else {
                    ToastUtil.show(R.string.please_choose_upz_file);
                }
            } else if (requestCode == REQUEST_CODE_UPLOAD_FILE) {
                if (file.getName().endsWith(".png")) {
                    String absolutePath = file.getAbsolutePath();
                    LogUtil.i(TAG, "onActivityResult 上传背景图片=" + absolutePath);
                    jni.uploadFile(0, 0, InterfaceMacro.Pb_MeetFileAttrib.Pb_MEETFILE_ATTRIB_BACKGROUND_VALUE,
                            file.getName(), absolutePath, 0, Constant.UPLOAD_BACKGROUND_IMAGE);
                } else {
                    ToastUtil.show(R.string.please_choose_png);
                }
            }
        }
    }

    private void modifyAdminPassword() {
        String oldPwd = edt_old_pwd.getText().toString().trim();
        String newPwd = edt_new_pwd.getText().toString().trim();
        String confirmPwd = edt_confirm_pwd.getText().toString().trim();
        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            ToastUtil.show(R.string.please_enter_password);
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            ToastUtil.show(R.string.new_passwords_are_inconsistent);
            return;
        }
//        if(confirmPwd.length()<6){
//            ToastUtil.show(R.string.password_length_tip);
//            return;
//        }
        presenter.modifyAdminPassword(oldPwd, confirmPwd);
    }

    @Override
    public void updateCompany(String company) {
        edt_company.setText(company);
    }

    @Override
    public void updateCurrentReleaseFileName(String fileName) {
        getActivity().runOnUiThread(() -> edt_file.setText(fileName));
    }

    @Override
    public void updateReleaseFileRv() {
        if (releaseFilePop != null && releaseFilePop.isShowing()) {
            releaseFileAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateInterface(List<MainInterfaceBean> mainInterfaceBeans, List<MainInterfaceBean> projectiveInterfaceBeans, List<MainInterfaceBean> noticeInterfaceBeans) {
        if (interfacePop != null && interfacePop.isShowing()) {
            getActivity().runOnUiThread(() -> {
                switch (CURRENT_POP_TAG) {
                    case POP_TAG_MAIN:
                        dragView.initInterfaceView(mainInterfaceBeans);
                        break;
                    case POP_TAG_PROJECTIVE:
                        dragView.initInterfaceView(projectiveInterfaceBeans);
                        break;
                    case POP_TAG_NOTICE:
                        dragView.initInterfaceView(noticeInterfaceBeans);
                        break;
                    default:
                        break;
                }
            });
        }
    }

    @Override
    public void updateMainBgImg(String filePath) {
        if (interfacePop != null && interfacePop.isShowing()) {
            if (dragView != null) {
                LogUtil.i(TAG, "updateMainBgImg filePath=" + filePath);
                if (CURRENT_POP_TAG == POP_TAG_MAIN) {
                    Drawable drawable = Drawable.createFromPath(filePath);
                    dragView.setBackground(drawable);
                }
            }
        }
    }

    @Override
    public void updateSubviewBgImg(String filePath) {
        if (subViewPop != null && subViewPop.isShowing()) {
            if (iv_subview != null) {
                Drawable drawable = Drawable.createFromPath(filePath);
                iv_subview.setBackground(drawable);
            }
        }
    }

    @Override
    public void updateProjectiveBgImg(String filePath) {
        if (interfacePop != null && interfacePop.isShowing()) {
            if (dragView != null) {
                LogUtil.i(TAG, "updateMainBgImg filePath=" + filePath);
                if (CURRENT_POP_TAG == POP_TAG_PROJECTIVE) {
                    Drawable drawable = Drawable.createFromPath(filePath);
                    dragView.setBackground(drawable);
                }
            }
        }
    }

    @Override
    public void updateNoticeBgImg(String filePath) {
        if (interfacePop != null && interfacePop.isShowing()) {
            if (dragView != null) {
                LogUtil.i(TAG, "updateMainBgImg filePath=" + filePath);
                if (CURRENT_POP_TAG == POP_TAG_NOTICE) {
                    Drawable drawable = Drawable.createFromPath(filePath);
                    dragView.setBackground(drawable);
                }
            }
        }
    }

    @Override
    public void updateMainLogoImg(String filePath) {
        if (interfacePop != null && interfacePop.isShowing()) {
            getActivity().runOnUiThread(() -> {
                dragView.updateLogoImg(POP_TAG_MAIN, filePath);
            });
        }
    }

    @Override
    public void updateProjectiveLogoImg(String filePath) {
        if (interfacePop != null && interfacePop.isShowing()) {
            getActivity().runOnUiThread(() -> {
                dragView.updateLogoImg(POP_TAG_PROJECTIVE, filePath);
            });
        }
    }

    @Override
    public void updateNoticeLogoImg(String filePath) {
        if (interfacePop != null && interfacePop.isShowing()) {
            getActivity().runOnUiThread(() -> {
                dragView.updateLogoImg(POP_TAG_NOTICE, filePath);
            });
        }
    }

    @Override
    public void updateUpGradeFileRv() {
        if (upGradeFilePop != null && upGradeFilePop.isShowing()) {
            upGradeFileAdapter.notifyDataSetChanged();
        }
    }

}
