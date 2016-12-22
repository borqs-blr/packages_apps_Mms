/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * BORQS Software Solutions Pvt Ltd. CONFIDENTIAL
 * Copyright (c) 2016 All rights reserved.
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by BORQS Software
 * Solutions Pvt Ltd. No part of the Material may be used,copied,
 * reproduced, modified, published, uploaded,posted, transmitted,
 * distributed, or disclosed in any way without BORQS Software
 * Solutions Pvt Ltd. prior written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license
 * under such intellectual property rights must be express and
 * approved by BORQS Software Solutions Pvt Ltd. in writing.
 *
 */
package com.android.mms.ui;

import static android.content.res.Configuration.KEYBOARDHIDDEN_NO;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_ABORT;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_COMPLETE;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_START;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_STATUS_ACTION;
import static com.android.mms.ui.MessageListAdapter.COLUMN_ID;
import static com.android.mms.ui.MessageListAdapter.COLUMN_MSG_TYPE;
import static com.android.mms.ui.MessageListAdapter.COLUMN_SMS_ADDRESS;
import static com.android.mms.ui.MessageListAdapter.COLUMN_SMS_BODY;
import static com.android.mms.ui.MessageListAdapter.COLUMN_SMS_DATE;
import static com.android.mms.ui.MessageListAdapter.COLUMN_SMS_DATE_SENT;
import static com.android.mms.ui.MessageListAdapter.COLUMN_SMS_LOCKED;
import static com.android.mms.ui.MessageListAdapter.COLUMN_SMS_READ;
import static com.android.mms.ui.MessageListAdapter.COLUMN_SMS_STATUS;
import static com.android.mms.ui.MessageListAdapter.COLUMN_SMS_TYPE;
import static com.android.mms.ui.MessageListAdapter.COLUMN_SUB_ID;
import static com.android.mms.ui.MessageListAdapter.COLUMN_THREAD_ID;
import static com.android.mms.ui.MessageListAdapter.COLUMN_RCS_MSG_TYPE;
import static com.android.mms.ui.MessageListAdapter.PROJECTION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.drm.DrmStore;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.media.MediaFile;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.provider.ContactsContract;
import android.provider.ContactsContract.QuickContact;
import android.provider.Telephony;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents;
import android.provider.DocumentsContract.Document;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.MSimSmsManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.MSimConstants;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.TempFileProvider;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.data.WorkingMessage;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import com.android.mms.drm.DrmUtils;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ContactSelectActivity;
import com.android.mms.ui.MessageListView.OnSizeChangedListener;
import com.android.mms.ui.MessageUtils.ResizeImageResultCallback;
import com.android.mms.ui.MultiPickContactGroups;
import com.android.mms.ui.RecipientsEditor.RecipientContextMenuInfo;
import com.android.mms.util.DraftCache;
import com.android.mms.util.PhoneNumberFormatter;
import com.android.mms.util.SendingProgressTokenManager;
import com.android.mms.widget.MmsWidgetProvider;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;
import com.suntek.mway.rcs.client.api.autoconfig.RcsAccountApi;
import com.suntek.mway.rcs.client.aidl.capability.RCSCapabilities;
import com.suntek.mway.rcs.client.api.capability.callback.CapabiltyListener;
import com.suntek.mway.rcs.client.aidl.constant.BroadcastConstants;
import com.suntek.mway.rcs.client.api.im.impl.MessageApi;
import com.suntek.mway.rcs.client.api.impl.groupchat.ConfApi;
import com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon.EmoticonBO;
import com.suntek.mway.rcs.client.aidl.provider.model.GroupChatModel;
import com.suntek.mway.rcs.client.aidl.provider.model.GroupChatUser;
import com.suntek.mway.rcs.client.api.support.RcsSupportApi;
import com.suntek.mway.rcs.client.api.util.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.util.FileSuffixException;
import com.suntek.mway.rcs.client.api.util.FileTransferException;
import com.suntek.mway.rcs.client.api.util.log.LogHelper;
import com.suntek.mway.rcs.client.aidl.provider.SuntekMessageData;
import com.suntek.mway.rcs.client.aidl.provider.model.ChatMessage;
import com.android.mms.rcs.RcsChatMessageUtils;
import com.android.mms.rcs.RcsEmojiInitialize;
import com.suntek.mway.rcs.client.api.util.FileDurationException;

import android.media.MediaFile;
import android.content.SharedPreferences;

import java.util.regex.Matcher;
import java.util.Arrays;

import static com.android.mms.ui.MessageListAdapter.COLUMN_FAVOURITE;
import static com.android.mms.ui.MessageListAdapter.COLUMN_RCS_ID;

import com.android.mms.rcs.GroupChatManagerReceiver;
import com.android.mms.rcs.GroupChatManagerReceiver.GroupChatNotifyCallback;
import com.android.mms.rcs.RcsEmojiInitialize.ViewOnClickListener;
import com.android.mms.rcs.RcsContactsUtils;
import com.suntek.mway.rcs.client.aidl.contacts.RCSContact;
import com.android.mms.rcs.RcsUtils;

import android.view.MenuItem.OnMenuItemClickListener;

import com.android.mms.rcs.RcsApiManager;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.text.InputType;
import android.content.res.AssetFileDescriptor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Looper;
import android.media.MediaFile;
import android.media.MediaPlayer;
import android.os.RemoteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
/**
 * This is the main UI for:
 * 1. Composing a new message;
 * 2. Viewing/managing message history of a conversation.
 *
 * This activity can handle following parameters from the intent
 * by which it's launched.
 * thread_id long Identify the conversation to be viewed. When creating a
 *         new message, this parameter shouldn't be present.
 * msg_uri Uri The message which should be opened for editing in the editor.
 * address String The addresses of the recipients in current conversation.
 * exit_on_sent boolean Exit this activity after the message is sent.
 */
