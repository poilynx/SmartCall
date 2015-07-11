package zhexian.app.smartcall;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import zhexian.app.smartcall.base.BaseActivity;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.contact.ContactEntity;
import zhexian.app.smartcall.image.ZImage;
import zhexian.app.smartcall.lib.ZContact;
import zhexian.app.smartcall.tools.DoAction;
import zhexian.app.smartcall.tools.Utils;
import zhexian.app.smartcall.ui.NotifyBar;

public class ContactDetailActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {
    private static final String PARAM_CONTACT_ENTITY = "zhexian.app.smartcall.contact.PARAM_CONTACT_ENTITY";
    private ContactEntity mEntity;
    private ClipboardManager clip;
    private View mBackBtn;

    private ImageView mUserAvatar;
    private TextView mUserName;
    private TextView mJobTitle;
    private TextView mCompany;
    private TextView mDepartment;

    private View mPhoneContainer;
    private View mPhoneNumberContainer;
    private TextView mPhoneNumber;
    private View mPhoneMessage;

    private View mShortPhoneContainer;
    private View mShortPhoneNumberContainer;
    private TextView mShortPhoneNumber;
    private View mShortPhoneMessage;

    private View mLocalContactContainer;
    private ImageView mLocalContactIcon;
    private TextView mLocalContactText;
    private boolean isNeedToAdd;

    public static void actionStart(Context context, ContactEntity entity) {
        Intent intent = new Intent(context, ContactDetailActivity.class);
        intent.putExtra(PARAM_CONTACT_ENTITY, entity);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isNeedToAdd)
            showDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        setContentView(R.layout.activity_contact_detail);
        baseApp = (BaseApplication) getApplication();
        mBackBtn = findViewById(R.id.detail_back);
        mUserAvatar = (ImageView) findViewById(R.id.detail_avatar);
        mUserName = (TextView) findViewById(R.id.detail_user_name);
        mJobTitle = (TextView) findViewById(R.id.detail_job);

        mPhoneContainer = findViewById(R.id.detail_phone_container);
        mPhoneNumberContainer = findViewById(R.id.detail_phone_number_container);
        mPhoneNumber = (TextView) findViewById(R.id.detail_phone_number);
        mPhoneMessage = findViewById(R.id.detail_phone_message);

        mShortPhoneContainer = findViewById(R.id.detail_short_phone_container);
        mShortPhoneNumberContainer = findViewById(R.id.detail_short_phone_number_container);
        mShortPhoneNumber = (TextView) findViewById(R.id.detail_short_phone_number);
        mShortPhoneMessage = findViewById(R.id.detail_short_phone_message);

        mLocalContactContainer = findViewById(R.id.detail_local_contact);
        mLocalContactIcon = (ImageView) findViewById(R.id.detail_local_contact_icon);
        mLocalContactText = (TextView) findViewById(R.id.detail_local_contact_text);
        mCompany = (TextView) findViewById(R.id.detail_company);
        mDepartment = (TextView) findViewById(R.id.detail_department);

        mBackBtn.setOnClickListener(this);
        mLocalContactContainer.setOnClickListener(this);

        mEntity = (ContactEntity) getIntent().getSerializableExtra(PARAM_CONTACT_ENTITY);
        bindData();
    }

    @Override
    public void onClick(View v) {
        int senderID = v.getId();

        switch (senderID) {
            case R.id.detail_back:
                finish();
                break;
            case R.id.detail_phone_number_container:
                callNumber(mEntity.getPhone());
                break;
            case R.id.detail_phone_message:
                DoAction.JumpToSMS(this, mEntity.getPhone(), String.format("%s 你好：", mEntity.getUserName()));
                break;
            case R.id.detail_short_phone_number_container:
                callNumber(mEntity.getShortPhone());
                break;
            case R.id.detail_short_phone_message:
                DoAction.JumpToSMS(this, mEntity.getShortPhone(), String.format("%s 你好：", mEntity.getUserName()));
                break;
            case R.id.detail_local_contact:
                addToLocalContacts();
                break;
        }
    }

    void callNumber(String number) {
        DoAction.Call(this, number);
        isNeedToAdd = true;
    }

    void showDialog() {
        isNeedToAdd = false;

        if (ZContact.isPhoneExists(this, mEntity.getPhone()))
            return;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("添加到通讯录")
                .setMessage(String.format("将%s的信息导入本地通讯录？ ", mEntity.getUserName()))
                .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ZContact.Add(ContactDetailActivity.this, mEntity);
                        notify.show(R.string.notify_add_contact_success, NotifyBar.DURATION_SHORT, NotifyBar.IconType.Success);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        dialog.show();
    }

    void bindData() {
        if (mEntity == null)
            return;

        ZImage.getInstance().load(mEntity.getAvatarURL(), mUserAvatar, Utils.AVATAR_IMAGE_SIZE, Utils.AVATAR_IMAGE_SIZE, true, baseApp.isNetworkWifi());
        mUserName.setText(mEntity.getUserName());
        mJobTitle.setText(mEntity.getJobTitle());
        mCompany.setText(mEntity.getCompany());
        mDepartment.setText(mEntity.getDepartment());

        String phone = mEntity.getPhone();

        if (phone.isEmpty())
            mPhoneContainer.setVisibility(View.GONE);
        else {
            mPhoneNumber.setText(phone);
            mPhoneNumberContainer.setOnClickListener(this);
            mPhoneNumberContainer.setOnLongClickListener(this);
            mPhoneMessage.setOnClickListener(this);
        }

        String shortPhone = mEntity.getShortPhone();

        if (shortPhone.isEmpty())
            mShortPhoneContainer.setVisibility(View.GONE);
        else {
            mShortPhoneNumber.setText(shortPhone);
            mShortPhoneNumberContainer.setOnClickListener(this);
            mShortPhoneNumberContainer.setOnLongClickListener(this);
            mShortPhoneMessage.setOnClickListener(this);
        }

        if (ZContact.isPhoneExists(this, mEntity.getPhone()))
            setContactAdded();
    }

    void setContactAdded() {
        mLocalContactContainer.setEnabled(false);
        mLocalContactIcon.setImageResource(R.drawable.contact_added);
        mLocalContactText.setTextColor(getResources().getColor(R.color.gray));
        mLocalContactText.setText("已添加");
    }

    void addToLocalContacts() {
        ZContact.Add(this, mEntity);
        setContactAdded();
        notify.show(R.string.notify_add_contact_success, NotifyBar.DURATION_SHORT, NotifyBar.IconType.Success);
    }

    @Override
    public boolean onLongClick(View view) {
        int senderID = view.getId();

        switch (senderID) {
            case R.id.detail_phone_number_container:
                clip.setText(String.format("%s：%s", mEntity.getUserName(), mEntity.getPhone()));
                notify.show("号码1复制成功", NotifyBar.DURATION_SHORT, NotifyBar.IconType.Success);
                break;
            case R.id.detail_short_phone_number_container:
                clip.setText(String.format("%s：%s", mEntity.getUserName(), mEntity.getShortPhone()));
                notify.show("号码2复制成功", NotifyBar.DURATION_SHORT, NotifyBar.IconType.Success);
                break;
        }
        return true;
    }
}
