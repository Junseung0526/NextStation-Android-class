# NextStation

서울시 버스 실시간 도착 정보를 기반으로 하는 대중교통 하차 도우미 애플리케이션입니다.

## 주요 기능
- 실시간 버스 정보 조회: 서울시 버스 API 연동을 통한 정류장별/노선별 실시간 도착 예정 정보 제공.
- 목적지 기반 경로 검색: 목적지 검색 시 최적의 버스 노선 추천 및 선택 기능.
- 하차 알림 예약: 선택한 버스의 도착 예정 시간을 계산하여 백그라운드 알림 설정.
- 자동화 액션: 도착 시점 맞춰 진동 알림, TTS 음성 안내, 지정 번호로 SMS 자동 발송.
- 이용 기록 관리: 최근 이용한 목적지 및 하차 알림 내역 저장 및 관리.

## 기술 스택
- 언어: Kotlin
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
