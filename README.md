# NextStation

서울시 버스 실시간 도착 정보를 기반으로 하는 대중교통 하차 도우미 애플리케이션입니다.

## 주요 기능
- 실시간 버스 정보 조회: 서울시 버스 API 연동을 통한 정류장별/노선별 실시간 도착 예정 정보 제공.
- 목적지 기반 경로 검색: 목적지 검색 시 최적의 버스 노선 추천 및 선택 기능.
- 하차 알림 예약: 선택한 버스의 도착 예정 시간을 계산하여 백그라운드 알림 설정.
- 자동화 액션: 도착 시점 맞춰 진동 알림, TTS 음성 안내, 지정 번호로 SMS 자동 발송.
- 이용 기록 관리: 최근 이용한 목적지 및 하차 알림 내역 저장 및 관리.

## 기술 스택
- 언어: Kotlin (JDK 17)
- UI 프레임워크: Jetpack Compose
- 아키텍처: MVVM + Clean Architecture
- 의존성 주입: Hilt
- 로컬 데이터베이스: Room
- 네트워크: Retrofit2, OkHttp3
- 비동기 처리: Coroutines, Flow

## 개발 및 테스트 환경
- 최소 SDK: API 26 (Android 8.0)
- 타겟 SDK: API 35 (Android 15)
- 테스트 도구: Android Virtual Device (AVD)
- 특이사항: GPS 하드웨어 없이 시간 기반 로직으로 동작하도록 설계됨.

## 실행 방법

### 1. 환경 변수 설정
본 프로젝트는 `secrets-gradle-plugin`을 사용하여 API 키를 관리합니다. 프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 다음과 같이 공공데이터포털에서 발급받은 인증키를 입력해야 합니다.

```env
BUS_SERVICE_KEY=발급받은_인증키_입력
```

### 2. 프로젝트 빌드 및 실행
1. Android Studio를 실행하고 `Open`을 통해 프로젝트 루트 폴더를 선택합니다.
2. Gradle 동기화가 완료될 때까지 기다립니다.
3. 상단 메뉴의 `Run > Run 'app'` 또는 Shift + F10을 눌러 에뮬레이터(AVD) 또는 실물 기기에서 실행합니다.

### 3. 데이터 모드 전환 (Mock vs API)
현재 초기 설정은 API 키 활성화 대기 시간을 고려하여 **Mock 데이터** 모드로 동작합니다. 실제 API 데이터로 전환하려면 다음 파일을 수정하세요.

- **파일 위치:** `app/src/main/java/com/example/nextstation/data/repository/ArrivalRepositoryImpl.kt`
- **수정 방법:**
    - `getRealTimeArrival` 및 `searchRoutesToDestination` 함수 내의 Mock 데이터 반환 코드를 주석 처리합니다.
    - 하단에 주석 처리되어 있는 `Original API Logic` 부분의 주석을 해제합니다.

## 주의 사항
- **권한 승인:** 앱 실행 후 최초 1회 알림(Notification) 및 문자 발송(SMS) 권한 승인이 필요합니다.
- **배터리 최적화:** Foreground Service가 원활하게 동작하도록 에뮬레이터 설정에서 해당 앱의 배터리 최적화를 해제하는 것을 권장합니다.
