package com.rkxh.ftpdownoad.SFTP;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.rkxh.ftpdownoad.LoadingDialog;
import com.rkxh.ftpdownoad.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import static android.content.ContentValues.TAG;

public class SFtpDialog implements View.OnClickListener {
    private Context context;
    private Dialog dialog;
    private TextView txt_title;
    private TextView txt_path;
    private ImageView img_back;
    private Button btn_ok;
    private Button btn_cancle;
    private Display display;

    private RecyclerView recyclerView;
    SFtpDialogAdapter adapter;

    LoadingDialog progressDialog;

    private SFTPUtils sftp;
    String host;
    int port;
    String userName;
    String password;
    String remotePath;
    String localPath;
    List<String> list = new ArrayList<>();
    String dateDir;

    public SFtpDialog(Context context, String host, int port,
                      String userName, String password,
                      String remotePath, String localPath) {
        this.context = context;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.remotePath = remotePath;
        this.localPath = localPath;
        dateDir = localPath;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public SFtpDialog builder() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_ftp, null);

        dialog = new Dialog(context, R.style.AlertDialogStyle);
        dialog.setContentView(view);
        //弹窗点击周围空白处弹出层自动消失弹窗消失(false时为点击周围空白处弹出层不自动消失)
        dialog.setCanceledOnTouchOutside(false);

        txt_title = view.findViewById(R.id.txt_title);
        img_back = view.findViewById(R.id.imageView);
        txt_path = view.findViewById(R.id.txt_path);

        recyclerView = view.findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        Refresh(remotePath);


        btn_ok = view.findViewById(R.id.btn_ok);
        btn_cancle = view.findViewById(R.id.btn_cancle);

        btn_cancle.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        img_back.setOnClickListener(this);
        txt_path.setText(remotePath);

        return this;
    }

    public SFtpDialog setTitle(String title) {
        if ("".equals(title)) {
            txt_title.setText("标题");
        } else {
            txt_title.setText(title);
        }
        return this;
    }

    public SFtpDialog setTitleTextColor(String color) {
        if (color != null) {
            txt_title.setTextColor(Color.parseColor(color));
        }
        return this;
    }

    public SFtpDialog setTitleTextSizeSp(int size) {
        if (size != 0) {
            txt_title.setTextSize(size);
        }
        return this;
    }

    public SFtpDialog setTitleTextBold(boolean b) {
        if (b) {
            TextPaint tp = txt_title.getPaint();
            tp.setFakeBoldText(true);
        } else {
            TextPaint tp = txt_title.getPaint();
            tp.setFakeBoldText(false);
        }
        return this;
    }

    public SFtpDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    /**
     * textSizeSp传0默认大小
     * textColor传""默认颜色
     * btnBgColor传""按钮默认背景颜色
     */
    public SFtpDialog setOkButton(String text, int textSizeSp, String textColor, String btnBgColor) {
        if ("".equals(text)) {
            btn_ok.setText("确定");
        } else {
            btn_ok.setText(text);
        }
        if (0 != textSizeSp) {
            btn_ok.setTextSize(textSizeSp);
        }
        if ("" != textColor) {
            btn_ok.setTextColor(Color.parseColor(textColor));
        }
        if ("" != btnBgColor) {
            btn_ok.setBackgroundColor(Color.parseColor(btnBgColor));
        }

        return this;
    }

    /**
     * textSizeSp传0默认大小
     * textColor传""默认颜色
     * btnBgColor传""按钮默认背景颜色
     */
    public SFtpDialog setCancleButton(String text, int textSizeSp, String textColor, String btnBgColor) {
        if ("".equals(text)) {
            btn_cancle.setText("取消");
        } else {
            btn_cancle.setText(text);
        }

        if (0 != textSizeSp) {
            btn_cancle.setTextSize(textSizeSp);
        }
        if ("" != textColor) {
            btn_cancle.setTextColor(Color.parseColor(textColor));
        }
        if ("" != btnBgColor) {
            btn_cancle.setBackgroundColor(Color.parseColor(btnBgColor));
        }
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView:
                img_back.setVisibility(View.GONE);
                txt_path.setText(remotePath);
                dateDir = localPath;
                Refresh(remotePath);
                break;
            case R.id.btn_cancle:
                dialog.dismiss();
                break;
            case R.id.btn_ok:
                fileDownload(dateDir);
                break;
            default:
                break;
        }
    }

    public void show() {
        dialog.show();
    }

    public void fileDownload(final String dateDir) {
        if (adapter.selectCheck().size() > 0) {
            progressDialog = new LoadingDialog(context);
            progressDialog.show();
            progressDialog.setCancelable(false);
            new Thread() {
                @Override
                public void run() {
                    //批量下载
                    for (String s : adapter.selectCheck()) {
                        sftp.downloadFile(remotePath, s,
                                dateDir, s);
                    }
                    Message message = new Message();
                    message.what = 1;
                    myHandler.sendMessage(message);
                }
            }.start();
        } else {
            Toast.makeText(context, "请选择要下载的文件", Toast.LENGTH_LONG).show();
        }
    }

    public void Refresh(final String path) {
        sftp = new SFTPUtils(host, port, userName, password, remotePath);
        list.clear();
        new Thread() {
            @Override
            public void run() {
                sftp.connect();
                try {
                    Vector v = sftp.listFiles(path);
                    if (v.size() > 0) {
                        Iterator it = v.iterator();
                        while (it.hasNext()) {
                            String filename = ((ChannelSftp.LsEntry) it.next()).getFilename();
                            if (filename.length() > 2) {
                                Log.e(TAG, "" + filename);
                                list.add(filename);
                            }
                        }
                        Message message = new Message();
                        message.what = 2;
                        myHandler.sendMessage(message);
                    }
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void initData() {
        adapter = new SFtpDialogAdapter(list, dateDir, context);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new SFtpDialogAdapter.OnItemClickListener() {
            @Override
            public void isCheck(String path) {
                img_back.setVisibility(View.VISIBLE);
                txt_path.setText(remotePath + path);
                dateDir = localPath + path + "/";
                Refresh(remotePath + path);
            }
        });
    }

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    progressDialog.dismiss();
                    adapter.deselectAll();
                    break;
                case 2:
                    initData();
                    adapter.notifyDataSetChanged();
                    break;
                case 3:
                    Log.e(TAG, "handleMessage: " + 123123);
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
