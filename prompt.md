# Role
너는 시니어 안드로이드 개발자이자 아키텍트야.
아이폰 사용자로서 안드로이드 실물 기기 없이 에뮬레이터(AVD)만으로 개발해야 하는 상황을 고려해서,
'대중교통 하차 도우미' 앱의 MVP 모델을 Kotlin과 Jetpack Compose로 구현하려고 해.

# App Concept
- 이름: 안심 하차 (Safe Arrival)
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

1. 기술 스택 (Tech Stack)현대적인 안드로이드 개발 표준을 따릅니다.Language: Java
2. UI: Jetpack Compose (선언형 UI, 아이폰의 SwiftUI와 유사)
3. Architecture: MVVM + Clean Architecture (관심사 분리)
4. Local DB: Room (하차 기록 및 즐겨찾기 경로 저장)
5. Background: WorkManager & Foreground Service (타이머 유지)
6. DI: Hilt (의존성 주입, Spring의 @Autowired와 비슷한 역할)
6. 2. 마일스톤 및 작업 분할 (WBS)에뮬레이터로만 개발해야 하므로 단계를 세밀하게 나눕니다.
7. 단계목표주요 작업 내용테스트 방법 (에뮬레이터)Phase 1UI 및 기본 구조프로젝트 셋업, Compose 기반 입력창 디자인UI 상호작용 확인Phase 2타이머 서비스Foreground Service 구현, 알림(Notification) 연동상단바 알림 유지 확인Phase 3스케줄링 로직AlarmManager를 이용한 정밀 예약 및 진동 알림시스템 로그(Logcat) 확인Phase 4자동화 액션SmsManager 연동, TTS 음성 가이드 추가가상 번호 문자 수신 확인Phase 5데이터 영속화Room DB 연동 (최근 목적지 저장 기능)앱 재실행 시 데이터 유지 확인3. 디렉토리 구조 (Package Structure)백엔드 구조와 비슷하게 가져가면 이해하기 편하실 거예요.Plaintextcom.jskim.safearrival
   ├── data
   │   ├── local (Room DB, Entity, DAO)
   │   ├── repository (Data Source 구현체)
   ├── di (Hilt Modules)
   ├── domain
   │   ├── model (Domain Objects)
   │   ├── repository (Interface)
   │   └── usecase (Business Logic - 예: CalculateAlarmTime)
   ├── service
   │   └── ArrivalService.kt (Foreground Service)
   ├── ui
   │   ├── theme (Colors, Type, Shape)
   │   ├── main (MainActivity, Home Screen)
   │   └── components (Common UI Components)
   └── util
   └── AlarmReceiver.kt (BroadcastReceiver)
4. 핵심 관리 포인트 (Back-end Developer's View)상태 관리: 안드로이드는 화면 회전이나 앱 전환 시 Activity가 죽었다 살아날 수 있습니다. ViewModel을 사용해 타이머 상태를 안전하게 보존하세요.리소스 최적화: 안드로이드의 Doze Mode(배터리 절약 모드)는 백엔드의 스케줄러와 다르게 잠자기 상태에 빠지면 알림을 막습니다. setExactAndAllowWhileIdle() 메서드를 써서 이를 우회하는 로직이 핵심입니다.권한 관리: 안드로이드 13(API 33) 이상부터는 알림 권한(POST_NOTIFICATIONS)을 사용자에게 직접 받아야 합니다. 시작할 때 이 체크 로직부터 짜야 합니다.5. 협업 및 기록 (Documentation)README.md: 에뮬레이터에서 테스트하기 위한 환경 설정 가이드를 꼭 작성하세요. (예: "API Level 34, Pixel 7 Pro 권장")GitHub Issue/Project: 기능을 하나씩 만들 때마다 Issue를 생성하고 커밋 메시지에 Close #1 같은 태그를 달아 관리해 보세요. (하루 1커밋 유지에도 도움 됩니다!)