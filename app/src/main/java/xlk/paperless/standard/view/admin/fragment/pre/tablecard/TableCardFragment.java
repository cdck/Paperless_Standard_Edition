package xlk.paperless.standard.view.admin.fragment.pre.tablecard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.blankj.utilcode.util.UriUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceTablecard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.TableCardFileAdapter;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.helper.AfterTextWatcher;
import xlk.paperless.standard.ui.ColorPickerDialog;
import xlk.paperless.standard.ui.TableCardView;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author Created by xlk on 2020/11/4.
 * @desc
 */
public class TableCardFragment extends BaseFragment implements TableCardInterface, AdapterView.OnItemSelectedListener,
        View.OnClickListener {

    private TableCardPresenter presenter;
    private Spinner one_sp_title, one_sp_font, one_sp_bold, one_sp_align, one_sp_hide;
    private ImageView one_iv_color;
    private EditText one_edt_size;

    private Spinner two_sp_title, two_sp_font, two_sp_bold, two_sp_align, two_sp_hide;
    private ImageView two_iv_color;
    private EditText two_edt_size;
    private Spinner three_sp_title, three_sp_font, three_sp_bold, three_sp_align, three_sp_hide;
    private ImageView three_iv_color;
    private EditText three_edt_size;

    private RecyclerView rv_file;
    private Button btn_save_set;
    private Button btn_default_location;
    private Button btn_delete_bg;
    private Button btn_delete_file;
    private Button btn_import_file;
    private Button btn_save_tableCard;
    private EditText one_edt_height, two_edt_height, three_edt_height;
    private TableCardView table_card_view;
    List<TableCardBean> datas = new ArrayList<>();
    private int viewWidth;
    private int viewHeight;
    private int currentMediaId;
    private TableCardFileAdapter fileAdapter;
    private final int REQUEST_CODE_TABLE_CARD = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_table_card, container, false);
        initView(inflate);
        presenter = new TableCardPresenter(this);
        presenter.queryBgPicture();
        table_card_view.post(() -> {
            viewWidth = table_card_view.getWidth();
            viewHeight = table_card_view.getHeight();
            LogUtil.i(TAG, "区域大小 width=" + viewWidth + ",height=" + viewHeight);
            presenter.queryTableCard();
        });
        return inflate;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initSpinnerAdapter();
    }

    @Override
    protected void reShow() {
        presenter.queryBgPicture();
        presenter.queryTableCard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void updatePictureRv() {
        if (fileAdapter == null) {
            fileAdapter = new TableCardFileAdapter(R.layout.item_table_card, presenter.pictureData);
            rv_file.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_file.setAdapter(fileAdapter);
            fileAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    int mediaid = presenter.pictureData.get(position).getMediaid();
                    fileAdapter.setSelect(mediaid);
                    String name = presenter.pictureData.get(position).getName().toStringUtf8();
                    updateTableCardBg(Constant.DIR_PICTURE + name, mediaid);

                }
            });
        } else {
            fileAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateTableCardBg(String filePath, int mediaid) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                currentMediaId = mediaid;
                Drawable drawable = Drawable.createFromPath(filePath);
                table_card_view.setBackground(drawable);
            }
        } else {
            LogUtil.i(TAG, "updateTableCardBg 没有找到该文件，进行从新下载 filepath=" + filePath);
            FileUtil.createDir(Constant.DIR_PICTURE);
            jni.creationFileDownload(filePath, mediaid, 0, 1, Constant.DOWNLOAD_TABLE_CARD_BG);
        }
    }

    @Override
    public void clearBgImage() {
        currentMediaId = 0;
        table_card_view.setBackgroundColor(Color.WHITE);
    }

    @Override
    public void updateTableCard(InterfaceTablecard.pbui_Type_MeetTableCardDetailInfo info) {
        LogUtil.i(TAG, "updateTableCard 当前线程=" + Thread.currentThread().getName());
        InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo top = info.getItem(0);
        InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo main = info.getItem(1);
        InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo bottom = info.getItem(2);
        updateTop(top);
        updateMain(main);
        updateBottom(bottom);
        datas.clear();
        datas.add(new TableCardBean(top.getFontname().toStringUtf8(), top.getFontsize(), top.getFontcolor(), top.getLx(),
                top.getLy(), top.getRx(), top.getRy(), top.getFlag(), top.getAlign(), top.getType()));

        datas.add(new TableCardBean(main.getFontname().toStringUtf8(), main.getFontsize(), main.getFontcolor(), main.getLx(),
                main.getLy(), main.getRx(), main.getRy(), main.getFlag(), main.getAlign(), main.getType()));

        datas.add(new TableCardBean(bottom.getFontname().toStringUtf8(), bottom.getFontsize(), bottom.getFontcolor(), bottom.getLx(),
                bottom.getLy(), bottom.getRx(), bottom.getRy(), bottom.getFlag(), bottom.getAlign(), bottom.getType()));

        table_card_view.initView(datas);
    }

    private void updateTop(InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo top) {
        String fontName = top.getFontname().toStringUtf8();
        int fontsize = top.getFontsize();
        int fontcolor = top.getFontcolor();
        float lx = top.getLx();
        float ly = top.getLy();
        float rx = top.getRx();
        float ry = top.getRy();
        int flag = top.getFlag();
        int align = top.getAlign();
        int type = top.getType();
        LogUtil.i(TAG, "updateTop fontName=" + fontName
                + "\nlx=" + lx
                + "\nly=" + ly
                + "\nrx=" + rx
                + "\nry=" + ry
                + "\nflag=" + flag
                + "\nalign=" + align
                + "\ntype=" + type
        );
        int titleIndex = getTitleIndex(type);
        one_sp_title.setSelection(titleIndex, false);
        one_iv_color.setBackgroundColor(fontcolor);
        one_edt_size.setText(String.valueOf(fontsize));
        one_sp_font.setSelection(getFontNameIndex(fontName), false);
        boolean isBold = (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE) == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE;
        one_sp_bold.setSelection(isBold ? 1 : 0, false);
        one_sp_align.setSelection(getAlignIndex(top.getAlign()), false);
        boolean isHide = (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE) == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
        one_sp_hide.setSelection(isHide ? 0 : 1, false);
        one_edt_height.setText(String.valueOf((int) (ry - ly)));
    }

    private void updateMain(InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo main) {
        String fontName = main.getFontname().toStringUtf8();
        int fontsize = main.getFontsize();
        int fontcolor = main.getFontcolor();
        float lx = main.getLx();
        float ly = main.getLy();
        float rx = main.getRx();
        float ry = main.getRy();
        int flag = main.getFlag();
        int align = main.getAlign();
        int type = main.getType();
        LogUtil.i(TAG, "updateMain fontName=" + fontName
                + "\nlx=" + lx
                + "\nly=" + ly
                + "\nrx=" + rx
                + "\nry=" + ry
                + "\nflag=" + flag
                + "\nalign=" + align
                + "\ntype=" + type
        );
        int titleIndex = getTitleIndex(type);
        two_sp_title.setSelection(titleIndex, false);
        two_iv_color.setBackgroundColor(fontcolor);
        two_edt_size.setText(String.valueOf(fontsize));
        two_sp_font.setSelection(getFontNameIndex(fontName), false);
        boolean isBold = (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE) == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE;
        two_sp_bold.setSelection(isBold ? 1 : 0, false);
        two_sp_align.setSelection(getAlignIndex(main.getAlign()), false);
        boolean isHide = (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE) == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
        two_sp_hide.setSelection(isHide ? 0 : 1, false);
        two_edt_height.setText(String.valueOf((int) (ry - ly)));
    }

    private void updateBottom(InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo bottom) {
        String fontName = bottom.getFontname().toStringUtf8();
        int fontsize = bottom.getFontsize();
        int fontcolor = bottom.getFontcolor();
        float lx = bottom.getLx();
        float ly = bottom.getLy();
        float rx = bottom.getRx();
        float ry = bottom.getRy();
        int flag = bottom.getFlag();
        int align = bottom.getAlign();
        int type = bottom.getType();
        LogUtil.i(TAG, "updateBottom fontName=" + fontName
                + "\nlx=" + lx
                + "\nly=" + ly
                + "\nrx=" + rx
                + "\nry=" + ry
                + "\nflag=" + flag
                + "\nalign=" + align
                + "\ntype=" + type
        );
        int titleIndex = getTitleIndex(type);
        three_sp_title.setSelection(titleIndex, false);
        three_iv_color.setBackgroundColor(fontcolor);
        three_edt_size.setText(String.valueOf(fontsize));
        three_sp_font.setSelection(getFontNameIndex(fontName), false);
        boolean isBold = (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE) == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE;
        three_sp_bold.setSelection(isBold ? 1 : 0, false);
        three_sp_align.setSelection(getAlignIndex(bottom.getAlign()), false);
        boolean isHide = (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE) == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
        three_sp_hide.setSelection(isHide ? 0 : 1, false);
        three_edt_height.setText(String.valueOf((int) (ry - ly)));
    }

    private int getAlignIndex(int align) {
        int index;
        switch (align) {
            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT_VALUE:
                index = 1;
                break;
            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT_VALUE:
                index = 2;
                break;
//            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER_VALUE:
//                index = 3;
//                break;
//            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP_VALUE:
//                index = 4;
//                break;
//            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM_VALUE:
//                index = 5;
//                break;
            default:
                index = 0;
                break;
        }
        LogUtil.i(TAG, "getAlignIndex index=" + index);
        return index;
    }

    /**
     * 根据字体名称获取Spinner索引
     *
     * @param fontName 字体名称 eg:“黑体”
     */
    private int getFontNameIndex(String fontName) {
        int index;
        switch (fontName) {
            case "黑体":
                index = 1;
                break;
            case "楷体":
                index = 2;
                break;
            case "隶书":
                index = 3;
                break;
            case "微软雅黑":
                index = 4;
                break;
            case "小楷":
                index = 5;
                break;
            default:
                index = 0;
                break;
        }
        return index;
    }

    /**
     * 根据显示内容类别获取Spinner索引
     *
     * @param type InterfaceMacro#Pb_TableCardType
     */
    private int getTitleIndex(int type) {
        int index;
        switch (type) {
            case InterfaceMacro.Pb_TableCardType.Pb_conf_memname_VALUE:
                index = 1;
                break;
            case InterfaceMacro.Pb_TableCardType.Pb_conf_job_VALUE:
                index = 2;
                break;
            case InterfaceMacro.Pb_TableCardType.Pb_conf_company_VALUE:
                index = 3;
                break;
            case InterfaceMacro.Pb_TableCardType.Pb_conf_position_VALUE:
                index = 4;
                break;
            default:
                index = 0;
                break;
        }
        LogUtil.i(TAG, "getTitleIndex index=" + index);
        return index;
    }

    public void initView(View rootView) {
        this.one_sp_title = (Spinner) rootView.findViewById(R.id.one_sp_title);
        this.one_iv_color = (ImageView) rootView.findViewById(R.id.one_iv_color);
        this.one_edt_size = (EditText) rootView.findViewById(R.id.one_edt_size);
        this.one_sp_font = (Spinner) rootView.findViewById(R.id.one_sp_font);
        this.one_sp_bold = (Spinner) rootView.findViewById(R.id.one_sp_bold);
        this.one_sp_align = (Spinner) rootView.findViewById(R.id.one_sp_align);
        this.one_sp_hide = (Spinner) rootView.findViewById(R.id.one_sp_hide);
        this.one_edt_height = (EditText) rootView.findViewById(R.id.one_edt_height);

        this.two_sp_title = (Spinner) rootView.findViewById(R.id.two_sp_title);
        this.two_iv_color = (ImageView) rootView.findViewById(R.id.two_iv_color);
        this.two_edt_size = (EditText) rootView.findViewById(R.id.two_edt_size);
        this.two_sp_font = (Spinner) rootView.findViewById(R.id.two_sp_font);
        this.two_sp_bold = (Spinner) rootView.findViewById(R.id.two_sp_bold);
        this.two_sp_align = (Spinner) rootView.findViewById(R.id.two_sp_align);
        this.two_sp_hide = (Spinner) rootView.findViewById(R.id.two_sp_hide);
        this.two_edt_height = (EditText) rootView.findViewById(R.id.two_edt_height);

        this.three_sp_title = (Spinner) rootView.findViewById(R.id.three_sp_title);
        this.three_iv_color = (ImageView) rootView.findViewById(R.id.three_iv_color);
        this.three_edt_size = (EditText) rootView.findViewById(R.id.three_edt_size);
        this.three_sp_font = (Spinner) rootView.findViewById(R.id.three_sp_font);
        this.three_sp_bold = (Spinner) rootView.findViewById(R.id.three_sp_bold);
        this.three_sp_align = (Spinner) rootView.findViewById(R.id.three_sp_align);
        this.three_sp_hide = (Spinner) rootView.findViewById(R.id.three_sp_hide);
        this.three_edt_height = (EditText) rootView.findViewById(R.id.three_edt_height);


        this.rv_file = (RecyclerView) rootView.findViewById(R.id.rv_file);
        this.btn_save_set = (Button) rootView.findViewById(R.id.btn_save_set);
        this.btn_default_location = (Button) rootView.findViewById(R.id.btn_default_location);
        this.btn_delete_bg = (Button) rootView.findViewById(R.id.btn_delete_bg);
        this.btn_delete_file = (Button) rootView.findViewById(R.id.btn_delete_file);
        this.btn_import_file = (Button) rootView.findViewById(R.id.btn_import_file);
        this.btn_save_tableCard = (Button) rootView.findViewById(R.id.btn_save_tableCard);

        this.table_card_view = (TableCardView) rootView.findViewById(R.id.table_card_view);

        one_edt_size.addTextChangedListener(new AfterTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        int size = Integer.parseInt(s.toString().trim());
                        table_card_view.updateViewTextSize(0, size);
                    });
                }
            }
        });
        two_edt_size.addTextChangedListener(new AfterTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        int size = Integer.parseInt(s.toString().trim());
                        table_card_view.updateViewTextSize(1, size);
                    });
                }
            }
        });
        three_edt_size.addTextChangedListener(new AfterTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        int size = Integer.parseInt(s.toString().trim());
                        table_card_view.updateViewTextSize(2, size);
                    });
                }
            }
        });

        one_edt_height.addTextChangedListener(new AfterTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                LogUtil.i(TAG, "afterTextChanged 1s=" + s);
                if (!s.toString().trim().isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        int size = Integer.parseInt(s.toString().trim());
                        if (size > 100) {
                            size = 100;
                            one_edt_height.setText(String.valueOf(size));
                        }
                        table_card_view.updateViewHeight(0, size);
                    });
                }
            }
        });
        two_edt_height.addTextChangedListener(new AfterTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                LogUtil.i(TAG, "afterTextChanged 2s=" + s);
                if (!s.toString().trim().isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        int size = Integer.parseInt(s.toString().trim());
                        if (size > 100) {
                            size = 100;
                            two_edt_height.setText(String.valueOf(size));
                        }
                        table_card_view.updateViewHeight(1, size);
                    });
                }
            }
        });
        three_edt_height.addTextChangedListener(new AfterTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                LogUtil.i(TAG, "afterTextChanged 3s=" + s);
                if (!s.toString().trim().isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        int size = Integer.parseInt(s.toString().trim());
                        if (size > 100) {
                            size = 100;
                            three_edt_height.setText(String.valueOf(size));
                        }
                        table_card_view.updateViewHeight(2, size);
                    });
                }
            }
        });

        one_iv_color.setOnClickListener(this);
        two_iv_color.setOnClickListener(this);
        three_iv_color.setOnClickListener(this);
        btn_save_set.setOnClickListener(this);
        btn_default_location.setOnClickListener(this);
        btn_delete_bg.setOnClickListener(this);
        btn_delete_file.setOnClickListener(this);
        btn_import_file.setOnClickListener(this);
        btn_save_tableCard.setOnClickListener(this);
    }

    public void initSpinnerAdapter() {
        LogUtil.i(TAG, "initSpinnerAdapter ");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.table_card_title));
        one_sp_title.setAdapter(adapter1);
        one_sp_title.setSelection(0, false);
        one_sp_title.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.table_card_font));
        one_sp_font.setAdapter(adapter2);
        one_sp_font.setSelection(0, false);
        one_sp_font.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.confidential_spinner));
        one_sp_bold.setAdapter(adapter3);
        one_sp_bold.setSelection(0, false);
        one_sp_bold.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter4 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.table_card_align));
        one_sp_align.setAdapter(adapter4);
        one_sp_align.setSelection(0, false);
        one_sp_align.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter5 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.confidential_spinner));
        one_sp_hide.setAdapter(adapter5);
        one_sp_hide.setSelection(0, false);
        one_sp_hide.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter6 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.table_card_title));
        two_sp_title.setAdapter(adapter6);
        two_sp_title.setSelection(0, false);
        two_sp_title.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter7 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.table_card_font));
        two_sp_font.setAdapter(adapter7);
        two_sp_font.setSelection(0, false);
        two_sp_font.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter8 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.confidential_spinner));
        two_sp_bold.setAdapter(adapter8);
        two_sp_bold.setSelection(0, false);
        two_sp_bold.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter9 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.table_card_align));
        two_sp_align.setAdapter(adapter9);
        two_sp_align.setSelection(0, false);
        two_sp_align.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter10 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.confidential_spinner));
        two_sp_hide.setAdapter(adapter10);
        two_sp_hide.setSelection(0, false);
        two_sp_hide.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter11 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.table_card_title));
        three_sp_title.setAdapter(adapter11);
        three_sp_title.setSelection(0, false);
        three_sp_title.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter12 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.table_card_font));
        three_sp_font.setAdapter(adapter12);
        three_sp_font.setSelection(0, false);
        three_sp_font.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter13 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.confidential_spinner));
        three_sp_bold.setAdapter(adapter13);
        three_sp_bold.setSelection(0, false);
        three_sp_bold.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter14 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.table_card_align));
        three_sp_align.setAdapter(adapter14);
        three_sp_align.setSelection(0, false);
        three_sp_align.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter15 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.confidential_spinner));
        three_sp_hide.setAdapter(adapter15);
        three_sp_hide.setSelection(0, false);
        three_sp_hide.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.one_sp_title:
                table_card_view.updateViewText(0, position);
                break;
            case R.id.one_sp_font:
                table_card_view.updateViewFont(0, (String) one_sp_font.getSelectedItem());
                break;
            case R.id.one_sp_bold:
                table_card_view.updateViewBold(0, position);
                break;
            case R.id.one_sp_align:
                table_card_view.updateViewAlign(0, position);
                break;
            case R.id.one_sp_hide:
                table_card_view.updateViewHide(0, position);
                break;
            //第二行
            case R.id.two_sp_title:
                table_card_view.updateViewText(1, position);
                break;
            case R.id.two_sp_font:
                table_card_view.updateViewFont(1, (String) two_sp_font.getSelectedItem());
                break;
            case R.id.two_sp_bold:
                table_card_view.updateViewBold(1, position);
                break;
            case R.id.two_sp_align:
                table_card_view.updateViewAlign(1, position);
                break;
            case R.id.two_sp_hide:
                table_card_view.updateViewHide(1, position);
                break;
            //第三行
            case R.id.three_sp_title:
                table_card_view.updateViewText(2, position);
                break;
            case R.id.three_sp_font:
                table_card_view.updateViewFont(2, (String) three_sp_font.getSelectedItem());
                break;
            case R.id.three_sp_bold:
                table_card_view.updateViewBold(2, position);
                break;
            case R.id.three_sp_align:
                table_card_view.updateViewAlign(2, position);
                break;
            case R.id.three_sp_hide:
                table_card_view.updateViewHide(2, position);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        LogUtil.i(TAG, "onNothingSelected ");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one_iv_color:
                new ColorPickerDialog(getContext(), color -> getActivity().runOnUiThread(() -> {
                    table_card_view.updateViewColor(0, color);
                    one_iv_color.setBackgroundColor(color);
                }), Color.BLACK).show();
                break;
            case R.id.two_iv_color:
                new ColorPickerDialog(getContext(), color -> getActivity().runOnUiThread(() -> {
                    table_card_view.updateViewColor(1, color);
                    two_iv_color.setBackgroundColor(color);
                }), Color.BLACK).show();
                break;
            case R.id.three_iv_color:
                new ColorPickerDialog(getContext(), color -> getActivity().runOnUiThread(() -> {
                    table_card_view.updateViewColor(2, color);
                    three_iv_color.setBackgroundColor(color);
                }), Color.BLACK).show();
                break;
            case R.id.btn_save_set:
                jni.modifyTableCard(currentMediaId, 0, table_card_view.getTableCardData());
                break;
            case R.id.btn_default_location:
                one_edt_height.setText(String.valueOf(34));
                two_edt_height.setText(String.valueOf(34));
                three_edt_height.setText(String.valueOf(34));
                table_card_view.setDefaultLocation();
                break;
            case R.id.btn_delete_bg:
                clearBgImage();
                break;
            case R.id.btn_delete_file:
                if (fileAdapter != null) {
                    InterfaceFile.pbui_Item_MeetDirFileDetailInfo selectFile = fileAdapter.getSelectedFile();
                    if (selectFile != null) {
                        clearBgImage();
                        jni.deleteMeetDirFile(0, selectFile);
                    } else {
                        ToastUtil.show(R.string.please_choose_file_first);
                    }
                }
                break;
            case R.id.btn_import_file:
                chooseLocalFile(REQUEST_CODE_TABLE_CARD);
                break;
            case R.id.btn_save_tableCard:
                jni.modifyTableCard(currentMediaId,
                        InterfaceTablecard.Pb_TableCard_ModifyFlag.Pb_TABLECARD_MODFLAG_SETDEFAULT_VALUE,
                        table_card_view.getTableCardData());
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_TABLE_CARD) {
            Uri uri = data.getData();
            File file = UriUtils.uri2File(uri);
            if (file != null) {
                if (file.getName().endsWith(".png")) {
                    String absolutePath = file.getAbsolutePath();
                    LogUtil.i(TAG, "onActivityResult 上传桌牌底图图片=" + absolutePath);
                    jni.uploadFile(0, 0, InterfaceMacro.Pb_MeetFileAttrib.Pb_MEETFILE_ATTRIB_TABLECARD_VALUE,
                            file.getName(), absolutePath, 0, Constant.UPLOAD_TABLE_CARD_BACKGROUND_IMAGE);
                } else {
                    ToastUtil.show(R.string.please_choose_png);
                }
            }
        }
    }
}
