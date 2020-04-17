package xlk.paperless.standard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.FileUtil;

/**
 * @author xlk
 * @date 2020/3/14
 * @Description: 会议资料文件分页展示adapter
 */
public class MeetDataFileListAdapter extends BaseAdapter {
    private final Context cxt;
    private final List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> datas;
    private final int itemCount;
    private int pageNow;
    private ItemDownloadClickListener listener;
    private int chooseId = -1;

    public MeetDataFileListAdapter(Context cxt, List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> datas, int itemCount) {
        this.cxt = cxt;
        this.datas = datas;
        this.itemCount = itemCount;
    }

    @Override
    public int getCount() {
        //  数据的总数
        int ori = itemCount * pageNow;
        //值的总个数-前几页的个数就是这一页要显示的个数，如果比默认的值小，说明这是最后一页，只需显示这么多就可以了
        if (datas.size() - ori < itemCount) {
            return datas.size() - ori;
        } else {
            //如果比默认的值还要大，说明一页显示不完，还要用换一页显示，这一页用默认的值显示满就可以了。
            return itemCount;
        }
    }

    @Override
    public Object getItem(int position) {
        return datas != null ? datas.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(cxt).inflate(R.layout.item_meet_data_file, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = datas.get(position + itemCount * pageNow);
        holder.i_m_d_file_number.setText(String.valueOf(position + itemCount * pageNow + 1));
        holder.i_m_d_file_name.setText(item.getName().toStringUtf8());
        holder.i_m_d_file_size.setText(FileUtil.formatFileSize(item.getSize()));
        holder.i_m_d_file_root.setSelected(chooseId == item.getMediaid());
        holder.i_m_d_file_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.clickDownload(item);
                }
            }
        });
        return convertView;
    }

    public void setChoose(int mediaId) {
        this.chooseId = mediaId;
        notifyDataSetChanged();
    }

    public void setOnDownloadClickListener(ItemDownloadClickListener listener) {
        this.listener = listener;
    }

    public interface ItemDownloadClickListener {
        void clickDownload(InterfaceFile.pbui_Item_MeetDirFileDetailInfo item);
    }

    public static class ViewHolder {
        public View rootView;
        public TextView i_m_d_file_number;
        public TextView i_m_d_file_name;
        public TextView i_m_d_file_size;
        public ImageView i_m_d_file_download;
        public LinearLayout i_m_d_file_root;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.i_m_d_file_number = (TextView) rootView.findViewById(R.id.i_m_d_file_number);
            this.i_m_d_file_name = (TextView) rootView.findViewById(R.id.i_m_d_file_name);
            this.i_m_d_file_size = (TextView) rootView.findViewById(R.id.i_m_d_file_size);
            this.i_m_d_file_download = (ImageView) rootView.findViewById(R.id.i_m_d_file_download);
            this.i_m_d_file_root = (LinearLayout) rootView.findViewById(R.id.i_m_d_file_root);
        }

    }
}
