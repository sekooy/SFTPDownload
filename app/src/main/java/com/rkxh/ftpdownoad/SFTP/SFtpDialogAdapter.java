package com.rkxh.ftpdownoad.SFTP;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rkxh.ftpdownoad.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


//作品list带图片的适配器，异步加载
public class SFtpDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<String> list;
    private String localPath;
    private boolean[] isCheck = null;

    private OnItemClickListener onItemClickListener;

    public SFtpDialogAdapter(List<String> list, String localPath, Context context) {
        this.list = list;
        this.localPath = localPath;
        this.context = context;

        if (list != null) {
            isCheck = new boolean[list.size()];
            for (int i = 0; i < list.size(); i++) {
                isCheck[i] = false;
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //给Adapter添加布局，bq把这个view传递给HoldView，让HoldView找到空间
        View view = LayoutInflater.from(context).inflate(R.layout.item_name, parent, false);
        HoldView holdView = new HoldView(view);
        return holdView;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        //position为Adapter的位置，数据从list里面可以拿出来。

        ((HoldView) holder).tvName.setText(list.get(position));

        if (getFileName(localPath).contains(list.get(position))) {
            ((HoldView) holder).tvDown.setVisibility(View.VISIBLE);
        } else {
            ((HoldView) holder).tvDown.setVisibility(View.GONE);
        }

        File file = new File(localPath + list.get(position));


        if (list.get(position).contains(".json")) {
            ((HoldView) holder).checkBox.setVisibility(View.VISIBLE);
            ((HoldView) holder).checkBox.setChecked(isCheck[position]);
            ((HoldView) holder).checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    choiceState(position);
                    //onItemClickListener.isCheck();
                }
            });
        } else {
            ((HoldView) holder).checkBox.setVisibility(View.GONE);
            ((HoldView) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.isCheck(list.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private class HoldView extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvDown;
        CheckBox checkBox;

        public HoldView(View itemView) {
            super(itemView);
            //根据onCreateViewHolder的HoldView所添加的xml布局找到空间
            tvName = itemView.findViewById(R.id.tv_name);
            tvDown = itemView.findViewById(R.id.tv_down);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    /**
     * 改变某一个选项的状态
     *
     * @param post
     */
    public void choiceState(int post) {
        isCheck[post] = isCheck[post] != true;

        this.notifyDataSetChanged();
    }


    public void deselectAll() {
        // 全不选
        for (int i = 0; i < isCheck.length; i++) {
            isCheck[i] = false;
        }
        this.notifyDataSetChanged();
    }


    /**
     * 获取选中的
     *
     * @return
     */
    public List<String> selectCheck() {
        List<String> selectList = new ArrayList<>();
        for (int i = 0; i < isCheck.length; i++) {
            if (isCheck[i] == true) {
                selectList.add(list.get(i));
            }
        }
        return selectList;
    }

    public List<String> getFileName(String fileAbsolutePaht) {
        List<String> result = new ArrayList<>();
        File file = new File(fileAbsolutePaht);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                if (!file1.isDirectory()) {
                    result.add(file1.getName());
                }
            }
        }
        return result;
    }


    public interface OnItemClickListener {
        void isCheck(String path);
    }
}
