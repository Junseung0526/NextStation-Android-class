# 안심 하차 (Safe Arrival) - Modern Edition

본 프로젝트는 실물 기기 없이 안드로이드 에뮬레이터(AVD)만으로 테스트할 수 있도록 설계된 '대중교통 하차 도우미' 앱입니다. 최신 안드로이드 트렌드인 **글래스모피즘(Glassmorphism)** UI와 **실시간 버스 정보 API**가 통합되었습니다.

## 🚀 주요 기능
- **실시간 버스 정보 (New)**: 정류장 ID(arsId)를 입력하여 실시간 버스 도착 정보를 조회하고, 원클릭으로 타이머를 설정합니다.
- **글래스모피즘 UI (New)**: 반투명 유리 질감과 다이나믹 그라데이션이 적용된 세련된 디자인을 제공합니다.
- **시간 기반 타이머**: GPS 대신 사용자가 입력한 예상 도착 시간을 기반으로 작동합니다.
- **백그라운드 알림**: `Foreground Service`를 통해 앱이 백그라운드에 있어도 타이머가 유지됩니다.
- **정밀 알림 (AlarmManager)**: `Doze Mode`에서도 정확한 시간에 알림을 제공합니다.
- **자동 액션**: 도착 시 진동, TTS 음성 안내, 그리고 지정된 번호로 자동 문자(SMS)를 발송합니다.

## 🛠 기술 스택
- **Language**: Kotlin
- **UI**: Jetpack Compose (Glassmorphism Concept)
- **Architecture**: MVVM + Clean Architecture
- **Network**: Retrofit2, OkHttp, Gson
- **DI**: Hilt
- **Local DB**: Room
- **Background**: Foreground Service + AlarmManager

## 📱 에뮬레이터 테스트 가이드
- **권장 설정**: API Level 34 (Android 14) 이상, Pixel 7 Pro 에뮬레이터.
- **실시간 데이터**: `ArrivalRepositoryImpl.kt`에 공공데이터포털 API 키를 입력하면 실제 서울시 버스 데이터를 확인할 수 있습니다.
- **로그 확인**: Logcat에서 `AlarmReceiver` 및 `ArrivalService` 태그를 검색하세요.

## 🔋 배터리 최적화 (Doze Mode) 해결
```bash
adb shell dumpsys deviceidle force-idle
```
