package zhexian.app.smartcall.contact;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import zhexian.app.smartcall.ContactDetailActivity;
import zhexian.app.smartcall.R;
import zhexian.app.smartcall.base.BaseActivity;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.image.ZImage;
import zhexian.app.smartcall.tools.DoAction;
import zhexian.app.smartcall.tools.Utils;
import zhexian.app.smartcall.ui.NotifyBar;

/**
 * 联系人绑定类
 */
class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int NORMAL_ITEM = 0;
    private static final int GROUP_ITEM = 1;
    private BaseActivity mBaseActivity;
    private List<ContactEntity> mDataList;
    private LayoutInflater mLayoutInflater;
    private BaseApplication mBaseApp;
    private ContactListFragment mContactListFragment;

    public ContactAdapter(ContactListFragment contactListFragment, List<ContactEntity> mDataList) {
        this.mBaseActivity = contactListFragment.mBaseActivity;
        this.mDataList = mDataList;
        mLayoutInflater = LayoutInflater.from(mBaseActivity);
        mBaseApp = (BaseApplication) mBaseActivity.getApplication();
        mContactListFragment = contactListFragment;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == NORMAL_ITEM) {
            return new NormalItemHolder(mLayoutInflater.inflate(R.layout.contact_list_item_normal, parent, false));
        } else {
            return new GroupItemHolder(mLayoutInflater.inflate(R.layout.contact_list_item_group, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContactEntity entity = mDataList.get(position);

        if (null == entity)
            return;

        if (holder instanceof GroupItemHolder) {
            bindGroupItem(entity, (GroupItemHolder) holder);
        } else {
            bindNormalItem(entity, (NormalItemHolder) holder);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return GROUP_ITEM;

        char current = mDataList.get(position).getUserNameStartPY();
        char previous = mDataList.get(position - 1).getUserNameStartPY();

        return current != previous ? GROUP_ITEM : NORMAL_ITEM;
    }

    private void bindNormalItem(ContactEntity entity, NormalItemHolder normalItemHolder) {
        int avatarSize = mBaseActivity.baseApp.getAvatarWidth();
        ZImage.ready().want(entity.getAvatarURL()).reSize(avatarSize, avatarSize).cache(ZImage.CacheType.DiskMemory).empty(R.drawable.user_default).into(normalItemHolder.userAvatar);

        normalItemHolder.userName.setText(entity.getUserName());
        normalItemHolder.userJob.setText(entity.getJobTitle());
    }

    private void bindGroupItem(ContactEntity entity, GroupItemHolder holder) {
        bindNormalItem(entity, holder);
        holder.userGroup.setText(String.valueOf(entity.getUserNameStartPY()));
    }

    private String GetCallNumber(boolean isCallShort, String longNo, String shortNo) {

        if (shortNo.isEmpty())
            return longNo;

        return isCallShort ? shortNo : longNo;
    }

    public class NormalItemHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView userAvatar;
        TextView userJob;

        public NormalItemHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.contact_userName);
            userAvatar = (ImageView) itemView.findViewById(R.id.contact_avatar);
            userJob = (TextView) itemView.findViewById(R.id.contact_job);

            itemView.findViewById(R.id.contact_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactEntity entity = mDataList.get(getPosition());
                    String number = GetCallNumber(mBaseApp.getIsCallShort(), entity.getPhone(), entity.getShortPhone());
                    if (number.isEmpty()) {
                        if (mContactListFragment.isLoadingImage)
                            Utils.toast(mBaseActivity, R.string.alert_empty_number);
                        else
                            mBaseActivity.notify.show(R.string.alert_empty_number, NotifyBar.DURATION_SHORT, NotifyBar.IconType.Error);
                        return;
                    }
                    DoAction.Call(mBaseActivity, number);
                    mContactListFragment.isNeedToAddContact = true;
                    mContactListFragment.contactToAdd = entity;
                }
            });

            itemView.findViewById(R.id.contact_container).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ContactEntity entity = mDataList.get(getPosition());
                    ContactDetailActivity.actionStart(mBaseActivity, entity);
                    return true;
                }
            });
        }
    }

    public class GroupItemHolder extends NormalItemHolder {
        TextView userGroup;

        public GroupItemHolder(View itemView) {
            super(itemView);
            userGroup = (TextView) itemView.findViewById(R.id.contact_group);
        }
    }
}
