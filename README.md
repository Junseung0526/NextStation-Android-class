# 안심 하차 (Safe Arrival) - MVP 모델

본 프로젝트는 실물 기기 없이 안드로이드 에뮬레이터(AVD)만으로 테스트할 수 있도록 설계된 '대중교통 하차 도우미' 앱입니다.

## 🚀 주요 기능
- **시간 기반 타이머**: GPS 대신 사용자가 입력한 예상 도착 시간을 기반으로 작동합니다.
- **백그라운드 알림**: `Foreground Service`를 통해 앱이 백그라운드에 있어도 타이머가 유지됩니다.
- **정밀 알림 (AlarmManager)**: `Doze Mode`에서도 정확한 시간에 알림을 제공하기 위해 `setExactAndAllowWhileIdle()`을 사용합니다.
- **자동 액션**: 도착 시 진동, TTS 음성 안내, 그리고 지정된 번호로 자동 문자(SMS)를 발송합니다.
- **이력 저장**: Room DB를 사용하여 최근 목적지를 저장합니다.

## 🛠 기술 스택
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Local DB**: Room
- **Background**: Foreground Service + AlarmManager

## 📱 에뮬레이터 테스트 가이드
- **권장 설정**: API Level 34 (Android 14) 이상, Pixel 7 Pro 에뮬레이터.
- **권한 승인**: 앱 실행 시 요청하는 '알림(Notification)' 및 'SMS 발송' 권한을 승인해야 합니다.
- **정밀 알람 권한**: Android 12(API 31) 이상에서는 '알람 및 리마인더' 설정에서 '정확한 알람 예약' 권한이 필요할 수 있습니다. (앱에서 설정 화면으로 유도합니다)
- **로그 확인**: Android Studio의 `Logcat` 탭에서 `AlarmReceiver` 및 `ArrivalService` 태그를 검색하여 작동 여부를 확인할 수 있습니다.

## 🔋 배터리 최적화 (Doze Mode) 해결
- 안드로이드 시스템은 배터리 절약을 위해 `Doze Mode`를 지원합니다.
- 본 앱은 이를 우회하여 정시 알림을 보장하기 위해 `AlarmManager.setExactAndAllowWhileIdle()`를 사용합니다.
- 테스트 시 에뮬레이터에서 강제로 Doze Mode에 진입시켜 테스트하려면 다음 ADB 명령어를 사용할 수 있습니다:
  ```bash
  adb shell dumpsys deviceidle force-idle
  ```

## 📂 디렉토리 구조
- `data`: local(Room), repository 구현부
- `domain`: model, repository 인터페이스
- `service`: `ArrivalService` (Foreground Service)
- `ui`: Compose UI 및 `MainViewModel`
- `util`: `AlarmReceiver` (BroadcastReceiver)
