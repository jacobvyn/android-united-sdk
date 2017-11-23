package com.loopme.tracker.partners;

import android.text.TextUtils;

import com.loopme.BuildConfig;
import com.loopme.Constants;
import com.loopme.HttpUtil;
import com.loopme.Logging;
import com.loopme.ad.LoopMeAd;
import com.loopme.common.LoopMeError;
import com.loopme.debugging.LiveDebug;
import com.loopme.debugging.Params;
import com.loopme.request.RequestUtils;
import com.loopme.utils.StringUtils;
import com.loopme.utils.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoopMeTracker {
    private static final String LOG_TAG = LoopMeTracker.class.getSimpleName();

    private static String sPackageId;
    private static String sAppKey;
    private static Set<String> sVastErrorUrlSet = new HashSet<>();
    private static ExecutorService sExecutor;

    private LoopMeTracker() {
    }

    public static void init(LoopMeAd loopMeAd) {
        sExecutor = Executors.newCachedThreadPool();
        if (loopMeAd != null) {
            sAppKey = loopMeAd.getAppKey();
            sPackageId = loopMeAd.getContext().getPackageName();
        }
    }

    public static void initVastErrorUrl(List<String> errorUrlList) {
        sVastErrorUrlSet.addAll(errorUrlList);
    }

    public static void post(String errorMessage) {
        post(errorMessage, Constants.ErrorType.CUSTOM);
    }

    public static void post(String errorMessage, String errorType) {
        Logging.out(LOG_TAG, errorType + " - " + errorMessage);
        String request = buildRequest(errorType, errorMessage);
        proceedBuildEvent(request);
    }

    private static String buildServerIssueUrl() {
        String url = Constants.ERROR_URL;
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }
        return url;
    }

    private static String buildRequest(String errorType, String errorMessage) {
        Map<String, String> params = new HashMap<>();
        params.putAll(getGeneralInfo());
        params.put(Params.MSG, Constants.SDK_ERROR_MSG);
        params.put(Params.ERROR_TYPE, errorType);
        params.put(Params.ERROR_MSG, errorMessage);
        return HttpUtil.obtainRequestString(params);
    }

    private static void sendDataToServer(final String errorUrl, final Map<String, String> headers, final String request) {
        sExecutor.submit(new Runnable() {
            @Override
            public void run() {
                HttpUtil.sendRequest(errorUrl, headers, request);
            }
        });
    }

    public static synchronized void postVastError(String vastErrorCode) {
        for (String url : sVastErrorUrlSet) {
            String urlWithCode = StringUtils.setErrorCode(url, vastErrorCode);
            sendDataToServer(urlWithCode, null, null);
            Logging.out(LOG_TAG, urlWithCode);
        }
    }

    public static void clear() {
        sVastErrorUrlSet.clear();
    }

    public static void trackSdkFeedBack(List<String> packageIds, String token) {
        if (Utils.isPackageInstalled(packageIds)) {
            sendDataToServer(buildSdkFeedBackUrl(token), null, null);
        }
    }

    private static String buildSdkFeedBackUrl(String token) {
        String url = Constants.OLD_TRACK_FEEDBACK_URL + "?";
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(Params.EVENT_TYPE, Params.SDK_FEEDBACK);
        requestParams.put(Params.R, "1");
        requestParams.put(Params.ID, token);
        return url + HttpUtil.obtainRequestString(requestParams);
    }

    public static void postDebugEvent(String param, String value) {
        if (LiveDebug.isDebugOn()) {
            Logging.out(LOG_TAG, param + "=" + value);
            String request = buildDebugRequest(param, value);
            proceedBuildEvent(request);
        }
    }

    private static String buildDebugRequest(String param, String value) {
        Map<String, String> params = new HashMap<>();
        params.putAll(getGeneralInfo());
        params.put(Params.ERROR_TYPE, Constants.ErrorType.CUSTOM);
        params.put(param, value);
        return HttpUtil.obtainRequestString(params);
    }

    private static Map<String, String> getGeneralInfo() {
        Map<String, String> params = new HashMap<>();
        params.put(Params.DEVICE_OS, Constants.ADNROID_DEVICE_OS);
        params.put(Params.SDK_TYPE, BuildConfig.SDK_TYPE);
        params.put(Params.SDK_VERSION, BuildConfig.VERSION_NAME);
        params.put(Params.DEVICE_ID, RequestUtils.getIfa());
        params.put(Params.APP_KEY, sAppKey);
        params.put(Params.PACKAGE_ID, sPackageId);
        return params;
    }

    private static void proceedBuildEvent(String request) {
        String url = buildServerIssueUrl();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        sendDataToServer(url, headers, request);
    }

    public static void post(LoopMeError error) {
        if (error != null) {
            post(error.getMessage(), error.getErrorType());
            if (isVastError(error.getErrorType())) {
                LoopMeTracker.postVastError(String.valueOf(error.getErrorCode()));
            }
        }
    }

    private static boolean isVastError(String errorType) {
        return TextUtils.equals(errorType, Constants.ErrorType.VAST) ||
                TextUtils.equals(errorType, Constants.ErrorType.VPAID);
    }
}