public class ComposeMessageActivity extends Activity
        implements View.OnClickListener, TextView.OnEditorActionListener,
        MessageStatusListener, Contact.UpdateListener {
    public static final int REQUEST_CODE_ATTACH_IMAGE     = 100;
    public static final int REQUEST_CODE_TAKE_PICTURE     = 101;
    public static final int REQUEST_CODE_ATTACH_VIDEO     = 102;
    public static final int REQUEST_CODE_TAKE_VIDEO       = 103;
    public static final int REQUEST_CODE_ATTACH_SOUND     = 104;
    public static final int REQUEST_CODE_RECORD_SOUND     = 105;
    public static final int REQUEST_CODE_CREATE_SLIDESHOW = 106;
    public static final int REQUEST_CODE_ECM_EXIT_DIALOG  = 107;
    public static final int REQUEST_CODE_ADD_CONTACT      = 108;
    public static final int REQUEST_CODE_PICK             = 109;
    public static final int REQUEST_CODE_ATTACH_ADD_CONTACT_INFO     = 110;
    public static final int REQUEST_CODE_ATTACH_ADD_CONTACT_VCARD    = 111;
    public static final int REQUEST_CODE_ATTACH_REPLACE_CONTACT_INFO = 112;
    public static final int REQUEST_CODE_BATCH_DELETE     = 113;
    public static final int REQUEST_CODE_BATCH_FAVOURITE  = 114;
    public static final int REQUEST_CODE_BATCH_BACKUP     = 115;
    public static final int REQUEST_CODE_ATTACH_ADD_CONTACT_RCS_VCARD = 200;
    public static final int REQUEST_CODE_ATTACH_MAP       = 201;
    public static final int REQUEST_CODE_RCS_PICK         = 202;
    public static final int REQUEST_SELECT_CONV           = 203;
    public static final int REQUEST_SELECT_GROUP          = 204;
    public static final int REQUEST_CODE_VCARD_GROUP      = 205;
    public static final int REQUEST_CODE_SAIYUN           = 206;
    public static final int REQUEST_CODE_EMOJI_STORE      = 207;

    private static final Uri WHITELIST_CONTENT_URI = Uri
            .parse("content://com.android.firewall/whitelistitems");
    private static final Uri BLACKLIST_CONTENT_URI = Uri
            .parse("content://com.android.firewall/blacklistitems");
    private static final String TAG = "Mms/compose";
    private static final String RCS_TAG = "RCS_UI";
    private static final String FIREWALL_APK_NAME = "com.android.firewall";

    private static final boolean DEBUG = false;
    private static final boolean TRACE = false;
    private static final boolean LOCAL_LOGV = false;
    private boolean FIRST_LUNCH = true;

    // Menu ID
    private static final int MENU_ADD_SUBJECT           = 0;
    private static final int MENU_DELETE_THREAD         = 1;
    private static final int MENU_ADD_ATTACHMENT        = 2;
    private static final int MENU_DISCARD               = 3;
    private static final int MENU_SEND                  = 4;
    private static final int MENU_CALL_RECIPIENT        = 5;
    private static final int MENU_CONVERSATION_LIST     = 6;
    private static final int MENU_DEBUG_DUMP            = 7;
    private static final int MENU_SEND_BY_SLOT1         = 9;
    private static final int MENU_SEND_BY_SLOT2         = 10;
    private static final int MENU_FORWARD_CONVERSATION  = 11;

    // Context menu ID
    private static final int MENU_VIEW_CONTACT          = 12;
    private static final int MENU_ADD_TO_CONTACTS       = 13;

    private static final int MENU_EDIT_MESSAGE          = 14;
    private static final int MENU_VIEW_SLIDESHOW        = 16;
    private static final int MENU_VIEW_MESSAGE_DETAILS  = 17;
    private static final int MENU_DELETE_MESSAGE        = 18;
    private static final int MENU_SEARCH                = 19;
    private static final int MENU_DELIVERY_REPORT       = 20;
    private static final int MENU_FORWARD_MESSAGE       = 21;
    private static final int MENU_CALL_BACK             = 22;
    private static final int MENU_SEND_EMAIL            = 23;
    private static final int MENU_COPY_MESSAGE_TEXT     = 24;
    private static final int MENU_COPY_TO_SDCARD        = 25;
    private static final int MENU_ADD_ADDRESS_TO_CONTACTS = 27;
    private static final int MENU_LOCK_MESSAGE          = 28;
    private static final int MENU_UNLOCK_MESSAGE        = 29;
    private static final int MENU_SAVE_RINGTONE         = 30;
    private static final int MENU_PREFERENCES           = 31;
    private static final int MENU_GROUP_PARTICIPANTS    = 32;
    private static final int MENU_COPY_TO_SIM           = 33;
    private static final int MENU_IMPORT_TEMPLATE       = 34;
    private static final int MENU_RESEND                = 35;
    private static final int MENU_COPY_EXTRACT_URL      = 36;
    private static final int MENU_BATCH_DELETE          = 38;
    private static final int MENU_BATCH_FAVOURITE       = 39;
    private static final int MENU_BATCH_BACKUP          = 40;

    // RCS menu ID
    private static final int MENU_RCS_GROUP_CHAT_DETAIL = 100;
    private static final int MENU_RCS_BURN_MESSGEE_FLAG = 101;
    private static final int MENU_RCS_SWITCH_TO_GROUP_CHAT     = 102;
    private static final int MENU_RCS_MCLOUD_SHARE      = 103;
    private static final int MENU_FIERWALL_ADD_BLACKLIST       = 104;
    private static final int MENU_FIERWALL_ADD_WHITELIST       = 105;
    private static final int MENU_FAVOURITE_MESSAGE     = 106;
    private static final int MENU_UNFAVOURITE_MESSAGE   = 107;
    private static final int MENU_TOP_CONVERSATION      = 108;
    private static final int MENU_CANCEL_TOP_CONVERSATION     = 109;

    private static final int RECIPIENTS_MAX_LENGTH = 312;
    private static final int RCS_MAX_SMS_LENGHTH = 900;
    private static final int MESSAGE_LIST_QUERY_TOKEN = 9527;
    private static final int MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN = 9528;

    private static final int DELETE_MESSAGE_TOKEN  = 9700;

    private static final int CHARS_REMAINING_BEFORE_COUNTER_SHOWN = 10;

    private static final long NO_DATE_FOR_DIALOG = -1L;

    private static final int MENU_ADD_CONTACT = 5425;
    private static final int MENU_ADD_GROUP = 5426;

    protected static final String KEY_EXIT_ON_SENT = "exit_on_sent";
    protected static final String KEY_FORWARDED_MESSAGE = "forwarded_message";
    protected static final String KEY_REPLY_MESSAGE = "reply_message";


    private static final String EXIT_ECM_RESULT = "exit_ecm_result";

    private static final String INTENT_MULTI_PICK = "com.android.contacts.action.MULTI_PICK";
    private static final String INTENT_CAMERA_CROP = "com.android.camera.action.CROP";
    private static final String ACTION_LUNCHER_RCS_SHAREFILE =
            "com.suntek.mway.rcs.nativeui.ACTION_LUNCHER_RCS_SHAREFILE";

    // When the conversation has a lot of messages and a new message is sent, the list is scrolled
    // so the user sees the just sent message. If we have to scroll the list more than 20 items,
    // then a scroll shortcut is invoked to move the list near the end before scrolling.
    private static final int MAX_ITEMS_TO_INVOKE_SCROLL_SHORTCUT = 20;

    // Any change in height in the message list view greater than this threshold will not
    // cause a smooth scroll. Instead, we jump the list directly to the desired position.
    private static final int SMOOTH_SCROLL_THRESHOLD = 200;

    // To reduce janky interaction when message history + draft loads and keyboard opening
    // query the messages + draft after the keyboard opens. This controls that behavior.
    private static final boolean DEFER_LOADING_MESSAGES_AND_DRAFT = true;

    // The max amount of delay before we force load messages and draft.
    // 500ms is determined empirically. We want keyboard to have a chance to be shown before
    // we force loading. However, there is at least one use case where the keyboard never shows
    // even if we tell it to (turning off and on the screen). So we need to force load the
    // messages+draft after the max delay.
    private static final int LOADING_MESSAGES_AND_DRAFT_MAX_DELAY_MS = 500;

    // The max length of characters for subject.
    private static final int SUBJECT_MAX_LENGTH = MmsConfig.getMaxSubjectLength();
    // The number of buttons in two send button mode
    private static final int NUMBER_OF_BUTTONS = 2;
    private static final int MSG_ADD_ATTACHMENT_FAILED = 1;

    private static final int KILOBYTE = 1024;

    // Preferred CDMA subscription mode is NV.
    private static final int CDMA_SUBSCRIPTION_NV = 1;

    // The default displaying page when selecting attachments.
    private static final int DEFAULT_ATTACHMENT_PAGER = 0;

    private ContentResolver mContentResolver;

    private BackgroundQueryHandler mBackgroundQueryHandler;

    private Conversation mConversation;     // Conversation we are working in

    // When mSendDiscreetMode is true, this activity only allows a user to type in and send
    // a single sms, send the message, and then exits. The message history and menus are hidden.
    private boolean mSendDiscreetMode;
    private boolean mForwardMessageMode;
    private boolean mReplyMessageMode;

    private View mTopPanel;                 // View containing the recipient and subject editors
    private View mBottomPanel;              // View containing the text editor, send button, ec.
    private EditText mTextEditor;           // Text editor to type your message into
    private TextView mTextCounter;          // Shows the number of characters used in text editor
    private View mAttachmentSelector;       // View containing the added attachment types
    private ViewPager mAttachmentPager;     // Attachment selector pager
    private AttachmentPagerAdapter mAttachmentPagerAdapter;  // Attachment selector pager adapter
     /* commented for no-touch feature phone*/
    /*private TextView mSendButtonMms;        // Press to send mms
    private ImageButton mSendButtonSms;      // Press to send sms
    private ImageButton mButtonEmoj;*/
    private EditText mSubjectTextEditor;    // Text editor for MMS subject
     /* commented for no-touch feature phone*/
    /*private View mSendLayoutMmsFir;        // The first mms send layout with sim indicator
    private View mSendLayoutSmsFir;      // The first sms send layout with sim indicator
    private View mSendLayoutMmsSec;    // The second mms send layout with sim indicator
    private View mSendLayoutSmsSec;    // The second sms send layout with sim indicator
    private TextView mSendButtonMmsViewSec;    // The second mms send button without sim indicator
    private ImageButton mSendButtonSmsViewSec; // The second sms send button without sim indicator
    private ImageView mIndicatorForSimMmsFir, mIndicatorForSimSmsFir;
    private ImageView mIndicatorForSimMmsSec, mIndicatorForSimSmsSec;*/

    private AttachmentEditor mAttachmentEditor;
    private View mAttachmentEditorScrollView;

    private MessageListView mMsgListView;        // ListView for messages in this conversation
    public MessageListAdapter mMsgListAdapter;  // and its corresponding ListAdapter

    private RecipientsEditor mRecipientsEditor;  // UI control for editing recipients

    private ImageView mIndicatorForSim1, mIndicatorForSim2;
    private View mIndicatorContainer1, mIndicatorContainer2;


    // For HW keyboard, 'mIsKeyboardOpen' indicates if the HW keyboard is open.
    // For SW keyboard, 'mIsKeyboardOpen' should always be true.
    private boolean mIsKeyboardOpen;
    private boolean mIsLandscape;                // Whether we're in landscape mode

    private boolean mToastForDraftSave;   // Whether to notify the user that a draft is being saved

    private boolean mSentMessage;       // true if the user has sent a message while in this
                                        // activity. On a new compose message case, when the first
                                        // message is sent is a MMS w/ attachment, the list blanks
                                        // for a second before showing the sent message. But we'd
                                        // think the message list is empty, thus show the recipients
                                        // editor thinking it's a draft message. This flag should
                                        // help clarify the situation.

    private WorkingMessage mWorkingMessage;         // The message currently being composed.

    private AlertDialog mInvalidRecipientDialog;

    private boolean mWaitingForSubActivity;
    private boolean mInAsyncAddAttathProcess = false;
    private int mLastRecipientCount;            // Used for warning the user on too many recipients.

    private boolean mSendingMessage;    // Indicates the current message is sending, and shouldn't send again.

    private Intent mAddContactIntent;   // Intent used to add a new contact

    private Uri mTempMmsUri;            // Only used as a temporary to hold a slideshow uri
    private long mTempThreadId;         // Only used as a temporary to hold a threadId

    private AsyncDialog mAsyncDialog;   // Used for background tasks.

    private String mDebugRecipients;
    private int mLastSmoothScrollPosition;
    private boolean mScrollOnSend;      // Flag that we need to scroll the list to the end.

    private boolean mIsReplaceAttachment;
    private int mCurrentAttachmentPager;
    private int mSavedScrollPosition = -1;  // we save the ListView's scroll position in onPause(),
                                            // so we can remember it after re-entering the activity.
                                            // If the value >= 0, then we jump to that line. If the
                                            // value is maxint, then we jump to the end.
    private long mLastMessageId;

    // Record the resend sms recipient when the sms send to more than one recipient
    private String mResendSmsRecipient;

    private AlertDialog mMsimDialog;     // Used for MSIM subscription choose

    private static final int MSG_COPY_TO_SIM_FAILED = 1;
    private static final int MSG_COPY_TO_SIM_SUCCESS = 2;
    private static final int DIALOG_IMPORT_TEMPLATE = 1;

    private static final int MSG_ONLY_ONE_FAIL_LIST_ITEM = 1;

    private static final String INTENT_ACTION_LTE_DATA_ONLY_DIALOG =
            "com.qualcomm.qti.phonefeature.DISABLE_TDD_DATA_ONLY";
    private static final String LTE_DATA_ONLY_KEY = "tdd_data_only";
    private static final int LTE_DATA_ONLY_MODE = 1;

    /**
     * Whether this activity is currently running (i.e. not paused)
     */
    private boolean mIsRunning;

    // we may call loadMessageAndDraft() from a few different places. This is used to make
    // sure we only load message+draft once.
    private boolean mMessagesAndDraftLoaded;

    /**
     * Whether the attachment error is in the case of sendMms.
     */
    private boolean mIsAttachmentErrorOnSend = false;

    private boolean mIsFromSearchActivity = false;

    // whether we should load the draft. For example, after attaching a photo and coming back
    // in onActivityResult(), we should not load the draft because that will mess up the draft
    // state of mWorkingMessage. Also, if we are handling a Send or Forward Message Intent,
    // we should not load the draft.
    private boolean mShouldLoadDraft;

    // If a message A is currently being edited, and user decides to edit
    // another sent message B, we need to send message A and put B in edit state
    // after A is sent. This variable is used to save the message B during A is
    // sending in progress.
    private MessageItem mEditMessageItem;

    // Whether or not we are currently enabled for SMS. This field is updated in onStart to make
    // sure we notice if the user has changed the default SMS app.
    private boolean mIsSmsEnabled;

    // Whether or not the RCS Service is installed and the Sim is supported RCS.
    private boolean mIsRcsEnabled;

    private boolean mIsAirplaneModeOn = false;

    private boolean isDisposeImage = false;

    private static final int CAPABILITY_RCS_ONLINE = 200;
    private static final int CAPABILITY_RCS_OFFLINE = 408;

    private static final int PHOTO_CROP = 10000;
    private static final long NO_THREAD_ID = -1;
    private static final String PATTERN_QUALTITY = "^(?:[0-9]?\\d|100|00[1-9])$";

    public int rcsforwardid = 0;

    private static boolean mRcsShareVcard = false;

    private static boolean rcsShareVcardAddNumber = false;

    // RCS Conference API
    private ConfApi mConfApi;

    // RCS Message API
    private MessageApi mMessageApi;

    // RCS Account API
    private RcsAccountApi mAccountApi;

    // RCS Support API
    private RcsSupportApi mSupportApi;

    private List<RCSContact> mRcsContactList = new ArrayList<RCSContact>();

    // Manage the progress dialog flow when creating RCS group chat.
    private CreateGroupChatCallback mCreateGroupChatCallback;
    private boolean mIsBurnMessage = false;
    private List<Long> mTopThread = new ArrayList<Long>();
    private RcsEmojiInitialize mRcsEmojiInitialize = null;

    private AttachmentTypeSelectorAdapter mAttachmentTypeSelectorAdapter;

    private GroupChatManagerReceiver mGroupReceiver = new GroupChatManagerReceiver(
            new GroupChatNotifyCallback() {

                @Override
                public void onNewSubject(String groupId, String newSubject) {
                    if (mConversation != null && mConversation.getGroupChat() != null
                            && !TextUtils.isEmpty(groupId)
                            && groupId.equals(mConversation.getGroupChat().getId())) {
                        setTitle(newSubject);
                    }
                }

                @Override
                public void onMemberAliasChange(String groupId) {
                    if (mMsgListAdapter != null) {
                        mMsgListAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onDisband(String groupId) {
                    if (mConversation != null && mConversation.getGroupChat() != null
                            && !TextUtils.isEmpty(groupId)
                            && groupId.equals(mConversation.getGroupChat().getId())) {
                        try {
                            GroupChatModel groupChat = mMessageApi.getGroupChatById(groupId);
                            mConversation.setGroupChat(groupChat);
                        } catch (ServiceDisconnectedException e) {
                            Log.w(RCS_TAG, "Exception onDisband()" + e);
                        }
                    }
                }
            });

    private Handler mHandler = new Handler();

    // keys for extras and icicles
    public final static String THREAD_ID = "thread_id";
    private final static String RECIPIENTS = "recipients";
    public final static String MANAGE_MODE = "manage_mode";
    private final static String MESSAGE_ID = "message_id";
    private final static String MESSAGE_TYPE = "message_type";
    private final static String MESSAGE_BODY = "message_body";
    private final static String MESSAGE_SUBJECT = "message_subject";
    private final static String MESSAGE_SUBJECT_CHARSET = "message_subject_charset";
    private final static String NEED_RESEND = "needResend";
    private final static String MSG_SUBJECT_SIZE = "subject_size";

    private boolean isLocked = false;

    private boolean mIsPickingContact = false;
    // List for contacts picked from People.
    private ContactList mRecipientsPickList = null;
    /**
    * Whether the recipients is picked from Contacts
    */
    private boolean mIsProcessPickedRecipients = false;
    private int mExistsRecipientsCount = 0;
    private boolean enableMmsData = false;

    private boolean mIsMessageChanged = false;
    private boolean mShowTwoButtons = false;

    private Object mAddAttachmentLock = new Object();

    // rcs progress
    long lastProgress = 0;

    // top convsersation IDlist
    private List<Long> mTopMsgThreadIdList = new ArrayList<Long>();

    private boolean mHasBurnCapability = false;

    private boolean mShowAttIcon = false;
    private final static int REPLACE_ATTACHMEN_MASK = 1 << 16;

    /**
     * Whether the audio attachment player activity is launched and running
     */
    private boolean mIsAudioPlayerActivityRunning = false;
    private boolean mIsLocked = false;

    private static String FILE_PATH_COLUMN = "_data";
    private static String BROADCAST_DATA_SCHEME = "file";
    private static String URI_SCHEME_CONTENT = "content";
    private static String URI_HOST_MEDIA = "media";

    @SuppressWarnings("unused")
    public static void log(String logMsg) {
        Thread current = Thread.currentThread();
        long tid = current.getId();
        StackTraceElement[] stack = current.getStackTrace();
        String methodName = stack[3].getMethodName();
        // Prepend current thread ID and name of calling method to the message.
        logMsg = "[" + tid + "] [" + methodName + "] " + logMsg;
        Log.d(TAG, logMsg);
    }

    //==========================================================
    // Inner classes
    //==========================================================

    private void editSlideshow() {
        final int subjectSize = mWorkingMessage.hasSubject()
                    ? mWorkingMessage.getSubject().toString().getBytes().length : 0;
        // The user wants to edit the slideshow. That requires us to persist the slideshow to
        // disk as a PDU in saveAsMms. This code below does that persisting in a background
        // task. If the task takes longer than a half second, a progress dialog is displayed.
        // Once the PDU persisting is done, another runnable on the UI thread get executed to start
        // the SlideshowEditActivity.
        getAsyncDialog().runAsync(new Runnable() {
            @Override
            public void run() {
                // This runnable gets run in a background thread.
                mTempMmsUri = mWorkingMessage.saveAsMms(false);
            }
        }, new Runnable() {
            @Override
            public void run() {
                // Once the above background thread is complete, this runnable is run
                // on the UI thread.
                if (mTempMmsUri == null) {
                    return;
                }
                Intent intent = new Intent(ComposeMessageActivity.this,
                        SlideshowEditActivity.class);
                intent.setData(mTempMmsUri);
                intent.putExtra(MSG_SUBJECT_SIZE, subjectSize);
                startActivityForResult(intent, REQUEST_CODE_CREATE_SLIDESHOW);
            }
        }, R.string.building_slideshow_title);
    }

    private void pickContacts(int mode, int requestCode) {
        Intent intent = new Intent(this, ContactSelectActivity.class);
        intent.putExtra(ContactSelectActivity.MODE, mode);
        startActivityForResult(intent, requestCode);
    }

    private final Handler mAttachmentEditorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AttachmentEditor.MSG_EDIT_SLIDESHOW: {
                    editSlideshow();
                    break;
                }
                case AttachmentEditor.MSG_SEND_SLIDESHOW: {
                    if (isPreparedForSending()) {
                        ComposeMessageActivity.this.confirmSendMessageIfNeeded();
                    }
                    break;
                }
                case AttachmentEditor.MSG_VIEW_IMAGE:
                case AttachmentEditor.MSG_PLAY_VIDEO:
                case AttachmentEditor.MSG_PLAY_AUDIO:
                case AttachmentEditor.MSG_PLAY_SLIDESHOW:
                case AttachmentEditor.MSG_VIEW_VCARD:
                    if (mWorkingMessage.getSlideshow() != null) {
                         viewMmsMessageAttachment(msg.what);
                    }
                    break;

                case AttachmentEditor.MSG_REPLACE_IMAGE:
                case AttachmentEditor.MSG_REPLACE_VIDEO:
                case AttachmentEditor.MSG_REPLACE_AUDIO:
                case AttachmentEditor.MSG_REPLACE_VCARD:
                    if (mAttachmentSelector.getVisibility() == View.VISIBLE
                            && mIsReplaceAttachment) {
                        mAttachmentSelector.setVisibility(View.GONE);
                    } else {
                        showAddAttachmentDialog(true);
                        Toast.makeText(ComposeMessageActivity.this,
                                R.string.replace_current_attachment, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case AttachmentEditor.MSG_REMOVE_ATTACHMENT:
                    // Update the icon state in attachment selector.
                    if (mAttachmentSelector.getVisibility() == View.VISIBLE
                            && !mIsReplaceAttachment) {
                        showAddAttachmentDialog(true);
                    }
                    mWorkingMessage.removeAttachment(true);
                    break;

                default:
                    break;
            }
        }
    };


    private void viewMmsMessageAttachment(final int requestCode) {
        SlideshowModel slideshow = mWorkingMessage.getSlideshow();
        if (slideshow == null) {
            throw new IllegalStateException("mWorkingMessage.getSlideshow() == null");
        }
        if (slideshow.isSimple()) {
            MessageUtils.viewSimpleSlideshow(this, slideshow);
        } else {
            // The user wants to view the slideshow. That requires us to persist the slideshow to
            // disk as a PDU in saveAsMms. This code below does that persisting in a background
            // task. If the task takes longer than a half second, a progress dialog is displayed.
            // Once the PDU persisting is done, another runnable on the UI thread get executed to
            // start the SlideshowActivity.
            getAsyncDialog().runAsync(new Runnable() {
                @Override
                public void run() {
                    // This runnable gets run in a background thread.
                    mTempMmsUri = mWorkingMessage.saveAsMms(false);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    // Once the above background thread is complete, this runnable is run
                    // on the UI thread.
                    if (mTempMmsUri == null) {
                        return;
                    }

                    if (isAudioPlayerActivityRunning(requestCode)) {
                        return;
                    }

                    SlideshowModel slideshowModel = mWorkingMessage.getSlideshow();
                    if (requestCode == AttachmentEditor.MSG_PLAY_AUDIO &&
                            slideshowModel.isSimpleAudio()) {
                        MediaModel mm = slideshowModel.get(0).getAudio();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(mm.getUri(), mm.getContentType());
                        startActivityForResult(intent, requestCode);
                        return;
                    }

                    MessageUtils.launchSlideshowActivity(ComposeMessageActivity.this, mTempMmsUri,
                            requestCode);
                }
            }, R.string.building_slideshow_title);
        }
    }

    private boolean isAudioPlayerActivityRunning(int requestCode) {
        // When the attachment is Audio, if the mIsAudioPlayerActivityRunning is true,
        // that means user is continuously clicking the play button, we return this
        // thread and cancel this click event; else we put it to true and response this
        // event.
        if (requestCode == AttachmentEditor.MSG_PLAY_AUDIO) {
            if (mIsAudioPlayerActivityRunning) {
                return true;
            } else {
                mIsAudioPlayerActivityRunning = true;
                return false;
            }
        } else {
            return false;
        }
    }

    private final Handler mMessageListItemHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MessageItem msgItem = (MessageItem) msg.obj;
            if (msgItem != null) {
                switch (msg.what) {
                    case MessageListItem.MSG_LIST_DETAILS:
                        showMessageDetails(msgItem);
                        break;

                    case MessageListItem.MSG_LIST_EDIT:
                        editMessageItem(msgItem);
                        drawBottomPanel();
                        invalidateOptionsMenu();
                        break;

                    case MessageListItem.MSG_LIST_PLAY:
                        switch (msgItem.mAttachmentType) {
                            case WorkingMessage.IMAGE:
                            case WorkingMessage.VIDEO:
                            case WorkingMessage.AUDIO:
                            case WorkingMessage.VCARD:
                            case WorkingMessage.SLIDESHOW:
                                MessageUtils.viewMmsMessageAttachment(ComposeMessageActivity.this,
                                        msgItem.mMessageUri, msgItem.mSlideshow,
                                        getAsyncDialog());
                                break;
                        }
                        break;

                    default:
                        Log.w(TAG, "Unknown message: " + msg.what);
                        return;
                }
            }
        }
    };

    private boolean showMessageDetails(MessageItem msgItem) {
        Cursor cursor = mMsgListAdapter.getCursorForItem(msgItem);
        if (cursor == null) {
            return false;
        }
        int subjectSize = (msgItem.mSubject == null) ? 0 : msgItem.mSubject.getBytes().length;
        int messageSize =  msgItem.mMessageSize + subjectSize;
        if (DEBUG) {
            Log.v(TAG,"showMessageDetails subjectSize = " + subjectSize);
            Log.v(TAG,"showMessageDetails messageSize = " + messageSize);
        }
        String messageDetails = MessageUtils.getMessageDetails(
                ComposeMessageActivity.this, cursor, messageSize);
        new AlertDialog.Builder(ComposeMessageActivity.this)
                .setTitle(R.string.message_details_title)
                .setMessage(messageDetails)
                .setCancelable(true)
                .show();
        return true;
    }

    private final OnKeyListener mSubjectKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            // When the subject editor is empty, press "DEL" to hide the input field.
            if ((keyCode == KeyEvent.KEYCODE_DEL) && (mSubjectTextEditor.length() == 0)) {
                showSubjectEditor(false);
                mWorkingMessage.setSubject(null, true);
                return true;
            }
            return false;
        }
    };

    /**
     * Return the messageItem associated with the type ("mms" or "sms") and message id.
     * @param type Type of the message: "mms" or "sms"
     * @param msgId Message id of the message. This is the _id of the sms or pdu row and is
     * stored in the MessageItem
     * @param createFromCursorIfNotInCache true if the item is not found in the MessageListAdapter's
     * cache and the code can create a new MessageItem based on the position of the current cursor.
     * If false, the function returns null if the MessageItem isn't in the cache.
     * @return MessageItem or null if not found and createFromCursorIfNotInCache is false
     */
    private MessageItem getMessageItem(String type, long msgId,
            boolean createFromCursorIfNotInCache) {
        return mMsgListAdapter.getCachedMessageItem(type, msgId,
                createFromCursorIfNotInCache ? mMsgListAdapter.getCursor() : null);
    }

    private boolean isCursorValid() {
        // Check whether the cursor is valid or not.
        Cursor cursor = mMsgListAdapter.getCursor();
        if (cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            Log.e(TAG, "Bad cursor.", new RuntimeException());
            return false;
        }
        return true;
    }

    private void resetCounter() {
        mTextCounter.setText("");
        mTextCounter.setVisibility(View.GONE);
    }

    private void updateCounter(CharSequence text, int start, int before, int count) {
        WorkingMessage workingMessage = mWorkingMessage;
        if (workingMessage.requiresMms()) {
            // If we're not removing text (i.e. no chance of converting back to SMS
            // because of this change) and we're in MMS mode, just bail out since we
            // then won't have to calculate the length unnecessarily.
            final boolean textRemoved = (before > count);
            if (!textRemoved) {
                 /* commented for no-touch feature phone*/
                /*if (mShowTwoButtons) {
                    showTwoSmsOrMmsSendButton(workingMessage.requiresMms());
                } else {
                    showSmsOrMmsSendButton(workingMessage.requiresMms());
                }*/

                return;
            }
        }

        int[] params = SmsMessage.calculateLength(text, false);
            /* SmsMessage.calculateLength returns an int[4] with:
             *   int[0] being the number of SMS's required,
             *   int[1] the number of code units used,
             *   int[2] is the number of code units remaining until the next message.
             *   int[3] is the encoding type that should be used for the message.
             */
        int msgCount = params[0];
        int remainingInCurrentMessage = params[2];
        if (!mSupportApi.isOnline()) {
            if (!MmsConfig.getMultipartSmsEnabled()) {
                // The provider doesn't support multi-part sms's so as soon as
                // the user types
                // an sms longer than one segment, we have to turn the message
                // into an mms.
                mWorkingMessage.setLengthRequiresMms(msgCount > 1, true);
            } else {
                int threshold = MmsConfig.getSmsToMmsTextThreshold(ComposeMessageActivity.this);
                mWorkingMessage.setLengthRequiresMms(threshold > 0 && msgCount > threshold, true);
            }
        }

        // Show the counter only if:
        // - We are not in MMS mode
        // - We are going to send more than one message OR we are getting close
        boolean showCounter = false;
        if (!workingMessage.requiresMms() &&
                (msgCount > 1 ||
                 remainingInCurrentMessage <= CHARS_REMAINING_BEFORE_COUNTER_SHOWN)) {
            showCounter = true;
        }
         /* commented for no-touch feature phone*/
        /*if (mShowTwoButtons) {
            showTwoSmsOrMmsSendButton(workingMessage.requiresMms());
        } else {
            showSmsOrMmsSendButton(workingMessage.requiresMms());
        }*/

        if (showCounter) {
            // Update the remaining characters and number of messages required.
            String counterText = msgCount > 1 ? remainingInCurrentMessage + " / " + msgCount
                    : String.valueOf(remainingInCurrentMessage);
            mTextCounter.setText(counterText);
            mTextCounter.setVisibility(View.VISIBLE);
        } else {
            mTextCounter.setVisibility(View.GONE);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        // requestCode >= 0 means the activity in question is a sub-activity.
        if (requestCode >= 0) {
            mWaitingForSubActivity = true;
        }
        // The camera and other activities take a long time to hide the keyboard so we pre-hide
        // it here. However, if we're opening up the quick contact window while typing, don't
        // mess with the keyboard.
        if (mIsKeyboardOpen && !QuickContact.ACTION_QUICK_CONTACT.equals(intent.getAction())) {
            hideKeyboard();
        }

        super.startActivityForResult(intent, requestCode);
    }

    private void showConvertToMmsToast() {
        Toast.makeText(this, R.string.converting_to_picture_message, Toast.LENGTH_SHORT).show();
    }

    private void showConvertToSmsToast() {
        Toast.makeText(this, R.string.converting_to_text_message, Toast.LENGTH_SHORT).show();
    }

    private class DeleteMessageListener implements OnClickListener {
        private final MessageItem mMessageItem;

        public DeleteMessageListener(MessageItem messageItem) {
            mMessageItem = messageItem;
        }

        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();

            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... none) {
                    if (mMessageItem.isMms()) {
                        WorkingMessage.removeThumbnailsFromCache(mMessageItem.getSlideshow());

                        MmsApp.getApplication().getPduLoaderManager()
                            .removePdu(mMessageItem.mMessageUri);
                        // Delete the message *after* we've removed the thumbnails because we
                        // need the pdu and slideshow for removeThumbnailsFromCache to work.
                    }
                    Boolean deletingLastItem = false;
                    Cursor cursor = mMsgListAdapter != null ? mMsgListAdapter.getCursor() : null;
                    if (cursor != null) {
                        cursor.moveToLast();
                        long msgId = cursor.getLong(COLUMN_ID);
                        deletingLastItem = msgId == mMessageItem.mMsgId;
                    }
                    mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                            deletingLastItem, mMessageItem.mMessageUri,
                            mMessageItem.mLocked ? null : "locked=0", null);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class DiscardDraftListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            mWorkingMessage.discard();
            dialog.dismiss();
            finish();
        }
    }

    private class SendIgnoreInvalidRecipientListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            boolean isMms = mWorkingMessage.requiresMms();
            if (MessageUtils.isMobileDataDisabled(ComposeMessageActivity.this)
                    && enableMmsData && isMms) {
                showMobileDataDisabledDialog();
            } else if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                sendMsimMessage(true);
            } else {
                sendMessage(true);
            }
            dialog.dismiss();
        }
    }

    private class CancelSendingListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            if (isRecipientsEditorVisible()) {
                mRecipientsEditor.requestFocus();
            }
            dialog.dismiss();
        }
    }

    private void dismissMsimDialog() {
        if (mMsimDialog != null) {
            mMsimDialog.dismiss();
        }
    }

   private void processMsimSendMessage(int subscription, final boolean bCheckEcmMode) {
        if (mMsimDialog != null) {
            mMsimDialog.dismiss();
        }
        mWorkingMessage.setWorkingMessageSub(subscription);
        sendMessage(bCheckEcmMode);
    }

    private void LaunchMsimDialog(final boolean bCheckEcmMode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ComposeMessageActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.multi_sim_sms_sender,
                              (ViewGroup)findViewById(R.id.layout_root));
        builder.setView(layout);
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK: {
                            dismissMsimDialog();
                            return true;
                        }
                        case KeyEvent.KEYCODE_SEARCH: {
                            return true;
                        }
                    }
                    return false;
                }
            }
        );

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dismissMsimDialog();
            }
        });

        ContactList recipients = isRecipientsEditorVisible() ?
            mRecipientsEditor.constructContactsFromInput(false) : getRecipients();
        builder.setTitle(getResources().getString(R.string.to_address_label)
                + recipients.formatNamesAndNumbers(","));

        mMsimDialog = builder.create();
        mMsimDialog.setCanceledOnTouchOutside(true);

        int[] smsBtnIds = {R.id.BtnSubOne, R.id.BtnSubTwo, R.id.BtnSubThree};
        int[] subString={R.string.sub1, R.string.sub2, R.string.sub3};
        int phoneCount = MSimTelephonyManager.getDefault().getPhoneCount();
        Button[] smsBtns = new Button[phoneCount];

        for (int i = 0; i < phoneCount; i++) {
            final int subscription = i;
            smsBtns[i] = (Button) layout.findViewById(smsBtnIds[i]);
            smsBtns[i].setVisibility(View.VISIBLE);
            smsBtns[i].setText(subString[i]);
            smsBtns[i].setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, "Sub slected "+subscription);
                        processMsimSendMessage(subscription, bCheckEcmMode);
                }
            });
        }
        mMsimDialog.show();
    }

    private void sendMsimMessage(boolean bCheckEcmMode, int subscription) {
        mWorkingMessage.setWorkingMessageSub(subscription);
        sendMessage(bCheckEcmMode);
    }

    private void sendMsimMessage(boolean bCheckEcmMode) {

        if(MSimSmsManager.getDefault().isSMSPromptEnabled()) {
            LaunchMsimDialog(bCheckEcmMode);
        } else {
            int preferredSmsSub = MSimSmsManager.getDefault().getPreferredSmsSubscription();
            mWorkingMessage.setWorkingMessageSub(preferredSmsSub);
            sendMessage(bCheckEcmMode);
        }
    }

    private boolean isLTEOnlyMode() {
        try {
            int tddOnly = Settings.Global.getInt(getContentResolver(), LTE_DATA_ONLY_KEY);
            int network = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE);
            return network == RILConstants.NETWORK_MODE_LTE_ONLY && tddOnly == LTE_DATA_ONLY_MODE;
        } catch (SettingNotFoundException snfe) {
            Log.w(TAG, "isLTEOnlyMode: Could not find PREFERRED_NETWORK_MODE!");
        }
        return false;
    }

    private boolean isLTEOnlyMode(int subscription) {
        try {
            int tddOnly = MSimTelephonyManager.getIntAtIndex(getContentResolver(),
                    LTE_DATA_ONLY_KEY, subscription);
            int network = MSimTelephonyManager.getIntAtIndex(getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE, subscription);
            return network == RILConstants.NETWORK_MODE_LTE_ONLY && tddOnly == 1;
        } catch (SettingNotFoundException snfe) {
            Log.w(TAG, "isLTEOnlyMode: Could not find PREFERRED_NETWORK_MODE!");
        }
        return false;
    }

    private void showDisableLTEOnlyDialog(int subscription) {
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION_LTE_DATA_ONLY_DIALOG);
        intent.putExtra("subscription", subscription);
        startActivity(intent);
    }

    private void confirmSendMessageIfNeeded(int subscription) {
        if (isLTEOnlyMode(subscription)) {
            showDisableLTEOnlyDialog(subscription);
            return;
        }
        boolean isMms = mWorkingMessage.requiresMms();
        if (!isRecipientsEditorVisible()) {
            if (MessageUtils.isMobileDataDisabled(this) &&
                    enableMmsData && isMms) {
                showMobileDataDisabledDialog();
            } else {
                sendMsimMessage(true, subscription);
            }
            return;
        }

        if (mRecipientsEditor.hasInvalidRecipient(isMms)) {
            showInvalidRecipientDialog();
        } else if (MessageUtils.isMobileDataDisabled(this) &&
                enableMmsData && isMms) {
            showMobileDataDisabledDialog();
        } else {
            // The recipients editor is still open. Make sure we use what's showing there
            // as the destination.
            ContactList contacts = mRecipientsEditor.constructContactsFromInput(false);
            mDebugRecipients = contacts.serialize();
            sendMsimMessage(true, subscription);
        }
    }

    private void confirmSendMessageIfNeeded() {
        if (mRcsShareVcard) {
            mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_VCARD);
            mRcsShareVcard = false;
        }

        if ((MSimTelephonyManager.getDefault().isMultiSimEnabled() &&
                isLTEOnlyMode(MSimSmsManager.getDefault().getPreferredSmsSubscription()))
                || (!MSimTelephonyManager.getDefault().isMultiSimEnabled()
                        && isLTEOnlyMode())) {
            showDisableLTEOnlyDialog(MSimSmsManager.getDefault().getPreferredSmsSubscription());
            return;
        }
        boolean isMms = mWorkingMessage.requiresMms();
        if (!isRecipientsEditorVisible()) {
            if (MessageUtils.isMobileDataDisabled(this) && enableMmsData && isMms) {
                showMobileDataDisabledDialog();
            } else if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                sendMsimMessage(true);
            } else {
                sendMessage(true);
            }
            return;
        }

        if (mRecipientsEditor.hasInvalidRecipient(isMms)) {
            showInvalidRecipientDialog();
        } else if (MessageUtils.isMobileDataDisabled(this) && enableMmsData && isMms) {
            showMobileDataDisabledDialog();
        } else {
            // The recipients editor is still open. Make sure we use what's showing there
            // as the destination.
            ContactList contacts = mRecipientsEditor.constructContactsFromInput(false);
            mDebugRecipients = contacts.serialize();
            if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                sendMsimMessage(true);
            } else {
                sendMessage(true);
            }
        }
    }

    private void showInvalidRecipientDialog() {
        boolean isMms = mWorkingMessage.requiresMms();
        if (mRecipientsEditor.getValidRecipientsCount(isMms)
                > MessageUtils.ALL_RECIPIENTS_INVALID) {
            String title = getResourcesString(R.string.has_invalid_recipient,
                    mRecipientsEditor.formatInvalidNumbers(isMms));
            mInvalidRecipientDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(R.string.invalid_recipient_message)
                .setPositiveButton(R.string.try_to_send,
                        new SendIgnoreInvalidRecipientListener())
                .setNegativeButton(R.string.no, new CancelSendingListener())
                .show();
        } else {
            mInvalidRecipientDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.cannot_send_message)
                .setMessage(R.string.cannot_send_message_reason)
                .setPositiveButton(R.string.yes, new CancelSendingListener())
                .show();
        }
    }

    private void showMobileDataDisabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.send)
            .setMessage(this.getString(R.string.mobile_data_disable,
                this.getString(R.string.mobile_data_send)))
            .setPositiveButton(R.string.yes, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                        sendMsimMessage(true);
                    } else {
                        sendMessage(true);
                    }
                    dialog.dismiss();
                }
            })
            .setNegativeButton(R.string.no, null).show();
    }

    private final TextWatcher mRecipientsWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // This is a workaround for bug 1609057.  Since onUserInteraction() is
            // not called when the user touches the soft keyboard, we pretend it was
            // called when textfields changes.  This should be removed when the bug
            // is fixed.
            onUserInteraction();
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Bug 1474782 describes a situation in which we send to
            // the wrong recipient.  We have been unable to reproduce this,
            // but the best theory we have so far is that the contents of
            // mRecipientList somehow become stale when entering
            // ComposeMessageActivity via onNewIntent().  This assertion is
            // meant to catch one possible path to that, of a non-visible
            // mRecipientsEditor having its TextWatcher fire and refreshing
            // mRecipientList with its stale contents.
            if (!isRecipientsEditorVisible()) {
                IllegalStateException e = new IllegalStateException(
                        "afterTextChanged called with invisible mRecipientsEditor");
                // Make sure the crash is uploaded to the service so we
                // can see if this is happening in the field.
                Log.w(TAG,
                     "RecipientsWatcher: afterTextChanged called with invisible mRecipientsEditor");
                return;
            }

            mWorkingMessage.setWorkingRecipients(mRecipientsEditor.getNumbers());
            mWorkingMessage.setHasEmail(mRecipientsEditor.containsEmail(), true);

            checkForTooManyRecipients();

            // If pick recipients from Contacts,
            // then only update title once when process finished
            if (mIsProcessPickedRecipients) {
                 return;
            }

            if (mRecipientsPickList != null) {
                // Update UI with mRecipientsPickList, which is picked from
                // People.
                updateTitle(mRecipientsPickList);
                mRecipientsPickList = null;
            } else {
                // If we have gone to zero recipients, we need to update the title.
                if (TextUtils.isEmpty(s.toString().trim())) {
                    ContactList contacts = mRecipientsEditor.constructContactsFromInput(false);
                    updateTitle(contacts);
                }

                // Walk backwards in the text box, skipping spaces. If the last
                // character is a comma, update the title bar.
                for (int pos = s.length() - 1; pos >= 0; pos--) {
                    char c = s.charAt(pos);
                    if (c == ' ') continue;

                    if (c == ',') {
                        ContactList contacts = mRecipientsEditor
                                .constructContactsFromInput(false);
                        updateTitle(contacts);
                    }

                    break;
                }
            }

            // If we have gone to zero recipients, disable send button.
            updateSendButtonState();
        }
    };

    private void checkForTooManyRecipients() {
        final int recipientLimit = MmsConfig.getRecipientLimit();
        if (recipientLimit != Integer.MAX_VALUE) {
            final int recipientCount = recipientCount();
            boolean tooMany = recipientCount > recipientLimit;

            if (recipientCount != mLastRecipientCount) {
                // Don't warn the user on every character they type when they're over the limit,
                // only when the actual # of recipients changes.
                mLastRecipientCount = recipientCount;
                if (tooMany) {
                    String tooManyMsg = getString(R.string.too_many_recipients, recipientCount,
                            recipientLimit);
                    Toast.makeText(ComposeMessageActivity.this,
                            tooManyMsg, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private final OnCreateContextMenuListener mRecipientsMenuCreateListener =
        new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            if (menuInfo != null) {
                Contact c = ((RecipientContextMenuInfo) menuInfo).recipient;
                RecipientsMenuClickListener l = new RecipientsMenuClickListener(c);

                menu.setHeaderTitle(c.getName());

                if (c.existsInDatabase()) {
                    menu.add(0, MENU_VIEW_CONTACT, 0, R.string.menu_view_contact)
                            .setOnMenuItemClickListener(l);
                } else if (canAddToContacts(c)){
                    menu.add(0, MENU_ADD_TO_CONTACTS, 0, R.string.menu_add_to_contacts)
                            .setOnMenuItemClickListener(l);
                }
            }
        }
    };

    private final class RecipientsMenuClickListener implements MenuItem.OnMenuItemClickListener {
        private final Contact mRecipient;

        RecipientsMenuClickListener(Contact recipient) {
            mRecipient = recipient;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (null == mRecipient) {
                return false;
            }

            switch (item.getItemId()) {
                // Context menu handlers for the recipients editor.
                case MENU_VIEW_CONTACT: {
                    Uri contactUri = mRecipient.getUri();
                    Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(intent);
                    return true;
                }
                case MENU_ADD_TO_CONTACTS: {
                    mAddContactIntent = ConversationList.createAddContactIntent(
                            mRecipient.getNumber());
                    ComposeMessageActivity.this.startActivityForResult(mAddContactIntent,
                            REQUEST_CODE_ADD_CONTACT);
                    return true;
                }
            }
            return false;
        }
    }

    private boolean canAddToContacts(Contact contact) {
        // There are some kind of automated messages, like STK messages, that we don't want
        // to add to contacts. These names begin with special characters, like, "*Info".
        final String name = contact.getName();
        if (!TextUtils.isEmpty(contact.getNumber())) {
            char c = contact.getNumber().charAt(0);
            if (isSpecialChar(c)) {
                return false;
            }
        }
        if (!TextUtils.isEmpty(name)) {
            char c = name.charAt(0);
            if (isSpecialChar(c)) {
                return false;
            }
        }
        if (!(Mms.isEmailAddress(name) ||
                Telephony.Mms.isPhoneNumber(name) ||
                contact.isMe())) {
            return false;
        }
        return true;
    }

    private boolean isSpecialChar(char c) {
        return c == '*' || c == '%' || c == '$';
    }

    private void addPositionBasedMenuItems(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo");
            return;
        }
        final int position = info.position;

        addUriSpecificMenuItems(menu, v, position);
    }

    private Uri getSelectedUriFromMessageList(ListView listView, int position) {
        if(listView.getChildAt(position) instanceof RcsNotificationMessageListItem){
            return null;
        }
        // If the context menu was opened over a uri, get that uri.
        MessageListItem msglistItem = (MessageListItem) listView.getChildAt(position);
        if (msglistItem == null) {
            // FIXME: Should get the correct view. No such interface in ListView currently
            // to get the view by position. The ListView.getChildAt(position) cannot
            // get correct view since the list doesn't create one child for each item.
            // And if setSelection(position) then getSelectedView(),
            // cannot get corrent view when in touch mode.
            return null;
        }

        TextView textView;
        CharSequence text = null;
        int selStart = -1;
        int selEnd = -1;

        //check if message sender is selected
        textView = (TextView) msglistItem.getBodyTextView();
        if (textView != null) {
            text = textView.getText();
            selStart = textView.getSelectionStart();
            selEnd = textView.getSelectionEnd();
        }

        // Check that some text is actually selected, rather than the cursor
        // just being placed within the TextView.
        if (selStart != selEnd) {
            int min = Math.min(selStart, selEnd);
            int max = Math.max(selStart, selEnd);

            URLSpan[] urls = ((Spanned) text).getSpans(min, max,
                                                        URLSpan.class);

            if (urls.length == 1) {
                return Uri.parse(urls[0].getURL());
            }
        }

        //no uri was selected
        return null;
    }

    private void addUriSpecificMenuItems(ContextMenu menu, View v, int position) {
        Uri uri = getSelectedUriFromMessageList((ListView) v, position);

        if (uri != null) {
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);
            menu.addIntentOptions(0, 0, 0,
                    new android.content.ComponentName(this, ComposeMessageActivity.class),
                    null, intent, 0, null);
        }
    }

    private final void addCallAndContactMenuItems(
            ContextMenu menu, MsgListMenuClickListener l, MessageItem msgItem) {
        if (TextUtils.isEmpty(msgItem.mBody)) {
            return;
        }
        SpannableString msg = new SpannableString(msgItem.mBody);
        Linkify.addLinks(msg, Linkify.ALL);
        ArrayList<String> uris =
            MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));

        // Remove any dupes so they don't get added to the menu multiple times
        HashSet<String> collapsedUris = new HashSet<String>();
        for (String uri : uris) {
            collapsedUris.add(uri.toLowerCase());
        }
        for (String uriString : collapsedUris) {
            String prefix = null;
            int sep = uriString.indexOf(":");
            if (sep >= 0) {
                prefix = uriString.substring(0, sep);
                uriString = uriString.substring(sep + 1);
            }
            Uri contactUri = null;
            String extractedUrl = null;
            boolean knownPrefix = true;
            boolean isUrl = false;
            if ("mailto".equalsIgnoreCase(prefix))  {
                contactUri = getContactUriForEmail(uriString);
            } else if ("tel".equalsIgnoreCase(prefix)) {
                contactUri = getContactUriForPhoneNumber(uriString);
            } else if ("http".equalsIgnoreCase(prefix) || "https".equalsIgnoreCase(prefix)) {
                extractedUrl = prefix + ":" + uriString;
                isUrl = true;
            } else {
                knownPrefix = false;
            }
            if (knownPrefix && contactUri == null && !isUrl) {
                Intent intent = ConversationList.createAddContactIntent(uriString);

                String addContactString = getString(R.string.menu_add_address_to_contacts,
                        uriString);
                menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, addContactString)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
            } else if (isUrl) {
                String copyurl = getString(R.string.menu_copy_url,
                        extractedUrl);
                Intent intent = new Intent();
                intent.putExtra("copyurl", extractedUrl);
                menu.add(0, MENU_COPY_EXTRACT_URL, 0, copyurl)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
            }
        }
    }

    private Uri getContactUriForEmail(String emailAddress) {
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
                Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
                new String[] { Email.CONTACT_ID, Contacts.DISPLAY_NAME }, null, null, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(1);
                    if (!TextUtils.isEmpty(name)) {
                        return ContentUris.withAppendedId(Contacts.CONTENT_URI, cursor.getLong(0));
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    private Uri getContactUriForPhoneNumber(String phoneNumber) {
        Contact contact = Contact.get(phoneNumber, false);
        if (contact.existsInDatabase()) {
            return contact.getUri();
        }
        return null;
    }

    private final OnCreateContextMenuListener mMsgListMenuCreateListener =
        new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (!isCursorValid()) {
                return;
            }
            Cursor cursor = mMsgListAdapter.getCursor();
            String type = cursor.getString(COLUMN_MSG_TYPE);
            long msgId = cursor.getLong(COLUMN_ID);
            int msgType =cursor.getInt(COLUMN_RCS_MSG_TYPE);
            if(msgType == RcsUtils.RCS_MSG_TYPE_NOTIFICATION){
                return;
            }

            addPositionBasedMenuItems(menu, v, menuInfo);

            MessageItem msgItem = mMsgListAdapter.getCachedMessageItem(type, msgId, cursor);
            if (msgItem == null) {
                Log.e(TAG, "Cannot load message item for type = " + type
                        + ", msgId = " + msgId);
                return;
            }

            menu.setHeaderTitle(R.string.message_options);

            MsgListMenuClickListener l = new MsgListMenuClickListener(msgItem);
            if (mIsSmsEnabled && msgItem.mRcsBurnFlag == RcsUtils.RCS_IS_BURN_TRUE) {
                menu.add(0, MENU_DELETE_MESSAGE, 0, R.string.delete_message)
                        .setOnMenuItemClickListener(l);
            } else {
                // It is unclear what would make most sense for copying an MMS message
                // to the clipboard, so we currently do SMS only.
                if (msgItem.isSms()) {
                    // Message type is sms. Only allow "edit" if the message has a single recipient
                    if (getRecipients().size() == 1 &&
                            (msgItem.mBoxId == Sms.MESSAGE_TYPE_OUTBOX ||
                                    msgItem.mBoxId == Sms.MESSAGE_TYPE_FAILED)) {
                        menu.add(0, MENU_EDIT_MESSAGE, 0, R.string.menu_edit)
                                .setOnMenuItemClickListener(l);
                    }

                    menu.add(0, MENU_COPY_MESSAGE_TEXT, 0, R.string.copy_message_text)
                            .setOnMenuItemClickListener(l);
                }

                addCallAndContactMenuItems(menu, l, msgItem);

                // Forward is not available for undownloaded messages.
                if (msgItem.isDownloaded() && (msgItem.isSms() || msgItem.mIsForwardable)) {
                    menu.add(0, MENU_FORWARD_MESSAGE, 0, R.string.menu_forward)
                        .setOnMenuItemClickListener(l);
                }

                // Only failed send message have resend function
                if (msgItem.isFailedMessage()) {
                        menu.add(0, MENU_RESEND, 0, R.string.menu_resend)
                                .setOnMenuItemClickListener(l);
                }

                if (msgItem.isMms()) {
                    switch (msgItem.mBoxId) {
                        case Mms.MESSAGE_BOX_INBOX:
                            break;
                        case Mms.MESSAGE_BOX_OUTBOX:
                            // Since we currently break outgoing messages to multiple
                            // recipients into one message per recipient, only allow
                            // editing a message for single-recipient conversations.
                            // Just Failed Mms should to be provided Edit function.
                            if (getRecipients().size() == 1 &&
                                    (msgItem.mErrorType >= MmsSms.ERR_TYPE_GENERIC_PERMANENT)) {
                                menu.add(0, MENU_EDIT_MESSAGE, 0, R.string.menu_edit)
                                        .setOnMenuItemClickListener(l);
                            }
                            break;
                    }
                    switch (msgItem.mAttachmentType) {
                        case WorkingMessage.TEXT:
                            break;
                        case WorkingMessage.VIDEO:
                        case WorkingMessage.IMAGE:
                        case WorkingMessage.VCARD:
                            if (msgItem.mHaveSomethingToCopyToSDCard) {
                                menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard)
                                .setOnMenuItemClickListener(l);
                            }
                            break;
                        case WorkingMessage.SLIDESHOW:
                        default:
                            menu.add(0, MENU_VIEW_SLIDESHOW, 0, R.string.view_slideshow)
                            .setOnMenuItemClickListener(l);
                            if (msgItem.mHaveSomethingToCopyToSDCard) {
                                menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard)
                                .setOnMenuItemClickListener(l);
                            }
                            if (msgItem.mIsDrmRingtoneWithRights) {
                                menu.add(0, MENU_SAVE_RINGTONE, 0,
                                        getDrmMimeMenuStringRsrc(msgItem.mIsDrmRingtoneWithRights))
                                .setOnMenuItemClickListener(l);
                            }
                            break;
                    }
                }

                if (msgItem.mLocked && mIsSmsEnabled) {
                    menu.add(0, MENU_UNLOCK_MESSAGE, 0, R.string.menu_unlock)
                        .setOnMenuItemClickListener(l);
                } else if (mIsSmsEnabled) {
                    menu.add(0, MENU_LOCK_MESSAGE, 0, R.string.menu_lock)
                        .setOnMenuItemClickListener(l);
                }
                if (msgItem.mFavourite==1) {
                    menu.add(0, MENU_UNFAVOURITE_MESSAGE, 0, R.string.unfavorited)
                            .setOnMenuItemClickListener(l);
                } else if (mIsSmsEnabled) {
                    menu.add(0, MENU_FAVOURITE_MESSAGE, 0, R.string.favorited)
                            .setOnMenuItemClickListener(l);
                }

                if (msgItem.isSms()) {
                    if (MessageUtils.getActivatedIccCardCount() > 0) {
                        menu.add(0, MENU_COPY_TO_SIM, 0, R.string.copy_to_sim)
                                .setOnMenuItemClickListener(l);
                    }
                }

                menu.add(0, MENU_VIEW_MESSAGE_DETAILS, 0, R.string.view_message_details)
                    .setOnMenuItemClickListener(l);

                if (msgItem.mDeliveryStatus != MessageItem.DeliveryStatus.NONE || msgItem.mReadReport) {
                    menu.add(0, MENU_DELIVERY_REPORT, 0, R.string.view_delivery_report)
                            .setOnMenuItemClickListener(l);
                }

                if (mIsSmsEnabled) {
                    menu.add(0, MENU_DELETE_MESSAGE, 0, R.string.delete_message)
                        .setOnMenuItemClickListener(l);
                }
            }
        }
    };

    private void editMessageItem(MessageItem msgItem) {
        if ("sms".equals(msgItem.mType)) {
            editSmsMessageItem(msgItem);
        } else {
            // There is a bug, when we edit a sending Mms, this Mms will be
            // draft status, at this moment, If we edit anther sending Mms, the
            // first Mms will be delete and the second Mms will be a draft
            // message and in edit status, becase one threadID just have only
            // one draft message. So we must send the first Mms then make the
            // second Mms to edit status to fix this bug. The
            // isPreparedForSending method is check that if there is a edit
            // message now, if there is, then we send this message first.
            if (isPreparedForSending()) {
                // Send the first edit message. Here we must not use parameter
                // true to check emergency mode,if we do this. The Mms will not
                // be send out but delete when now is emergency mode
                // and the bug still exist.
                sendMessage(false);
                // Save the msgItem, and show it when the onMessageSend method
                // called
                mEditMessageItem = msgItem;
                return;
            } else {
                editMmsMessageItem(msgItem);
            }
        }
        if (msgItem.isFailedMessage() && mMsgListAdapter.getCount() <= 1) {
            // For messages with bad addresses, let the user re-edit the recipients.
            initRecipientsEditor();
        }
    }

    private void editSmsMessageItem(MessageItem msgItem) {
        editSmsMessageItem(msgItem.mMsgId, msgItem.mBody);
    }

    private void editSmsMessageItem(long msgId, String msgBody) {
        // When the message being edited is the only message in the conversation, the delete
        // below does something subtle. The trigger "delete_obsolete_threads_pdu" sees that a
        // thread contains no messages and silently deletes the thread. Meanwhile, the mConversation
        // object still holds onto the old thread_id and code thinks there's a backing thread in
        // the DB when it really has been deleted. Here we try and notice that situation and
        // clear out the thread_id. Later on, when Conversation.ensureThreadId() is called, we'll
        // create a new thread if necessary.
        synchronized(mConversation) {
            if (mConversation.getMessageCount() <= 1) {
                mConversation.clearThreadId();
                MessagingNotification.setCurrentlyDisplayedThreadId(
                    MessagingNotification.THREAD_NONE);
            }
        }
        // Delete the old undelivered SMS and load its content.
        Uri uri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
        int count = SqliteWrapper.delete(ComposeMessageActivity.this,
                mContentResolver, uri, null, null);

        mWorkingMessage.setText(msgBody);

        // if the ListView only has one message and delete the message success
        // the uri of conversation will be null, so it can't qurey info from DB,
        // so the mMsgListAdapter should change Cursor to null
        if (count > 0) {
            if (mMsgListAdapter.getCount() == MSG_ONLY_ONE_FAIL_LIST_ITEM) {
                mMsgListAdapter.changeCursor(null);
            }
        }
    }


    private void editMmsMessageItem(MessageItem msgItem) {
        editMmsMessageItem(msgItem.mMessageUri, msgItem.mSubject);
    }

    private void editMmsMessageItem(Uri uri, String subject) {
        // Load the selected message in as the working message.
        WorkingMessage newWorkingMessage = WorkingMessage.load(this, uri);
        if (newWorkingMessage == null) {
            return;
        }

        // Discard the current message in progress.
        mWorkingMessage.discard();

        mWorkingMessage = newWorkingMessage;
        mWorkingMessage.setConversation(mConversation);

        drawTopPanel(false);

        // WorkingMessage.load() above only loads the slideshow. Set the
        // subject here because we already know what it is and avoid doing
        // another DB lookup in load() just to get it.
        mWorkingMessage.setSubject(subject, false);

        if (mWorkingMessage.hasSubject()) {
            showSubjectEditor(true);
        }
    }

    private void copyToClipboard(String str) {
        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(null, str));
    }

    private void forwardMessage(final MessageItem msgItem) {
        mTempThreadId = 0;
        // The user wants to forward the message. If the message is an mms message, we need to
        // persist the pdu to disk. This is done in a background task.
        // If the task takes longer than a half second, a progress dialog is displayed.
        // Once the PDU persisting is done, another runnable on the UI thread get executed to start
        // the ForwardMessageActivity.
        getAsyncDialog().runAsync(new Runnable() {
            @Override
            public void run() {
                // This runnable gets run in a background thread.
                if (msgItem.mType.equals("mms")) {
                    SendReq sendReq = new SendReq();
                    String subject = getString(R.string.forward_prefix);
                    if (msgItem.mSubject != null) {
                        subject += msgItem.mSubject;
                    }
                    sendReq.setSubject(new EncodedStringValue(subject));
                    sendReq.setBody(msgItem.mSlideshow.makeCopy());

                    mTempMmsUri = null;
                    try {
                        PduPersister persister =
                                PduPersister.getPduPersister(ComposeMessageActivity.this);
                        // Copy the parts of the message here.
                        mTempMmsUri = persister.persist(sendReq, Mms.Draft.CONTENT_URI, true,
                                MessagingPreferenceActivity
                                    .getIsGroupMmsEnabled(ComposeMessageActivity.this), null);
                        mTempThreadId = MessagingNotification.getThreadId(
                                ComposeMessageActivity.this, mTempMmsUri);
                    } catch (MmsException e) {
                        Log.e(TAG, "Failed to copy message: " + msgItem.mMessageUri);
                        Toast.makeText(ComposeMessageActivity.this,
                                R.string.cannot_save_message, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                // Once the above background thread is complete, this runnable is run
                // on the UI thread.
                Intent intent = createIntent(ComposeMessageActivity.this, 0);

                intent.putExtra(KEY_EXIT_ON_SENT, true);
                intent.putExtra(KEY_FORWARDED_MESSAGE, true);
                if (mTempThreadId > 0) {
                    intent.putExtra(THREAD_ID, mTempThreadId);
                }

                if (msgItem.mType.equals("sms")) {
                    intent.putExtra("sms_body", msgItem.mBody);
                } else {
                    intent.putExtra("msg_uri", mTempMmsUri);
                    String subject = getString(R.string.forward_prefix);
                    if (msgItem.mSubject != null) {
                        subject += msgItem.mSubject;
                    }
                    intent.putExtra("subject", subject);
                    String[] numbers = mConversation.getRecipients().getNumbers();
                    if (numbers != null) {
                        intent.putExtra("msg_recipient",numbers);
                    }
                }
                // ForwardMessageActivity is simply an alias in the manifest for
                // ComposeMessageActivity. We have to make an alias because ComposeMessageActivity
                // launch flags specify singleTop. When we forward a message, we want to start a
                // separate ComposeMessageActivity. The only way to do that is to override the
                // singleTop flag, which is impossible to do in code. By creating an alias to the
                // activity, without the singleTop flag, we can launch a separate
                // ComposeMessageActivity to edit the forward message.
                intent.setClassName(ComposeMessageActivity.this,
                        "com.android.mms.ui.ForwardMessageActivity");
                startActivity(intent);
            }
        }, R.string.building_slideshow_title);
    }

    private void resendMessage(MessageItem msgItem) {
        if (msgItem.isMms()) {
            // If it is mms, we delete current mms and use current mms
            // uri to create new working message object.
            WorkingMessage newWorkingMessage = WorkingMessage.load(this, msgItem.mMessageUri);
            if (newWorkingMessage == null)
                return;

            // Discard the current message in progress.
            mWorkingMessage.discard();

            mWorkingMessage = newWorkingMessage;
            mWorkingMessage.setConversation(mConversation);
            mWorkingMessage.setSubject(msgItem.mSubject, false);
        } else {
            if (getRecipients().size() > 1) {
                // If the number is more than one when send sms, there will show serveral msg items
                // the recipient of msg item is not equal with recipients of conversation
                // so we should record the recipient of this msg item.
                mWorkingMessage.setResendMultiRecipients(true);
                mResendSmsRecipient = msgItem.mAddress;
            }

            editSmsMessageItem(msgItem);
        }

        sendMessage(true);
    }

    /**
     * Context menu handlers for the message list view.
     */
    private final class MsgListMenuClickListener implements MenuItem.OnMenuItemClickListener {
        private MessageItem mMsgItem;

        public MsgListMenuClickListener(MessageItem msgItem) {
            mMsgItem = msgItem;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mMsgItem == null) {
                return false;
            }

            switch (item.getItemId()) {
                case MENU_EDIT_MESSAGE:
                    editMessageItem(mMsgItem);
                    drawBottomPanel();
                    return true;

                case MENU_COPY_MESSAGE_TEXT:
                    copyToClipboard(mMsgItem.mBody);
                    return true;

                case MENU_FORWARD_MESSAGE:
                    if (mMsgItem.isMms() && !isAllowForwardMessage(mMsgItem)) {
                        Toast.makeText(ComposeMessageActivity.this,
                                R.string.forward_size_over, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    try {
                        if (mIsRcsEnabled && mMsgItem.mRcsId > 0) {
                            if (mAccountApi.isOnline()) {
                                rcsforwardid = mMsgItem.mRcsId;
                                RcsChatMessageUtils.forwardContactOrConversation(
                                        ComposeMessageActivity.this, new ForwardClickListener());
                                return true;
                            } else {
                                if (mMsgItem.mRcsType == RcsUtils.RCS_MSG_TYPE_TEXT){
                                    if (mMsgItem.mBody.getBytes().length <= RCS_MAX_SMS_LENGHTH) {
                                        rcsforwardid = mMsgItem.mRcsId;
                                        RcsChatMessageUtils.forwardContactOrConversation(
                                                ComposeMessageActivity.this,
                                                new ForwardClickListener());
                                        return true;
                                    } else {
                                        Toast.makeText(ComposeMessageActivity.this,
                                            R.string.not_online_message_too_big, Toast.LENGTH_SHORT)
                                            .show();
                                        return false;
                                    }
                                } else {
                                    Toast.makeText(ComposeMessageActivity.this,
                                            R.string.rcs_offline, Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            }
                        }
                    } catch (ServiceDisconnectedException e) {
                        Log.w(RCS_TAG,e);
                    }
                    forwardMessage(mMsgItem);
                    return true;

                case MENU_RESEND:
                    resendMessage(mMsgItem);
                    return true;

                case MENU_VIEW_SLIDESHOW:
                    MessageUtils.viewMmsMessageAttachment(ComposeMessageActivity.this,
                            ContentUris.withAppendedId(Mms.CONTENT_URI, mMsgItem.mMsgId), null,
                            getAsyncDialog());
                    return true;

                case MENU_VIEW_MESSAGE_DETAILS:
                    return showMessageDetails(mMsgItem);

                case MENU_DELETE_MESSAGE: {
                    DeleteMessageListener l = new DeleteMessageListener(mMsgItem);
                    confirmDeleteDialog(l, mMsgItem.mLocked);
                    return true;
                }
                case MENU_DELIVERY_REPORT:
                    showDeliveryReport(mMsgItem.mMsgId, mMsgItem.mType);
                    return true;

                case MENU_COPY_TO_SDCARD: {
                    int resId = copyMedia(mMsgItem.mMsgId) ? R.string.copy_to_sdcard_success :
                        R.string.copy_to_sdcard_fail;
                    Toast.makeText(ComposeMessageActivity.this, resId, Toast.LENGTH_SHORT).show();
                    return true;
                }

                case MENU_SAVE_RINGTONE: {
                    int resId = getDrmMimeSavedStringRsrc(mMsgItem.mIsDrmRingtoneWithRights,
                            saveRingtone(mMsgItem.mMsgId));
                    Toast.makeText(ComposeMessageActivity.this, resId, Toast.LENGTH_SHORT).show();
                    return true;
                }

                case MENU_LOCK_MESSAGE: {
                    lockMessage(mMsgItem, true);
                    return true;
                }

                case MENU_UNLOCK_MESSAGE: {
                    lockMessage(mMsgItem, false);
                    return true;
                }
                case MENU_FAVOURITE_MESSAGE: {
                    System.out.println("MENU_FAVOURITE_MESSAGE");
                    favouriteMessage(mMsgItem, true);
                    return true;
                }

                case MENU_UNFAVOURITE_MESSAGE: {
                    System.out.println("MENU_UNFAVOURITE_MESSAGE");
                    favouriteMessage(mMsgItem, false);
                    return true;
                }

                case MENU_COPY_TO_SIM: {
                    if (MessageUtils.getActivatedIccCardCount() > 1) {
                        String[] items = new String[MSimTelephonyManager.getDefault()
                                .getPhoneCount()];
                        for (int i = 0; i < items.length; i++) {
                            items[i] = MessageUtils.getMultiSimName(ComposeMessageActivity.this, i);
                        }
                        CopyToSimSelectListener listener = new CopyToSimSelectListener(mMsgItem);
                        new AlertDialog.Builder(ComposeMessageActivity.this)
                            .setTitle(R.string.copy_to_sim)
                            .setPositiveButton(android.R.string.ok, listener)
                            .setSingleChoiceItems(items, 0, listener)
                            .setCancelable(true)
                            .show();
                    } else {
                        new Thread(new CopyToSimThread(mMsgItem)).start();
                    }
                    return true;
                }

                case MENU_COPY_EXTRACT_URL:
                    String copyedUrl = item.getIntent().getStringExtra("copyurl");
                    copyToClipboard(copyedUrl);
                    return true;
                default:
                    return false;
            }
        }
    }

    public class ForwardClickListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton) {
            switch (whichButton) {
                case 0:
                   launchRcsPhonePicker();
                    break;
                case 1:
                    Intent intent = new Intent(ComposeMessageActivity.this,ConversationList.class);
                    intent.putExtra("select_conversation",true);
                    MessageUtils.setMailboxMode(false);
                    startActivityForResult(intent, REQUEST_SELECT_CONV);
                    break;
                case 2:
                    launchRcsContactGroupPicker(REQUEST_SELECT_GROUP);
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isAllowForwardMessage(MessageItem msgItem) {
        int messageSize = msgItem.getSlideshow().getTotalMessageSize();
        int smilSize = msgItem.getSlideshow().getSMILSize();
        int forwardStrSize = getString(R.string.forward_prefix).getBytes().length;
        int subjectSize =  (msgItem.mSubject == null) ? 0 : msgItem.mSubject.getBytes().length;
        int totalSize = messageSize + forwardStrSize + subjectSize + smilSize;
        if (DEBUG) {
            Log.e(TAG,"isAllowForwardMessage messageSize = "+ messageSize
                    + ", forwardStrSize = "+forwardStrSize+ ", subjectSize = "+subjectSize
                    + ", totalSize = " + totalSize);
        }
        return totalSize <= (MmsConfig.getMaxMessageSize() - SlideshowModel.SLIDESHOW_SLOP);
    }

    private void lockMessage(MessageItem msgItem, boolean locked) {
        Uri uri;
        if ("sms".equals(msgItem.mType)) {
            uri = Sms.CONTENT_URI;
        } else {
            uri = Mms.CONTENT_URI;
        }
        final Uri lockUri = ContentUris.withAppendedId(uri, msgItem.mMsgId);

        final ContentValues values = new ContentValues(1);
        values.put("locked", locked ? 1 : 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                getContentResolver().update(lockUri,
                        values, null, null);
            }
        }, "ComposeMessageActivity.lockMessage").start();
    }

    private void favouriteMessage(MessageItem msgItem, boolean favourite) {
        Uri uri;

        uri = Sms.CONTENT_URI;
        final Uri lockUri = ContentUris.withAppendedId(uri, msgItem.mMsgId);

        final ContentValues values = new ContentValues(1);
        values.put("favourite", favourite ? 1 : 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                getContentResolver().update(lockUri,
                        values, null, null);
            }
        }, "ComposeMessageActivity.favourite").start();
    }

    /**
     * Copies media from an Mms to the DrmProvider
     * @param msgId
     */
    private boolean saveRingtone(long msgId) {
        boolean result = true;
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(this,
                        ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            Log.e(TAG, "copyToDrmProvider can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }

        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());
            if (DrmUtils.isDrmType(type)) {
                // All parts (but there's probably only a single one) have to be successful
                // for a valid result.
                result &= copyPart(part, Long.toHexString(msgId));
            }
        }
        return result;
    }

    private int getDrmMimeMenuStringRsrc(boolean isDrmRingtoneWithRights) {
        if (isDrmRingtoneWithRights) {
            return R.string.save_ringtone;
        }
        return 0;
    }

    private int getDrmMimeSavedStringRsrc(boolean isDrmRingtoneWithRights, boolean success) {
        if (isDrmRingtoneWithRights) {
            return success ? R.string.saved_ringtone : R.string.saved_ringtone_fail;
        }
        return 0;
    }

    /**
     * Copies media from an Mms to the "download" directory on the SD card. If any of the parts
     * are audio types, drm'd or not, they're copied to the "Ringtones" directory.
     * @param msgId
     */
    private boolean copyMedia(long msgId) {
        boolean result = true;
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(this,
                        ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            Log.e(TAG, "copyMedia can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }

        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);

            // all parts have to be successful for a valid result.
            result &= copyPart(part, Long.toHexString(msgId));
        }
        return result;
    }

    private boolean copyPart(PduPart part, String fallback) {
        Uri uri = part.getDataUri();
        String type = new String(part.getContentType());
        type = MessageUtils.parseOctStreamContentType(part, type);
        boolean isDrm = DrmUtils.isDrmType(type);
        if (isDrm) {
            type = MmsApp.getApplication().getDrmManagerClient()
                    .getOriginalMimeType(part.getDataUri());
        }
        if (!ContentType.isImageType(type)
                && !ContentType.isVideoType(type)
                && !ContentType.isAudioType(type)
                && !(ContentType.TEXT_VCARD.toLowerCase().equals(type.toLowerCase()))
                && !(ContentType.AUDIO_OGG.toLowerCase().equals(type.toLowerCase()))) {
            return true;    // we only save pictures, videos, and sounds. Skip the text parts,
                            // the app (smil) parts, and other type that we can't handle.
                            // Return true to pretend that we successfully saved the part so
                            // the whole save process will be counted a success.
        }
        InputStream input = null;
        FileOutputStream fout = null;
        try {
            input = mContentResolver.openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;

                byte[] location = part.getName();
                if (location == null) {
                    location = part.getFilename();
                }
                if (location == null) {
                    location = part.getContentLocation();
                }

                String fileName;
                if (location == null) {
                    // Use fallback name.
                    fileName = fallback;
                } else {
                    // For locally captured videos, fileName can end up being something like this:
                    //      /mnt/sdcard/Android/data/com.android.mms/cache/.temp1.3gp
                    fileName = new String(location);
                }
                File originalFile = new File(fileName);
                fileName = originalFile.getName();  // Strip the full path of where the "part" is
                                                    // stored down to just the leaf filename.

                // Depending on the location, there may be an
                // extension already on the name or not. If we've got audio, put the attachment
                // in the Ringtones directory.
                String dir = Environment.getExternalStorageDirectory() + "/"
                                + (ContentType.isAudioType(type) ? Environment.DIRECTORY_RINGTONES :
                                    Environment.DIRECTORY_DOWNLOADS)  + "/";
                String extension;
                int index;
                if ((index = fileName.lastIndexOf('.')) == -1) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
                } else {
                    extension = fileName.substring(index + 1, fileName.length());
                    fileName = fileName.substring(0, index);
                }
                if (isDrm) {
                    extension += DrmUtils.getConvertExtension(type);
                }

                // Remove leading periods. The gallery ignores files starting with a period.
                fileName = fileName.replaceAll("^\\.", "");

                File file = getUniqueDestination(dir + fileName, extension);

                // make sure the path is valid and directories created for this file.
                File parentFile = file.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    Log.e(TAG, "[MMS] copyPart: mkdirs for " + parentFile.getPath() + " failed!");
                    return false;
                }

                fout = new FileOutputStream(file);

                byte[] buffer = new byte[8000];
                int size = 0;
                while ((size=fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, size);
                }

                // Notify other applications listening to scanner events
                // that a media file has been added to the sd card
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)));
            }
        } catch (IOException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            return false;
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
            if (null != fout) {
                try {
                    fout.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
        }
        return true;
    }

    private File getUniqueDestination(String base, String extension) {
        File file = new File(base + "." + extension);

        for (int i = 2; file.exists(); i++) {
            file = new File(base + "_" + i + "." + extension);
        }
        return file;
    }

    private void showDeliveryReport(long messageId, String type) {
        Intent intent = new Intent(this, DeliveryReportActivity.class);
        intent.putExtra(MESSAGE_ID, messageId);
        intent.putExtra(MESSAGE_TYPE, type);
        startActivity(intent);
    }

    private final IntentFilter mHttpProgressFilter = new IntentFilter(PROGRESS_STATUS_ACTION);

    private final BroadcastReceiver mHttpProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PROGRESS_STATUS_ACTION.equals(intent.getAction())) {
                long token = intent.getLongExtra("token",
                                    SendingProgressTokenManager.NO_TOKEN);
                if (token != mConversation.getThreadId()) {
                    return;
                }

                int progress = intent.getIntExtra("progress", 0);
                switch (progress) {
                    case PROGRESS_START:
                        setProgressBarVisibility(true);
                        break;
                    case PROGRESS_ABORT:
                    case PROGRESS_COMPLETE:
                        setProgressBarVisibility(false);
                        break;
                    default:
                        setProgress(100 * progress);
                }
            }
        }
    };

    private BroadcastReceiver mFileTranferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager)arg0.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!gprs.isConnected() && !wifi.isConnected()) {
                mMsgListAdapter.setRcsIsStopDown(true);
                mMsgListAdapter.notifyDataSetChanged();
                return;
            }
            long start = intent.getLongExtra(BroadcastConstants.BC_VAR_TRANSFER_PRG_START, -1);
            long end = intent.getLongExtra(BroadcastConstants.BC_VAR_TRANSFER_PRG_END, -1);
            long total = intent.getLongExtra(BroadcastConstants.BC_VAR_TRANSFER_PRG_TOTAL, -1);
            String notifyMessageId = intent
                    .getStringExtra(BroadcastConstants.BC_VAR_TRANSFER_PRG_MESSAGE_ID);
            LogHelper.trace("messageId =" + notifyMessageId
                    + "start =" + start + ";end =" + end + ";total =" + total);
            HashMap<String, Long> fileProgressHashMap = mMsgListAdapter.getFileTrasnferHashMap();
            if (notifyMessageId != null && start == end) {
                LogHelper.trace("download finish ");
                fileProgressHashMap.remove(notifyMessageId);
                mMsgListAdapter.notifyDataSetChanged();
                return;
            }
            if (fileProgressHashMap != null && total != 0) {
                Long lastProgress = fileProgressHashMap.get(notifyMessageId);
                long temp = start * 100 / total;
                if (temp == 100) {
                    fileProgressHashMap.remove(notifyMessageId);
                    Log.i(RCS_TAG,"100");
                    return;
                }
                if (lastProgress != null) {
                    LogHelper.trace("file tranfer progress = " + temp + "% ; lastprogress = "
                            + lastProgress + "% .");
                }
                if (lastProgress == null || temp - lastProgress >= 5) {
                    lastProgress = temp;
                    fileProgressHashMap.put(notifyMessageId, Long.valueOf(temp));
                    mMsgListAdapter.setsFileTrasnfer(fileProgressHashMap);
                    mMsgListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

  private BroadcastReceiver mCloudFileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) arg0
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!gprs.isConnected() && !wifi.isConnected()) {
                mMsgListAdapter.setRcsIsStopDown(true);
                mMsgListAdapter.notifyDataSetChanged();
                return;
            }
            String eventType = intent.getStringExtra(BroadcastConstants.BC_VAR_MC_ENENTTYPE);
            int messageId = intent.getIntExtra(BroadcastConstants.BC_VAR_MC_CHATMESSAGE_ID, -1);
            Log.i(TAG, "eventType=" + eventType + ",tag=" + "tag_file_" + messageId);
            HashMap<String, Long> fileProgressHashMap = mMsgListAdapter.getFileTrasnferHashMap();

            TextView textDataView = (TextView) mMsgListView
                    .findViewWithTag("tag_file_" + messageId);

            if (!TextUtils.isEmpty(eventType) &&
                    BroadcastConstants.BC_V_MC_EVENTTYPE_ERROR.equals(eventType)) {
                String message = intent.getStringExtra(BroadcastConstants.BC_VAR_MC_MESSAGE);
                toast(R.string.download_mcloud_file_fail);
                fileProgressHashMap.remove(String.valueOf(messageId));
                if (textDataView != null) {
                    textDataView.setText(getString(R.string.stop_down_load));
                }
            } else if (!TextUtils.isEmpty(eventType)
                    && BroadcastConstants.BC_V_MC_EVENTTYPE_PROGRESS.equals(eventType)) {
                float process = (int) intent.getLongExtra(
                        BroadcastConstants.BC_VAR_MC_PROCESS_SIZE, 0);
                float total = (int) intent.getLongExtra(
                        BroadcastConstants.BC_VAR_MC_TOTAL_SIZE, 0);
                long percent = (long) ((process / total) * 100);
                fileProgressHashMap.put(String.valueOf(messageId), percent);
                if (textDataView != null) {
                    textDataView.setText(getString(R.string.downloading_percent, percent));
                }
            } else if (!TextUtils.isEmpty(eventType)
                    && BroadcastConstants.BC_V_MC_EVENTTYPE_SUCCESS.equals(eventType)) {
                fileProgressHashMap.remove(String.valueOf(messageId));
                if (textDataView != null) {
                    textDataView.setText(getString(R.string.downloading_finish));
                }
            } else if(!TextUtils.isEmpty(eventType)
                    && BroadcastConstants.BC_V_MC_EVENTTYPE_FILE_TOO_LARGE.equals(eventType)) {
                toast(R.string.file_is_too_larger);
            } else if(!TextUtils.isEmpty(eventType)
                    && BroadcastConstants.BC_V_MC_EVENTTYPE_SUFFIX_NOT_ALLOWED.equals(eventType)) {
                toast(R.string.name_not_fix);
            }
        }
    };

    private final BroadcastReceiver mMediaStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "mMediaStateReceiver action = " + intent.getAction());
            checkAttachFileState(context);
        }
    };

    private static ContactList sEmptyContactList;

    private ContactList getRecipients() {
        // If the recipients editor is visible, the conversation has
        // not really officially 'started' yet.  Recipients will be set
        // on the conversation once it has been saved or sent.  In the
        // meantime, let anyone who needs the recipient list think it
        // is empty rather than giving them a stale one.
        if (isRecipientsEditorVisible()) {
            if (sEmptyContactList == null) {
                sEmptyContactList = new ContactList();
            }
            return sEmptyContactList;
        }
        return mConversation.getRecipients();
    }

    private void updateTitle(ContactList list) {
        String title = null;
        String subTitle = null;

        if (mConversation.isGroupChat()) {
            GroupChatModel groupChat = mConversation.getGroupChat();
            if (groupChat != null) {
                title = RcsUtils.getDisplayName(groupChat); // TODO change to gorupChat.getDisplayName();
            } else if (!mSentMessage) {
                title = getString(R.string.new_group_chat);
            } else {
                title = getString(R.string.group_chat);
            }
            subTitle = getString(R.string.group_chat) + mConversation.getGroupChatStatusText();
        } else {
            int cnt = list.size();
            switch (cnt) {
                case 0: {
                    String recipient = null;
                    if (mRecipientsEditor != null) {
                        recipient = mRecipientsEditor.getText().toString();
                    }
                    if (MessageUtils.isWapPushNumber(recipient)) {
                        String[] mAddresses = recipient.split(":");
                        title = mAddresses[getResources().getInteger(R.integer.wap_push_address_index)];
                    } else {
                        title = TextUtils.isEmpty(recipient)
                                ? getString(R.string.new_message) : recipient;
                    }
                    break;
                }
                case 1: {
                    title = list.get(0).getName();      // get name returns the number if there's no
                                                        // name available.
                    String number = list.get(0).getNumber();
                    if (MessageUtils.isWapPushNumber(number)) {
                        String[] mTitleNumber = number.split(":");
                        number = mTitleNumber[getResources().getInteger(
                                R.integer.wap_push_address_index)];
                    }
                    if (MessageUtils.isWapPushNumber(title)) {
                        String[] mTitle = title.split(":");
                        title = mTitle[getResources().getInteger(R.integer.wap_push_address_index)];
                    }
                    if (!title.equals(number)) {
                        subTitle = PhoneNumberUtils.formatNumber(number, number,
                                MmsApp.getApplication().getCurrentCountryIso());
                    }
                    break;
                }
                default: {
                    // Handle multiple recipients
                    title = list.formatNames(", ");
                    subTitle = getResources().getQuantityString(R.plurals.recipient_count, cnt, cnt);
                    break;
                }
            }
            mDebugRecipients = list.serialize();

            // the cnt is already be added recipients count
            mExistsRecipientsCount = cnt;
        }

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(title);
        actionBar.setSubtitle(subTitle);
    }

    // Get the recipients editor ready to be displayed onscreen.
    private void initRecipientsEditor() {
        if (isRecipientsEditorVisible()) {
            return;
        }
        // Must grab the recipients before the view is made visible because getRecipients()
        // returns empty recipients when the editor is visible.
        ContactList recipients = getRecipients();

        ViewStub stub = (ViewStub)findViewById(R.id.recipients_editor_stub);
        if (stub != null) {
            View stubView = stub.inflate();
            mRecipientsEditor = (RecipientsEditor) stubView.findViewById(R.id.recipients_editor);
        } else {
            mRecipientsEditor = (RecipientsEditor)findViewById(R.id.recipients_editor);
            mRecipientsEditor.setVisibility(View.VISIBLE);
        }

        mRecipientsEditor.setAdapter(new ChipsRecipientAdapter(this));
        mRecipientsEditor.populate(recipients);
        mRecipientsEditor.setOnCreateContextMenuListener(mRecipientsMenuCreateListener);
        mRecipientsEditor.addTextChangedListener(mRecipientsWatcher);
        // TODO : Remove the max length limitation due to the multiple phone picker is added and the
        // user is able to select a large number of recipients from the Contacts. The coming
        // potential issue is that it is hard for user to edit a recipient from hundred of
        // recipients in the editor box. We may redesign the editor box UI for this use case.
        // mRecipientsEditor.setFilters(new InputFilter[] {
        //         new InputFilter.LengthFilter(RECIPIENTS_MAX_LENGTH) });

        mRecipientsEditor.setOnSelectChipRunnable(new Runnable() {
            @Override
            public void run() {
                // After the user selects an item in the pop-up contacts list, move the
                // focus to the text editor if there is only one recipient.  This helps
                // the common case of selecting one recipient and then typing a message,
                // but avoids annoying a user who is trying to add five recipients and
                // keeps having focus stolen away.
                if (mRecipientsEditor.getRecipientCount() == 1) {
                    // if we're in extract mode then don't request focus
                    final InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputManager == null || !inputManager.isFullscreenMode()) {
                        mTextEditor.requestFocus();
                    }
                }
            }
        });

        mRecipientsEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    RecipientsEditor editor = (RecipientsEditor) v;
                    ContactList contacts = editor.constructContactsFromInput(false);
                    updateTitle(contacts);
                    if(mRecipientsEditor.getNumbers().size() == 1){
                        checkCapability(mRecipientsEditor.getNumbers().get(0));
                     }
                } else {
                    if (mAttachmentSelector.getVisibility() == View.VISIBLE) {
                        mAttachmentSelector.setVisibility(View.GONE);
                    }
                }
            }
        });

        PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(this, mRecipientsEditor);

        mTopPanel.setVisibility(View.VISIBLE);
    }

    //==========================================================
    // Activity methods
    //==========================================================

    public static boolean cancelFailedToDeliverNotification(Intent intent, Context context) {
        if (MessagingNotification.isFailedToDeliver(intent)) {
            // Cancel any failed message notifications
            MessagingNotification.cancelNotification(context,
                        MessagingNotification.MESSAGE_FAILED_NOTIFICATION_ID);
            return true;
        }
        return false;
    }

    public static boolean cancelFailedDownloadNotification(Intent intent, Context context) {
        if (MessagingNotification.isFailedToDownload(intent)) {
            // Cancel any failed download notifications
            MessagingNotification.cancelNotification(context,
                        MessagingNotification.DOWNLOAD_FAILED_NOTIFICATION_ID);
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsSmsEnabled = MmsConfig.isSmsEnabled(this);
        super.onCreate(savedInstanceState);

        resetConfiguration(getResources().getConfiguration());

        setContentView(R.layout.compose_message_activity);
        setProgressBarVisibility(false);

        mShowAttIcon = getResources().getBoolean(R.bool.config_show_attach_icon_always);
        boolean isBtnStyle = getResources().getBoolean(R.bool.config_btnstyle);
        mShowTwoButtons = isBtnStyle && MessageUtils.isMsimIccCardActive();
        // Initialize members for UI elements.
        initResourceRefs();

        mContentResolver = getContentResolver();
        mBackgroundQueryHandler = new BackgroundQueryHandler(mContentResolver);

        initialize(savedInstanceState, 0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mAirplaneModeBroadcastReceiver, intentFilter);

        if (TRACE) {
            android.os.Debug.startMethodTracing("compose");
        }
    }

    private void sendRcsOption() {
        if (getRecipients().size() == 1) {
            Contact data = getRecipients().get(0);
            if (data != null && !TextUtils.isEmpty(data.getNumber())) {
                checkCapability(data.getNumber());
            }
        }
    }

    private void initRcsComponents() {
        Intent intent = getIntent();

        // Whether is creating a RCS group chat.
        if (intent.hasExtra("isGroupChat") && mConversation != null) {
            boolean isGroupChat = intent.getBooleanExtra("isGroupChat", false);
            mConversation.setIsGroupChat(isGroupChat);
        }

        mConfApi = RcsApiManager.getConfApi();
        mMessageApi = RcsApiManager.getMessageApi();
        mAccountApi = RcsApiManager.getRcsAccountApi();
        mSupportApi = RcsApiManager.getSupportApi();
        mIsRcsEnabled = mSupportApi.isRcsSupported();

        if (mConversation.isGroupChat()) {
            String groupId = intent.getStringExtra("groupId");
            if (!TextUtils.isEmpty(groupId)) {
                try {
                    GroupChatModel groupChat = mMessageApi.getGroupChatById(groupId);
                    mConversation.setGroupChat(groupChat);
                    mSentMessage = true;
                } catch (ServiceDisconnectedException e) {
                    Log.w(RCS_TAG, "Exception initRcsComponents()" + e);
                }
            } else {
                long threadId = mConversation.getThreadId();
                if (threadId > 0) {
                    try {
                        long rcsThreadId = RcsUtils.getRcsThreadIdByThreadId(
                                ComposeMessageActivity.this, threadId);
                        if (rcsThreadId > 0) {
                            GroupChatModel groupChat = mMessageApi
                                    .getGroupChatByThreadId(rcsThreadId);
                            mConversation.setGroupChat(groupChat);
                            mSentMessage = true;
                        }
                    } catch (ServiceDisconnectedException e) {
                        Log.w(RCS_TAG, "Exception initRcsComponents()" + e);
                    }
                }
            }
        } else {
            sendRcsOption();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastConstants.UI_ALERT_FILE_SUFFIX_INVALID);
        filter.addAction(BroadcastConstants.UI_ALERT_FILE_TOO_LARGE);
        filter.addAction(BroadcastConstants.UI_DOWNLOADING_FILE_CHANGE);
        filter.addAction(BroadcastConstants.UI_MESSAGE_ADD_DATABASE);
        filter.addAction(BroadcastConstants.UI_MESSAGE_STATUS_CHANGE_NOTIFY);
        filter.addAction(BroadcastConstants.UI_REFRESH_MESSAGE_LIST);
        filter.addAction(BroadcastConstants.UI_SHOW_MESSAGE_NOTIFY);
        filter.addAction(BroadcastConstants.UI_SHOW_MESSAGE_SEND_ERROR);
        filter.addAction(BroadcastConstants.UI_MC_DOWNLOAD_FILE_FROM_URL);
        if (mConversation.isGroupChat()) {
            filter.addAction(BroadcastConstants.UI_GROUP_MANAGE_NOTIFY);
            filter.addAction(BroadcastConstants.UI_GROUP_CHAT_SUBJECT_CHANGE);
            filter.addAction(BroadcastConstants.UI_INVITE_TO_JOIN_GROUP);
            filter.addAction(BroadcastConstants.UI_SHOW_GROUP_MESSAGE_NOTIFY);
            filter.addAction(BroadcastConstants.UI_SHOW_GROUP_REFER_ERROR);
        }
        registerReceiver(mRcsServiceCallbackReceiver, filter);
        IntentFilter photoUpdateFilter = new IntentFilter(RcsContactsUtils
                .NOTIFY_CONTACT_PHOTO_CHANGE);
        registerReceiver(mPhotoUpdateReceiver, photoUpdateFilter);
    }

    private void showSubjectEditor(boolean show) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("" + show);
        }

        if (mSubjectTextEditor == null) {
            // Don't bother to initialize the subject editor if
            // we're just going to hide it.
            if (show == false) {
                return;
            }
            mSubjectTextEditor = (EditText)findViewById(R.id.subject);
            mSubjectTextEditor.setFilters(new InputFilter[] {
                    new LengthFilter(SUBJECT_MAX_LENGTH)});
        }

        mSubjectTextEditor.setOnKeyListener(show ? mSubjectKeyListener : null);

        if (show) {
            mSubjectTextEditor.addTextChangedListener(mSubjectEditorWatcher);
        } else {
            mSubjectTextEditor.removeTextChangedListener(mSubjectEditorWatcher);
        }

        mSubjectTextEditor.setText(mWorkingMessage.getSubject());
        mSubjectTextEditor.setVisibility(show ? View.VISIBLE : View.GONE);
        hideOrShowTopPanel();
    }

    private void hideOrShowTopPanel() {
        boolean anySubViewsVisible = (isSubjectEditorVisible() || isRecipientsEditorVisible());
        mTopPanel.setVisibility(anySubViewsVisible ? View.VISIBLE : View.GONE);
    }

    public void initialize(Bundle savedInstanceState, long originalThreadId) {
        // Create a new empty working message.
        mWorkingMessage = WorkingMessage.createEmpty(this);

        enableMmsData = getResources().getBoolean(
                com.android.internal.R.bool.config_setup_mms_data);

        // Read parameters or previously saved state of this activity. This will load a new
        // mConversation
        initActivityState(savedInstanceState);

        // Init the RCS components.
        initRcsComponents();

        if (LogTag.SEVERE_WARNING && originalThreadId != 0 &&
                originalThreadId == mConversation.getThreadId()) {
            LogTag.warnPossibleRecipientMismatch("ComposeMessageActivity.initialize: " +
                    " threadId didn't change from: " + originalThreadId, this);
        }

        log("savedInstanceState = " + savedInstanceState +
            " intent = " + getIntent() +
            " mConversation = " + mConversation);

        if (MessageUtils.cancelFailedToDeliverNotification(getIntent(), this)) {
            // Show a pop-up dialog to inform user the message was
            // failed to deliver.
            undeliveredMessageDialog(getMessageDate(null));
        }
        MessageUtils.cancelFailedDownloadNotification(getIntent(), this);

        // Set up the message history ListAdapter
        initMessageList();

        mShouldLoadDraft = true;

        // Load the draft for this thread, if we aren't already handling
        // existing data, such as a shared picture or forwarded message.
        boolean isForwardedMessage = false;
        // We don't attempt to handle the Intent.ACTION_SEND when saveInstanceState is non-null.
        // saveInstanceState is non-null when this activity is killed. In that case, we already
        // handled the attachment or the send, so we don't try and parse the intent again.
        if (savedInstanceState == null && (handleSendIntent() || handleForwardedMessage())) {
            mShouldLoadDraft = false;
        }

        // Let the working message know what conversation it belongs to
        mWorkingMessage.setConversation(mConversation);

        handleResendMessage();

        // Show the recipients editor if we don't have a valid thread. Hide it otherwise.
        if (mConversation.getThreadId() <= 0
                && (!mConversation.isGroupChat() || mConversation.getGroupChat() == null)) {
            // Hide the recipients editor so the call to initRecipientsEditor won't get
            // short-circuited.
            hideRecipientEditor();
            initRecipientsEditor();
        } else {
            hideRecipientEditor();
        }

        updateSendButtonState();

        drawTopPanel(false);
        if (!mShouldLoadDraft) {
            // We're not loading a draft, so we can draw the bottom panel immediately.
            drawBottomPanel();
        }

        onKeyboardStateChanged();

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("update title, mConversation=" + mConversation.toString());
        }

        updateTitle(mConversation.getRecipients());

        if (isForwardedMessage && isRecipientsEditorVisible()) {
            // The user is forwarding the message to someone. Put the focus on the
            // recipient editor rather than in the message editor.
            mRecipientsEditor.requestFocus();
        }

        mMsgListAdapter.setIsGroupConversation(mConversation.getRecipients().size() > 1);
        GroupChatModel groupChat = mConversation.getGroupChat();
        if (groupChat != null) {
            mMsgListAdapter.setRcsGroupId(groupChat.getId());
        }
    }

    private void handleResendMessage(){
        // In mailbox mode, click sent failed message in outbox folder, re-send message.
        Intent intent = getIntent();
        boolean needResend = intent.getBooleanExtra(NEED_RESEND, false);
        if (!needResend) {
            return;
        }
        long messageId = intent.getLongExtra(MESSAGE_ID, 0);
        String messageType = intent.getStringExtra(MESSAGE_TYPE);
        if (messageId != 0 && !TextUtils.isEmpty(messageType)) {
            if ("sms".equals(messageType)) {
                String messageBody = intent.getStringExtra(MESSAGE_BODY);
                editSmsMessageItem(messageId, messageBody);
                drawBottomPanel();
                invalidateOptionsMenu();
                return;
            } else if ("mms".equals(messageType)) {
                Uri messageUri = ContentUris.withAppendedId(Mms.CONTENT_URI, messageId);
                String messageSubject = "";
                String subject = intent.getStringExtra(MESSAGE_SUBJECT);
                if (!TextUtils.isEmpty(subject)) {
                    int subjectCharset = intent.getIntExtra(MESSAGE_SUBJECT_CHARSET, 0);
                    EncodedStringValue v = new EncodedStringValue(subjectCharset,
                            PduPersister.getBytes(subject));
                    messageSubject = MessageUtils.cleanseMmsSubject(this, v.getString());
                }
                editMmsMessageItem(messageUri, messageSubject);
                drawBottomPanel();
                invalidateOptionsMenu();
                return;
            }
        }
    }

    private void resetEditorText() {
        // We have to remove the text change listener while the text editor gets cleared and
        // we subsequently turn the message back into SMS. When the listener is listening while
        // doing the clearing, it's fighting to update its counts and itself try and turn
        // the message one way or the other.
        mTextEditor.removeTextChangedListener(mTextEditorWatcher);
        // Clear the text box.
        TextKeyListener.clear(mTextEditor.getText());
        mTextEditor.addTextChangedListener(mTextEditorWatcher);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        Conversation conversation = null;
        mSentMessage = false;

        // If we have been passed a thread_id, use that to find our
        // conversation.

        // Note that originalThreadId might be zero but if this is a draft and we save the
        // draft, ensureThreadId gets called async from WorkingMessage.asyncUpdateDraftSmsMessage
        // the thread will get a threadId behind the UI thread's back.
        long originalThreadId = mConversation.getThreadId();
        long threadId = intent.getLongExtra(THREAD_ID, 0);
        boolean needReload = intent.getBooleanExtra(MessageUtils.EXTRA_KEY_NEW_MESSAGE_NEED_RELOAD,
                false);
        Uri intentUri = intent.getData();

        boolean sameThread = false;
        if (threadId > 0) {
            conversation = Conversation.get(this, threadId, false);
        } else {
            if (mConversation.getThreadId() == 0) {
                // We've got a draft. Make sure the working recipients are synched
                // to the conversation so when we compare conversations later in this function,
                // the compare will work.
                mWorkingMessage.syncWorkingRecipients();
            }
            // Get the "real" conversation based on the intentUri. The intentUri might specify
            // the conversation by a phone number or by a thread id. We'll typically get a threadId
            // based uri when the user pulls down a notification while in ComposeMessageActivity and
            // we end up here in onNewIntent. mConversation can have a threadId of zero when we're
            // working on a draft. When a new message comes in for that same recipient, a
            // conversation will get created behind CMA's back when the message is inserted into
            // the database and the corresponding entry made in the threads table. The code should
            // use the real conversation as soon as it can rather than finding out the threadId
            // when sending with "ensureThreadId".
            conversation = Conversation.get(this, intentUri, false);
        }

        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("onNewIntent: data=" + intentUri + ", thread_id extra is " + threadId +
                    ", new conversation=" + conversation + ", mConversation=" + mConversation);
        }

        // this is probably paranoid to compare both thread_ids and recipient lists,
        // but we want to make double sure because this is a last minute fix for Froyo
        // and the previous code checked thread ids only.
        // (we cannot just compare thread ids because there is a case where mConversation
        // has a stale/obsolete thread id (=1) that could collide against the new thread_id(=1),
        // even though the recipient lists are different)
        sameThread = ((conversation.getThreadId() == mConversation.getThreadId() ||
                mConversation.getThreadId() == 0) &&
                conversation.equals(mConversation));

        if (sameThread) {
            log("onNewIntent: same conversation");
            if (mConversation.getThreadId() == 0) {
                mConversation = conversation;
                mWorkingMessage.setConversation(mConversation);
                updateThreadIdIfRunning();
                invalidateOptionsMenu();
            }
        } else {
            if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("onNewIntent: different conversation");
            }
            if (needReload) {
                mMessagesAndDraftLoaded = false;
            }
            saveDraft(false);    // if we've got a draft, save it first
            resetEditorText();
            initialize(null, originalThreadId);
        }
        loadMessagesAndDraft(0);
    }

    private void sanityCheckConversation() {
        if (mWorkingMessage.getConversation() != mConversation) {
            LogTag.warnPossibleRecipientMismatch(
                    "ComposeMessageActivity: mWorkingMessage.mConversation=" +
                    mWorkingMessage.getConversation() + ", mConversation=" +
                    mConversation + ", MISMATCH!", this);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (mWorkingMessage.isDiscarded()) {
            // If the message isn't worth saving, don't resurrect it. Doing so can lead to
            // a situation where a new incoming message gets the old thread id of the discarded
            // draft. This activity can end up displaying the recipients of the old message with
            // the contents of the new message. Recognize that dangerous situation and bail out
            // to the ConversationList where the user can enter this in a clean manner.
            if (mWorkingMessage.isWorthSaving() || mInAsyncAddAttathProcess) {
                if (LogTag.VERBOSE) {
                    log("onRestart: mWorkingMessage.unDiscard()");
                }
                mWorkingMessage.unDiscard();    // it was discarded in onStop().

                sanityCheckConversation();
            } else if (isRecipientsEditorVisible() && recipientCount() > 0) {
                if (LogTag.VERBOSE) {
                    log("onRestart: goToConversationList");
                }
                goToConversationList();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean isSmsEnabled = MmsConfig.isSmsEnabled(this);
        if (isSmsEnabled != mIsSmsEnabled) {
            mIsSmsEnabled = isSmsEnabled;
            invalidateOptionsMenu();
        }

        initFocus();

        // Register a BroadcastReceiver to listen on HTTP I/O process.
        registerReceiver(mHttpProgressReceiver, mHttpProgressFilter);

        // Register a BroadcastReceiver to listen on SD card state.
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        f.addAction(Intent.ACTION_MEDIA_REMOVED);
        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        f.addDataScheme(BROADCAST_DATA_SCHEME);
        registerReceiver(mMediaStateReceiver, f);

        // register Rcs fileTransfer
        IntentFilter fileFilter = new IntentFilter();
        fileFilter.addAction(BroadcastConstants.UI_DOWNLOADING_FILE_CHANGE);
        fileFilter.addAction(BroadcastConstants.FILE_TRANSFER_PROGRESS);
        fileFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mFileTranferReceiver, fileFilter);
        registerReceiver(mGroupReceiver, new IntentFilter(BroadcastConstants.UI_GROUP_MANAGE_NOTIFY));

        IntentFilter cloudFileFilter = new IntentFilter();
        cloudFileFilter.addAction(BroadcastConstants.UI_MC_DOWNLOAD_FILE_FROM_URL);
        cloudFileFilter.addAction(BroadcastConstants.UI_MC_SHARE_AND_SEND_FILE);
        cloudFileFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mCloudFileReceiver, cloudFileFilter);
        IntentFilter emotionFilter = new IntentFilter();
        emotionFilter.addAction(BroadcastConstants.UI_MESSAGE_PAID_EMO_DOWNLOAD_RESULT);
        registerReceiver(mEmotionDownloadReceiver, emotionFilter);

        // figure out whether we need to show the keyboard or not.
        // if there is draft to be loaded for 'mConversation', we'll show the keyboard;
        // otherwise we hide the keyboard. In any event, delay loading
        // message history and draft (controlled by DEFER_LOADING_MESSAGES_AND_DRAFT).
        int mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

        if (DraftCache.getInstance().hasDraft(mConversation.getThreadId())) {
            mode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
        } else if (mConversation.getThreadId() <= 0) {
            // For composing a new message, bring up the softkeyboard so the user can
            // immediately enter recipients. This call won't do anything on devices with
            // a hard keyboard.
            mode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
        } else {
            mode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        }

        getWindow().setSoftInputMode(mode);

        // reset mMessagesAndDraftLoaded
        mMessagesAndDraftLoaded = false;

        CharSequence text = mWorkingMessage.getText();
        if (text != null) {
            mTextEditor.setTextKeepState(text);
        }
        if (!DEFER_LOADING_MESSAGES_AND_DRAFT) {
            loadMessagesAndDraft(1);
        } else {
            // HACK: force load messages+draft after max delay, if it's not already loaded.
            // this is to work around when coming out of sleep mode. WindowManager behaves
            // strangely and hides the keyboard when it should be shown, or sometimes initially
            // shows it when we want to hide it. In that case, we never get the onSizeChanged()
            // callback w/ keyboard shown, so we wouldn't know to load the messages+draft.
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    loadMessagesAndDraft(2);
                }
            }, LOADING_MESSAGES_AND_DRAFT_MAX_DELAY_MS);
        }

        // Update the fasttrack info in case any of the recipients' contact info changed
        // while we were paused. This can happen, for example, if a user changes or adds
        // an avatar associated with a contact.
        mWorkingMessage.syncWorkingRecipients();

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("update title, mConversation=" + mConversation.toString());
        }

        updateTitle(mConversation.getRecipients());

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (rcsShareVcardAddNumber) {
            launchMultiplePhonePicker();
            rcsShareVcardAddNumber = false;
        }
    }

    public void loadMessageContent() {
        // Don't let any markAsRead DB updates occur before we've loaded the messages for
        // the thread. Unblocking occurs when we're done querying for the conversation
        // items.
        mConversation.blockMarkAsRead(true);
        mConversation.markAsRead();         // dismiss any notifications for this convo
        startMsgListQuery();
        updateSendFailedNotification();
    }

    /**
     * Load message history and draft. This method should be called from main thread.
     * @param debugFlag shows where this is being called from
     */
    private void loadMessagesAndDraft(int debugFlag) {
        if (!mSendDiscreetMode && !mMessagesAndDraftLoaded) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                Log.v(TAG, "### CMA.loadMessagesAndDraft: flag=" + debugFlag);
            }
            loadMessageContent();
            boolean drawBottomPanel = true;
            long threadId = mWorkingMessage.getConversation().getThreadId();
            // Do not load draft when forwarding to the same recipients.
            if (mShouldLoadDraft && !MessageUtils.sSameRecipientList.contains(threadId)) {
                if (loadDraft()) {
                    drawBottomPanel = false;
                }
            }
            if (drawBottomPanel) {
                drawBottomPanel();
            }
            mMessagesAndDraftLoaded = true;
        }
    }

    private void updateSendFailedNotification() {
        final long threadId = mConversation.getThreadId();
        if (threadId <= 0)
            return;

        // updateSendFailedNotificationForThread makes a database call, so do the work off
        // of the ui thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                MessagingNotification.updateSendFailedNotificationForThread(
                        ComposeMessageActivity.this, threadId);
            }
        }, "ComposeMessageActivity.updateSendFailedNotification").start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(RECIPIENTS, getRecipients().serialize());

        mWorkingMessage.writeStateToBundle(outState);

        if (mSendDiscreetMode) {
            outState.putBoolean(KEY_EXIT_ON_SENT, mSendDiscreetMode);
        }
        if (mForwardMessageMode) {
            outState.putBoolean(KEY_FORWARDED_MESSAGE, mForwardMessageMode);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // OLD: get notified of presence updates to update the titlebar.
        // NEW: we are using ContactHeaderWidget which displays presence, but updating presence
        //      there is out of our control.
        //Contact.startPresenceObserver();

        mIsPickingContact = false;
        addRecipientsListeners();

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("update title, mConversation=" + mConversation.toString());
        }

        // There seems to be a bug in the framework such that setting the title
        // here gets overwritten to the original title.  Do this delayed as a
        // workaround.
        mMessageListItemHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ContactList recipients = isRecipientsEditorVisible() ?
                        mRecipientsEditor.constructContactsFromInput(false) : getRecipients();
                updateTitle(recipients);
            }
        }, 100);

        mIsRunning = true;

        // refresh autotext state after adding word to dictionary
        if (mTextEditor.isCursorVisible()) {
            mTextEditor.setText(mTextEditor.getText());
        }
        if (mSubjectTextEditor != null && mSubjectTextEditor.isCursorVisible()) {
            mSubjectTextEditor.setText(mSubjectTextEditor.getText());
        }
        updateThreadIdIfRunning();
        mConversation.markAsRead();
        mIsAirplaneModeOn = MessageUtils.isAirplaneModeOn(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (DEBUG) {
            Log.v(TAG, "onPause: setCurrentlyDisplayedThreadId: " +
                        MessagingNotification.THREAD_NONE);
        }
        MessagingNotification.setCurrentlyDisplayedThreadId(MessagingNotification.THREAD_NONE);

        // OLD: stop getting notified of presence updates to update the titlebar.
        // NEW: we are using ContactHeaderWidget which displays presence, but updating presence
        //      there is out of our control.
        //Contact.stopPresenceObserver();

        removeRecipientsListeners();

        // remove any callback to display a progress spinner
        if (mAsyncDialog != null) {
            mAsyncDialog.clearPendingProgressDialog();
        }

        // Remember whether the list is scrolled to the end when we're paused so we can rescroll
        // to the end when resumed.
        if (mMsgListAdapter != null &&
                mMsgListView.getLastVisiblePosition() >= mMsgListAdapter.getCount() - 1) {
            mSavedScrollPosition = Integer.MAX_VALUE;
        } else {
            mSavedScrollPosition = mMsgListView.getFirstVisiblePosition();
        }
        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.v(TAG, "onPause: mSavedScrollPosition=" + mSavedScrollPosition);
        }

        mConversation.markAsRead();
        mIsRunning = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        // No need to do the querying when finished this activity
        mBackgroundQueryHandler.cancelOperation(MESSAGE_LIST_QUERY_TOKEN);

        // Allow any blocked calls to update the thread's read status.
        mConversation.blockMarkAsRead(false);

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("save draft");
        }
        saveDraft(true);

        // set 'mShouldLoadDraft' to true, so when coming back to ComposeMessageActivity, we would
        // load the draft, unless we are coming back to the activity after attaching a photo, etc,
        // in which case we should set 'mShouldLoadDraft' to false.
        mShouldLoadDraft = true;

        // Cleanup the BroadcastReceiver.
        unregisterReceiver(mHttpProgressReceiver);
        unregisterReceiver(mMediaStateReceiver);

        if (mAttachmentSelector.getVisibility() == View.VISIBLE) {
            mAttachmentSelector.setVisibility(View.GONE);
        }
        unregisterReceiver(mFileTranferReceiver);
        unregisterReceiver(mGroupReceiver);
        unregisterReceiver(mCloudFileReceiver);
        unregisterReceiver(mEmotionDownloadReceiver);
    }

    @Override
    protected void onDestroy() {
        if (TRACE) {
            android.os.Debug.stopMethodTracing();
        }

        if (mMsgListAdapter != null) {
            mMsgListAdapter.changeCursor(null);
            mMsgListAdapter.cancelBackgroundLoading();
        }

        unregisterReceiver(mRcsServiceCallbackReceiver);
        unregisterReceiver(mPhotoUpdateReceiver);
        unregisterReceiver(mAirplaneModeBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (resetConfiguration(newConfig)) {
            // Have to re-layout the attachment editor because we have different layouts
            // depending on whether we're portrait or landscape.
            drawTopPanel(isSubjectEditorVisible());
        }
        if (LOCAL_LOGV) {
            Log.v(TAG, "CMA.onConfigurationChanged: " + newConfig +
                    ", mIsKeyboardOpen=" + mIsKeyboardOpen);
        }
        onKeyboardStateChanged();

        // If locale changed, we need reload the source of mInvalidRecipientDialog's
        // title and message from xml file.
        if (mInvalidRecipientDialog != null && mInvalidRecipientDialog.isShowing()) {
            mInvalidRecipientDialog.dismiss();
            showInvalidRecipientDialog();
        }
        mInvalidRecipientDialog = null;
        if (mAttachmentSelector.getVisibility() == View.VISIBLE) {
            setAttachmentSelectorHeight();
            resetGridColumnsCount();
        }
    }

    // returns true if landscape/portrait configuration has changed
    private boolean resetConfiguration(Configuration config) {
        mIsKeyboardOpen = config.keyboardHidden == KEYBOARDHIDDEN_NO;
        boolean isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            return true;
        }
        return false;
    }

    private void onKeyboardStateChanged() {
        // If the keyboard is hidden, don't show focus highlights for
        // things that cannot receive input.
        mTextEditor.setEnabled(mIsSmsEnabled);
        if (!mIsSmsEnabled) {
            if (mRecipientsEditor != null) {
                mRecipientsEditor.setFocusableInTouchMode(false);
            }
            if (mSubjectTextEditor != null) {
                mSubjectTextEditor.setFocusableInTouchMode(false);
            }
            mTextEditor.setFocusableInTouchMode(false);
            mTextEditor.setHint(R.string.sending_disabled_not_default_app);
        } else if (mIsKeyboardOpen) {
            if (mRecipientsEditor != null) {
                mRecipientsEditor.setFocusableInTouchMode(true);
            }
            if (mSubjectTextEditor != null) {
                mSubjectTextEditor.setFocusableInTouchMode(true);
            }
            mTextEditor.setFocusableInTouchMode(true);
            mTextEditor.setHint(R.string.type_to_compose_text_enter_to_send);
        } else {
            if (mRecipientsEditor != null) {
                mRecipientsEditor.setFocusable(false);
            }
            if (mSubjectTextEditor != null) {
                mSubjectTextEditor.setFocusable(false);
            }
            mTextEditor.setFocusable(false);
            mTextEditor.setHint(R.string.open_keyboard_to_compose_message);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                if ((mMsgListAdapter != null) && mMsgListView.isFocused()) {
                    Cursor cursor;
                    try {
                        cursor = (Cursor) mMsgListView.getSelectedItem();
                    } catch (ClassCastException e) {
                        Log.e(TAG, "Unexpected ClassCastException.", e);
                        return super.onKeyDown(keyCode, event);
                    }

                    if (cursor != null) {
                        String type = cursor.getString(COLUMN_MSG_TYPE);
                        long msgId = cursor.getLong(COLUMN_ID);
                        MessageItem msgItem = mMsgListAdapter.getCachedMessageItem(type, msgId,
                                cursor);
                        if (msgItem != null) {
                            DeleteMessageListener l = new DeleteMessageListener(msgItem);
                            confirmDeleteDialog(l, msgItem.mLocked);
                        }
                        return true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isPreparedForSending()) {
                    confirmSendMessageIfNeeded();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                exitComposeMessageActivity(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void exitComposeMessageActivity(final Runnable exit) {
        // If the message is empty, just quit -- finishing the
        // activity will cause an empty draft to be deleted.
        if (!mWorkingMessage.isWorthSaving()) {

            // If is from SearchActivity, need set ResultCode is RESULT_OK
            if (mIsFromSearchActivity) {
                // If the msg database has changed (eg. delete or new message),
                // we should set a result let SearchActivity refresh view.
                setResult(mIsMessageChanged ?
                        SearchActivity.RESULT_MSG_HAS_CHANGED : RESULT_OK, null);
                mIsMessageChanged = false;
            }
            exit.run();
            mWorkingMessage.discard();
            new Thread() {
                @Override
                public void run() {
                    // Remove the obsolete threads in database.
                    getContentResolver().delete(
                            android.provider.Telephony.Threads.OBSOLETE_THREADS_URI, null, null);
                }
            }.start();
            return;
        }

        // If the recipient is empty, the meesgae shouldn't be saved, and should pop up the
        // confirm delete dialog.
        if (isRecipientEmpty()) {
            // If mRecipientsEditor is empty we need show empty info.
            int validNum = MessageUtils.ALL_RECIPIENTS_EMPTY;
            if (!TextUtils.isEmpty(mRecipientsEditor.getText())) {
                validNum = mRecipientsEditor
                        .getValidRecipientsCount(mWorkingMessage.requiresMms());
            }
            MessageUtils.showDiscardDraftConfirmDialog(this,
                    new DiscardDraftListener(), validNum);

            return;
        }

        mToastForDraftSave = true;

        // If is from SearchActivity, and save sms draft,
        // need set ResultCode is SearchActivity.RESULT_SAVE_SMS_DRAFT
        if (mIsFromSearchActivity) {
            if (mWorkingMessage.hasAttachment() || mWorkingMessage.hasSubject()) {
                this.setResult(SearchActivity.RESULT_SAVE_MMS_DRAFT, null);
            } else {
                this.setResult(SearchActivity.RESULT_SAVE_SMS_DRAFT, null);
            }
        }
        exit.run();
    }

    private boolean isRecipientEmpty() {
        return isRecipientsEditorVisible()
                && (mRecipientsEditor.getValidRecipientsCount(mWorkingMessage.requiresMms())
                != MessageUtils.ALL_RECIPIENTS_VALID
                || (0 == mRecipientsEditor.getRecipientCount()));
    }

    private void goToConversationList() {
        finish();
        startActivity(new Intent(this, ConversationList.class));
    }

    private void hideRecipientEditor() {
        if (mRecipientsEditor != null) {
            mRecipientsEditor.removeTextChangedListener(mRecipientsWatcher);
            mRecipientsEditor.setVisibility(View.GONE);
            hideOrShowTopPanel();
        }
    }

    private boolean isRecipientsEditorVisible() {
        return (null != mRecipientsEditor)
                    && (View.VISIBLE == mRecipientsEditor.getVisibility());
    }

    private boolean isSubjectEditorVisible() {
        return (null != mSubjectTextEditor)
                    && (View.VISIBLE == mSubjectTextEditor.getVisibility());
    }

    @Override
    public void onAttachmentChanged() {
        // Have to make sure we're on the UI thread. This function can be called off of the UI
        // thread when we're adding multi-attachments
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawBottomPanel();
                updateSendButtonState();
                drawTopPanel(isSubjectEditorVisible());
            }
        });
    }

    @Override
    public void onProtocolChanged(final boolean convertToMms) {
        // Have to make sure we're on the UI thread. This function can be called off of the UI
        // thread when we're adding multi-attachments
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                 /* commented for no-touch feature phone*/
                /*if (mShowTwoButtons) {
                    showTwoSmsOrMmsSendButton(convertToMms);
                } else {
                    showSmsOrMmsSendButton(convertToMms);
                }*/

                if (convertToMms) {
                    // In the case we went from a long sms with a counter to an mms because
                    // the user added an attachment or a subject, hide the counter --
                    // it doesn't apply to mms.
                    mTextCounter.setVisibility(View.GONE);
                    showConvertToMmsToast();
                } else {
                    mTextCounter.setVisibility(View.VISIBLE);
                    showConvertToSmsToast();
                }
            }
        });
    }
     /* commented for no-touch feature phone*/
    // Show or hide the Sms or Mms button as appropriate. Return the view so that the caller
    // can adjust the enableness and focusability.
    /*private View showSmsOrMmsSendButton(boolean isMms) {
        View showButton;
        View hideButton;
        if (isMms) {
            showButton = mSendButtonMms;
            hideButton = mSendButtonSms;
        } else {
            showButton = mSendButtonSms;
            hideButton = mSendButtonMms;
        }
        showButton.setVisibility(View.VISIBLE);
        hideButton.setVisibility(View.GONE);

        return showButton;
    }

    private View[] showTwoSmsOrMmsSendButton(boolean isMms) {
        View[] showButton = new View[NUMBER_OF_BUTTONS];
        View[] hideButton = new View[NUMBER_OF_BUTTONS];
        if (isMms) {
            showButton[MSimConstants.SUB1] = mSendLayoutMmsFir;
            showButton[MSimConstants.SUB2] = mSendLayoutMmsSec;
            hideButton[MSimConstants.SUB1] = mSendLayoutSmsFir;
            hideButton[MSimConstants.SUB2] = mSendLayoutSmsSec;
        } else {
            showButton[MSimConstants.SUB1] = mSendLayoutSmsFir;
            showButton[MSimConstants.SUB2] = mSendLayoutSmsSec;
            hideButton[MSimConstants.SUB1] = mSendLayoutMmsFir;
            hideButton[MSimConstants.SUB2] = mSendLayoutMmsSec;
        }
        showButton[MSimConstants.SUB1].setVisibility(View.VISIBLE);
        showButton[MSimConstants.SUB2].setVisibility(View.VISIBLE);
        hideButton[MSimConstants.SUB1].setVisibility(View.GONE);
        hideButton[MSimConstants.SUB2].setVisibility(View.GONE);

        return showButton;
    }*/

    Runnable mResetRcsMessageRunnable = new Runnable() {
        @Override
        public void run() {
            resetRcsMessage();
        }
    };

    private void resetRcsMessage() {
        mAttachmentEditor.hideView();
        mAttachmentEditorScrollView.setVisibility(View.GONE);

        showSubjectEditor(false);
        CharSequence text = mWorkingMessage.getText();
        mWorkingMessage.clearConversation(mConversation, false);
        mWorkingMessage = WorkingMessage.createEmpty(this);
        if (text != null) {
            mWorkingMessage.setText(text);
        }
        mWorkingMessage.setConversation(mConversation);
        hideRecipientEditor();
        updateSendButtonState();

        if (mIsLandscape) {
            hideKeyboard();
        }

        mLastRecipientCount = 0;
        mSendingMessage = false;
        invalidateOptionsMenu();
   }

    Runnable mResetMessageRunnable = new Runnable() {
        @Override
        public void run() {
            resetMessage();
        }
    };

    @Override
    public void onPreRcsMessageSent() {
        runOnUiThread(mResetRcsMessageRunnable);
    }

    @Override
    public void onPreMessageSent() {
        runOnUiThread(mResetMessageRunnable);
    }

    @Override
    public void onMessageSent() {
        // This callback can come in on any thread; put it on the main thread to avoid
        // concurrency problems
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // If we already have messages in the list adapter, it
                // will be auto-requerying; don't thrash another query in.
                // TODO: relying on auto-requerying seems unreliable when priming an MMS into the
                // outbox. Need to investigate.
//                if (mMsgListAdapter.getCount() == 0) {
                    if (LogTag.VERBOSE) {
                        log("onMessageSent");
                    }
                    startMsgListQuery();
//                }

                // The thread ID could have changed if this is a new message that we just inserted
                // into the database (and looked up or created a thread for it)
                updateThreadIdIfRunning();

                // If there is saved message waiting to be edited, edit it now.
                if (null != mEditMessageItem) {
                    editMessageItem(mEditMessageItem);
                    drawBottomPanel();
                    mEditMessageItem = null;
                }
            }
        });
    }

    @Override
    public void onMaxPendingMessagesReached() {
        saveDraft(false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ComposeMessageActivity.this, R.string.too_many_unsent_mms,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onAttachmentError(final int error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIsAttachmentErrorOnSend = true;
                handleAddAttachmentError(error, R.string.type_picture);
                onMessageSent();        // now requery the list of messages
            }
        });
    }

    // We don't want to show the "call" option unless there is only one
    // recipient and it's a phone number.
    private boolean isRecipientCallable() {
        ContactList recipients = getRecipients();
        return (recipients.size() == 1 && !recipients.containsEmail()
                && !(MessageUtils.isWapPushNumber(recipients.get(0).getNumber())));
    }

    private String getNumbersExceptMe() throws ServiceDisconnectedException {
        String myPhoneNumber = mAccountApi.getRcsUserProfileInfo().getUserName();
        String numbers = "";

        ContactList recipients = getRecipients();
        int size = recipients.size();
        for (int i = 0; i < size; i++) {
            String number = recipients.get(i).getNumber();

            // Skip my phone number.
            if (myPhoneNumber != null && myPhoneNumber.endsWith(number)) {
                continue;
            }

            numbers += number;
            if (i + 1 < size) {
                numbers += ";";
            }
        }

        return numbers;
    }

    private String getGroupChatDialNumbers() throws ServiceDisconnectedException {
        String numbers = "";
        GroupChatModel groupChat = mConversation.getGroupChat();
        if (groupChat != null) {
            List<GroupChatUser> users = groupChat.getUserList();
            if (users != null) {
                String myPhoneNumber = mAccountApi.getRcsUserProfileInfo().getUserName();

                int size = users.size();
                for (int i = 0; i < size; i++) {
                    String number = users.get(i).getNumber();

                    // Skip my phone number.
                    if (myPhoneNumber != null && myPhoneNumber.endsWith(number)) {
                        continue;
                    }

                    numbers += number;
                    if (i + 1 < size) {
                        numbers += ";";
                    }
                }
            }
        }

        return numbers;
    }

    private void dialConferenceCall() {
        try {
            String dialNumbers = getNumbersExceptMe();
            RcsUtils.onShowConferenceCallStartScreen(this, dialNumbers);
        } catch (Exception e) {
            RcsUtils.onShowConferenceCallStartScreen(this);
        }
    }

    private void dialGroupChat() {
        try {
            String dialNumbers = getGroupChatDialNumbers();
            RcsUtils.onShowConferenceCallStartScreen(this, dialNumbers);
        } catch (Exception e) {
            RcsUtils.onShowConferenceCallStartScreen(this);
        }
    }

    private void dialRecipient() {
        if (isRecipientCallable()) {
            if (mConversation != null && mConversation.isGroupChat()) {
                dialGroupChat();
            } else {
                ContactList recipients = getRecipients();
                int size = recipients.size();
                if (size > 1) {
                    dialConferenceCall();
                } else if (size == 1) {
                    String number = getRecipients().get(0).getNumber();
                    if (mIsRcsEnabled && RcsUtils.isDeletePrefixSpecailNumberAvailable(this)) {
                        try {
                            number = RcsApiManager.getSpecialServiceNumApi()
                                    .delSpecialPreNum(number);
                        } catch (ServiceDisconnectedException e){
                            Log.i(RCS_TAG,"delSpecialPreNum error");
                        }
                    }
                    MessageUtils.dialNumber(this, number);
                }
            }
        }
    }

    private void dialRecipient(int subscription) {
        if (isRecipientCallable()) {
            if (mConversation != null && mConversation.isGroupChat()) {
                dialGroupChat();
            } else {
                ContactList recipients = getRecipients();
                int size = recipients.size();
                if (size > 1) {
                    dialConferenceCall();
                } else if (size == 1) {
                    String number = getRecipients().get(0).getNumber();
                    MessageUtils.dialRecipient(this, number, subscription);
                }
            }
        }
    }

    private void initTwoCallButtonOnActionBar() {
        ActionBar mActionBar = getActionBar();
        if (mActionBar == null) {
            return;
        }
        // Configure action bar.
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
               | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);

        // Prepare the custom view
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.action_bar_call_button, null);
        mActionBar.setCustomView(view, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.RIGHT));

        mIndicatorContainer1 = (View)view.findViewById(R.id.indicatorContainer1);
        mIndicatorContainer2 = (View)view.findViewById(R.id.indicatorContainer2);
        mIndicatorForSim1 = (ImageView)view.findViewById(R.id.sim_card_indicator1);
        mIndicatorForSim2 = (ImageView)view.findViewById(R.id.sim_card_indicator2);

        View mCallSim1 = view.findViewById(R.id.indicatorContainer1);
        mCallSim1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialRecipient(MessageUtils.SUB1);
            }
        });

        View mCallSim2 = view.findViewById(R.id.indicatorContainer2);
        mCallSim2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialRecipient(MessageUtils.SUB2);
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu) ;

        menu.clear();

        if (mSendDiscreetMode && !mForwardMessageMode && !mReplyMessageMode) {
            // When we're in send-a-single-message mode from the lock screen, don't show
            // any menus.
            return true;
        }

        if (isRecipientCallable()) {
            boolean showTwoCallButton = getResources().getBoolean(R.bool.config_two_call_button);
            int phoneCount = MSimTelephonyManager.getDefault().getPhoneCount();
            if (showTwoCallButton && MessageUtils.isMultiSimEnabledMms()
                    && MessageUtils.getActivatedIccCardCount() >= phoneCount) {
                if (mIndicatorContainer1 != null && mIndicatorContainer2 != null
                        && mIndicatorForSim1 != null && mIndicatorForSim2 != null) {
                    mIndicatorForSim1.setImageDrawable(MessageUtils
                            .getMultiSimIcon(this, MessageUtils.SUB1));
                    mIndicatorForSim2.setImageDrawable(MessageUtils
                            .getMultiSimIcon(this, MessageUtils.SUB2));
                    mIndicatorContainer1.setVisibility(View.VISIBLE);
                    mIndicatorContainer2.setVisibility(View.VISIBLE);
                }
            } else {
                if (mIndicatorContainer1 != null && mIndicatorContainer2 != null) {
                    mIndicatorContainer1.setVisibility(View.GONE);
                    mIndicatorContainer2.setVisibility(View.GONE);
                }
                MenuItem item = menu.add(0, MENU_CALL_RECIPIENT, 0, R.string.menu_call)
                        .setIcon(R.drawable.ic_menu_call)
                        .setTitle(R.string.menu_call);
                if (!isRecipientsEditorVisible()) {
                    // If we're not composing a new message, show the call icon in the actionbar
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }
            }
        } else {
            if (mIndicatorContainer1 != null && mIndicatorContainer2 != null) {
                mIndicatorContainer1.setVisibility(View.GONE);
                mIndicatorContainer2.setVisibility(View.GONE);
            }
        }

        if (MmsConfig.getMmsEnabled() && mIsSmsEnabled) {
            if (!isSubjectEditorVisible()) {
                menu.add(0, MENU_ADD_SUBJECT, 0, R.string.add_subject).setIcon(
                        R.drawable.ic_menu_edit);
            }

            // Attachment Menu Item
            if (showAddAttachementButton()) {
                menu.add(0, MENU_ADD_ATTACHMENT, 0, R.string.add_attachment)
                        .setTitle(R.string.add_attachment)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);    // add to actionbar
            }

            if (mConversation.getIsTop() == 0 && !mTopThread.contains(mConversation.getThreadId())) {
                menu.add(0, MENU_TOP_CONVERSATION, 0, R.string.top_conversation).setIcon(
                        R.drawable.ic_menu_edit);
            } else {
                if (FIRST_LUNCH) {
                    menu.add(0,MENU_CANCEL_TOP_CONVERSATION,0,R.string.cancel_top_conversation)
                            .setIcon(R.drawable.ic_menu_edit);
                } else {
                    if (mTopThread.contains(mConversation.getThreadId())) {
                        menu.add(0, MENU_CANCEL_TOP_CONVERSATION, 0,
                                R.string.cancel_top_conversation).setIcon(R.drawable.ic_menu_edit);
                    } else {
                        menu.add(0, MENU_TOP_CONVERSATION, 0, R.string.top_conversation).setIcon(
                                R.drawable.ic_menu_edit);
                    }
                }
            }
        }

        if(isRecipientsEditorVisible()){
            menu.add(0, MENU_ADD_CONTACT, 0, R.string.add_contact_menu);
            menu.add(0, MENU_ADD_GROUP, 0, R.string.add_group_menu);
        }

        if (isPreparedForSending() && mIsSmsEnabled) {
           /* commented for no-touch feature phone*/
          /* if (mShowTwoButtons) {
                menu.add(0, MENU_SEND_BY_SLOT1, 0, R.string.send_by_slot1)
                        .setIcon(android.R.drawable.ic_menu_send);
                menu.add(0, MENU_SEND_BY_SLOT2, 0, R.string.send_by_slot2)
                        .setIcon(android.R.drawable.ic_menu_send);
            } else {
                menu.add(0, MENU_SEND, 0, R.string.send).setIcon(android.R.drawable.ic_menu_send);
            }*/
            menu.add(0, MENU_SEND, 0, R.string.send);
        }

        if ((isSubjectEditorVisible() && mSubjectTextEditor.isFocused())
                || !mWorkingMessage.hasSlideshow()) {
            menu.add(0, MENU_IMPORT_TEMPLATE, 0, R.string.import_message_template)
                .setIcon(R.drawable.import_sms_template);
        }

        if (getRecipients().size() > 1) {
            menu.add(0, MENU_GROUP_PARTICIPANTS, 0, R.string.menu_group_participants);
        }

        if (mMsgListAdapter.getCount() > 0 && mIsSmsEnabled) {
            // Removed search as part of b/1205708
            //menu.add(0, MENU_SEARCH, 0, R.string.menu_search).setIcon(
            //        R.drawable.ic_menu_search);
            Cursor cursor = mMsgListAdapter.getCursor();
            if ((null != cursor) && (cursor.getCount() > 0)) {
                menu.add(0, MENU_DELETE_THREAD, 0, R.string.delete_thread).setIcon(
                    android.R.drawable.ic_menu_delete);
                menu.add(0, MENU_BATCH_DELETE, 0, R.string.menu_batch_delete);
                if (getResources().getBoolean(R.bool.config_forwardconv)
                        && mMsgListAdapter.hasSmsInConversation(cursor)) {
                    menu.add(0, MENU_FORWARD_CONVERSATION, 0, R.string.menu_forward_conversation);
                }
            }
        } else if (mIsSmsEnabled) {
            menu.add(0, MENU_DISCARD, 0, R.string.discard).setIcon(android.R.drawable.ic_menu_delete);
        }

        // add batch favourite
        if (mMsgListAdapter.getCount() > 0 && mIsSmsEnabled) {
            Cursor cursor = mMsgListAdapter.getCursor();
            if ((null != cursor) && (cursor.getCount() > 0)) {
                menu.add(0, MENU_BATCH_FAVOURITE, 0, R.string.batch_favourite);
            }
        }

        // add batch backup
        if (mMsgListAdapter.getCount() > 0 && mIsSmsEnabled) {
            Cursor cursor = mMsgListAdapter.getCursor();
            if ((null != cursor) && (cursor.getCount() > 0)) {
                menu.add(0, MENU_BATCH_BACKUP, 0, R.string.batch_backup);
            }
        }

        buildAddAddressToContactMenuItem(menu);
        // ADD firewall menu
        if (!mConversation.isGroupChat() && getRecipients().size() == 1 && isFirewallInstalled(this)) {
            menu.add(0, MENU_FIERWALL_ADD_BLACKLIST, 0, getString(R.string.menuid_add_to_black_list));
            menu.add(0, MENU_FIERWALL_ADD_WHITELIST, 0, getString(R.string.menuid_add_to_white_list));
        }

        menu.add(0, MENU_PREFERENCES, 0, R.string.menu_preferences).setIcon(
                android.R.drawable.ic_menu_preferences);

        if (LogTag.DEBUG_DUMP) {
            menu.add(0, MENU_DEBUG_DUMP, 0, R.string.menu_debug_dump);
        }
        if (mIsRcsEnabled) {
            if (mConversation.isGroupChat()) {
                GroupChatModel groupChat = mConversation.getGroupChat();
                if (groupChat != null) {
                    menu.add(0, MENU_RCS_GROUP_CHAT_DETAIL, 0, R.string.rcs_group_chat_detail);
                }
            } else {
                if (getRecipients().size() == 1
                        || (mRecipientsEditor != null && mRecipientsEditor.getNumbers().size() == 1)) {
                    addBurnMessageMenu(menu);
                }

                if (!isRecipientsEditorVisible()) {
                    addSwitchToGroupChatMenuItem(menu);
                }
            }
        }
       if (RcsSupportApi.isRcsPluginInstalled(this)) {
                menu.add(0, MENU_RCS_MCLOUD_SHARE, 0, R.string.rcs_mcloud_share_file);
        }
        return true;
    }

    private void addBurnMessageMenu(Menu menu) {
        try {
            if (mHasBurnCapability && mAccountApi.isOnline()) {
                MenuItem burnMenu = menu.add(0, MENU_RCS_BURN_MESSGEE_FLAG, 0,
                        getString(R.string.burn_message_flag));
                burnMenu.setCheckable(true);
                burnMenu.setChecked(mIsBurnMessage);
                burnMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem arg0) {
                        mIsBurnMessage = !mIsBurnMessage;
                        arg0.setChecked(mIsBurnMessage);
                        mWorkingMessage.setIsBurn(mIsBurnMessage);
                        return false;
                    }
                });
            }
        } catch (ServiceDisconnectedException e) {
            Toast.makeText(ComposeMessageActivity.this, R.string.rcs_service_is_not_available,
                    Toast.LENGTH_LONG).show();
            Log.w(RCS_TAG, e);
        }
    }

    private void addSwitchToGroupChatMenuItem(Menu menu) {
        MenuItem item = menu.add(0, MENU_RCS_SWITCH_TO_GROUP_CHAT, 0,
                getString(R.string.switch_to_group_chat));
        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switchToGroupChat();
                return false;
            }
        });
    }

    private void checkCapability(final String number) {
        try {
            RcsApiManager.getCapabilityApi().findCapabilityByNumber(number,
                    new CapabiltyListener() {
                        @Override
                        public void onCallback(RCSCapabilities capabilities, int resultCode,
                                String resultDesc, String arg3) throws RemoteException {
                            Log.i(RCS_TAG, "resultCode = " + resultCode
                                    + " RCSCapabilities = "
                                    + (capabilities == null ? "null" : capabilities.toString()));
                            if ((resultCode == CAPABILITY_RCS_ONLINE
                                    || resultCode == CAPABILITY_RCS_OFFLINE)
                                    && capabilities != null
                                    && capabilities.isBurnAfterReading()) {
                                mHasBurnCapability = true;
                            } else {
                                mHasBurnCapability = false;
                            }
                        }
                    });
        } catch (ServiceDisconnectedException e1) {
            e1.printStackTrace();
        }
    }

    private void switchToGroupChat() {
        try {
            String numbers = getNumbersExceptMe();
            finish();
            RcsUtils.startCreateGroupChatActivity(ComposeMessageActivity.this, numbers, null);
        } catch (Exception e) {
            Log.w(RCS_TAG, e);
        }
    }

    private void buildAddAddressToContactMenuItem(Menu menu) {
        // bug #7087793: for group of recipients, remove "Add to People" action. Rely on
        // individually creating contacts for unknown phone numbers by touching the individual
        // sender's avatars, one at a time
        ContactList contacts = getRecipients();
        if (contacts.size() != 1) {
            return;
        }

        // if we don't have a contact for the recipient, create a menu item to add the number
        // to contacts.
        Contact c = contacts.get(0);
        if (!c.existsInDatabase() && canAddToContacts(c)) {
            Intent intent = ConversationList.createAddContactIntent(c.getNumber());
            menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, R.string.menu_add_to_contacts)
                .setIcon(android.R.drawable.ic_menu_add)
                .setIntent(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_SUBJECT:
                showSubjectEditor(true);
                mWorkingMessage.setSubject("", true);
                updateSendButtonState();
                mSubjectTextEditor.requestFocus();
                break;
            case MENU_ADD_ATTACHMENT:
                // Launch the add-attachment list dialog
                showAddAttachmentDialog(false);
                break;
            case MENU_ADD_CONTACT:
                launchMultiplePhonePicker();
                break;
            case MENU_ADD_GROUP:
                launchContactGroupPicker();
                break;
            case MENU_DISCARD:
                mWorkingMessage.discard();
                finish();
                break;
            case MENU_SEND:
                if (isPreparedForSending()) {
                    confirmSendMessageIfNeeded();
                }
                break;
            case MENU_SEND_BY_SLOT1:
                if (isPreparedForSending()) {
                    confirmSendMessageIfNeeded(MSimConstants.SUB1);
                }
                break;
            case MENU_SEND_BY_SLOT2:
                if (isPreparedForSending()) {
                    confirmSendMessageIfNeeded(MSimConstants.SUB2);
                }
                break;
            case MENU_SEARCH:
                onSearchRequested();
                break;
            case MENU_DELETE_THREAD:
                confirmDeleteThread(mConversation.getThreadId());
                break;
            case MENU_BATCH_DELETE: {
                Intent intent = new Intent(this, ManageMultiSelectAction.class);
                intent.putExtra(MANAGE_MODE, MessageUtils.BATCH_DELETE_MODE);
                intent.putExtra(THREAD_ID, mConversation.getThreadId());
                startActivityForResult(intent, REQUEST_CODE_BATCH_DELETE);
                break;
            }
            case MENU_BATCH_FAVOURITE: {
                Intent intent = new Intent(this, ManageMultiSelectAction.class);
                intent.putExtra(MANAGE_MODE, MessageUtils.BATCH_FAVOURITE_MODE);
                intent.putExtra(THREAD_ID, mConversation.getThreadId());
                startActivityForResult(intent, REQUEST_CODE_BATCH_FAVOURITE);
                break;
            }
            case MENU_BATCH_BACKUP: {
                Intent intent = new Intent(this, ManageMultiSelectAction.class);
                intent.putExtra(MANAGE_MODE, MessageUtils.BATCH_BACKUP_MODE);
                intent.putExtra(THREAD_ID, mConversation.getThreadId());
                startActivityForResult(intent, REQUEST_CODE_BATCH_BACKUP);
                break;
            }
            case MENU_TOP_CONVERSATION:
                FIRST_LUNCH = false;
                mTopThread.add(mConversation.getThreadId());
                RcsUtils.topConversion(this,mConversation.getThreadId());
                break;
            case MENU_CANCEL_TOP_CONVERSATION:
                FIRST_LUNCH = false;
                if (mTopThread.contains(mConversation.getThreadId())) {
                    mTopThread.remove(mConversation.getThreadId());
                }
                RcsUtils.cancelTopConversion(this, mConversation.getThreadId());
                break;
            case android.R.id.home:
            case MENU_CONVERSATION_LIST:
                exitComposeMessageActivity(new Runnable() {
                    @Override
                    public void run() {
                        goToConversationList();
                    }
                });
                break;
            case MENU_CALL_RECIPIENT:
                dialRecipient();
                break;
            case MENU_FORWARD_CONVERSATION: {
                Intent intent = new Intent(this, ManageMultiSelectAction.class);
                intent.putExtra(MANAGE_MODE, MessageUtils.FORWARD_MODE);
                intent.putExtra(THREAD_ID, mConversation.getThreadId());
                startActivity(intent);
                break;
            }
            case MENU_IMPORT_TEMPLATE:
                showDialog(DIALOG_IMPORT_TEMPLATE);
                break;
            case MENU_GROUP_PARTICIPANTS:
            {
                Intent intent = new Intent(this, RecipientListActivity.class);
                intent.putExtra(THREAD_ID, mConversation.getThreadId());
                startActivity(intent);
                break;
            }
            case MENU_VIEW_CONTACT: {
                // View the contact for the first (and only) recipient.
                ContactList list = getRecipients();
                if (list.size() == 1 && list.get(0).existsInDatabase()) {
                    Uri contactUri = list.get(0).getUri();
                    Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(intent);
                }
                break;
            }
            case MENU_ADD_ADDRESS_TO_CONTACTS:
                mAddContactIntent = item.getIntent();
                startActivityForResult(mAddContactIntent, REQUEST_CODE_ADD_CONTACT);
                break;
            case MENU_PREFERENCES: {
                Intent intent = new Intent(this, MessagingPreferenceActivity.class);
                startActivityIfNeeded(intent, -1);
                break;
            }
            case MENU_DEBUG_DUMP:
                mWorkingMessage.dump();
                Conversation.dump();
                LogTag.dumpInternalTables(this);
                break;
            case MENU_RCS_GROUP_CHAT_DETAIL: { // launch the RCS group chat detail activity.
                GroupChatModel groupChat = mConversation.getGroupChat();
                if (groupChat != null) {
                    String groupId = String.valueOf(groupChat.getId());
                    RcsUtils.startGroupChatDetailActivity(ComposeMessageActivity.this, groupId);
                }
                break;
            }
            case MENU_RCS_MCLOUD_SHARE:{
                Intent intent = new Intent();
                intent.setAction(ACTION_LUNCHER_RCS_SHAREFILE);
                startActivityForResult(intent, REQUEST_CODE_SAIYUN);
            }
            break;
            case MENU_FIERWALL_ADD_WHITELIST:{
                if (addToFirewallList(false)) {
                     Toast.makeText(ComposeMessageActivity.this, getString(R.string.firewall_save_success),
                        Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case MENU_FIERWALL_ADD_BLACKLIST:{
                new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.firewall_add_blacklist_wring))
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (addToFirewallList(true)) {
                                Toast.makeText(ComposeMessageActivity.this, getString(R.string.firewall_save_success),
                                    Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show();

            }
            break;
        }

        return true;
    }

    private void showAddAttachmentDialog(final boolean replace) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_dialog_attach);
        builder.setTitle(R.string.add_attachment);

        if (mAttachmentTypeSelectorAdapter == null) {
            mAttachmentTypeSelectorAdapter = new AttachmentTypeSelectorAdapter(
                    this, AttachmentTypeSelectorAdapter.MODE_WITH_SLIDESHOW);
        }
        builder.setAdapter(mAttachmentTypeSelectorAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addAttachment(mAttachmentTypeSelectorAdapter.buttonToCommand(which), replace);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_IMPORT_TEMPLATE:
            return showImportTemplateDialog();
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_IMPORT_TEMPLATE:
                removeDialog(id);
                break;
        }
        super.onPrepareDialog(id, dialog);
    }

    private Dialog showImportTemplateDialog(){
        String [] smsTempArray = null;
        Uri uri = Uri.parse("content://com.android.mms.MessageTemplateProvider/messages");
        Cursor cur = null;
        try {
            cur = getContentResolver().query(uri, null, null, null, null);
            if (cur != null && cur.moveToFirst()) {
                int index = 0;
                smsTempArray = new String[cur.getCount()];
                String title = null;
                do {
                    title = cur.getString(cur.getColumnIndex("message"));
                    smsTempArray[index++] = title;
                } while (cur.moveToNext());
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        TemplateSelectListener listener = new TemplateSelectListener(smsTempArray);
        return new AlertDialog.Builder(ComposeMessageActivity.this)
                .setTitle(R.string.message_template)
                .setItems(smsTempArray, listener)
                .create();
    }

    private class TemplateSelectListener implements DialogInterface.OnClickListener {

        private String[] mTempArray;
        TemplateSelectListener(String[] tempArray) {
            mTempArray = tempArray;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            if (mTempArray != null && mTempArray.length > which) {
                // If the subject EditText is visible and has the focus,
                // add the string from the template to the subject EditText
                // or else add the string to the message EditText.
                EditText etSubject = ComposeMessageActivity.this.mSubjectTextEditor;
                if (isSubjectEditorVisible() && etSubject.hasFocus()) {
                    int subjectIndex = etSubject.getSelectionStart();
                    etSubject.getText().insert(subjectIndex, mTempArray[which]);
                } else {
                    EditText et = ComposeMessageActivity.this.mTextEditor;
                    int index = et.getSelectionStart();
                    et.getText().insert(index, mTempArray[which]);
                    // Need require foucus,if do not do so,foucus still on mRecipientEditor,
                    // so mRecipientsWatcher will  call afterTextChanged to do
                    // setWorkingRecipients(...), and then mWorkingRecipients != null and will
                    // call setRecipients() set mThreadId = 0. Because of mThreadId = 0,
                    // asyncDeleteDraftSmsMessage will can not delete draft successful.
                    et.requestFocus();
                }
            }
        }

    }

    private void confirmDeleteThread(long threadId) {
        Conversation.startQueryHaveLockedMessages(mBackgroundQueryHandler,
                threadId, ConversationList.HAVE_LOCKED_MESSAGES_TOKEN);
    }

//    static class SystemProperties { // TODO, temp class to get unbundling working
//        static int getInt(String s, int value) {
//            return value;       // just return the default value or now
//        }
//    }

    private int getSlideNumber() {
        int slideNum = 0;
        SlideshowModel slideshow = mWorkingMessage.getSlideshow();
        if (slideshow != null) {
            slideNum = slideshow.size();
        }
        return slideNum;
    }

    private boolean showAddAttachementButton() {
        if (!mShowAttIcon) {
            return !mWorkingMessage.hasAttachment();
        } else {
            return !mWorkingMessage.hasVcard()
                    && getSlideNumber() < MmsConfig.getMaxSlideNumber();
        }
    }

    private boolean isAppendRequest(int requestCode) {
        return (requestCode & REPLACE_ATTACHMEN_MASK) == 0;
    }

    private int getRequestCode(int requestCode) {
        return requestCode & ~REPLACE_ATTACHMEN_MASK;
    }

    private int getMakRequestCode(boolean replace, int requestCode) {
        if (replace) {
            return requestCode | REPLACE_ATTACHMEN_MASK;
        }
        return requestCode;
    }

    private void addAttachment(int type, boolean replace) {
        // Calculate the size of the current slide if we're doing a replace so the
        // slide size can optionally be used in computing how much room is left for an attachment.
        int currentSlideSize = 0;
        SlideshowModel slideShow = mWorkingMessage.getSlideshow();
        if (replace && slideShow != null) {
            WorkingMessage.removeThumbnailsFromCache(slideShow);
            SlideModel slide = slideShow.get(0);
            currentSlideSize = slide.getSlideSize();
        }
        switch (type) {
            case AttachmentPagerAdapter.ADD_IMAGE:
                MessageUtils.selectImage(this,
                        getMakRequestCode(replace, REQUEST_CODE_ATTACH_IMAGE));
                break;

            case AttachmentPagerAdapter.TAKE_PICTURE: {
                MessageUtils.capturePicture(this,
                        getMakRequestCode(replace, REQUEST_CODE_TAKE_PICTURE));
                break;
            }

            case AttachmentPagerAdapter.ADD_VIDEO:
                MessageUtils.selectVideo(this,
                        getMakRequestCode(replace, REQUEST_CODE_ATTACH_VIDEO));
                break;

            case AttachmentPagerAdapter.RECORD_VIDEO: {
                long sizeLimit = computeAttachmentSizeLimit(slideShow, currentSlideSize);
                if (sizeLimit > 0) {
                    MessageUtils.recordVideo(this,
                        getMakRequestCode(replace, REQUEST_CODE_TAKE_VIDEO), sizeLimit);
                } else {
                    Toast.makeText(this,
                            getString(R.string.message_too_big_for_video),
                            Toast.LENGTH_SHORT).show();
                }
            }
            break;

            case AttachmentPagerAdapter.ADD_SOUND:
                MessageUtils.selectAudio(this,
                        getMakRequestCode(replace, REQUEST_CODE_ATTACH_SOUND));
                break;

            case AttachmentPagerAdapter.RECORD_SOUND:
                long sizeLimit = computeAttachmentSizeLimit(slideShow, currentSlideSize);
                MessageUtils.recordSound(this,
                        getMakRequestCode(replace, REQUEST_CODE_RECORD_SOUND), sizeLimit);
                break;

            case AttachmentPagerAdapter.ADD_SLIDESHOW:
                editSlideshow();
                break;

            case AttachmentPagerAdapter.ADD_CONTACT_AS_TEXT:
                pickContacts(MultiPickContactsActivity.MODE_INFO,
                        replace ? REQUEST_CODE_ATTACH_REPLACE_CONTACT_INFO
                                : REQUEST_CODE_ATTACH_ADD_CONTACT_INFO);
                break;

            case AttachmentPagerAdapter.ADD_CONTACT_AS_VCARD:
                if (mIsRcsEnabled) {
                    try {
                        if (mAccountApi.isOnline()) {
                            vcardContactOrGroup(new sendVcardClickListener());
                        } else {
                            pickContacts(MultiPickContactsActivity.MODE_VCARD,
                                    REQUEST_CODE_ATTACH_ADD_CONTACT_VCARD);
                        }
                    } catch (ServiceDisconnectedException e) {
                        pickContacts(MultiPickContactsActivity.MODE_VCARD,
                                REQUEST_CODE_ATTACH_ADD_CONTACT_VCARD);
                    }
                } else {
                    pickContacts(MultiPickContactsActivity.MODE_VCARD,
                            REQUEST_CODE_ATTACH_ADD_CONTACT_VCARD);
                }
                break;
            case AttachmentTypeSelectorAdapter.ADD_MAP:
               // <action android:name="com.suntek.mway.rcs.MAP_POSITION_SELECT" />
                try {
                    Intent intent = new Intent();
                    intent.setAction("com.suntek.mway.rcs.MAP_POSITION_SELECT");
                    startActivityForResult(intent, REQUEST_CODE_ATTACH_MAP);
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_MAP);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.please_install_rcs_map),
                            Toast.LENGTH_LONG).show();
                }

                break;
            default:
                break;
        }
    }

    private void vcardContactOrGroup(OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ComposeMessageActivity.this);
        builder.setCancelable(true);
        builder.setTitle(R.string.select_contact_conversation);
        builder.setItems(new String[] {
                getString(R.string.forward_contact),
                getString(R.string.forward_contact_group),
                getString(R.string.my_vcard)
        },listener);
        builder.show();
    }

    private class sendVcardClickListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton) {
            switch (whichButton) {
                case 0:
                    pickContacts(MultiPickContactsActivity.MODE_VCARD,
                            REQUEST_CODE_ATTACH_ADD_CONTACT_VCARD);
                    break;
                case 1:
                    launchRcsContactGroupPicker(REQUEST_CODE_VCARD_GROUP);
                    break;
                case 2:
                    String rawContactId = RcsContactsUtils
                            .getMyRcsRawContactId(ComposeMessageActivity.this);
                    if (TextUtils.isEmpty(rawContactId)) {
                        toast(R.string.please_set_my_profile);
                        return;
                    }

                    Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
                            Long.parseLong(rawContactId));
                    String lookup = Uri.encode(Contacts
                            .getLookupUri(ComposeMessageActivity.this.getContentResolver(),
                                    contactUri).getPathSegments().get(2));

                    Uri uri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookup);
                    RcsUtils.setVcard(ComposeMessageActivity.this, uri);
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_VCARD);
                    if (!isDisposeImage) {
                        mWorkingMessage.setIsBurn(mIsBurnMessage);
                        rcsSend();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static long computeAttachmentSizeLimit(SlideshowModel slideShow, int currentSlideSize) {
        if (RcsApiManager.getSupportApi().isOnline()) {
            return RcsUtils.getVideoFtMaxSize();
        }
        // Computer attachment size limit. Subtract 1K for some text.
        long sizeLimit = MmsConfig.getMaxMessageSize() - SlideshowModel.SLIDESHOW_SLOP;
        if (slideShow != null) {
            sizeLimit -= slideShow.getCurrentMessageSize() + slideShow.getTotalTextMessageSize();

            // We're about to ask the camera to capture some video (or the sound recorder
            // to record some audio) which will eventually replace the content on the current
            // slide. Since the current slide already has some content (which was subtracted
            // out just above) and that content is going to get replaced, we can add the size of the
            // current slide into the available space used to capture a video (or audio).
            sizeLimit += currentSlideSize;
        }
        return sizeLimit;
    }

    private void showAttachmentSelector(final boolean replace) {
        mAttachmentPager = (ViewPager) findViewById(R.id.attachments_selector_pager);
        mIsReplaceAttachment = replace;
        mCurrentAttachmentPager = DEFAULT_ATTACHMENT_PAGER;
        hideKeyboard();
        if (mAttachmentPagerAdapter == null) {
            mAttachmentPagerAdapter = new AttachmentPagerAdapter(this);
        } else {
            mAttachmentPagerAdapter.setExistAttachmentType(mWorkingMessage.hasAttachment(),
                    mWorkingMessage.hasVcard(), mWorkingMessage.hasSlideshow(), replace);
        }

        mAttachmentPagerAdapter.setGridItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    addAttachment((mCurrentAttachmentPager > DEFAULT_ATTACHMENT_PAGER ? position
                            + mAttachmentPagerAdapter.PAGE_GRID_COUNT : position), replace);
                    mAttachmentSelector.setVisibility(View.GONE);
                }
            }
        });
        setAttachmentSelectorHeight();
        mAttachmentPager.setAdapter(mAttachmentPagerAdapter);
        mAttachmentPager.setCurrentItem(0);
        mAttachmentPager.setOnPageChangeListener(mAttachmentPagerChangeListener);
        mAttachmentSelector.setVisibility(View.VISIBLE);
        // Delay 200ms for drawing view completed.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAttachmentSelector.requestFocus();
            }
        }, 200);
    }

    private final OnPageChangeListener mAttachmentPagerChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            updateAttachmentSelectorIndicator(position);
            mCurrentAttachmentPager = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private void updateAttachmentSelectorIndicator(int pagerPosition) {
        ImageView pagerIndicatorFirst = (ImageView) mAttachmentSelector.findViewById(
                R.id.pager_indicator_first);
        ImageView pagerIndicatorSecond = (ImageView) mAttachmentSelector.findViewById(
                R.id.pager_indicator_second);
        pagerIndicatorFirst.setImageResource(pagerPosition == 0 ? R.drawable.dot_chosen
                : R.drawable.dot_unchosen);
        pagerIndicatorSecond.setImageResource(pagerPosition == 0 ? R.drawable.dot_unchosen
                : R.drawable.dot_chosen);
    }

    private void setAttachmentSelectorHeight() {
        // Show different lines of grid for horizontal and vertical screen.
        Configuration configuration = getResources().getConfiguration();
        LayoutParams params = (LayoutParams) mAttachmentPager.getLayoutParams();
        int pagerHeight = (int) (mAttachmentPagerAdapter.GRID_ITEM_HEIGHT
                * getResources().getDisplayMetrics().density + 0.5f);
        params.height = (configuration.orientation == configuration.ORIENTATION_PORTRAIT)
                ? pagerHeight * 2 : pagerHeight;
        mAttachmentPager.setLayoutParams(params);
    }

    private void resetGridColumnsCount() {
        Configuration configuration = getResources().getConfiguration();
        ArrayList<GridView> pagerGridViews = mAttachmentPagerAdapter.getPagerGridViews();
        for (GridView grid : pagerGridViews) {
            grid.setNumColumns((configuration.orientation == configuration.ORIENTATION_PORTRAIT)
                    ? mAttachmentPagerAdapter.GRID_COLUMN_COUNT
                    : mAttachmentPagerAdapter.GRID_COLUMN_COUNT * 2);
        }
    }

    public void rcsSend() {
        if (isPreparedForSending() || mSupportApi.isOnline()) {
            confirmSendMessageIfNeeded();
        }
    }

    @Override
    protected void onActivityResult(int maskResultCode, int resultCode, Intent data) {
        if (LogTag.VERBOSE) {
            log("onActivityResult: requestCode=" + getRequestCode(maskResultCode) +
                    ", resultCode=" + resultCode + ", data=" + data);
        }
        mWaitingForSubActivity = false;          // We're back!
        mShouldLoadDraft = false;
        int requestCode = getRequestCode(maskResultCode);
        boolean append = isAppendRequest(maskResultCode);
        if (mWorkingMessage.isFakeMmsForDraft()) {
            // We no longer have to fake the fact we're an Mms. At this point we are or we aren't,
            // based on attachments and other Mms attrs.
            mWorkingMessage.removeFakeMmsForDraft();
        }

        if (requestCode == REQUEST_CODE_PICK) {
            mWorkingMessage.asyncDeleteDraftSmsMessage(mConversation);
        }

        if (requestCode == REQUEST_CODE_ADD_CONTACT) {
            // The user might have added a new contact. When we tell contacts to add a contact
            // and tap "Done", we're not returned to Messaging. If we back out to return to
            // messaging after adding a contact, the resultCode is RESULT_CANCELED. Therefore,
            // assume a contact was added and get the contact and force our cached contact to
            // get reloaded with the new info (such as contact name). After the
            // contact is reloaded, the function onUpdate() in this file will get called
            // and it will update the title bar, etc.
            if (mAddContactIntent != null) {
                String address =
                    mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.EMAIL);
                if (address == null) {
                    address =
                        mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.PHONE);
                }
                if (address != null) {
                    Contact contact = Contact.get(address, false);
                    if (contact != null) {
                        contact.reload();
                    }
                }
            }
        }

        if (requestCode == AttachmentEditor.MSG_PLAY_AUDIO) {
            // When the audio has finished to play, we put the
            // mIsAudioPlayerActivityRunning to false.
            mIsAudioPlayerActivityRunning = false;
        }

        if(requestCode == REQUEST_CODE_EMOJI_STORE){
            if(mRcsEmojiInitialize != null){
                mRcsEmojiInitialize.refreshData();
            }
        }

        if (resultCode != RESULT_OK){
            if (LogTag.VERBOSE) log("bail due to resultCode=" + resultCode);
            return;
        }

        boolean is_rcs_message = (requestCode == REQUEST_CODE_ATTACH_IMAGE)
                || (requestCode == REQUEST_CODE_TAKE_PICTURE)
                || (requestCode == REQUEST_CODE_ATTACH_VIDEO)
                || (requestCode == PHOTO_CROP)
                || (requestCode == REQUEST_CODE_TAKE_VIDEO)
                || (requestCode == REQUEST_CODE_ATTACH_SOUND)
                || (requestCode == REQUEST_CODE_RECORD_SOUND)
                || (requestCode == REQUEST_CODE_ATTACH_ADD_CONTACT_VCARD)
                || (requestCode == REQUEST_CODE_ATTACH_ADD_CONTACT_RCS_VCARD)
                || (requestCode == REQUEST_CODE_ATTACH_MAP)
                || (requestCode == REQUEST_CODE_VCARD_GROUP)
                || (requestCode == REQUEST_CODE_SAIYUN);
        if (RcsApiManager.getSupportApi().isOnline() && is_rcs_message) {
            switch (requestCode) {
                case PHOTO_CROP:
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_IMAGE);
                    Uri cropData = data.getData();
                    String mRcs_cropPath = getRealPathFromURI(cropData);
                    mWorkingMessage.setRcsPath(mRcs_cropPath);
                    break;
                case REQUEST_CODE_ATTACH_IMAGE:
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_IMAGE);
                    Uri uriData = data.getData();
                    String mRcs_path = RcsUtils.getPath(this, uriData);
                    if (!TextUtils.isEmpty(mRcs_path)) {
                        if (mRcs_path.toLowerCase(Locale.US).endsWith("gif")) {
                            mWorkingMessage.setRcsPath(mRcs_path);
                        } else {
                            ImageDispose(mRcs_path);
                        }
                    }
                    break;
                case REQUEST_CODE_TAKE_PICTURE:
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_IMAGE);
                    File file = new File(TempFileProvider.getScrapPath(this));
                    Uri uri1 = Uri.fromFile(file);
                    mWorkingMessage.setRcsPath(RcsUtils.getPath(this, uri1));
                    break;
                case REQUEST_CODE_ATTACH_VIDEO:
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_VIDEO);
                    mWorkingMessage.setRcsPath(RcsUtils.getPath(this, data.getData()));
                    mWorkingMessage.setDuration(RcsUtils.getDuration(this, data.getData()));
                    mWorkingMessage.setIsRecord(false);
                    break;
                case REQUEST_CODE_TAKE_VIDEO:
                    Uri videoUri = TempFileProvider.renameScrapFile(".3gp", null, this);
                    if(videoUri == null){
                        toast(R.string.file_size_over);
                        return;
                    }
                    int videoDuration = RcsUtils.getDuration(this, videoUri);
                    if(videoDuration < 1){
                        Toast.makeText(
                                this,
                                getString(R.string.cannot_send_video), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (videoDuration > RcsUtils.getVideoMaxTime()) {
                        toast(getString(R.string.video_record_out_time,
                                RcsUtils.getVideoMaxTime()));
                        return;
                    }
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_VIDEO);
                    mWorkingMessage.setRcsPath(RcsUtils.getPath(this, videoUri)); // can
                    mWorkingMessage.setDuration(videoDuration);
                    mWorkingMessage.setIsRecord(true);
                    break;
                case REQUEST_CODE_ATTACH_SOUND:

                    // Attempt to add the audio to the attachment.
                    Uri uri = (Uri)data
                            .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri == null) {
                        uri = data.getData();
                    } else if (Settings.System.DEFAULT_RINGTONE_URI.equals(uri)) {
                        break;
                    }
                    mWorkingMessage.setRcsPath(RcsUtils.getPath(this, uri));
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_AUDIO);
                    mWorkingMessage.setDuration(RcsUtils.getDuration(this, uri));
                    mWorkingMessage.setIsRecord(false);
                    break;
                case REQUEST_CODE_RECORD_SOUND:
                    if (data != null) {
                        Uri audioUri = data.getData();
                        int audioDuration = RcsUtils.getDuration(this, audioUri);
                        if(audioDuration < 1){
                            Toast.makeText(
                                    this,
                                    getString(R.string.cannot_send_audio), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (audioDuration > RcsUtils.getAudioMaxTime()) {
                            Toast.makeText(
                                    this,
                                    getString(R.string.audio_record_out_time,
                                            RcsUtils.getAudioMaxTime()), Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_AUDIO);
                        mWorkingMessage.setRcsPath(RcsUtils.getPath(this, audioUri));
                        mWorkingMessage.setDuration(audioDuration);
                        mWorkingMessage.setIsRecord(true);
                        // addAudio(data.getData(), append);
                    }
                    break;
                case REQUEST_CODE_VCARD_GROUP:
                    if (data == null) {
                        return;
                    }
                    mRcsContactList.clear();
                    Bundle bundle = data.getExtras().getBundle("result");
                    final Set<String> keySet = bundle.keySet();
                    final int recipientCount = (keySet != null) ? keySet.size() : 0;
                    final ContactList list = ContactList.blockingGetByUris(buildUris(keySet,
                            recipientCount));
                    StringBuffer buffer = new StringBuffer();
                    for (Contact contact : list) {
                        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
                                contact.getPersonId());
                        String lookup = Uri.encode(Contacts
                                .getLookupUri(this.getContentResolver(), contactUri)
                                .getPathSegments().get(2));
                        buffer.append(lookup + ":");
                    }
                    String buffer2 = buffer.substring(0, buffer.lastIndexOf(":"));
                    Uri uri2 = Uri.withAppendedPath(Contacts.CONTENT_MULTI_VCARD_URI,
                            Uri.encode(buffer2));
                    RcsUtils.setVcard(this, uri2);
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_VCARD);

                    break;
                case REQUEST_CODE_ATTACH_ADD_CONTACT_VCARD:
                        if (data == null) {
                            return;
                        }
                        // In a case that a draft message has an attachment whose type is slideshow,
                        // then reopen it and replace the attachment through attach icon, we have to
                        // remove the old attachement silently first.
                        if (mWorkingMessage != null) {
                            mWorkingMessage.removeAttachment(false);
                        }
                        String extraVCard = data.getStringExtra(MultiPickContactsActivity.EXTRA_VCARD);
                        if (extraVCard != null) {
                            Uri vcard = Uri.parse(extraVCard);
                            RcsUtils.setVcard(this,vcard);
                            mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_VCARD);

                        }
                    break;
                case REQUEST_CODE_ATTACH_MAP:
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_MAP);

                    double latitude = data.getDoubleExtra("latitude", 39.90865);
                    mWorkingMessage.setLatitude(latitude);

                    double longitude = data.getDoubleExtra("longitude", 116.39751);

                    mWorkingMessage.setLongitude(longitude);
                    String address = data.getStringExtra("address");
                    mWorkingMessage.setLocation(address);
                    break;
                case REQUEST_CODE_SAIYUN:
                    if (data == null) {
                        return;
                    }
                    String id = data.getStringExtra("id");
                    Log.i(TAG, "ID=" + id);
                    try {
                        if (mConversation.isGroupChat()) {
                            GroupChatModel groupChat = mConversation.getGroupChat();
                            RcsApiManager.getMcloudFileApi().shareFileAndSendGroup(id, "",
                                    groupChat.getThreadId(), groupChat.getConversationId(),
                                    String.valueOf(groupChat.getId()));
                        } else if (mConversation.getRecipients().size() == 1) {
                            Log.i(TAG,"SEND 1 V 1");
                            RcsApiManager.getMcloudFileApi().shareFileAndSend(id, "",
                                    mConversation.getRecipients().getNumbers()[0],
                                    mConversation.getThreadId(),
                                    getString(R.string.mcloud_share_file_sms_temp));
                        } else {
                            String[] numArr = mConversation.getRecipients().getNumbers();
                            List<String> numberList = new ArrayList<String>();
                            for (int i = 0; i < numArr.length; i++) {
                                numberList.add(numArr[i]);
                            }
                            RcsApiManager.getMcloudFileApi().shareFileAndSendOne2Many(id, "",
                                    numberList, mConversation.getThreadId(),
                                    getString(R.string.mcloud_share_file_sms_temp));
                        }
                    } catch (ServiceDisconnectedException e) {
                        Log.i(TAG, "SHARE CLOUD FILE EXCEPTION");
                    }

                    break;
                default:
                    break;
            }
            if (!isDisposeImage) {
                mWorkingMessage.setIsBurn(mIsBurnMessage);

                rcsSend();
            }
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_CREATE_SLIDESHOW:
                if (data != null) {
                    WorkingMessage newMessage = WorkingMessage.load(this, data.getData());
                    if (newMessage != null) {
                        // Here we should keep the subject from the old mWorkingMessage.
                        setNewMessageSubject(newMessage);
                        mWorkingMessage = newMessage;
                        mWorkingMessage.setConversation(mConversation);
                        updateThreadIdIfRunning();
                        updateMmsSizeIndicator();
                        drawTopPanel(false);
                        drawBottomPanel();
                        updateSendButtonState();
                    }
                }
                break;

            case REQUEST_CODE_TAKE_PICTURE: {
                // create a file based uri and pass to addImage(). We want to read the JPEG
                // data directly from file (using UriImage) instead of decoding it into a Bitmap,
                // which takes up too much memory and could easily lead to OOM.
                File file = new File(TempFileProvider.getScrapPath(this));
                Uri uri = Uri.fromFile(file);

                // Remove the old captured picture's thumbnail from the cache
                MmsApp.getApplication().getThumbnailManager().removeThumbnail(uri);

                addImageAsync(uri, append);
                break;
            }

            case REQUEST_CODE_ATTACH_IMAGE: {
                if (data != null) {
                    addImageAsync(data.getData(), append);
                }
                break;
            }

            case REQUEST_CODE_TAKE_VIDEO:
                Uri videoUri = TempFileProvider.renameScrapFile(".3gp",
                        Integer.toString(getSlideNumber()), this);
                // Remove the old captured video's thumbnail from the cache
                MmsApp.getApplication().getThumbnailManager().removeThumbnail(videoUri);

                addVideoAsync(videoUri, append);      // can handle null videoUri
                break;

            case REQUEST_CODE_ATTACH_VIDEO:
                if (data != null) {
                    addVideoAsync(data.getData(), append);
                }
                break;

            case REQUEST_CODE_ATTACH_SOUND: {
                // Attempt to add the audio to the  attachment.
                Uri uri = (Uri) data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri == null) {
                    uri = data.getData();
                } else if (Settings.System.DEFAULT_RINGTONE_URI.equals(uri)) {
                    break;
                }
                addAudio(uri, append);
                break;
            }

            case REQUEST_CODE_RECORD_SOUND:
                if (data != null) {
                    addAudio(data.getData(), append);
                }
                break;

            case REQUEST_CODE_ECM_EXIT_DIALOG:
                boolean outOfEmergencyMode = data.getBooleanExtra(EXIT_ECM_RESULT, false);
                if (outOfEmergencyMode) {
                    sendMessage(false);
                }
                break;

            case REQUEST_CODE_PICK:
                if (data != null) {
                    processPickResult(data);
                }
                break;
            case REQUEST_SELECT_GROUP:
                if (data != null) {
                    sendForwardRcsMessage(data);
                }
                break;
            case REQUEST_CODE_RCS_PICK:
                if (data != null) {
                    sendForwardRcsMessage(data);
                    // forward rcs message
                }

                break;
            case REQUEST_SELECT_CONV:
                if(data != null && !"".equals(data)){
                    ContactList contactList  = new ContactList();
                    HashSet<Long> threadIds = (HashSet)data.getSerializableExtra("selectThreadId");
                    Iterator it = threadIds.iterator();
                    Conversation conv = null;
                    while(it.hasNext()){
                        conv = Conversation.get(ComposeMessageActivity.this, Long.valueOf(it.next().toString()), true);
                        contactList.addAll(conv.getRecipients());
                    }
                    try {
                        long a = -1;
                        boolean success = false;
                        ChatMessage message = mMessageApi.getMessageById(
                                rcsforwardid + "");
                        if (conv.isGroupChat()) {
                            GroupChatModel groupChatModel = conv.getGroupChat();
                            success = RcsChatMessageUtils.forwardToGroupMessage(a,
                                    Arrays.asList(contactList.getNumbers()), message,
                                    groupChatModel);
                        } else {
                            success = RcsChatMessageUtils.forwardMessage(a,
                                    Arrays.asList(contactList.getNumbers()), message);
                        }
                        if (success) {
                            toast(R.string.forward_message_success);
                        } else {
                            toast(R.string.forward_message_fail);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_CODE_ATTACH_REPLACE_CONTACT_INFO:
                // Caused by user choose to replace the attachment, so we need remove
                // the attachment and then add the contact info to text.
                if (data != null) {
                    mWorkingMessage.removeAttachment(true);
                }
            case REQUEST_CODE_ATTACH_ADD_CONTACT_INFO:
                if (data != null) {
                    String newText = mWorkingMessage.getText() +
                        data.getStringExtra(ContactSelectActivity.EXTRA_INFO);
                    mWorkingMessage.setText(newText);
                }
                break;

            case REQUEST_CODE_ATTACH_ADD_CONTACT_VCARD:
                if (data != null) {
                    String extraVCard = data.getStringExtra(ContactSelectActivity.EXTRA_VCARD);
                    if (extraVCard != null) {
                        Uri vcard = Uri.parse(extraVCard);
                        addVcard(vcard);
                    }
                }
                break;

            case REQUEST_CODE_BATCH_DELETE:
                startMsgListQuery(MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN);
                break;

            case REQUEST_CODE_BATCH_FAVOURITE:
                Log.i("BATCH_FAVOURITE","LOOK THE FAVOURITE");
                break;

            case REQUEST_CODE_BATCH_BACKUP:
                Log.i("BATCH_BACKUP","BACKUP SUCCESS");
                break;

            default:
                if (LogTag.VERBOSE) log("bail due to unknown requestCode=" + requestCode);
                break;
        }
    }

    private void ImageDispose(final String photoPath){
        isDisposeImage = true;
        String[] imageItems = getResources().getStringArray(R.array.del_image_mode);
        new AlertDialog.Builder(ComposeMessageActivity.this)
                .setTitle(R.string.del_image_action)
                .setItems(imageItems, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDisposeImage = false;
                        switch (which) {
                            case 0:
                                File mCurrentPhotoFile = new File(photoPath);
                                doCropPhoto(mCurrentPhotoFile);
                                dialog.dismiss();
                                break;
                            case 1:
                                showQualityDialog(photoPath);
                                break;
                            case 2:
                                mWorkingMessage.setRcsPath(photoPath);
                                mWorkingMessage.setIsBurn(mIsBurnMessage);
                                rcsSend();
                                dialog.dismiss();
                                break;
                            case 3:
                                dialog.dismiss();
                                break;
                            default:
                                break;
                        }
                    }
                }).create().show();
    }

    private void doCropPhoto(File file) {
        try {
            Intent intent = new Intent(INTENT_CAMERA_CROP);
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            intent.putExtra("crop", true);
            startActivityForResult(intent, PHOTO_CROP);
        } catch (Exception e) {
            Toast.makeText(ComposeMessageActivity.this, R.string.not_intent, Toast.LENGTH_SHORT).show();
        }
    }

    private void showQualityDialog(final String photoPath) {
        final EditText editText = new EditText(ComposeMessageActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint(R.string.please_input_1_100_int);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                Pattern pattern = Pattern.compile(PATTERN_QUALTITY);
                Matcher matcher = pattern.matcher(s);
                if (!matcher.find()) {
                    s.clear();
                    Toast.makeText(ComposeMessageActivity.this, R.string.input_no_fit, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(ComposeMessageActivity.this);
        builder.setTitle(R.string.input_quality);
        builder.setView(editText);
        builder.setPositiveButton(R.string.send_comfirm_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String quality = editText.getText().toString().trim();

                if (TextUtils.isEmpty(quality) || Long.parseLong(quality) > Integer.MAX_VALUE
                        || Integer.parseInt(quality) == 0 || Integer.parseInt(quality) > 100) {
                    Toast.makeText(ComposeMessageActivity.this, R.string.input_no_fit, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mWorkingMessage.setRcsPath(photoPath);
                    mWorkingMessage.setScaling(quality);
                    mWorkingMessage.setIsBurn(mIsBurnMessage);
                    rcsSend();
                }
            }
        });
        builder.setNegativeButton(R.string.send_comfirm_cancel, null);
        builder.create().show();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {
                MediaStore.Images.Media.DATA
        };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        String path = null;
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(column_index);
        }
        if (cursor != null) {
            cursor.close();
        }
        return path;
    }

    private void updateMmsSizeIndicator() {
        mAttachmentEditorHandler.post(mUpdateMmsSizeIndRunnable);
    }

    private Runnable mUpdateMmsSizeIndRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWorkingMessage.getSlideshow() != null) {
                mWorkingMessage.getSlideshow().updateTotalMessageSize();
            }
            mAttachmentEditor.update(mWorkingMessage);
        }
    };

    private void sendForwardRcsMessage(Intent data) {
        Bundle bundle = data.getExtras().getBundle("result");
        final Set<String> keySet = bundle.keySet();
        final int recipientCount = (keySet != null) ? keySet.size() : 0;
        final ContactList list;

        list = ContactList.blockingGetByUris(buildUris(keySet, recipientCount));

        boolean success = false;
        String[] numbers = list.getNumbers(false);
        try {
            ChatMessage message = RcsApiManager.getMessageApi().getMessageById(rcsforwardid + "");
            success = RcsChatMessageUtils.forwardMessage(NO_THREAD_ID,
                    Arrays.asList(list.getNumbers()), message);
            if (success) {
                toast(R.string.forward_message_success);
            } else {
                toast(R.string.forward_message_fail);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            toast(R.string.forward_message_fail);
        } catch(ServiceDisconnectedException e){
            e.printStackTrace();
            toast(R.string.forward_message_fail);
        }
    }

    /**
     * Set newWorkingMessage's subject from mWorkingMessage. If we create a new
     * slideshow. We will drop the old workingMessage and create a new one. And
     * we should keep the subject of the old workingMessage.
     */
    private void setNewMessageSubject(WorkingMessage newWorkingMessage) {
        if (null != newWorkingMessage && mWorkingMessage.hasSubject()) {
            newWorkingMessage.setSubject(mWorkingMessage.getSubject(), true);
        }
    }

    private void processPickResult(final Intent data) {
        // The EXTRA_PHONE_URIS stores the phone's urls that were selected by user in the
        // multiple phone picker.
        Bundle bundle = data.getExtras().getBundle("result");
        final Set<String> keySet = bundle.keySet();
        final int recipientCount = (keySet != null) ? keySet.size() : 0;

        // If total recipients count > recipientLimit,
        // then forbid add reipients to RecipientsEditor
        final int recipientLimit = MmsConfig.getRecipientLimit();
        int totalRecipientsCount = mExistsRecipientsCount + recipientCount;
        if (recipientLimit != Integer.MAX_VALUE && totalRecipientsCount > recipientLimit) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.too_many_recipients, totalRecipientsCount,
                            recipientLimit))
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // if already exists some recipients,
                            // then new pick recipients with exists recipients count
                            // can't more than recipient limit count.
                            int newPickRecipientsCount = recipientLimit - mExistsRecipientsCount;
                            if (newPickRecipientsCount <= 0) {
                                return;
                            }
                            processAddRecipients(keySet, newPickRecipientsCount);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show();
            return;
        }

        processAddRecipients(keySet, recipientCount);
    }

    private Uri[] buildUris(final Set<String> keySet, final int newPickRecipientsCount) {
        Uri[] newUris = new Uri[newPickRecipientsCount];
        Iterator<String> it = keySet.iterator();
        int i = 0;
        while (it.hasNext()) {
            String id = it.next();
            newUris[i++] = ContentUris.withAppendedId(Phone.CONTENT_URI, Integer.parseInt(id));
            if (i == newPickRecipientsCount) {
                break;
            }
        }
        return newUris;
    }

    private void processAddRecipients(final Set<String> keySet, final int newPickRecipientsCount) {
        // if process pick result that is pick recipients from Contacts
        mIsProcessPickedRecipients = true;
        final Handler handler = new Handler();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getText(R.string.pick_too_many_recipients));
        progressDialog.setMessage(getText(R.string.adding_recipients));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        final Runnable showProgress = new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
            }
        };
        // Only show the progress dialog if we can not finish off parsing the return data in 1s,
        // otherwise the dialog could flicker.
        handler.postDelayed(showProgress, 1000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri[] newuris = new Uri[newPickRecipientsCount];
                final ContactList list;
                 try {
                    Iterator<String> it = keySet.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        String id = it.next();
                        newuris[i++] = ContentUris.withAppendedId(Phone.CONTENT_URI,
                                Integer.parseInt(id));
                        if (i == newPickRecipientsCount) {
                            break;
                        }
                    }
                    list = ContactList.blockingGetByUris(newuris);
                } finally {
                    handler.removeCallbacks(showProgress);
                }
                if (mRecipientsEditor != null) {
                    ContactList exsitList = mRecipientsEditor.constructContactsFromInput(true);
                    // Remove the repeat recipients.
                  if(exsitList.equals(list)){
                    exsitList.clear();
                    list.addAll(0, exsitList);
                  }else{
                    list.removeAll(exsitList);
                    list.addAll(0, exsitList);
                     }
                }
                // TODO: there is already code to update the contact header widget and recipients
                // editor if the contacts change. we can re-use that code.
                final Runnable populateWorker = new Runnable() {
                    @Override
                    public void run() {
                        mRecipientsEditor.populate(list);
                        // Set value for mRecipientsPickList and
                        // mRecipientsWatcher will update the UI.
                        mRecipientsPickList = list;
                        updateTitle(list);
                        // if process finished, then dismiss the progress dialog
                        progressDialog.dismiss();

                        // if populate finished, then recipients pick process end
                        mIsProcessPickedRecipients = false;

                        if (mRcsShareVcard) {
                            rcsSend();
                        }
                    }
                };
                handler.post(populateWorker);
            }
        }, "ComoseMessageActivity.processPickResult").start();
    }

    private final ResizeImageResultCallback mResizeImageCallback = new ResizeImageResultCallback() {
        // TODO: make this produce a Uri, that's what we want anyway
        @Override
        public void onResizeResult(PduPart part, boolean append) {
            if (part == null) {
                handleAddAttachmentError(WorkingMessage.UNKNOWN_ERROR, R.string.type_picture);
                return;
            }

            Context context = ComposeMessageActivity.this;
            PduPersister persister = PduPersister.getPduPersister(context);
            int result;
            synchronized(mAddAttachmentLock) {
                Uri messageUri = mWorkingMessage.saveAsMms(true);
                if (messageUri == null) {
                    result = WorkingMessage.UNKNOWN_ERROR;
                } else {
                    try {
                        Uri dataUri = persister.persistPart(part,
                                ContentUris.parseId(messageUri), null);
                        result = mWorkingMessage.setAttachment(
                                WorkingMessage.IMAGE, dataUri, append);
                        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                            log("ResizeImageResultCallback: dataUri=" + dataUri);
                        }
                    } catch (MmsException e) {
                        result = WorkingMessage.UNKNOWN_ERROR;
                    }
                }
            }

            updateMmsSizeIndicator();
            handleAddAttachmentError(result, R.string.type_picture);
        }
    };

    private void handleAddAttachmentError(final int error, final int mediaTypeStringId) {
        if (error == WorkingMessage.OK) {
            return;
        }
        Log.d(TAG, "handleAddAttachmentError: " + error);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Resources res = getResources();
                String mediaType = res.getString(mediaTypeStringId);
                String title, message;

                switch(error) {
                case WorkingMessage.UNKNOWN_ERROR:
                    message = res.getString(R.string.failed_to_add_media, mediaType);
                    Toast.makeText(ComposeMessageActivity.this, message, Toast.LENGTH_SHORT).show();
                    return;
                case WorkingMessage.UNSUPPORTED_TYPE:
                    title = res.getString(R.string.unsupported_media_format, mediaType);
                    message = res.getString(R.string.select_different_media, mediaType);
                    break;
                case WorkingMessage.MESSAGE_SIZE_EXCEEDED:
                    title = res.getString(R.string.exceed_message_size_limitation,
                        mediaType);
                    // We should better prompt the "message size limit reached,
                    // cannot send out message" while we send out the Mms.
                    if (mIsAttachmentErrorOnSend) {
                        message = res.getString(R.string.media_size_limit);
                        mIsAttachmentErrorOnSend = false;
                    } else {
                        message = res.getString(R.string.failed_to_add_media, mediaType);
                    }
                    break;
                case WorkingMessage.IMAGE_TOO_LARGE:
                    title = res.getString(R.string.failed_to_resize_image);
                    message = res.getString(R.string.resize_image_error_information);
                    break;
                case WorkingMessage.NEGATIVE_MESSAGE_OR_INCREASE_SIZE:
                    title = res.getString(R.string.illegal_message_or_increase_size);
                    message = res.getString(R.string.failed_to_add_media, mediaType);
                    break;
                default:
                    throw new IllegalArgumentException("unknown error " + error);
                }

                MessageUtils.showErrorDialog(ComposeMessageActivity.this, title, message);
            }
        });
    }

    private void addImageAsync(final Uri uri, final boolean append) {
        mInAsyncAddAttathProcess = true;
        getAsyncDialog().runAsync(new Runnable() {
            @Override
            public void run() {
                addImage(uri, append);
                mInAsyncAddAttathProcess = false;
            }
        }, null, R.string.adding_attachments_title);
    }

    private void addImage(Uri uri, boolean append) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("addImage: append=" + append + ", uri=" + uri);
        }

        int result = mWorkingMessage.setAttachment(WorkingMessage.IMAGE, uri, append);

        if (result == WorkingMessage.IMAGE_TOO_LARGE ||
            result == WorkingMessage.MESSAGE_SIZE_EXCEEDED) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("resize image " + uri);
            }
            MessageUtils.resizeImageAsync(ComposeMessageActivity.this,
                    uri, mAttachmentEditorHandler, mResizeImageCallback, append);
            return;
        }

        updateMmsSizeIndicator();
        handleAddAttachmentError(result, R.string.type_picture);
    }

    private void addVideoAsync(final Uri uri, final boolean append) {
        mInAsyncAddAttathProcess = true;
        getAsyncDialog().runAsync(new Runnable() {
            @Override
            public void run() {
                addVideo(uri, append);
                mInAsyncAddAttathProcess = false;
            }
        }, null, R.string.adding_attachments_title);
    }

    private void addVideo(Uri uri, boolean append) {
        if (uri != null) {
            int result = mWorkingMessage.setAttachment(WorkingMessage.VIDEO, uri, append);
            updateMmsSizeIndicator();
            handleAddAttachmentError(result, R.string.type_video);
        }
    }

    private void addAudio(Uri uri, boolean append) {
        if (uri != null) {
            int result = mWorkingMessage.setAttachment(WorkingMessage.AUDIO, uri, append);
            updateMmsSizeIndicator();
            handleAddAttachmentError(result, R.string.type_audio);
        }
    }

    private void addVcard(Uri uri) {
        int result = mWorkingMessage.setAttachment(WorkingMessage.VCARD, uri, false);
        handleAddAttachmentError(result, R.string.type_vcard);
    }

    AsyncDialog getAsyncDialog() {
        if (mAsyncDialog == null) {
            mAsyncDialog = new AsyncDialog(this);
        }
        return mAsyncDialog;
    }

    private boolean handleForwardedMessage() {
        Intent intent = getIntent();

        // If this is a forwarded message, it will have an Intent extra
        // indicating so.  If not, bail out.
        if (!mForwardMessageMode) {
            if (mConversation != null) {
                mConversation.setHasMmsForward(false);
            }
            return false;
        }

        if (mConversation != null) {
            mConversation.setHasMmsForward(true);
            String[] recipientNumber = intent.getStringArrayExtra("msg_recipient");
            mConversation.setForwardRecipientNumber(recipientNumber);
        }
        Uri uri = intent.getParcelableExtra("msg_uri");

        if (Log.isLoggable(LogTag.APP, Log.DEBUG)) {
            log("" + uri);
        }

        if (uri != null) {
            mWorkingMessage = WorkingMessage.load(this, uri);
            mWorkingMessage.setSubject(intent.getStringExtra("subject"), false);
        } else {
            mWorkingMessage.setText(intent.getStringExtra("sms_body"));
        }

        // let's clear the message thread for forwarded messages
        mMsgListAdapter.changeCursor(null);

        return true;
    }

    // Handle send actions, where we're told to send a picture(s) or text.
    private boolean handleSendIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return false;
        }

        final String mimeType = intent.getType();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                final Uri uri = (Uri)extras.getParcelable(Intent.EXTRA_STREAM);

                boolean isRcsAvailable = RcsApiManager.getSupportApi().isRcsSupported()
                        && RcsApiManager.isRcsOnline();
                if (isRcsAvailable && uri.toString().contains("as_vcard")) {
                    RcsUtils.setVcard(this, uri);
                    mRcsShareVcard = true;
                    rcsShareVcardAddNumber = true;
                    mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_VCARD);
                    return false;
                }
                getAsyncDialog().runAsync(new Runnable() {
                    @Override
                    public void run() {
                        addAttachment(mimeType, uri, false);
                    }
                }, null, R.string.adding_attachments_title);
                return true;
            } else if (extras.containsKey(Intent.EXTRA_TEXT)) {
                mWorkingMessage.setText(extras.getString(Intent.EXTRA_TEXT));
                return true;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) &&
                extras.containsKey(Intent.EXTRA_STREAM)) {
            SlideshowModel slideShow = mWorkingMessage.getSlideshow();
            final ArrayList<Parcelable> uris = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
            int currentSlideCount = slideShow != null ? slideShow.size() : 0;
            int importCount = uris.size();
            if (importCount + currentSlideCount > MmsConfig.getMaxSlideNumber()) {
                importCount = Math.min(MmsConfig.getMaxSlideNumber() - currentSlideCount,
                        importCount);
                Toast.makeText(ComposeMessageActivity.this,
                        getString(R.string.too_many_attachments,
                                MmsConfig.getMaxSlideNumber(), importCount),
                                Toast.LENGTH_LONG).show();
            }

            // Attach all the pictures/videos asynchronously off of the UI thread.
            // Show a progress dialog if adding all the slides hasn't finished
            // within half a second.
            final int numberToImport = importCount;
            getAsyncDialog().runAsync(new Runnable() {
                @Override
                public void run() {
                    String type = mimeType;
                    for (int i = 0; i < numberToImport; i++) {
                        Parcelable uri = uris.get(i);
                        if (uri != null && "*/*".equals(mimeType)) {
                            type = getAttachmentMimeType((Uri) uri);
                        }
                        synchronized(mAddAttachmentLock) {
                            addAttachment(type, (Uri) uri, true);
                        }
                    }
                    updateMmsSizeIndicator();
                }
            }, null, R.string.adding_attachments_title);
            return true;
        }
        return false;
    }

    private String getAttachmentMimeType(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String scheme = uri.getScheme();
        String attachmentType = "*/*";
        // Support uri with "content" scheme
        if ("content".equals(scheme)) {
            Cursor metadataCursor = null;
            try {
                metadataCursor = contentResolver.query(uri, new String[] {
                        Document.COLUMN_MIME_TYPE}, null, null, null);
            } catch (SQLiteException e) {
                // some content providers don't support the COLUMN_MIME_TYPE columns
                if (metadataCursor != null) {
                    metadataCursor.close();
                }
                metadataCursor = null;
            } catch (Exception e) {
                metadataCursor = null;
            }
            if (metadataCursor != null) {
                try {
                    if (metadataCursor.moveToFirst()) {
                        attachmentType = metadataCursor.getString(0);
                        Log.d(TAG, "attachmentType = " + attachmentType);
                    }
                } finally {
                    metadataCursor.close();
                }
            }
        }
        return attachmentType;
    }
    private boolean isAudioFile(Uri uri) {
        String path = uri.getPath();
        String mimeType = MediaFile.getMimeTypeForFile(path);
        int fileType = MediaFile.getFileTypeForMimeType(mimeType);
        return MediaFile.isAudioFileType(fileType);
    }

    private boolean isImageFile(Uri uri) {
        String path = uri.getPath();
        String mimeType = MediaFile.getMimeTypeForFile(path);
        int fileType = MediaFile.getFileTypeForMimeType(mimeType);
        return MediaFile.isImageFileType(fileType);
    }

    private boolean isVideoFile(Uri uri) {
        String path = uri.getPath();
        String mimeType = MediaFile.getMimeTypeForFile(path);
        int fileType = MediaFile.getFileTypeForMimeType(mimeType);
        return MediaFile.isVideoFileType(fileType);
    }

    // mVideoUri will look like this: content://media/external/video/media
    private static final String mVideoUri = Video.Media.getContentUri("external").toString();
    // mImageUri will look like this: content://media/external/images/media
    private static final String mImageUri = Images.Media.getContentUri("external").toString();
    // mAudioUri will look like this: content://media/external/audio/media
    private static final String mAudioUri = Audio.Media.getContentUri("external").toString();

    private void addAttachment(String type, Uri uri, boolean append) {
        if (uri != null) {
            // When we're handling Intent.ACTION_SEND_MULTIPLE, the passed in items can be
            // videos, and/or images, and/or some other unknown types we don't handle. When
            // a single attachment is "shared" the type will specify an image or video. When
            // there are multiple types, the type passed in is "*/*". In that case, we've got
            // to look at the uri to figure out if it is an image or video.
            boolean wildcard = "*/*".equals(type);
            if (type.startsWith("image/") || (wildcard && uri.toString().startsWith(mImageUri))
                    || (wildcard && isImageFile(uri))) {
                addImage(uri, append);
            } else if (type.startsWith("video/") ||
                    (wildcard && uri.toString().startsWith(mVideoUri))
                    || (wildcard && isVideoFile(uri))) {
                addVideo(uri, append);
            } else if (type.startsWith("audio/")
                    || (wildcard && uri.toString().startsWith(mAudioUri))
                    || (wildcard && isAudioFile(uri))) {
                addAudio(uri, append);
            } else if (this.getResources().getBoolean(R.bool.config_vcard)
                    && (type.equals("text/x-vcard")
                    || (wildcard && isVcardFile(uri)))) {
                addVcard(uri);
            } else {
                // Add prompt when file type is not image/video/audio.
                Message msg = Message.obtain(mAddAttachmentHandler,
                        MSG_ADD_ATTACHMENT_FAILED, uri);
                mAddAttachmentHandler.sendMessage(msg);
           }
        }
    }

    // handler for handle add attachment failt.
    private Handler mAddAttachmentHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_ATTACHMENT_FAILED:
                    Toast.makeText(ComposeMessageActivity.this,
                            getAttachmentPostfix((Uri) msg.obj), Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
            }
        }

        private String getAttachmentPostfix(Uri uri) {
            // if uri is valid,parse it as normal.
            if (isValidUri(uri)) {
                int lastDot = uri.toString().lastIndexOf(".");
                String postfix = uri.toString().substring(lastDot + 1);
                return getResourcesString(R.string.unsupported_media_format,
                        postfix);
            } else {
                // if uri is invalid,show just show unsupported "Unsupported format".
                return getResources().getString(R.string.unsupported_format);
            }
        }

        //Used to check the uri is valid or not.
        private boolean isValidUri(Uri uri) {
            String path = uri == null ? null : uri.toString();
            if (null != path && path.contains("/")) {
                String fileName = path.substring(path.lastIndexOf("/"));
                if (null != fileName && !fileName.isEmpty()
                        && fileName.contains(".")) {
                    String fileType = fileName.substring(fileName
                            .lastIndexOf(".") + 1);
                    return !fileType.isEmpty() && fileType.trim().length() > 0
                            && fileType != "";
                }
            }
            return false;
        }
    };

    private String getResourcesString(int id, String mediaName) {
        Resources r = getResources();
        return r.getString(id, mediaName);
    }

    /**
     * draw the compose view at the bottom of the screen.
     */
    private void drawBottomPanel() {
        // Reset the counter for text editor.
        resetCounter();

        if (mWorkingMessage.hasSlideshow()) {
            mBottomPanel.setVisibility(View.GONE);
            mAttachmentEditor.requestFocus();
            return;
        }

        if (LOCAL_LOGV) {
            Log.v(TAG, "CMA.drawBottomPanel");
        }
        mBottomPanel.setVisibility(View.VISIBLE);
        CharSequence text = mWorkingMessage.getText();

        // TextView.setTextKeepState() doesn't like null input.
        if (text != null && mIsSmsEnabled) {
            mTextEditor.setTextKeepState(text);

            // Set the edit caret to the end of the text.
            mTextEditor.setSelection(mTextEditor.length());
        } else {
            mTextEditor.setText("");
        }
        onKeyboardStateChanged();
    }

    private void hideBottomPanel() {
        if (LOCAL_LOGV) {
            Log.v(TAG, "CMA.hideBottomPanel");
        }
        mBottomPanel.setVisibility(View.INVISIBLE);
    }

    private void drawTopPanel(boolean showSubjectEditor) {
        boolean showingAttachment = mAttachmentEditor.update(mWorkingMessage);
        mAttachmentEditorScrollView.setVisibility(showingAttachment ? View.VISIBLE : View.GONE);
        showSubjectEditor(showSubjectEditor || mWorkingMessage.hasSubject());
        if (mShowTwoButtons) {
            mAttachmentEditor.hideSlideshowSendButton();
        }
        int subjectSize = mWorkingMessage.hasSubject()
                ? mWorkingMessage.getSubject().toString().getBytes().length : 0;
        if (mWorkingMessage.getSlideshow()!= null) {
            mWorkingMessage.getSlideshow().setSubjectSize(subjectSize);
        }

        invalidateOptionsMenu();
        onKeyboardStateChanged();
    }

    //==========================================================
    // Interface methods
    //==========================================================


    @Override
    public void onClick(View v) {
        boolean isRcsAvailable = RcsApiManager.getSupportApi().isRcsSupported()
                && RcsApiManager.isRcsOnline();
        mWorkingMessage.setIsBurn(mIsBurnMessage);
        /* commented for no-touch feature phone*/
       /* if ((v == mSendButtonSms || v == mSendButtonMms) && isPreparedForSending()) {
            if (mShowTwoButtons) {
                confirmSendMessageIfNeeded(MSimConstants.SUB1);
            } else {
                confirmSendMessageIfNeeded();
            }
        } else if ((v == mSendButtonSmsViewSec || v == mSendButtonMmsViewSec) &&
                mShowTwoButtons && isPreparedForSending()) {
            confirmSendMessageIfNeeded(MSimConstants.SUB2);
        } else if (v == mButtonEmoj) {
            ViewStub viewStub = (ViewStub) findViewById(R.id.emoji_view_stub);
            showEmojiView(viewStub);
        }*/
    }

    private void showEmojiView(ViewStub emojiViewStub) {
        if (mRcsEmojiInitialize == null)
            mRcsEmojiInitialize = new RcsEmojiInitialize(this, emojiViewStub,
                    mViewOnClickListener);
        mRcsEmojiInitialize.closeOrOpenView();
    }

    private ViewOnClickListener mViewOnClickListener = new ViewOnClickListener() {
        @Override
        public void emojiSelectListener(EmoticonBO emoticonBO) {
            mWorkingMessage.setRcsType(RcsUtils.RCS_MSG_TYPE_PAID_EMO);
            mWorkingMessage.setRcsEmoId(emoticonBO.getEmoticonId());
            mWorkingMessage.setRcsEmoName(emoticonBO.getEmoticonName());
            mWorkingMessage.setIsBurn(mIsBurnMessage);
            rcsSend();
        }

        @Override
        public void faceTextSelectListener(String faceText) {
            CharSequence text = mTextEditor.getText() + faceText;
            mTextEditor.setText(text);
            mTextEditor.setSelection(text.length());
        }

        @Override
        public void onEmojiDeleteListener() {
            new Thread() {
                public void run() {
                    try {
                        Instrumentation inst = new Instrumentation();
                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                    } catch (Exception e) {
                        Log.e("Exception when sendKeyDownUpSync", e.toString());
                    }
                };
            }.start();
        }

        @Override
        public void addEmojiPackageListener() {
            RcsUtils.startEmojiStore(ComposeMessageActivity.this,
                    REQUEST_CODE_EMOJI_STORE);
        }

        @Override
        public void viewOpenOrCloseListener(boolean isOpen) {
            /* commented for no-touch feature phone*/
           /* if (isOpen) {
                mButtonEmoj.setImageResource(R.drawable.rcs_emotion_true);
            } else {
                mButtonEmoj.setImageResource(R.drawable.rcs_emotion_false);
            }*/
        }
    };

    private void launchRcsContactGroupPicker(int requestCode) {
        Intent intent = new Intent(this, MultiPickContactGroups.class);
        try {
            mIsPickingContact = true;
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.contact_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchContactGroupPicker() {
        Intent intent = new Intent(this, MultiPickContactGroups.class);
        try {
            mIsPickingContact = true;
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.contact_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchMultiplePhonePicker() {
        Intent intent = new Intent("com.android.contacts.action.MULTI_PICK",Contacts.CONTENT_URI);
        String exsitNumbers = mRecipientsEditor.getExsitNumbers();
        if (!TextUtils.isEmpty(exsitNumbers)) {
            intent.putExtra(Intents.EXTRA_PHONE_URIS, exsitNumbers);
        }
        try {
            mIsPickingContact = true;
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.contact_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchRcsPhonePicker() {
        Intent intent = new Intent(INTENT_MULTI_PICK, Contacts.CONTENT_URI);

        try {
            mIsPickingContact = true;
            startActivityForResult(intent, REQUEST_CODE_RCS_PICK);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.contact_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null) {
            // In the CMCC mode,if the enter key is down,insert the '\n' in TextView;
            if (!getResources().getBoolean(R.bool.config_enter_key_as_send) &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                return false;
            }

            // if shift key is down, then we want to insert the '\n' char in the TextView;
            // otherwise, the default action is to send the message.
            if (!event.isShiftPressed() && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (isPreparedForSending()) {
                    confirmSendMessageIfNeeded();
                }
                return true;
            }
            return false;
        }

        if (isPreparedForSending()) {
            confirmSendMessageIfNeeded();
        }
        return true;
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        private boolean mIsChanged = false;
        private String mTextBefore = "";

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (!mIsChanged) {
                mTextBefore = s.length() > 0 ? s.toString() : "";
             }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mIsChanged) {
                return;
            }
            if (mWorkingMessage.hasAttachment()) {
                if (!mAttachmentEditor.canAddTextForMms(s)) {
                    if (mTextEditor != null) {
                        mIsChanged = true;
                        mTextEditor.setText(mTextBefore);
                        mIsChanged = false;
                        Toast.makeText(ComposeMessageActivity.this,
                                R.string.cannot_add_text_anymore, Toast.LENGTH_SHORT).show();
                    }
                    mAttachmentEditor.canAddTextForMms(mTextBefore);
                    return;
                }
            }
            // This is a workaround for bug 1609057.  Since onUserInteraction() is
            // not called when the user touches the soft keyboard, we pretend it was
            // called when textfields changes.  This should be removed when the bug
            // is fixed.
            onUserInteraction();

            mWorkingMessage.setText(s);

            updateSendButtonState();

            updateCounter(s, start, before, count);

            ensureCorrectButtonHeight();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /**
     * Ensures that if the text edit box extends past two lines then the
     * button will be shifted up to allow enough space for the character
     * counter string to be placed beneath it.
     */
    private void ensureCorrectButtonHeight() {
        int currentTextLines = mTextEditor.getLineCount();
        if (currentTextLines <= 2) {
            mTextCounter.setVisibility(View.GONE);
        }
    }

    private final TextWatcher mSubjectEditorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().getBytes().length <= SUBJECT_MAX_LENGTH) {
                mWorkingMessage.setSubject(s, true);
                updateSendButtonState();
                if(s.toString().getBytes().length == SUBJECT_MAX_LENGTH
                        && before < SUBJECT_MAX_LENGTH) {
                    Toast.makeText(ComposeMessageActivity.this,
                            R.string.subject_full, Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().getBytes().length > SUBJECT_MAX_LENGTH) {
                String subject = s.toString();
                Toast.makeText(ComposeMessageActivity.this,
                        R.string.subject_full, Toast.LENGTH_SHORT).show();
                while(subject.getBytes().length > SUBJECT_MAX_LENGTH) {
                    subject = subject.substring(0, subject.length() - 1);
                }
                s.clear();
                s.append(subject);
            }
        }
    };

    //==========================================================
    // Private methods
    //==========================================================

    /**
     * Initialize all UI elements from resources.
     */
    private void initResourceRefs() {
        mMsgListView = (MessageListView) findViewById(R.id.history);
        mMsgListView.setDivider(null);      // no divider so we look like IM conversation.

        // called to enable us to show some padding between the message list and the
        // input field but when the message list is scrolled that padding area is filled
        // in with message content
        mMsgListView.setClipToPadding(false);
        mMsgListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mRcsEmojiInitialize != null)
                            mRcsEmojiInitialize.closeViewAndKB();
                        RcsUtils.closeKB(ComposeMessageActivity.this);
                        break;
                }
                return false;
            }
        });

        mMsgListView.setOnSizeChangedListener(new OnSizeChangedListener() {
            public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    Log.v(TAG, "onSizeChanged: w=" + width + " h=" + height +
                            " oldw=" + oldWidth + " oldh=" + oldHeight);
                }

                if (!mMessagesAndDraftLoaded && (oldHeight-height > SMOOTH_SCROLL_THRESHOLD)) {
                    // perform the delayed loading now, after keyboard opens
                    loadMessagesAndDraft(3);
                }


                // The message list view changed size, most likely because the keyboard
                // appeared or disappeared or the user typed/deleted chars in the message
                // box causing it to change its height when expanding/collapsing to hold more
                // lines of text.
                smoothScrollToEnd(false, height - oldHeight);
            }
        });

        if (mShowTwoButtons) {
            initTwoSendButton();
        } else {
            mBottomPanel = findViewById(R.id.bottom_panel);
            mBottomPanel.setVisibility(View.VISIBLE);
            mTextEditor = (EditText) findViewById(R.id.embedded_text_editor);
            mTextCounter = (TextView) findViewById(R.id.text_counter);
             /* commented for no-touch feature phone*/
            /*mSendButtonMms = (TextView) findViewById(R.id.send_button_mms);
            mSendButtonSms = (ImageButton) findViewById(R.id.send_button_sms);
            mButtonEmoj = (ImageButton)findViewById(R.id.send_emoj);
            mButtonEmoj.setOnClickListener(this);
            mSendButtonMms.setOnClickListener(this);
            mSendButtonSms.setOnClickListener(this);*/
        }
        mTextEditor.setOnEditorActionListener(this);
        mTextEditor.addTextChangedListener(mTextEditorWatcher);
        if (getResources().getInteger(R.integer.limit_count) == 0) {
            mTextEditor.setFilters(new InputFilter[] {
                    new LengthFilter(MmsConfig.getMaxTextLimit())});
        } else if (getResources().getInteger(R.integer.slide_text_limit_size) != 0) {
            mTextEditor.setFilters(new InputFilter[] {
                    new LengthFilter(getResources().getInteger(R.integer.slide_text_limit_size))});
        }
        mTextEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mAttachmentSelector.getVisibility() == View.VISIBLE) {
                    mAttachmentSelector.setVisibility(View.GONE);
                }
            }
        });
        mTextEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRcsEmojiInitialize != null)
                    mRcsEmojiInitialize.closeViewAndKB();
            }
        });

        mTopPanel = findViewById(R.id.recipients_subject_linear);
        mTopPanel.setFocusable(false);
        mAttachmentEditor = (AttachmentEditor) findViewById(R.id.attachment_editor);
        mAttachmentEditor.setHandler(mAttachmentEditorHandler);
        mAttachmentEditorScrollView = findViewById(R.id.attachment_editor_scroll_view);
        mAttachmentSelector = findViewById(R.id.attachments_selector);
        if (getResources().getBoolean(R.bool.config_two_call_button)) {
            initTwoCallButtonOnActionBar();
        }
         /* commented for no-touch feature phone*/
        /*setEmojBtnGone();*/
    }
     /* commented for no-touch feature phone*/
    /*private void setEmojBtnGone(){
        boolean isRcsAvailable = RcsApiManager.getSupportApi().isRcsSupported()
                && RcsApiManager.isRcsOnline();
        if (!isRcsAvailable) {
            mButtonEmoj.setVisibility(View.GONE);
        }
    }*/

    private void initTwoSendButton() {
        /* commented for no-touch feature phone*/
       /* mBottomPanel = findViewById(R.id.bottom_panel_btnstyle);
        mBottomPanel.setVisibility(View.VISIBLE);*/
        mTextEditor = (EditText) findViewById(R.id.embedded_text_editor_btnstyle);

        mTextCounter = (TextView) findViewById(R.id.text_counter_two_buttons);
        /*mSendButtonMms = (TextView) findViewById(R.id.first_send_button_mms_view);
        mSendButtonSms = (ImageButton) findViewById(R.id.first_send_button_sms_view);
        mSendLayoutMmsFir = findViewById(R.id.first_send_button_mms);
        mSendLayoutSmsFir = findViewById(R.id.first_send_button_sms);
        mIndicatorForSimMmsFir = (ImageView) findViewById(R.id.first_sim_card_indicator_mms);
        mIndicatorForSimSmsFir = (ImageView) findViewById(R.id.first_sim_card_indicator_sms);
        mIndicatorForSimMmsFir.setImageDrawable(MessageUtils
               .getMultiSimIcon(this, MSimConstants.SUB1));
        mIndicatorForSimSmsFir.setImageDrawable(MessageUtils
                .getMultiSimIcon(this, MSimConstants.SUB1));
        mSendButtonMms.setOnClickListener(this);
        mSendButtonSms.setOnClickListener(this);

        mSendButtonMmsViewSec = (TextView) findViewById(R.id.second_send_button_mms_view);
        mSendButtonSmsViewSec = (ImageButton) findViewById(R.id.second_send_button_sms_view);
        mSendLayoutMmsSec = findViewById(R.id.second_send_button_mms);
        mSendLayoutSmsSec = findViewById(R.id.second_send_button_sms);
        mIndicatorForSimMmsSec = (ImageView) findViewById(R.id.second_sim_card_indicator_mms);
        mIndicatorForSimSmsSec = (ImageView) findViewById(R.id.second_sim_card_indicator_sms);
        mIndicatorForSimMmsSec.setImageDrawable(MessageUtils
               .getMultiSimIcon(this, MSimConstants.SUB2));
        mIndicatorForSimSmsSec.setImageDrawable(MessageUtils
                .getMultiSimIcon(this, MSimConstants.SUB2));
        mSendButtonMmsViewSec.setOnClickListener(this);
        mSendButtonSmsViewSec.setOnClickListener(this);
        mButtonEmoj = (ImageButton)findViewById(R.id.send_emoj_btnstyle);
        mButtonEmoj.setOnClickListener(this);*/
    }

    private void confirmDeleteDialog(OnClickListener listener, boolean locked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setMessage(locked ? R.string.confirm_delete_locked_message :
                    R.string.confirm_delete_message);
        builder.setPositiveButton(R.string.delete, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    void undeliveredMessageDialog(long date) {
        String body;

        if (date >= 0) {
            body = getString(R.string.undelivered_msg_dialog_body,
                    MessageUtils.formatTimeStampString(this, date));
        } else {
            // FIXME: we can not get sms retry time.
            body = getString(R.string.undelivered_sms_dialog_body);
        }

        Toast.makeText(this, body, Toast.LENGTH_LONG).show();
    }

    private void startMsgListQuery() {
        startMsgListQuery(MESSAGE_LIST_QUERY_TOKEN);
    }

    private void startMsgListQuery(int token) {
        if (mSendDiscreetMode || MessageUtils.isMailboxMode()) {
            return;
        }
        Uri conversationUri = mConversation.getUri();

        if (conversationUri == null) {
            log("##### startMsgListQuery: conversationUri is null, bail!");
            return;
        }

        long threadId = mConversation.getThreadId();
        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("startMsgListQuery for " + conversationUri + ", threadId=" + threadId +
                    " token: " + token + " mConversation: " + mConversation);
        }

        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(token);
        try {
            // Kick off the new query
            mBackgroundQueryHandler.startQuery(
                    token,
                    threadId /* cookie */,
                    conversationUri,
                    PROJECTION,
                    null, null, null);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private void initMessageList() {
        if (mMsgListAdapter != null) {
            return;
        }

        // Set the flag of mIsFromSearchActivity
        mIsFromSearchActivity = getIntent().getBooleanExtra("from_search", false);
        String highlightString = getIntent().getStringExtra("highlight");
        Pattern highlight = highlightString == null
            ? null
            : Pattern.compile("\\b" + Pattern.quote(highlightString), Pattern.CASE_INSENSITIVE);

        // Initialize the list adapter with a null cursor.
        mMsgListAdapter = new MessageListAdapter(this, null, mMsgListView, true, highlight);
        mMsgListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
        mMsgListAdapter.setMsgListItemHandler(mMessageListItemHandler);
        mMsgListView.setAdapter(mMsgListAdapter);
        mMsgListView.setItemsCanFocus(false);
        mMsgListView.setVisibility((mSendDiscreetMode || MessageUtils.isMailboxMode())
                ? View.INVISIBLE : View.VISIBLE);
        mMsgListView.setOnCreateContextMenuListener(mMsgListMenuCreateListener);
        // Show context menu only when long click message body.
        mMsgListView.setLongClickable(false);
    }

    /**
     * Load the draft
     *
     * If mWorkingMessage has content in memory that's worth saving, return false.
     * Otherwise, call the async operation to load draft and return true.
     */
    private boolean loadDraft() {
        if (mWorkingMessage.isWorthSaving()) {
            Log.w(TAG, "CMA.loadDraft: called with non-empty working message, bail");
            return false;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("CMA.loadDraft");
        }

        mWorkingMessage = WorkingMessage.loadDraft(this, mConversation,
                new Runnable() {
                    @Override
                    public void run() {
                        updateMmsSizeIndicator();
                        // It decides whether or not to display the subject editText view,
                        // according to the situation whether there's subject
                        // or the editText view is visible before leaving it.
                        drawTopPanel(isSubjectEditorVisible());
                        drawBottomPanel();
                        updateSendButtonState();
                    }
                });

        // WorkingMessage.loadDraft() can return a new WorkingMessage object that doesn't
        // have its conversation set. Make sure it is set.
        mWorkingMessage.setConversation(mConversation);

        return true;
    }

    private void saveDraft(boolean isStopping) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("saveDraft");
        }
        // TODO: Do something better here.  Maybe make discard() legal
        // to call twice and make isEmpty() return true if discarded
        // so it is caught in the clause above this one?
        if (mWorkingMessage.isDiscarded()) {
            return;
        }

        if ((!mWaitingForSubActivity &&
                !mWorkingMessage.isWorthSaving() &&
                (!isRecipientsEditorVisible() || recipientCount() == 0)) ||
                (MessageUtils.checkIsPhoneMessageFull(this, !mWorkingMessage.requiresMms()))) {
            if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("not worth saving, discard WorkingMessage and bail");
            }
            mWorkingMessage.discard();
            return;
        }

        mWorkingMessage.saveDraft(isStopping);

        if (mToastForDraftSave) {
            Toast.makeText(this, R.string.message_saved_as_draft,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPreparedForSending() {
        int recipientCount = recipientCount();

        if (mIsAirplaneModeOn) {
            return false;
        }

        if (mConversation.isGroupChat()) {
            return (!mSentMessage && mConversation.getGroupChat() == null && recipientCount > 0
                    && (mWorkingMessage.hasAttachment() || mWorkingMessage.hasText()
                    || mWorkingMessage.hasSubject())) || mConversation.isGroupChatActive();
        }

        return (MessageUtils.getActivatedIccCardCount() > 0 || isCdmaNVMode()) &&
                recipientCount > 0 && recipientCount <= MmsConfig.getRecipientLimit() &&
                mIsSmsEnabled &&
                (mWorkingMessage.hasAttachment() || mWorkingMessage.hasText() ||
                    mWorkingMessage.hasSubject());
    }

    private boolean isCdmaNVMode() {
        if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            Log.d(TAG, "isCdmaNVMode: CDMA NV mode just for single SIM");
            return false;
        }
        int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();
        int cdmaSubscriptionMode = Settings.Global.getInt(getContentResolver(),
                Settings.Global.CDMA_SUBSCRIPTION_MODE, CDMA_SUBSCRIPTION_NV);
        Log.d(TAG, "isCdmaNVMode: activePhoneType=" + activePhoneType + " cdmaSubscriptionMode="
                + cdmaSubscriptionMode);
        if ((activePhoneType == TelephonyManager.PHONE_TYPE_CDMA) &&
                cdmaSubscriptionMode == CDMA_SUBSCRIPTION_NV) {
            return true;
        }
        return false;
    }

    private int recipientCount() {
        int recipientCount;

        // To avoid creating a bunch of invalid Contacts when the recipients
        // editor is in flux, we keep the recipients list empty.  So if the
        // recipients editor is showing, see if there is anything in it rather
        // than consulting the empty recipient list.
        if (isRecipientsEditorVisible()) {
            recipientCount = mRecipientsEditor.getRecipientCount();
        } else {
            recipientCount = getRecipients().size();
        }
        return recipientCount;
    }

    private boolean checkMessageSizeExceeded(){
        int messageSizeLimit = MmsConfig.getMaxMessageSize();
        int mmsCurrentSize = 0;
        boolean indicatorSizeOvered = false;
        SlideshowModel slideShow = mWorkingMessage.getSlideshow();
        if (slideShow != null) {
            mmsCurrentSize = slideShow.getTotalMessageSize();
            // The AttachmentEditor only can edit text if there only one silde.
            // And the slide already includes text size, need to recalculate the total size.
            if (mWorkingMessage.hasText() && slideShow.size() == 1) {
                int totalTextSize = slideShow.getTotalTextMessageSize();
                int currentTextSize = mWorkingMessage.getText().toString().getBytes().length;
                int subjectSize = slideShow.getSubjectSize();
                mmsCurrentSize = mmsCurrentSize - totalTextSize + currentTextSize;
                indicatorSizeOvered = getSizeWithOverHead(mmsCurrentSize + subjectSize)
                        > (MmsConfig.getMaxMessageSize() / KILOBYTE);
            }
        } else if (mWorkingMessage.hasText()) {
            mmsCurrentSize = mWorkingMessage.getText().toString().getBytes().length;
        }
        Log.v(TAG, "compose mmsCurrentSize = " + mmsCurrentSize
                + ", indicatorSizeOvered = " + indicatorSizeOvered);
        // Mms max size is 300k, but we reserved 1k just in case there are other over size problem.
        // In this way, here the first condition will always false.
        // Therefore add indicatorSizeOvered in it.
        // If indicator displays larger than 300k, it can not send this Mms.
        if (mmsCurrentSize > messageSizeLimit || indicatorSizeOvered) {
            mIsAttachmentErrorOnSend = true;
            handleAddAttachmentError(WorkingMessage.MESSAGE_SIZE_EXCEEDED,
                    R.string.type_picture);
            return true;
        }
        return false;
    }

    private int getSizeWithOverHead(int size) {
        return (size + KILOBYTE -1) / KILOBYTE + 1;
    }

    private void sendMessage(boolean bCheckEcmMode) {
        // Check message size, if >= max message size, do not send message.
        if(checkMessageSizeExceeded()){
            return;
        }

        // If message is sent make the mIsMessageChanged is true
        // when activity is from SearchActivity.
        mIsMessageChanged = mIsFromSearchActivity;
        if (bCheckEcmMode) {
            // TODO: expose this in telephony layer for SDK build
            String inEcm = SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE);
            if (Boolean.parseBoolean(inEcm)) {
                try {
                    startActivityForResult(
                            new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                            REQUEST_CODE_ECM_EXIT_DIALOG);
                    return;
                } catch (ActivityNotFoundException e) {
                    // continue to send message
                    Log.e(TAG, "Cannot find EmergencyCallbackModeExitDialog", e);
                }
            }
        }

        if (!mSendingMessage) {
            if (LogTag.SEVERE_WARNING) {
                String sendingRecipients = mConversation.getRecipients().serialize();
                if (!sendingRecipients.equals(mDebugRecipients)) {
                    String workingRecipients = mWorkingMessage.getWorkingRecipients();
                    if (mDebugRecipients != null && !mDebugRecipients.equals(workingRecipients)) {
                        LogTag.warnPossibleRecipientMismatch("ComposeMessageActivity.sendMessage" +
                                " recipients in window: \"" +
                                mDebugRecipients + "\" differ from recipients from conv: \"" +
                                sendingRecipients + "\" and working recipients: " +
                                workingRecipients, this);
                    }
                }
                sanityCheckConversation();
            }

            // send can change the recipients. Make sure we remove the listeners first and then add
            // them back once the recipient list has settled.
            removeRecipientsListeners();

            String recipient;
            if (mWorkingMessage.getResendMultiRecipients()) {
                // If resend sms recipient is more than one, use mResendSmsRecipient
                recipient = mResendSmsRecipient;
            } else {
                recipient = mDebugRecipients;
            }

            if (mConversation.isGroupChat()) {
                createGroupChatOrSendGroupChatMessage(recipient);
            } else {
                mWorkingMessage.send(recipient);

                mSentMessage = true;
                mSendingMessage = true;
            }

            addRecipientsListeners();

            mScrollOnSend = true;   // in the next onQueryComplete, scroll the list to the end.
        }
        // But bail out if we are supposed to exit after the message is sent.
        if (mSendDiscreetMode || MessageUtils.isMailboxMode()) {
            finish();
        }
    }
    /*
     * If group chat is created, send the group chat message. Otherwise create a
     * new RCS group chat.
     */
    private void createGroupChatOrSendGroupChatMessage(String recipient) {
        try {
            Log.d(RCS_TAG, "sendMessage(): isGroupChat=" + mConversation.isGroupChat()
                    + ", isRcsEnabled=" + mIsRcsEnabled + ", isOnline=" + mAccountApi.isOnline());
            if (mIsRcsEnabled && mSupportApi.isOnline()) {
                // create group chat if needed.
                GroupChatModel groupChat = mConversation.getGroupChat();
                if (groupChat == null) {
                    if (recipient != null) {
                        String[] dests = TextUtils.split(recipient, ";");
                        List<String> users = Arrays.asList(dests);
                        String subject = mWorkingMessage.getText().toString();
                        if (subject.getBytes().length > 30) {
                            toast(R.string.create_groupchat_name_toast);
                        }
                        // Make sure the subject is less than 30 bytes length.
                        subject = RcsUtils.trimToSpecificBytesLength(subject, 30);
                        mConfApi.createGroupChat(subject, users);

                        if (mCreateGroupChatCallback == null) {
                            mCreateGroupChatCallback = new CreateGroupChatCallback();
                        }
                        mCreateGroupChatCallback.onBegin();
                    } else {
                        toast(R.string.rcs_service_is_not_available);
                    }
                } else {
                    mWorkingMessage.send(recipient);

                    mSentMessage = true;
                    mSendingMessage = true;
                }
            } else {
                toast(R.string.rcs_service_is_not_available);
            }
        } catch (ServiceDisconnectedException e) {
            toast(R.string.rcs_service_is_not_available);
            Log.w(RCS_TAG, e);
        }
    }

    private BroadcastReceiver mRcsServiceCallbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();

            Log.d(RCS_TAG, "============ onReceive ============");
            Log.d(RCS_TAG, "action=" + action);
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Log.d(RCS_TAG, key + "=" + extras.get(key));
                }

                if (BroadcastConstants.UI_GROUP_MANAGE_NOTIFY.equals(action)) {
                    handleRcsGroupChatManagement(extras);
                } else if (BroadcastConstants.UI_SHOW_MESSAGE_SEND_ERROR.equals(action)) {
                    handleRcsMessageSendError(extras);
                } else if (BroadcastConstants.UI_ALERT_FILE_TOO_LARGE.equals(action)) {
                    toast(R.string.file_size_over);
                } else if (BroadcastConstants.UI_ALERT_FILE_SUFFIX_INVALID.equals(action)) {
                    toast(R.string.file_suffix_vaild_tip);
                } else {
                    Log.d(RCS_TAG, "Abort handdling broadcast: " + action);
                }
            }
        }
    };

    private BroadcastReceiver mPhotoUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mMsgListAdapter.notifyDataSetChanged();
        }
    };

    private BroadcastReceiver mAirplaneModeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
                updateSendButtonState();
            }
        }
    };

    private void handleRcsMessageSendError(Bundle extras) {
        String tickerText = extras.getString(BroadcastConstants.BC_VAR_MSG_TICKERTEXT);
        if (BroadcastConstants.BC_VAR_SEND_ERROR_NOT_REG.equals(tickerText)) {
            toast(R.string.rcs_service_is_not_available);
        } else if (BroadcastConstants.BC_VAR_SEND_ERROR_GROUP_HAS_DELETED.equals(tickerText)) {
            toast(R.string.group_chat_deleted);
        } else if (BroadcastConstants.BC_VAR_SEND_ERROR_GROUP_NOT_COMPLETED.equals(tickerText)) {
            toast(R.string.group_chat_not_active);
        } else if (BroadcastConstants.BC_VAR_SEND_ERROR_GROUP_NOT_EXIST.equals(tickerText)) {
            toast(R.string.group_chat_not_exist);
        } else {
            toast(R.string.send_message_failed);
        }
        mSendingMessage = false;
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void handleRcsGroupChatManagement(Bundle extras) {
        String actionType = extras.getString(BroadcastConstants.BC_VAR_MSG_ACTION_TYPE);

        if (BroadcastConstants.ACTION_TYPE_CREATE_NOT_ACTIVE.equals(actionType)) {
            // The group chat is created successfully
            handleRcsGroupChatCreateNotActive(extras);
        } else if (BroadcastConstants.ACTION_TYPE_CREATE.equals(actionType)) {
            handleRcsGroupChatCreate(extras);
        } else if (BroadcastConstants.ACTION_TYPE_UPDATE_SUBJECT.equals(actionType)) {
            handleRcsGroupChatUpdateSubject(extras);
        } else if (BroadcastConstants.ACTION_TYPE_UPDATE_REMARK.equals(actionType)) {
            handleRcsGroupChatUpdateRemark(extras);
        } else if (BroadcastConstants.ACTION_TYPE_DELETED.equals(actionType)) {
            handleRcsGroupChatDeleted(extras);
        }
    }

    private void handleRcsGroupChatCreateNotActive(Bundle extras) {
        if(mCreateGroupChatCallback != null){
            mCreateGroupChatCallback.onDone(true);
            mCreateGroupChatCallback.onEnd();
        }

        String groupId = extras.getString(BroadcastConstants.BC_VAR_GROUP_ID);
        try {
            GroupChatModel groupChat = mMessageApi.getGroupChatById(groupId);
            mConversation.setGroupChat(groupChat);
            mWorkingMessage.setConversation(mConversation);
            Log.d(RCS_TAG, groupChat.toString());
            notifyChangeGroupChat(groupId);
            // We already received the first group chat management notifiaction message.
            mSentMessage = true;

            // Reset text editor, receipents editor and update title.
            runOnUiThread(mResetMessageRunnable);
            mConversation.ensureThreadId();
            onMessageSent();
            updateTitle(new ContactList());
        } catch (ServiceDisconnectedException e) {
            Log.w(RCS_TAG, e);
        }
    }

    private void handleRcsGroupChatDeleted(Bundle extras) {
        String groupId = extras.getString(BroadcastConstants.BC_VAR_GROUP_ID);
        if (groupId == null) {
            return;
        }

        GroupChatModel groupChat = mConversation.getGroupChat();
        if (groupChat != null) {
            if (groupId.equals(String.valueOf(groupChat.getId()))) {
                groupChat.setStatus(GroupChatModel.GROUP_STATUS_DELETED);
                // Reset text editor, receipents editor and update title.
                runOnUiThread(mResetMessageRunnable);
                updateTitle(new ContactList());
                RcsUtils.disBandGroupChat(ComposeMessageActivity.this,groupChat);
            }
        }
    }

    private void handleRcsGroupChatCreate(Bundle extras) {
        String groupId = extras.getString(BroadcastConstants.BC_VAR_GROUP_ID);
        GroupChatModel groupChat = mConversation.getGroupChat();
        Log.d(RCS_TAG, "handleRcsGroupChatCreate(): groupId=" + groupId + ", groupChat="
                + groupChat);
        if (groupChat != null) {
            if (groupId.equals(String.valueOf(groupChat.getId()))) {
                Log.d(RCS_TAG, groupChat.toString());
                groupChat.setStatus(GroupChatModel.GROUP_STATUS_COMPETE);
                mConversation.setGroupChat(groupChat);
                mWorkingMessage.setConversation(mConversation);
                // Reset text editor, receipents editor and update title.
                runOnUiThread(mResetMessageRunnable);
                updateTitle(new ContactList());
                notifyChangeGroupChat(groupId);
                Toast.makeText(ComposeMessageActivity.this, R.string.group_chat_status_ok,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleRcsGroupChatUpdateSubject(Bundle extras) {
        GroupChatModel groupChat = mConversation.getGroupChat();
        if (groupChat != null) {
            String groupId = extras.getString(BroadcastConstants.BC_VAR_GROUP_ID);
            if (groupId != null && groupId.equals(String.valueOf(groupChat.getId()))) {
                String newSubject = extras.getString(BroadcastConstants.BC_VAR_GROUP_SUBJECT);
                Log.d(RCS_TAG, "update group subject: " + groupChat.getSubject() + " -> "
                        + newSubject);
                groupChat.setSubject(newSubject);

                updateTitle(new ContactList());
            }
        }
    }

    private void handleRcsGroupChatUpdateRemark(Bundle extras) {
        GroupChatModel groupChat = mConversation.getGroupChat();
        if (groupChat != null) {
            String groupId = extras.getString(BroadcastConstants.BC_VAR_GROUP_ID);
            if (groupId != null && groupId.equals(String.valueOf(groupChat.getId()))) {
                String newRemark = extras.getString(BroadcastConstants.BC_VAR_GROUP_REMARK);
                Log.d(RCS_TAG, "update group subject: " + groupChat.getRemark() + " -> "
                        + newRemark);
                groupChat.setRemark(newRemark);

                updateTitle(new ContactList());
            }
        }
    }

    private void resetMessage() {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("resetMessage");
        }

        // Make the attachment editor hide its view.
        mAttachmentEditor.hideView();
        mAttachmentEditorScrollView.setVisibility(View.GONE);

        // Hide the subject editor
        showSubjectEditor(false);

        // Focus to the text editor.
        mTextEditor.requestFocus();

        // We have to remove the text change listener while the text editor gets cleared and
        // we subsequently turn the message back into SMS. When the listener is listening while
        // doing the clearing, it's fighting to update its counts and itself try and turn
        // the message one way or the other.
        mTextEditor.removeTextChangedListener(mTextEditorWatcher);

        // Clear the text box.
        TextKeyListener.clear(mTextEditor.getText());

        mWorkingMessage.clearConversation(mConversation, false);
        mWorkingMessage = WorkingMessage.createEmpty(this);
        mWorkingMessage.setConversation(mConversation);

        hideRecipientEditor();
        drawBottomPanel();

        // "Or not", in this case.
        updateSendButtonState();

        // Our changes are done. Let the listener respond to text changes once again.
        mTextEditor.addTextChangedListener(mTextEditorWatcher);

        // Close the soft on-screen keyboard if we're in landscape mode so the user can see the
        // conversation.
        if (mIsLandscape) {
            hideKeyboard();
        }

        mLastRecipientCount = 0;
        mSendingMessage = false;
        invalidateOptionsMenu();
        if (mAttachmentSelector.getVisibility() == View.VISIBLE) {
            mAttachmentSelector.setVisibility(View.GONE);
        }
   }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
            (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mTextEditor.getWindowToken(), 0);
    }

    private void updateSendButtonState() {
        boolean enable = false;
        if (isPreparedForSending()) {
            enable = true;
        }

        // invalidate the menu whether the message can be send or can't.
        invalidateOptionsMenu();
        boolean requiresMms = mWorkingMessage.requiresMms();
         /* commented for no-touch feature phone*/
        /*if (mShowTwoButtons) {
            View[] sendButtons = showTwoSmsOrMmsSendButton(requiresMms);
            if (sendButtons[MSimConstants.SUB1] == mSendLayoutMmsFir
                    && sendButtons[MSimConstants.SUB2] == mSendLayoutMmsSec) {
                mSendButtonMms.setEnabled(enable);
                mSendButtonMmsViewSec.setEnabled(enable);
                mSendButtonMms.setFocusable(enable);
                mSendButtonMmsViewSec.setFocusable(enable);
            } else if (sendButtons[MSimConstants.SUB1] == mSendLayoutSmsFir
                    && sendButtons[MSimConstants.SUB2] == mSendLayoutSmsSec) {
                mSendButtonSms.setEnabled(enable);
                mSendButtonSmsViewSec.setEnabled(enable);
                mSendButtonSms.setFocusable(enable);
                mSendButtonSmsViewSec.setFocusable(enable);
            }
        } else {
            View sendButton = showSmsOrMmsSendButton(requiresMms);
            sendButton.setEnabled(enable);
            sendButton.setFocusable(enable);
        }*/
    }

    private long getMessageDate(Uri uri) {
        if (uri != null) {
            Cursor cursor = SqliteWrapper.query(this, mContentResolver,
                    uri, new String[] { Mms.DATE }, null, null, null);
            if (cursor != null) {
                try {
                    if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                        return cursor.getLong(0) * 1000L;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return NO_DATE_FOR_DIALOG;
    }

    private void initActivityState(Bundle bundle) {
        Intent intent = getIntent();
        if (bundle != null) {
            setIntent(getIntent().setAction(Intent.ACTION_VIEW));
            String recipients = bundle.getString(RECIPIENTS);
            if (LogTag.VERBOSE) log("get mConversation by recipients " + recipients);
            mConversation = Conversation.get(this,
                    ContactList.getByNumbers(recipients,
                            false /* don't block */, true /* replace number */), false);
            addRecipientsListeners();
            mSendDiscreetMode = bundle.getBoolean(KEY_EXIT_ON_SENT, false);
            mForwardMessageMode = bundle.getBoolean(KEY_FORWARDED_MESSAGE, false);

            if (mSendDiscreetMode) {
                mMsgListView.setVisibility(View.INVISIBLE);
            }
            mWorkingMessage.readStateFromBundle(bundle);

            return;
        }

        // If we have been passed a thread_id, use that to find our conversation.
        long threadId = intent.getLongExtra(THREAD_ID, 0);
        if (threadId > 0) {
            if (LogTag.VERBOSE) log("get mConversation by threadId " + threadId);
            mConversation = Conversation.get(this, threadId, false);
        } else {
            Uri intentData = intent.getData();
            if (intentData != null) {
                // try to get a conversation based on the data URI passed to our intent.
                if (LogTag.VERBOSE) log("get mConversation by intentData " + intentData);
                mConversation = Conversation.get(this, intentData, false);
                mWorkingMessage.setText(getBody(intentData));
            } else {
                // special intent extra parameter to specify the address
                String address = intent.getStringExtra("address");
                if (!TextUtils.isEmpty(address)) {
                    if (intent.getBooleanExtra("isGroupChat", false)) {
                        if (LogTag.VERBOSE) log("create new conversation");
                        mConversation = Conversation.createNew(this);
                        mConversation.setRecipients(ContactList.getByNumbers(address,
                                false /* don't block */, true /* replace number */));
                        mConversation.setIsGroupChat(true);
                    } else {
                        if (LogTag.VERBOSE) log("get mConversation by address " + address);
                        mConversation = Conversation.get(this, ContactList.getByNumbers(address,
                                false /* don't block */, true /* replace number */), false);
                    }
                } else {
                    if (LogTag.VERBOSE) log("create new conversation");
                    mConversation = Conversation.createNew(this);
                }
            }
        }
        addRecipientsListeners();
        updateThreadIdIfRunning();

        mSendDiscreetMode = intent.getBooleanExtra(KEY_EXIT_ON_SENT, false);
        mForwardMessageMode = intent.getBooleanExtra(KEY_FORWARDED_MESSAGE, false);
        mReplyMessageMode = intent.getBooleanExtra(KEY_REPLY_MESSAGE, false);
        if (mSendDiscreetMode) {
            mMsgListView.setVisibility(View.INVISIBLE);
        }
        if (intent.hasExtra("sms_body")) {
            mWorkingMessage.setText(intent.getStringExtra("sms_body"));
        }
        mWorkingMessage.setSubject(intent.getStringExtra("subject"), false);
    }

    private void initFocus() {
        if (!mIsKeyboardOpen) {
            return;
        }

        // If the recipients editor is visible, there is nothing in it,
        // and the text editor is not already focused, focus the
        // recipients editor.
        if (isRecipientsEditorVisible()
                && TextUtils.isEmpty(mRecipientsEditor.getText())
                && !mTextEditor.isFocused()) {
            mRecipientsEditor.requestFocus();
            return;
        }

        // If we decided not to focus the recipients editor, focus the text editor.
        mTextEditor.requestFocus();
    }

    private final MessageListAdapter.OnDataSetChangedListener
                    mDataSetChangedListener = new MessageListAdapter.OnDataSetChangedListener() {
        @Override
        public void onDataSetChanged(MessageListAdapter adapter) {
        }

        @Override
        public void onContentChanged(MessageListAdapter adapter) {
            startMsgListQuery();
        }
    };

    /**
     * smoothScrollToEnd will scroll the message list to the bottom if the list is already near
     * the bottom. Typically this is called to smooth scroll a newly received message into view.
     * It's also called when sending to scroll the list to the bottom, regardless of where it is,
     * so the user can see the just sent message. This function is also called when the message
     * list view changes size because the keyboard state changed or the compose message field grew.
     *
     * @param force always scroll to the bottom regardless of current list position
     * @param listSizeChange the amount the message list view size has vertically changed
     */
    private void smoothScrollToEnd(boolean force, int listSizeChange) {
        int lastItemVisible = mMsgListView.getLastVisiblePosition();
        int lastItemInList = mMsgListAdapter.getCount() - 1;
        if (lastItemVisible < 0 || lastItemInList < 0) {
            if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                Log.v(TAG, "smoothScrollToEnd: lastItemVisible=" + lastItemVisible +
                        ", lastItemInList=" + lastItemInList +
                        ", mMsgListView not ready");
            }
            return;
        }

        View lastChildVisible =
                mMsgListView.getChildAt(lastItemVisible - mMsgListView.getFirstVisiblePosition());
        int lastVisibleItemBottom = 0;
        int lastVisibleItemHeight = 0;
        if (lastChildVisible != null) {
            lastVisibleItemBottom = lastChildVisible.getBottom();
            lastVisibleItemHeight = lastChildVisible.getHeight();
        }

        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.v(TAG, "smoothScrollToEnd newPosition: " + lastItemInList +
                    " mLastSmoothScrollPosition: " + mLastSmoothScrollPosition +
                    " first: " + mMsgListView.getFirstVisiblePosition() +
                    " lastItemVisible: " + lastItemVisible +
                    " lastVisibleItemBottom: " + lastVisibleItemBottom +
                    " lastVisibleItemBottom + listSizeChange: " +
                    (lastVisibleItemBottom + listSizeChange) +
                    " mMsgListView.getHeight() - mMsgListView.getPaddingBottom(): " +
                    (mMsgListView.getHeight() - mMsgListView.getPaddingBottom()) +
                    " listSizeChange: " + listSizeChange);
        }
        // Only scroll if the list if we're responding to a newly sent message (force == true) or
        // the list is already scrolled to the end. This code also has to handle the case where
        // the listview has changed size (from the keyboard coming up or down or the message entry
        // field growing/shrinking) and it uses that grow/shrink factor in listSizeChange to
        // compute whether the list was at the end before the resize took place.
        // For example, when the keyboard comes up, listSizeChange will be negative, something
        // like -524. The lastChild listitem's bottom value will be the old value before the
        // keyboard became visible but the size of the list will have changed. The test below
        // add listSizeChange to bottom to figure out if the old position was already scrolled
        // to the bottom. We also scroll the list if the last item is taller than the size of the
        // list. This happens when the keyboard is up and the last item is an mms with an
        // attachment thumbnail, such as picture. In this situation, we want to scroll the list so
        // the bottom of the thumbnail is visible and the top of the item is scroll off the screen.
        int listHeight = mMsgListView.getHeight();
        boolean lastItemTooTall = lastVisibleItemHeight > listHeight;
        boolean willScroll = force ||
                ((listSizeChange != 0 || lastItemInList != mLastSmoothScrollPosition) &&
                lastVisibleItemBottom + listSizeChange <=
                    listHeight - mMsgListView.getPaddingBottom());
        if (willScroll || (lastItemTooTall && lastItemInList == lastItemVisible)) {
            if (Math.abs(listSizeChange) > SMOOTH_SCROLL_THRESHOLD) {
                // When the keyboard comes up, the window manager initiates a cross fade
                // animation that conflicts with smooth scroll. Handle that case by jumping the
                // list directly to the end.
                if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    Log.v(TAG, "keyboard state changed. setSelection=" + lastItemInList);
                }
                if (lastItemTooTall) {
                    // If the height of the last item is taller than the whole height of the list,
                    // we need to scroll that item so that its top is negative or above the top of
                    // the list. That way, the bottom of the last item will be exposed above the
                    // keyboard.
                    mMsgListView.setSelectionFromTop(lastItemInList,
                            listHeight - lastVisibleItemHeight);
                } else {
                    mMsgListView.setSelection(lastItemInList);
                }
            } else if (lastItemInList - lastItemVisible > MAX_ITEMS_TO_INVOKE_SCROLL_SHORTCUT) {
                if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    Log.v(TAG, "too many to scroll, setSelection=" + lastItemInList);
                }
                mMsgListView.setSelection(lastItemInList);
            } else {
                if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    Log.v(TAG, "smooth scroll to " + lastItemInList);
                }
                if (lastItemTooTall) {
                    // If the height of the last item is taller than the whole height of the list,
                    // we need to scroll that item so that its top is negative or above the top of
                    // the list. That way, the bottom of the last item will be exposed above the
                    // keyboard. We should use smoothScrollToPositionFromTop here, but it doesn't
                    // seem to work -- the list ends up scrolling to a random position.
                    mMsgListView.setSelectionFromTop(lastItemInList,
                            listHeight - lastVisibleItemHeight);
                } else {
                    mMsgListView.smoothScrollToPosition(lastItemInList);
                }
                mLastSmoothScrollPosition = lastItemInList;
            }
        }
    }

    private final class BackgroundQueryHandler extends ConversationQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch(token) {
                case MESSAGE_LIST_QUERY_TOKEN:
                    mConversation.blockMarkAsRead(false);

                    // check consistency between the query result and 'mConversation'
                    long tid = (Long) cookie;

                    if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        log("##### onQueryComplete: msg history result for threadId " + tid);
                    }
                    if (tid != mConversation.getThreadId()) {
                        if (mConversation.getThreadId() == 0) {
                            mConversation.setThreadId(tid);
                        } else {
                            log("onQueryComplete: msg history query result is for threadId " +
                                    tid + ", but mConversation has threadId " +
                                    mConversation.getThreadId() + " starting a new query");
                            if (cursor != null) {
                                cursor.close();
                            }
                            startMsgListQuery();
                            return;
                        }
                    }

                    // check consistency b/t mConversation & mWorkingMessage.mConversation
                    ComposeMessageActivity.this.sanityCheckConversation();

                    int newSelectionPos = -1;
                    long targetMsgId = getIntent().getLongExtra("select_id", -1);
                    if (targetMsgId != -1) {
                        if (cursor != null) {
                            cursor.moveToPosition(-1);
                            while (cursor.moveToNext()) {
                                long msgId = cursor.getLong(COLUMN_ID);
                                if (msgId == targetMsgId) {
                                    newSelectionPos = cursor.getPosition();
                                    break;
                                }
                            }
                        }
                    } else if (mSavedScrollPosition != -1) {
                        // mSavedScrollPosition is set when this activity pauses. If equals maxint,
                        // it means the message list was scrolled to the end. Meanwhile, messages
                        // could have been received. When the activity resumes and we were
                        // previously scrolled to the end, jump the list so any new messages are
                        // visible.
                        if (mSavedScrollPosition == Integer.MAX_VALUE) {
                            int cnt = mMsgListAdapter.getCount();
                            if (cnt > 0) {
                                // Have to wait until the adapter is loaded before jumping to
                                // the end.
                                newSelectionPos = cnt - 1;
                                mSavedScrollPosition = -1;
                            }
                        } else {
                            // remember the saved scroll position before the activity is paused.
                            // reset it after the message list query is done
                            newSelectionPos = mSavedScrollPosition;
                            mSavedScrollPosition = -1;
                        }
                    }

                    mMsgListAdapter.changeCursor(cursor);

                    if (newSelectionPos != -1) {
                        mMsgListView.setSelection(newSelectionPos);     // jump the list to the pos
                    } else {
                        int count = mMsgListAdapter.getCount();
                        long lastMsgId = 0;
                        if (cursor != null && count > 0) {
                            cursor.moveToLast();
                            lastMsgId = cursor.getLong(COLUMN_ID);
                        }
                        // mScrollOnSend is set when we send a message. We always want to scroll
                        // the message list to the end when we send a message, but have to wait
                        // until the DB has changed. We also want to scroll the list when a
                        // new message has arrived.
                        smoothScrollToEnd(mScrollOnSend || lastMsgId != mLastMessageId, 0);
                        mLastMessageId = lastMsgId;
                        mScrollOnSend = false;
                    }
                    // Adjust the conversation's message count to match reality. The
                    // conversation's message count is eventually used in
                    // WorkingMessage.clearConversation to determine whether to delete
                    // the conversation or not.
                    mConversation.setMessageCount(mMsgListAdapter.getCount());

                    // Once we have completed the query for the message history, if
                    // there is nothing in the cursor and we are not composing a new
                    // message, we must be editing a draft in a new conversation (unless
                    // mSentMessage is true).
                    // Show the recipients editor to give the user a chance to add
                    // more people before the conversation begins.
                    if (cursor != null && cursor.getCount() == 0
                            && !isRecipientsEditorVisible() && !mSentMessage) {
                        initRecipientsEditor();
                    }

                    // FIXME: freshing layout changes the focused view to an unexpected
                    // one, set it back to TextEditor forcely.
                    mTextEditor.requestFocus();

                    invalidateOptionsMenu();    // some menu items depend on the adapter's count
                    return;

                case ConversationList.HAVE_LOCKED_MESSAGES_TOKEN:
                    mIsLocked = (cursor != null && cursor.getCount() > 0);
                    @SuppressWarnings("unchecked")
                    ArrayList<Long> threadIds = (ArrayList<Long>)cookie;
                    ConversationList.confirmDeleteThreadDialog(
                            new ConversationList.DeleteThreadListener(threadIds,
                                mBackgroundQueryHandler, null, ComposeMessageActivity.this),
                            threadIds,
                            cursor != null && cursor.getCount() > 0,
                            ComposeMessageActivity.this);
                    if (cursor != null) {
                        cursor.close();
                    }
                    break;

                case MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN:
                    // check consistency between the query result and 'mConversation'
                    tid = (Long) cookie;

                    if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        log("##### onQueryComplete (after delete): msg history result for threadId "
                                + tid);
                    }
                    if (cursor == null) {
                        return;
                    }
                    if (tid > 0 && cursor.getCount() == 0) {
                        // We just deleted the last message and the thread will get deleted
                        // by a trigger in the database. Clear the threadId so next time we
                        // need the threadId a new thread will get created.
                        log("##### MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN clearing thread id: "
                                + tid);
                        Conversation conv = Conversation.get(ComposeMessageActivity.this, tid,
                                false);
                        if (conv != null) {
                            conv.clearThreadId();
                            conv.setDraftState(false);
                        }
                        // The last message in this converation was just deleted. Send the user
                        // to the conversation list.
                        exitComposeMessageActivity(new Runnable() {
                            @Override
                            public void run() {
                                goToConversationList();
                            }
                        });
                    }
                    cursor.close();
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
            // If message is deleted make the mIsMessageChanged is true
            // when activity is from SearchActivity.
            mIsMessageChanged = mIsFromSearchActivity;
            switch(token) {
                case ConversationList.DELETE_CONVERSATION_TOKEN:
                    mConversation.setMessageCount(0);
                    // fall through
                case DELETE_MESSAGE_TOKEN:
                    if (cookie instanceof Boolean && ((Boolean)cookie).booleanValue()) {
                        // If we just deleted the last message, reset the saved id.
                        mLastMessageId = 0;
                    }
                    // Update the notification for new messages since they
                    // may be deleted.
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                            ComposeMessageActivity.this, MessagingNotification.THREAD_NONE, false);
                    // Update the notification for failed messages since they
                    // may be deleted.
                    updateSendFailedNotification();
                    break;
            }
            // If we're deleting the whole conversation, throw away
            // our current working message and bail.
            if (token == ConversationList.DELETE_CONVERSATION_TOKEN) {
                if (mIsLocked && !ConversationList.getExitDialogueSign()) {
                    mIsLocked = false;
                    startMsgListQuery(MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN);
                    return;
                }
                ConversationList.setExitDialogueSign();
                ContactList recipients = mConversation.getRecipients();
                mWorkingMessage.discard();

                // Remove any recipients referenced by this single thread from the
                // contacts cache. It's possible for two or more threads to reference
                // the same contact. That's ok if we remove it. We'll recreate that contact
                // when we init all Conversations below.
                if (recipients != null) {
                    for (Contact contact : recipients) {
                        contact.removeFromCache();
                    }
                }

                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(ComposeMessageActivity.this);
                finish();
            } else if (token == DELETE_MESSAGE_TOKEN) {
                // Check to see if we just deleted the last message
                startMsgListQuery(MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN);
            }

            MmsWidgetProvider.notifyDatasetChanged(getApplicationContext());
        }
    }

    @Override
    public void onUpdate(final Contact updated) {
        // Using an existing handler for the post, rather than conjuring up a new one.
        mMessageListItemHandler.post(new Runnable() {
            @Override
            public void run() {
                ContactList recipients = isRecipientsEditorVisible() ?
                        mRecipientsEditor.constructContactsFromInput(false) : getRecipients();
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    log("[CMA] onUpdate contact updated: " + updated);
                    log("[CMA] onUpdate recipients: " + recipients);
                }
                updateTitle(recipients);

                // The contact information for one (or more) of the recipients has changed.
                // Rebuild the message list so each MessageItem will get the last contact info.
                ComposeMessageActivity.this.mMsgListAdapter.notifyDataSetChanged();

                // Don't do this anymore. When we're showing chips, we don't want to switch from
                // chips to text.
//                if (mRecipientsEditor != null) {
//                    mRecipientsEditor.populate(recipients);
//                }
            }
        });
    }

    private void addRecipientsListeners() {
        Contact.addListener(this);
    }

    private void removeRecipientsListeners() {
        Contact.removeListener(this);
    }

    public static Intent createGroupChatIntent(Context context, long threadId) {
        Intent intent = createIntent(context, threadId);
        intent.putExtra("isGroupChat", true);
        return intent;
    }

    public static Intent createIntent(Context context, long threadId) {
        Intent intent = new Intent(context, ComposeMessageActivity.class);

        if (threadId > 0) {
            intent.setData(Conversation.getUri(threadId));
        }

        return intent;
    }

    private String getBody(Uri uri) {
        if (uri == null) {
            return null;
        }
        String urlStr = uri.getSchemeSpecificPart();
        if (!urlStr.contains("?")) {
            return null;
        }
        urlStr = urlStr.substring(urlStr.indexOf('?') + 1);
        String[] params = urlStr.split("&");
        for (String p : params) {
            if (p.startsWith("body=")) {
                try {
                    return URLDecoder.decode(p.substring(5), "UTF-8");
                } catch (UnsupportedEncodingException e) { }
            }
        }
        return null;
    }

    private void updateThreadIdIfRunning() {
        if (mIsRunning && mConversation != null) {
            if (DEBUG) {
                Log.v(TAG, "updateThreadIdIfRunning: threadId: " +
                        mConversation.getThreadId());
            }
            MessagingNotification.setCurrentlyDisplayedThreadId(mConversation.getThreadId());
        } else {
            if (DEBUG) {
                Log.v(TAG, "updateThreadIdIfRunning: mIsRunning: " + mIsRunning +
                        " mConversation: " + mConversation);
            }
        }
        // If we're not running, but resume later, the current thread ID will be set in onResume()
    }

    // Get the path of uri and compare it to ".vcf" to judge whether it is a
    // vcard file.
    private boolean isVcardFile(Uri uri) {
        String path = uri.getPath();
        return null != path && path.toLowerCase().endsWith(".vcf");
    }

    // handler for handle copy mms to sim with toast.
    private Handler mCopyToSimWithToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        int resId = 0;
            switch (msg.what){
                case MSG_COPY_TO_SIM_FAILED:
                     resId = R.string.copy_to_sim_fail;
                     break;
                case MSG_COPY_TO_SIM_SUCCESS:
                     resId = R.string.copy_to_sim_success;
                     break;
                default:
                     break;
            }
            Toast.makeText(ComposeMessageActivity.this, resId, Toast.LENGTH_SHORT).show();
        }
    };

    private class CopyToSimSelectListener implements DialogInterface.OnClickListener {
        private MessageItem msgItem;
        private int subscription;

        public CopyToSimSelectListener(MessageItem msgItem) {
            super();
            this.msgItem = msgItem;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which >= 0) {
                subscription = which;
            } else if (which == DialogInterface.BUTTON_POSITIVE) {
                new Thread(new CopyToSimThread(msgItem, subscription)).start();
            }
        }
    }

    private class CopyToSimThread extends Thread {
        private MessageItem msgItem;
        private int subscription;
        public CopyToSimThread(MessageItem msgItem) {
            this.msgItem = msgItem;
            this.subscription = MSimSmsManager.getDefault().getPreferredSmsSubscription();
        }

        public CopyToSimThread(MessageItem msgItem, int subscription) {
            this.msgItem = msgItem;
            this.subscription = subscription;
        }

        @Override
        public void run() {
            Message msg = mCopyToSimWithToastHandler.obtainMessage();
            msg.what = copyToSim(msgItem, subscription) ?  MSG_COPY_TO_SIM_SUCCESS
                    : MSG_COPY_TO_SIM_FAILED;
            msg.sendToTarget();
        }
    }

    private boolean copyToSim(MessageItem msgItem) {
        return copyToSim(msgItem, MSimSmsManager.getDefault().getPreferredSmsSubscription());
    }

    private boolean copyToSim(MessageItem msgItem, int subscription) {
        int boxId = msgItem.mBoxId;
        String address = msgItem.mAddress;
        String text = msgItem.mBody;
        long timestamp = msgItem.mDate != 0 ? msgItem.mDate : System.currentTimeMillis();

        SmsManager sm = SmsManager.getDefault();
        MSimSmsManager msm = MSimSmsManager.getDefault();
        ArrayList<String> messages = SmsManager.getDefault().divideMessage(text);

        boolean ret = true;
        for (String message : messages) {
            byte pdu[] = null;
            int status;
            if (Sms.isOutgoingFolder(boxId)) {
                pdu = SmsMessage.getSubmitPdu(null, address, message, false,
                                subscription).encodedMessage;
                status = SmsManager.STATUS_ON_ICC_SENT;
            } else {
                pdu = MessageUtils.getDeliveryPdu(null, address,
                        message, timestamp, subscription);
                status = SmsManager.STATUS_ON_ICC_READ;
            }
            ret &= MSimTelephonyManager.getDefault().isMultiSimEnabled()
                    ? msm.copyMessageToIcc(null, pdu, status, subscription)
                    : sm.copyMessageToIcc(null, pdu, status);
            if (!ret) {
                break;
            }
        }
        return ret;
    }

    private String getAttachFilePath(Context context, Uri uri){
        if (URI_SCHEME_CONTENT.equals(uri.getScheme())
                && URI_HOST_MEDIA.equals(uri.getHost())) {
            Cursor c = context.getContentResolver().query(uri, null,
                    null, null, null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        return c.getString(c.getColumnIndex(FILE_PATH_COLUMN));
                    }
                } finally {
                    c.close();
                }
            }
            return null;
        } else {
            return uri.getPath().toString();
        }
    }

    private void checkAttachFileState(Context context) {
        if (mWorkingMessage.hasAttachment() && !mWorkingMessage.hasSlideshow()) {
            ArrayList<Uri> attachFileUris = mWorkingMessage.getSlideshow().getAttachFileUri();
            for (Uri uri : attachFileUris) {
                Log.i(TAG, "Attach file uri is " + uri);
                if (uri == null) {
                    continue;
                }
                String path = getAttachFilePath(context, uri);
                Log.i(TAG, "File path is " + path);
                File f = new File(path);
                if (f == null || !f.exists()) {
                    Log.i(TAG, "set attachment null");
                    Toast.makeText(ComposeMessageActivity.this,
                            R.string.cannot_send_attach_reason,
                            Toast.LENGTH_SHORT).show();
                    mWorkingMessage.setAttachment(WorkingMessage.TEXT, null, false);
                    break;
                }
            }
        }
    }

    private boolean addToFirewallList(boolean isBlacklist){
        ContactList list = mConversation.getRecipients();

        String number = mConversation.getRecipients().get(0).getNumber();
        Log.d(TAG, "number: " + number);
        if (null == number || number.length() <= 0) {
            // number length is not allowed 0-
            Toast.makeText(ComposeMessageActivity.this, getString(R.string.firewall_number_len_not_valid),
                    Toast.LENGTH_SHORT).show();

            return false;
        }
        boolean ret = true;
        ContentValues values = new ContentValues();

        number = number.replaceAll(" ", "");
        number = number.replaceAll("-", "");
        Log.d(TAG, "onAddOrEditFinished number:" + number);
        int len = number.length();
        if (len > 11)
        {
            number = number.substring(len - 11, len);
        }
        Uri firewallUri = isBlacklist? BLACKLIST_CONTENT_URI: WHITELIST_CONTENT_URI;
        Cursor cu = getContentResolver().query(firewallUri,
                new String[] {
                        "_id", "number", "person_id", "name"
                },
                "number" + " LIKE '%" + number + "'",
                null,
                null);
        if (cu != null) {
            try {
                if (cu.getCount() > 0) {
                    int resId = isBlacklist ? R.string.firewall_number_in_black
                            : R.string.firewall_number_in_white;
                    toast(resId);
                    return false;
                }
            } finally {
                cu.close();
                cu = null;
            }
        }

        values.put("number", number);

        if (list.size() == 1 && list.get(0).existsInDatabase()){
            values.put("name", list.get(0).getName(true));
            values.put("person_id", list.get(0).getPersonId());
        } else {
            values.put("name", "");
            values.put("person_id", -1);
        }
        // add new
        Uri mUri = getContentResolver().insert(firewallUri, values);

        return ret;
    }

    public static boolean isFirewallInstalled(Context context) {
        boolean installed = false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    FIREWALL_APK_NAME, PackageManager.GET_PROVIDERS);
            installed = (info != null);
        } catch (NameNotFoundException e) {
        }
        Log.d(TAG, "Is Firewall installed ? " + installed);
        return installed;
    }

    class CreateGroupChatCallback {
        private ProgressDialog mProgressDialog;

        void onBegin() {
            if (mProgressDialog == null || !mProgressDialog.isShowing()) {
                String title = getString(R.string.please_wait);
                String message = getString(R.string.creating_group_chat);
                mProgressDialog = ProgressDialog.show(ComposeMessageActivity.this, title, message, false, true);
            }
        }

        void onDone(boolean isSuccess) {
            int resId = isSuccess ? R.string.create_group_chat_successfully : R.string.create_group_chat_failed;
            Toast.makeText(ComposeMessageActivity.this, resId, Toast.LENGTH_LONG).show();
        }

        void onEnd() {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }

    public void disposeRcsSendMessageException(Exception exception, int msgType) {
        exception.printStackTrace();
        if (exception instanceof FileSuffixException) {
            Looper.prepare();
            toast(R.string.file_suffix_vaild_tip);
            Looper.loop();
        } else if (exception instanceof FileTransferException) {
            Looper.prepare();
            toast(R.string.file_size_over);
            Looper.loop();
        } else if (exception instanceof FileDurationException) {
            Looper.prepare();
            if (msgType == RcsUtils.RCS_MSG_TYPE_VIDEO) {
                toast(getString(R.string.video_record_out_time, RcsUtils.getVideoMaxTime()));
            } else if (msgType == RcsUtils.RCS_MSG_TYPE_AUDIO) {
                toast(getString(R.string.audio_record_out_time, RcsUtils.getAudioMaxTime()));
            }
            Looper.loop();
        }
    }

    private void notifyChangeGroupChat(String groupId) {
        if(!TextUtils.isEmpty(groupId)){
            mMsgListAdapter.setRcsGroupId(Integer.parseInt(groupId));
            mMsgListAdapter.notifyDataSetChanged();
        }
    }

    private BroadcastReceiver mEmotionDownloadReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BroadcastConstants.UI_MESSAGE_PAID_EMO_DOWNLOAD_RESULT.equals(action)) {
                boolean downloadResult =
                        intent.getBooleanExtra(BroadcastConstants.BC_VAR_RESULT, false);
                if(downloadResult){
                    startMsgListQuery();
                } else {
                    toast(R.string.emotion_download_fail);
                }
            }
        }
    };

}
