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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import zhexian.app.smartcall.MainActivity;
import zhexian.app.smartcall.R;
import zhexian.app.smartcall.base.BaseActivity;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.image.ImageTaskManager;
import zhexian.app.smartcall.lib.ZContact;
import zhexian.app.smartcall.tools.Format;
import zhexian.app.smartcall.tools.Utils;
import zhexian.app.smartcall.ui.LetterSideBar;
import zhexian.app.smartcall.ui.NotifyBar;


public class ContactListFragment extends Fragment implements LetterSideBar.OnLetterChangedListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int REFRESH_PROCESS_IMAGE_DURATION = 100;
    private static final int MIN_TO_SHOW_TASK_BAR = 20;
    private static final int MESSAGE_REFRESH_COUNT = 1;
    private static final int MESSAGE_DONE = 2;

    public boolean isLoadingImage;
    public boolean isNeedToAddContact;
    public BaseActivity baseActivity;
    public BaseApplication baseApp;
    public ContactEntity contactToAdd;
    private InputMethodManager imm;
    private boolean isRequestData;
    private int previousIndex = 0;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private List<ContactEntity> mTotalContacts;
    private List<ContactEntity> mSearchContacts;
    private HashMap<Character, Integer> mIndexMap;
    private List<Character> mLetterList;
    private LetterSideBar letterSideBar;
    private TextView mTextViewChar;
    private EditText mSearchText;
    private ImageView mKeyboardBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        baseActivity = (BaseActivity) getActivity();
        baseApp = baseActivity.baseApp;
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mIndexMap = new HashMap<>();
        mLetterList = new ArrayList<>();
        letterSideBar = ((LetterSideBar) view.findViewById(R.id.contact_letter_side_bar));
        letterSideBar.setOnLetterChangedListener(this);

        mTotalContacts = Dal.getList(baseApp);
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
        recyclerView = (RecyclerView) view.findViewById(R.id.contact_list);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        contactAdapter = new ContactAdapter(this, mSearchContacts);
        recyclerView.setAdapter(contactAdapter);
        mTextViewChar = (TextView) view.findViewById(R.id.contact_group_char);
        mKeyboardBtn = (ImageView) view.findViewById(R.id.bar_keyboard_btn);
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

        if (!baseApp.isLoadMostAvatars())
            showImageProcessBar();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isNeedToAddContact)
            showDialog();
    }

    @Override
    public void onRefresh() {
        if (!baseApp.isNetworkAvailable()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Utils.toast(baseApp, R.string.alert_network_not_available);
            return;
        }

        if (isLoadingImage) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (!isRequestData)
            new LoadContactTask().execute();
    }

    void searchContacts() {
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

    void generateIndexMap(List<ContactEntity> dataList) {
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

    public int getGroupPos(char groupChar) {
        if (mIndexMap == null || mIndexMap.size() == 0)
            return -1;

        if (mIndexMap.containsKey(groupChar))
            return mIndexMap.get(groupChar);

        return -1;
    }

    void showImageProcessBar() {
        if (!baseApp.isNetworkWifi())
            return;

        isLoadingImage = true;

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == MESSAGE_REFRESH_COUNT) {
                    baseActivity.notify.show(String.format("正在玩命加载头像，剩余%d", msg.arg1), NotifyBar.IconType.Progress);
                } else if (msg.what == MESSAGE_DONE) {
                    baseApp.setIsLoadMostAvatars(true);
                    baseActivity.notify.show("已全部加载完毕：）", NotifyBar.IconType.Success);
                    baseActivity.notify.hide(2000);
                }
            }
        };

        final Timer timer = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                int size = ImageTaskManager.getInstance().getLeftTaskCount();

                if (size <= 0) {
                    isLoadingImage = false;
                    handler.sendEmptyMessage(MESSAGE_DONE);
                    timer.cancel();
                    return;
                }
                Message message = new Message();
                message.what = MESSAGE_REFRESH_COUNT;
                message.arg1 = size;
                handler.sendMessage(message);
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


    void changeInputState() {
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    int getNotIncludeCount(List<ContactEntity> newList, List<ContactEntity> oldList) {
        int addAmount = 0;

        for (ContactEntity nEntity : newList) {
            String phone = nEntity.getPhone();
            if (phone.isEmpty())
                continue;
            boolean isFound = false;

            for (ContactEntity oEntity : oldList) {
                if (oEntity.getPhone().equals(phone)) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound)
                addAmount++;
        }
        return addAmount;
    }

    void showDialog() {
        isNeedToAddContact = false;
        if (ZContact.isPhoneExists(baseActivity, contactToAdd.getPhone()))
            return;

        AlertDialog dialog = new AlertDialog.Builder(baseActivity)
                .setTitle("添加到通讯录")
                .setMessage(String.format("将%s的信息导入本地通讯录？ ", contactToAdd.getUserName()))
                .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ZContact.Add(baseActivity, contactToAdd);
                        dialog.dismiss();
                        contactToAdd = null;
                        if (isLoadingImage)
                            Utils.toast(baseActivity, "添加成功");
                        else
                            baseActivity.notify.show("添加成功", NotifyBar.DURATION_SHORT, NotifyBar.IconType.Success);
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

    class LoadContactTask extends AsyncTask<Void, Void, Boolean> {
        int addAmount = 0;
        int deleteAmount = 0;

        @Override
        protected void onPreExecute() {
            isRequestData = true;
            mSwipeRefreshLayout.setRefreshing(true);
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            String result = Dal.readFromHttp(baseApp.getServiceUrl(), baseApp.getUserName(), baseApp.getPassword());
            if (result == null || result.isEmpty())
                return false;

            if (!Dal.SaveToFile(baseApp, result))
                return false;

            List<ContactEntity> temp = Dal.getList(baseApp);
            addAmount = getNotIncludeCount(temp, mTotalContacts);
            deleteAmount = getNotIncludeCount(mTotalContacts, temp);

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
                String alertStr = "更新成功";

                if (addAmount == 0 && deleteAmount == 0) {
                    alertStr += "，没啥变化";
                    baseActivity.notify.show(alertStr, NotifyBar.DURATION_SHORT, NotifyBar.IconType.Success);
                } else {
                    if (addAmount > 0)
                        alertStr += String.format(",新来了%d人", addAmount);

                    if (deleteAmount > 0)
                        alertStr += String.format(",离开了%d人", deleteAmount);

                    letterSideBar.invalidate();
                    mSearchText.setText("");
                    baseActivity.notify.show(alertStr, NotifyBar.DURATION_MIDDLE, NotifyBar.IconType.Success);

                    if (addAmount > MIN_TO_SHOW_TASK_BAR)
                        showImageProcessBar();
                }
            } else {
                Utils.toast(baseApp, R.string.alert_refresh_failed);
                baseApp.setIsLogin(false);
                ((MainActivity) getActivity()).JumpToLogin();
            }
        }
    }
}