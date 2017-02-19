package com.example.geethanjalijeevanatham.hackathonunknownapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

/**
 * Created by geethanjalijeevanatham on 2/5/17.
 */

public class FbUserListAdapter extends ArrayAdapter<fbusernode> {
    private List<fbusernode> items;
    private Context context;
    private Context context1 = this.getContext();
    private int layoutResourceId;
    int temp = 0;

    public FbUserListAdapter(Context context, int resource) {
        super(context, resource);
    }
    public FbUserListAdapter(Context context, int layoutResourceID, List<fbusernode> items){
        super(context, layoutResourceID, items);
        this.items = items;
        this.context = context;
        this.layoutResourceId = layoutResourceID;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FbUserholder holder = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new FbUserholder();
        holder.node = items.get(position);
        holder.textusername = (TextView)row.findViewById(R.id.username);
        holder.btncall = (Button) row.findViewById(R.id.callMenu);
        holder.btnmsg = (Button) row.findViewById(R.id.sendmessageMenu);

        row.setTag(holder);
        setupItem(holder);
        return row;
    }

    private void setupItem(FbUserholder holder) {
        holder.textusername.setText(holder.node.name.toString());
        holder.btncall.setText("Call");
        holder.btnmsg.setText("Message");

    }
}

class FbUserholder{
    fbusernode node;
    TextView textusername;
    Button btncall;
    Button btnmsg;
}
