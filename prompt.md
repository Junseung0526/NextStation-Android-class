# Role
너는 시니어 안드로이드 개발자이자 아키텍트야.
아이폰 사용자로서 안드로이드 실물 기기 없이 에뮬레이터(AVD)만으로 개발해야 하는 상황을 고려해서,
'대중교통 하차 도우미' 앱의 MVP 모델을 Kotlin과 Jetpack Compose로 구현하려고 해.

# App Concept
- 이름: NextStation
- 핵심 기능: 사용자가 입력한 '도착 예정 시간'을 기반으로 백그라운드에서 타이머를 구동하고, 하차 시점에 맞춰 단계별 알림(진동, TTS) 및 자동 문자 발송 수행.

# Technical Requirements (Hardware-Free)
1. GPS 하드웨어 대신 '시간(Time)' 기반의 로직을 사용할 것.
2. AlarmManager를 사용하여 정확한 시간에 백그라운드 작업이 트리거되도록 구성할 것.
3. 앱이 종료되어도 타이머가 유지되도록 Foreground Service를 구현할 것.
4. TextToSpeech(TTS)를 활용해 이어폰 사용자에게 음성 안내를 제공할 것.
5. SmsManager를 통해 특정 시간 도달 시 미리 설정된 번호로 자동 문자 전송 로직을 포함할 것.

# Instruction
1. 먼저 프로젝트의 전체적인 아키텍처(MVVM)와 디렉토리 구조를 제안해줘.
2. AndroidManifest.xml에 필요한 권한(알림, 문자 발송, 포그라운드 서비스 등)을 정리해줘.
3. 가장 핵심이 되는 '하차 타이머 로직'과 'Foreground Service' 코드를 Kotlin으로 작성해줘.
4. 에뮬레이터 환경에서 테스트할 때 주의해야 할 '배터리 최적화(Doze mode)' 관련 해결 방법을 알려줘.
5. 디자인은 Material 3 가이드를 따라 심플하고 직관적으로 짜줘.

# Constraints
- 실물 기기가 없으므로, 모든 테스트는 에뮬레이터 로그(Logcat)와 가상 알림으로 확인 가능해야 함.
- 코드에는 주석을 상세히 달아주고, 백엔드 전공자가 이해하기 쉽게 작성해줘.

1. 기술 스택 (Tech Stack)
현대적인 안드로이드 개발 표준을 따릅니다.
- Language: Kotlin
- UI: Jetpack Compose (선언형 UI)
- Architecture: MVVM + Clean Architecture
- Local DB: Room
- Background: AlarmManager & Foreground Service
- DI: Hilt

2. 마일스톤 및 작업 분할 (WBS)
에뮬레이터로만 개발해야 하므로 단계를 세밀하게 나눕니다.
Phase 1: UI 및 기본 구조 (Project Setup, Compose UI)
Phase 2: 타이머 서비스 (Foreground Service, Notification)
Phase 3: 스케줄링 로직 (AlarmManager, Vibration)
Phase 4: 자동화 액션 (SMS, TTS)
Phase 5: 데이터 영속화 (Room DB, History)

3. 디렉토리 구조 (Package Structure)
com.example.nextstation
   ├── data
   │   ├── local (Room DB, Entity, DAO)
   │   ├── remote (API Interface, Response Models)
   │   └── repository (Implementation)
   ├── di (Hilt Modules)
   ├── domain
   │   ├── model (Domain Objects)
   │   └── repository (Interface)
   ├── service
   │   └── ArrivalService.kt (Foreground Service)
   ├── ui
   │   ├── theme (Colors, Type, Shape)
   │   ├── main (MainActivity, ViewModel)
   │   ├── screens (Home, Search, History, Settings)
   │   └── components (Common UI Components)
   └── util (Receiver, Helpers)
