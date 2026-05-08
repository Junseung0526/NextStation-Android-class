# NextStation - Modern Edition
---
본 프로젝트는 실물 기기 없이 안드로이드 에뮬레이터(AVD)만으로 테스트할 수 있도록 설계된 '대중교통 하차 도우미' 앱입니다. 실시간 버스 정보 API가 통합되었습니다.

## 🚀 주요 기능
- **실시간 버스 정보**: 서울시 버스 API를 통해 정류장별 실시간 도착 정보를 조회합니다.
- **클린 UI**: Material 3 가이드를 준수하는 깔끔하고 직관적인 디자인을 제공합니다.
- **도착 알림 예약**: 버스 도착 예정 시간을 기반으로 알림을 예약합니다.
- **백그라운드 알림**: `Foreground Service`를 통해 앱이 백그라운드에 있어도 알림이 유지됩니다.
- **자동 액션**: 도착 시 진동, TTS 음성 안내, 그리고 지정된 번호로 자동 문자(SMS)를 발송합니다.

## 🛠 기술 스택
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Local DB**: Room
- **Network**: Retrofit2 & OkHttp3
