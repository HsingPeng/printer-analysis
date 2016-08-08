package com.github.openthos.printer.localprintui.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.statusbar.phone.PrinterJobStatus;
import com.github.openthos.printer.localprint.aidl.IJobPauseTaskCallBack;
import com.github.openthos.printer.localprint.aidl.IJobResumeTaskCallBack;
import com.github.openthos.printer.localprint.aidl.IJobCancelTaskCallBack;
import com.github.openthos.printer.localprintui.APP;
import com.github.openthos.printer.localprintui.R;
import com.github.openthos.printer.localprintui.util.LogUtils;

import java.util.List;

/**
 * Jobs' List adapter
 * Created by bboxh on 2016/6/5.
 */
public class JobAdapter extends BaseAdapter {
    private static final String TAG = "JobAdapter";
    private final Context mContext;
    private final List<PrinterJobStatus> mList;

    public JobAdapter(Context context, List<PrinterJobStatus> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Holder holder;
        final PrinterJobStatus item = mList.get(position);

        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_listview_job, null);

            holder = new Holder();

            holder.textview_name = (TextView) convertView.findViewById(R.id.textview_name);
            holder.textview_device = (TextView) convertView.findViewById(R.id.textview_device);
            holder.textview_status = (TextView) convertView.findViewById(R.id.textview_status);
            holder.textview_size = (TextView) convertView.findViewById(R.id.textview_size);
            holder.button_pause = (Button) convertView.findViewById(R.id.button_pause);
            holder.button_remove = (Button) convertView.findViewById(R.id.button_remove);

            convertView.setTag(holder);

        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.textview_name.setText(item.getFileName());
        holder.textview_device.setText(item.getPrinter());
        holder.textview_size.setText(item.getSize());

        int status = item.getStatus();
        switch (status) {
            case PrinterJobStatus.STATUS_ERROR:
                holder.textview_status.setText(mContext.getResources().getString(R.string.error)
                        + " " + mList.get(position).getERROR());
                break;
            case PrinterJobStatus.STATUS_HOLDING:
                holder.textview_status.setText(R.string.pause);
                break;
            case PrinterJobStatus.STATUS_PRINTING:
                holder.textview_status.setText(R.string.printing);
                break;
            case PrinterJobStatus.STATUS_READY:
                holder.textview_status.setText(R.string.ready);
                break;
            case PrinterJobStatus.STATUS_WAITING_FOR_PRINTER:
                holder.textview_status.setText(R.string.waiting_for_printer);
                break;
            default:
                holder.textview_status.setText(R.string.unknown);
                break;
        }

        //判断状态
        if (item.getStatus() == PrinterJobStatus.STATUS_HOLDING) {
            holder.button_pause.setText(R.string.resume);
        } else {
            holder.button_pause.setText(R.string.pause);
        }

        holder.button_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getStatus() == PrinterJobStatus.STATUS_HOLDING) {
                    resumeJob(item, v);
                } else {
                    pauseJob(item, v);
                }
            }
        });

        holder.button_remove.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                removeJob(item);
            }
        });

        return convertView;

    }

    private void resumeJob(final PrinterJobStatus jobItem, final View v) {
        boolean flag = false;
        try {
            final Handler handler = new Handler();
            flag = APP.remoteExec(new IJobResumeTaskCallBack.Stub() {

                @Override
                public void onPostExecute(final boolean aBoolean) throws RemoteException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (aBoolean) {
                                //Toast.makeText(mContext, R.string.resumed, Toast.LENGTH_SHORT).show();
                                jobItem.setStatus(PrinterJobStatus.STATUS_READY);
                                Button button = (Button) v;
                                button.setText(R.string.pause);
                            } else {
                                Toast.makeText(mContext, R.string.resume_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public PrinterJobStatus bindStart() throws RemoteException {
                    return jobItem;
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (!flag) {
            LogUtils.d(TAG, "IJobResumeTaskCallBack connect_service_error");
            Toast.makeText(mContext, R.string.connect_service_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void removeJob(final PrinterJobStatus jobItem) {
        boolean flag = false;
        try {
            final Handler handler = new Handler();
            flag = APP.remoteExec(new IJobCancelTaskCallBack.Stub() {

                @Override
                public void onPostExecute(final boolean aBoolean) throws RemoteException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (aBoolean) {
                                //Toast.makeText(mContext, R.string.canceled, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, R.string.cancel_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public PrinterJobStatus bindStart() throws RemoteException {
                    return jobItem;
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (!flag) {
            LogUtils.d(TAG, "IJobCancelTaskCallBack connect_service_error");
            Toast.makeText(mContext, R.string.connect_service_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseJob(final PrinterJobStatus jobItem, final View v) {
        boolean flag = false;
        try {
            final Handler handler = new Handler();
            flag = APP.remoteExec(new IJobPauseTaskCallBack.Stub() {

                @Override
                public void onPostExecute(final boolean aBoolean) throws RemoteException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (aBoolean) {
                                //Toast.makeText(mContext, R.string.paused, Toast.LENGTH_SHORT).show();
                                jobItem.setStatus(PrinterJobStatus.STATUS_HOLDING);
                                Button button = (Button) v;
                                button.setText(R.string.resume);
                            } else {
                                Toast.makeText(mContext, R.string.pause_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public PrinterJobStatus bindStart() throws RemoteException {
                    return jobItem;
                }

            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (!flag) {
            LogUtils.d(TAG, "IJobPauseTaskCallBack connect_service_error");
            Toast.makeText(mContext, R.string.connect_service_error, Toast.LENGTH_SHORT).show();
        }
    }


    class Holder {
        TextView textview_name;
        TextView textview_device;
        TextView textview_status;
        TextView textview_size;
        Button button_pause;
        Button button_remove;
    }

}
