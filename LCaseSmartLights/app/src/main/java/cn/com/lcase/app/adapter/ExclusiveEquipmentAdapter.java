package cn.com.lcase.app.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.net.LCaseConstants;

/**
 * Created by admin on 2016/10/21.
 */

public class ExclusiveEquipmentAdapter extends BaseExpandableListAdapter {

    List<Group> itemList;
    Context mContext;

    public ExclusiveEquipmentAdapter(List<Group> itemList, Context mContext) {
        this.itemList = itemList;
        this.mContext = mContext;
    }


    @Override
    public int getGroupCount() {
        return itemList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupPosition).getDevicelist().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return itemList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemList.get(groupPosition).getDevicelist().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.group_layout, null);
            holder = new GroupViewHolder();
            holder.chIsCheck = (CheckBox) convertView.findViewById(R.id.group_check);
            holder.tvGroupName = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }
        Group items = itemList.get(groupPosition);
        holder.tvGroupName.setText(items.getGroupname());
        if (items.getDevicelist().size() > 0) {
            for (Device device : items.getDevicelist()) {
                if (!device.isUnique()) {
                    holder.chIsCheck.setChecked(false);
                    break;
                }
                holder.chIsCheck.setChecked(true);
            }
        } else {
            holder.chIsCheck.setChecked(items.isChecked());
        }

        holder.chIsCheck.setOnClickListener(new Group_CheckBox_Click(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.child_layout, null);
            holder = new ChildViewHolder();
            holder.child_check2_layout = (RelativeLayout) convertView.findViewById(R.id.child_check2_layout);
            holder.chIsCheck = (ImageView) convertView.findViewById(R.id.child_check2);
            holder.img = (ImageView) convertView.findViewById(R.id.img_id);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tvGroupName = (TextView) convertView.findViewById(R.id.tv_fenzu);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }
        Device item = itemList.get(groupPosition).getDevicelist().get(childPosition);
        if (item.isUnique()) {
            holder.chIsCheck.setImageResource(R.mipmap.xuanzhong);
        } else {
            holder.chIsCheck.setImageResource(R.mipmap.tuoyuan);
        }
        holder.chIsCheck.setSelected(item.isUnique());
//        holder.child_check2_layout.setOnClickListener(new Child_CheckBox_Click(groupPosition, childPosition));
        holder.img.setImageResource(LCaseConstants.DEVICE_IMG[item.getImage()]);
        holder.tvName.setText(item.getName());
        holder.tvGroupName.setText("【" + item.getGroupname() + "】");
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    class GroupViewHolder {
        TextView tvGroupName;
        CheckBox chIsCheck;
    }

    class ChildViewHolder {
        ImageView img;
        TextView tvName;
        TextView tvGroupName;
        ImageView chIsCheck;
        RelativeLayout child_check2_layout;
    }

    /**
     * 勾選 Group CheckBox 時，存 Group CheckBox 的狀態，以及改變 Child CheckBox 的狀態
     */
    class Group_CheckBox_Click implements View.OnClickListener {
        private int groupPosition;

        Group_CheckBox_Click(int groupPosition) {
            this.groupPosition = groupPosition;
        }

        public void onClick(View v) {
//            itemList.get(groupPosition).toggle();
            // 將 Children 的 isChecked 全面設成跟 Group 一樣
//            int childrenCount = itemList.get(groupPosition).getDevicelist().size();
            boolean groupIsChecked = itemList.get(groupPosition).isChecked();
            if (groupIsChecked) {
                groupIsChecked = false;
            } else {
                groupIsChecked = true;
            }
        /*    for (int i = 0; i < childrenCount; i++) {
                itemList.get(groupPosition).getDevicelist().get(i).setUnique(groupIsChecked);
            }*/
       /*     boolean childrenAllIsChecked = true;
            for (int i = 0; i < itemList.size(); i++) {
                if (itemList.get(i).getDevicelist().size() == 0) {
                    childrenAllIsChecked = false;
                    itemList.get(i).setChecked(childrenAllIsChecked);
                } else {
                    childrenAllIsChecked = true;
                    for (int j = 0; j < itemList.get(i).getDevicelist().size(); j++) {
                        if (!itemList.get(i).getDevicelist().get(j).isUnique()) {
                            childrenAllIsChecked = false;
                        }
                    }
                }
                itemList.get(i).setChecked(childrenAllIsChecked);
            }*/
//其他组的数据都变为没有选中 这是一次单选
            for (int i = 0; i < itemList.size(); i++) {
                List<Device> temp = itemList.get(i).getDevicelist();
                if (i != groupPosition) {
                    itemList.get(i).setChecked(false);
                    for (int j = 0; j < temp.size(); j++) {
                        temp.get(j).setUnique(false);
                    }
                } else {
                    itemList.get(i).setChecked(groupIsChecked);
                    for (int j = 0; j < temp.size(); j++) {
                        temp.get(j).setUnique(groupIsChecked);
                    }
                }


            }

            // 注意，一定要通知 ExpandableListView 資料已經改變，ExpandableListView 會重新產生畫面
            notifyDataSetChanged();
        }
    }

    /**
     * 勾選 Child CheckBox 時，存 Child CheckBox 的狀態
     */
    class Child_CheckBox_Click implements View.OnClickListener {
        private int groupPosition;
        private int childPosition;

        Child_CheckBox_Click(int groupPosition, int childPosition) {
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;
        }

        public void onClick(View v) {
            handleClick(childPosition, groupPosition);
        }
    }

    public void handleClick(int childPosition, int groupPosition) {
        itemList.get(groupPosition).getDevicelist().get(childPosition).setUnique(!itemList.get(groupPosition).getDevicelist().get(childPosition).isUnique());

        // 檢查 Child CheckBox 是否有全部勾選，以控制 Group CheckBox
        int childrenCount = itemList.get(groupPosition).getDevicelist().size();
        boolean childrenAllIsChecked = true;
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getDevicelist().size() == 0) {
                childrenAllIsChecked = false;
                itemList.get(i).setChecked(childrenAllIsChecked);
            } else {
                childrenAllIsChecked = true;
                for (int j = 0; j < itemList.get(i).getDevicelist().size(); j++) {
                    if (!itemList.get(i).getDevicelist().get(j).isUnique()) {
                        childrenAllIsChecked = false;
                    }
                }
            }
            itemList.get(i).setChecked(childrenAllIsChecked);
        }
//        itemList.get(groupPosition).setChecked(childrenAllIsChecked);

        // 注意，一定要通知 ExpandableListView 資料已經改變，ExpandableListView 會重新產生畫面
        notifyDataSetChanged();
    }


}
