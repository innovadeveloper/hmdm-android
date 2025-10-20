## Listando información de políticas de dispositivo por ADB

```sh
% adb shell dumpsys device_policy
Current Device Policy Manager state:
  Immutable state:
    mHasFeature=true
    mIsWatch=false
    mIsAutomotive=false
    mHasTelephonyFeature=true
    mSafetyChecker=null
  Device Owner:
    admin=ComponentInfo{com.hmdm.launcher/com.hmdm.launcher.AdminReceiver}
    name=Usuario de dispositivo de trabajo
    package=com.hmdm.launcher
    isOrganizationOwnedDevice=true
    User ID: 0



  Enabled Device Admins (User 0, provisioningState: 3):
    com.hmdm.launcher/.AdminReceiver:
      uid=10170
      testOnlyAdmin=false
      policies:
        wipe-data
        limit-password
      passwordQuality=0x0
      minimumPasswordLength=0
      passwordHistoryLength=0
      minimumPasswordUpperCase=0
      minimumPasswordLowerCase=0
      minimumPasswordLetters=1
      minimumPasswordNumeric=1
      minimumPasswordSymbols=1
      minimumPasswordNonLetter=0
      maximumTimeToUnlock=0
      strongAuthUnlockTimeout=0
      maximumFailedPasswordsForWipe=0
      specifiesGlobalProxy=false
      passwordExpirationTimeout=0
      passwordExpirationDate=0
      encryptionRequested=false
      disableCamera=false
      disableCallerId=false
      disableContactsSearch=false
      disableBluetoothContactSharing=true
      disableScreenCapture=false
      requireAutoTime=false
      forceEphemeralUsers=false
      isNetworkLoggingEnabled=false
      disabledKeyguardFeatures=0
      crossProfileWidgetProviders=null
      organizationColor=-16746133
      userRestrictions:
        no_system_error_dialogs
        no_safe_boot
      defaultEnabledRestrictionsAlreadySet={}
      isParent=false
      mCrossProfileCalendarPackages=[]
      mCrossProfilePackages=[]
      mSuspendPersonalApps=false
      mProfileMaximumTimeOffMillis=0
      mProfileOffDeadline=0
      mAlwaysOnVpnPackage=null
      mAlwaysOnVpnLockdown=false
      mPreferentialNetworkServiceEnabled=false
      mCommonCriteriaMode=false
      mPasswordComplexity=0
      mNearbyNotificationStreamingPolicy=1
      mNearbyAppStreamingPolicy=1
      mAdminCanGrantSensorsPermissions=true
      mUsbDataSignaling=true

    mPasswordOwner=-1
    mUserControlDisabledPackages=[]
    mAppsSuspended=false
    mUserSetupComplete=true
    mAffiliationIds={}
    mNewUserDisclaimer=null

    PersonalAppsSuspensionHelper
      critical packages: 1 app
        0: com.google.android.apps.wellbeing
      launcher packages: 2 apps
        0: com.motorola.launcher3
        1: com.android.settings
      accessibility services: empty
      input method packages: 3 apps
        0: com.google.android.inputmethod.latin
        1: com.google.android.googlequicksearchbox
        2: com.google.android.tts
      SMS package: com.google.android.apps.messaging
      Settings package: com.android.settings
      Packages subject to suspension: 22 apps
        0: com.google.android.youtube
        1: com.hmdm.emuilauncherrestarter
        2: com.google.android.apps.googleassistant
        3: com.hmdm.pager
        4: com.myos.camera
        5: com.google.android.deskclock
        6: com.google.android.gm
        7: com.google.android.apps.tachyon
        8: com.ape.fmradio
        9: com.google.android.apps.nbu.files
        10: com.google.android.apps.docs
        11: com.google.android.apps.maps
        12: com.google.android.contacts
        13: com.google.android.calculator
        14: com.android.chrome
        15: com.abexa.simple_app
        16: com.motorola.genie
        17: com.google.android.videos
        18: com.google.android.apps.photos
        19: com.google.android.calendar
        20: com.facebook.katana
        21: com.google.android.apps.youtube.music


  Constants:
    DAS_DIED_SERVICE_RECONNECT_BACKOFF_SEC: 3600
    DAS_DIED_SERVICE_RECONNECT_BACKOFF_INCREASE: 2.0
    DAS_DIED_SERVICE_RECONNECT_MAX_BACKOFF_SEC: 86400
    DAS_DIED_SERVICE_STABLE_CONNECTION_THRESHOLD_SEC: 120

  Stats:
    LockGuard.guard(): count=28788, total=203.7ms, avg=0.007ms, max calls/s=835 max dur/s=9.6ms max time=9.3ms

  Encryption Status: per-user

  no pending user created callback tokens

  Device policy cache:
    Screen capture disabled: {0=false}
    Password quality: {0=0}
    Permission policy: {0=0}
    Admin can grant sensors permission: {0=true}

  Device state cache:
    Device provisioned: true

  OverlayPackagesProvider
    required_apps_managed_device: 9 apps
      0: com.android.settings
      1: com.android.systemui
      2: com.android.contacts
      3: com.android.dialer
      4: com.android.stk
      5: com.android.providers.downloads
      6: com.android.providers.downloads.ui
      7: com.android.documentsui
      8: com.android.cellbroadcastreceiver
    required_apps_managed_user: 8 apps
      0: com.android.settings
      1: com.android.systemui
      2: com.android.contacts
      3: com.android.dialer
      4: com.android.stk
      5: com.android.providers.downloads
      6: com.android.providers.downloads.ui
      7: com.android.documentsui
    required_apps_managed_profile: 6 apps
      0: com.android.contacts
      1: com.android.settings
      2: com.android.systemui
      3: com.android.providers.downloads
      4: com.android.providers.downloads.ui
      5: com.android.documentsui
    disallowed_apps_managed_device: empty
    disallowed_apps_managed_user: empty
    disallowed_apps_managed_device: empty
    vendor_required_apps_managed_device: 13 apps
      0: com.android.vending
      1: com.google.android.gms
      2: com.google.android.contacts
      3: com.google.android.apps.wellbeing
      4: com.google.android.googlequicksearchbox
      5: com.android.launcher
      6: com.google.android.dialer
      7: com.google.android.apps.messaging
      8: com.google.android.setupwizard
      9: com.google.android.documentsui
      10: com.google.android.apps.assistant
      11: com.google.android.apps.searchlite
      12: com.google.android.projection.gearhead
    vendor_required_apps_managed_user: 9 apps
      0: com.android.vending
      1: com.google.android.gms
      2: com.google.android.contacts
      3: com.google.android.apps.wellbeing
      4: com.google.android.googlequicksearchbox
      5: com.android.launcher
      6: com.google.android.dialer
      7: com.google.android.apps.messaging
      8: com.google.android.documentsui
    vendor_required_apps_managed_profile: 9 apps
      0: com.android.vending
      1: com.google.android.gms
      2: com.google.android.contacts
      3: com.google.android.apps.wellbeing
      4: com.google.android.googlequicksearchbox
      5: com.google.android.apps.assistant
      6: com.google.android.apps.searchlite
      7: com.google.android.documentsui
      8: com.google.android.projection.gearhead
    vendor_disallowed_apps_managed_user: empty
    vendor_disallowed_apps_managed_device: empty
    vendor_disallowed_apps_managed_profile: empty

  Other overlayable app resources
    cross_profile_apps: empty
    vendor_cross_profile_apps: 3 apps
      0: com.google.android.googlequicksearchbox
      1: com.google.android.inputmethod.latin
      2: com.google.android.projection.gearhead
    config_packagesExemptFromSuspension: 1 app
      0: com.google.android.apps.wellbeing
    policy_exempt_apps: empty
    vendor_policy_exempt_apps: empty
```