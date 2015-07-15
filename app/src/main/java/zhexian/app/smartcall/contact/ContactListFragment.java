package zhexian.app.smartcall.contact;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import zhexian.app.smartcall.MainActivity;
import zhexian.app.smartcall.R;
import zhexian.app.smartcall.base.BaseActivity;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.image.ImageTaskManager;
import zhexian.app.smartcall.image.ZImage;
import zhexian.app.smartcall.lib.ZContact;
import zhexian.app.smartcall.tools.Format;
import zhexian.app.smartcall.tools.Utils;
import zhexian.app.smartcall.ui.LetterSideBar;
import zhexian.app.smartcall.ui.NotifyBar;


public class ContactListFragment extends Fragment implements LetterSideBar.OnLetterChangedListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int REFRESH_PROCESS_IMAGE_DURATION = 100;
    private static final int MESSAGE_REFRESH_COUNT = 1;
    private static final int MESSAGE_DONE = 2;
    private static final int TRIGGER_AUTO_REFRESH = 0;
    private static final int TRIGGER_HAND_REFRESH = 1;
    /**
     * wifi下自动更新，最低间隔为1天，单位毫秒
     */
    private static final long REFRESH_CONTACTS_MIN_DURATION = 86400000;
    public boolean isLoadingImage;
    public boolean isNeedToAddContact;
    public BaseActivity mBaseActivity;
    public ContactEntity contactToAdd;

    private BaseApplication mBaseApp;
    private InputMethodManager imm;
    private boolean isRequestData;
    private int previousIndex = 0;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ContactAdapter contactAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private List<ContactEntity> mTotalContacts;
    private List<ContactEntity> mSearchContacts;
    private HashMap<Character, Integer> mIndexMap;
    private List<Character> mLetterList;
    private LetterSideBar letterSideBar;
    private TextView mTextViewChar;
    private EditText mSearchText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        mBaseActivity = (BaseActivity) getActivity();
        mBaseApp = mBaseActivity.baseApp;
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mIndexMap = new HashMap<>();
        mLetterList = new ArrayList<>();
        letterSideBar = ((LetterSideBar) view.findViewById(R.id.contact_letter_side_bar));
        letterSideBar.setOnLetterChangedListener(this);

        mTotalContacts = Dal.getList(mBaseApp);
        mSearchContacts = new ArrayList<>(mTotalContacts);
        generateIndexMap(mTotalContacts);
        letterSideBar.Init(mLetterList);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.contact_list_swipe);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.contact_list);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        contactAdapter = new ContactAdapter(this, mSearchContacts);
        recyclerView.setAdapter(contactAdapter);
        mTextViewChar = (TextView) view.findViewById(R.id.contact_group_char);
        ImageView mKeyboardBtn = (ImageView) view.findViewById(R.id.bar_keyboard_btn);
        mKeyboardBtn.setOnClickListener(this);
        mSearchText = (EditText) view.findViewById(R.id.bar_keyboard_text);

        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchContacts();
                contactAdapter.notifyDataSetChanged();
            }
        });
        showImageProcessBar();
        boolean isNeedRefreshContacts = mBaseApp.isNetworkWifi() && new Date().getTime() - mBaseApp.getLastModifyTime() >= REFRESH_CONTACTS_MIN_DURATION;

        if (isNeedRefreshContacts && !isRequestData && !isLoadingImage)
            new LoadContactTask().execute(TRIGGER_AUTO_REFRESH);
        else
            ZImage.getInstance().reloadMemory();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isNeedToAddContact)
            showAddContactDialog();
    }

    @Override
    public void onRefresh() {
        if (!mBaseApp.isNetworkAvailable()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Utils.toast(mBaseApp, R.string.alert_network_not_available);
            return;
        }

        if (isLoadingImage) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (!isRequestData)
            new LoadContactTask().execute(TRIGGER_HAND_REFRESH);
    }

    private void searchContacts() {
        String searchText = mSearchText.getText().toString().toLowerCase();
        mSearchContacts.clear();

        if (searchText.isEmpty()) {
            for (ContactEntity entity : mTotalContacts) {
                mSearchContacts.add(entity);
            }
        } else {
            boolean isChinese = Format.isChinese(searchText);
            boolean isNumeric = Format.isNumeric(searchText);

            for (ContactEntity entity : mTotalContacts) {
                boolean isMatch;

                if (isChinese) {
                    isMatch = entity.getUserName().contains(searchText);
                } else {
                    if (isNumeric)
                        isMatch = entity.getPhone().contains(searchText) || entity.getShortPhone().contains(searchText);
                    else
                        isMatch = entity.getUserNamePY().contains(searchText) || entity.getUserNameHeadPY().contains(searchText);
                }

                if (isMatch)
                    mSearchContacts.add(entity);
            }
        }
    }

    private void generateIndexMap(List<ContactEntity> dataList) {
        if (dataList == null || dataList.size() == 0)
            return;

        mIndexMap.clear();
        mLetterList.clear();
        char previous = '.';

        for (int i = 0; i < dataList.size(); i++) {
            char current = dataList.get(i).getUserNameStartPY();

            if (previous != current) {
                mIndexMap.put(current, i);
                mLetterList.add(current);
                previous = current;
            }
        }
    }

    private int getGroupPos(char groupChar) {
        if (mIndexMap == null || mIndexMap.size() == 0)
            return -1;

        if (mIndexMap.containsKey(groupChar))
            return mIndexMap.get(groupChar);

        return -1;
    }


    private void showImageProcessBar() {
        if (!mBaseApp.isNetworkWifi())
            return;

        if (mBaseApp.isLoadMostAvatars())
            return;

        isLoadingImage = true;

        final NotifyHandler mNotifyHandler = new NotifyHandler(mBaseActivity);
        final Timer timer = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                int size = ImageTaskManager.getInstance().getLeftSaveTaskCount();

                if (size <= 0) {
                    isLoadingImage = false;
                    mNotifyHandler.sendEmptyMessage(MESSAGE_DONE);
                    timer.cancel();
                    return;
                }
                Message message = new Message();
                message.what = MESSAGE_REFRESH_COUNT;
                message.arg1 = size;
                mNotifyHandler.sendMessage(message);
            }
        };
        timer.schedule(timerTask, 0, REFRESH_PROCESS_IMAGE_DURATION);
    }

    @Override
    public void OnTouchDown() {
        mTextViewChar.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnTouchMove(int yPos) {
        mTextViewChar.setY(yPos);
    }

    @Override
    public void OnTouchUp() {
        mTextViewChar.setVisibility(View.GONE);
    }

    @Override
    public void OnLetterChanged(Character s, int index) {
        mTextViewChar.setText(String.valueOf(s));

        int pos = getGroupPos(s);

        if (index < previousIndex)
            mLinearLayoutManager.scrollToPosition(pos);
        else
            mLinearLayoutManager.scrollToPositionWithOffset(pos, 0);

        previousIndex = index;
    }

    @Override
    public void onClick(View v) {
        int senderID = v.getId();

        switch (senderID) {
            case R.id.bar_keyboard_btn:
                changeInputState();
                break;
        }
    }

    private void changeInputState() {
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void showAddContactDialog() {
        isNeedToAddContact = false;
        if (ZContact.isPhoneExists(mBaseActivity, contactToAdd.getPhone()))
            return;

        AlertDialog dialog = new AlertDialog.Builder(mBaseActivity)
                .setTitle("添加到通讯录")
                .setMessage(String.format("将%s的信息导入本地通讯录？ ", contactToAdd.getUserName()))
                .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ZContact.Add(mBaseActivity, contactToAdd);
                        dialog.dismiss();
                        contactToAdd = null;
                        if (isLoadingImage)
                            Utils.toast(mBaseActivity, "添加成功");
                        else
                            mBaseActivity.notify.show("添加成功", NotifyBar.DURATION_SHORT, NotifyBar.IconType.Success);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        contactToAdd = null;
                    }
                }).create();

        dialog.show();
    }

    /**
     * 清理本地的头像图片，如果用户被删除、或者更新了新头像
     *
     * @param oldContactsList 老的用户列表
     * @param newContactsList 新获取的用户列表
     */
    private void cleanLocalAvatar(List<ContactEntity> oldContactsList, List<ContactEntity> newContactsList) {

        for (ContactEntity oldContact : oldContactsList) {
            String oldPhone = oldContact.getPhone();

            if (oldPhone.isEmpty())
                continue;

            String oldAvatar = oldContact.getAvatarURL();

            if (oldAvatar.isEmpty())
                continue;

            boolean isFound = false;

            for (ContactEntity newContact : newContactsList) {
                if (oldPhone.equals(newContact.getPhone())) {
                    isFound = true;
                    //用户是否修改了头像
                    if (!oldAvatar.equals(newContact.getAvatarURL()))
                        ZImage.getInstance().deleteFromLocal(oldAvatar);
                }
            }
            //用户被删除了，则把本地图片也删除
            if (!isFound)
                ZImage.getInstance().deleteFromLocal(oldAvatar);
        }
    }

    static class NotifyHandler extends Handler {
        WeakReference<BaseActivity> baseActivity;

        NotifyHandler(BaseActivity _activity) {
            baseActivity = new WeakReference<>(_activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity _activity = baseActivity.get();

            if (_activity == null)
                return;

            if (msg.what == MESSAGE_REFRESH_COUNT) {
                _activity.notify.show(String.format("正在玩命加载头像，剩余%d", msg.arg1), NotifyBar.IconType.Progress);
            } else if (msg.what == MESSAGE_DONE) {
                _activity.baseApp.setIsLoadMostAvatars(true);
                _activity.notify.show("已全部加载完毕：）", NotifyBar.IconType.Success);
                _activity.notify.hide(2000);
            }
        }
    }

    class LoadContactTask extends AsyncTask<Integer, Void, Boolean> {
        int taskType = 0;

        @Override
        protected void onPreExecute() {
            isRequestData = true;
            mSwipeRefreshLayout.setRefreshing(true);
        }


        @Override
        protected Boolean doInBackground(Integer... params) {
            taskType = params[0];
            String result = Dal.readFromHttp(mBaseApp.getServiceUrl(), mBaseApp.getUserName(), mBaseApp.getPassword());
            if (result == null || result.isEmpty())
                return false;

            if (!Dal.SaveToFile(mBaseApp, result))
                return false;

            List<ContactEntity> temp = Dal.getList(mBaseApp);
            cleanLocalAvatar(mTotalContacts, temp);

            mTotalContacts = temp;
            mSearchContacts.clear();
            for (ContactEntity entity : mTotalContacts) {
                mSearchContacts.add(entity);
            }
            generateIndexMap(mTotalContacts);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            isRequestData = false;
            mSwipeRefreshLayout.setRefreshing(false);

            if (aBoolean) {
                contactAdapter.notifyDataSetChanged();
                letterSideBar.Update();
                letterSideBar.invalidate();

                mSearchText.setText("");

                if (taskType == TRIGGER_HAND_REFRESH) {
                    mBaseActivity.notify.show("更新成功：）", NotifyBar.DURATION_SHORT, NotifyBar.IconType.Success);
                    showImageProcessBar();
                }
                ZImage.getInstance().reloadMemory();
                mBaseApp.setLastModifyTime(new Date().getTime());
            } else {
                Utils.toast(mBaseApp, R.string.alert_refresh_failed);
                mBaseApp.setIsLogin(false);
                ((MainActivity) getActivity()).JumpToLogin();
            }
        }
    }
}