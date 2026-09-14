# Daily Town 개인정보처리방침 초안

> 출시 전 아래 `[사람 입력 필요]` 값을 실제 운영 주체 정보로 교체하고 공개 HTTPS URL에 게시해야 합니다.

시행 예정일: [사람 입력 필요]

## 1. 운영 주체 및 문의

- 서비스명: Daily Town
- 운영자/개발자명: [사람 입력 필요]
- 문의 이메일: [사람 입력 필요]
- 개인정보처리방침 공개 URL: [사람 입력 필요]

## 2. 앱이 사용하는 정보

### 위치정보

Daily Town은 사용자가 앱에서 직접 `실제 위치` 탐험을 시작한 동안 주변 탐험, 이동 거리, POI 접근 및 미스터리 진행을 위해 기기의 위치정보를 사용합니다.

- Android의 대략적 위치 또는 정확한 위치 권한을 사용합니다.
- MVP는 백그라운드 위치 권한을 요청하지 않습니다.
- 탐험 추적을 중지하면 앱의 위치 추적 세션도 종료합니다.
- 위치정보는 광고 목적으로 사용하지 않습니다.

### 외부 POI 조회

한국관광공사 TourAPI가 설정된 빌드에서는 주변 관광 POI를 조회하기 위해 현재 위치를 그대로 전송하지 않고, 기기에서 약 2km 격자의 대표 좌표로 축약한 뒤 여유 검색 반경과 함께 TourAPI에 요청합니다. 반환된 POI는 기기 안에서 실제 탐험 반경에 맞게 다시 필터링합니다.

정확한 Google Play Data Safety 분류는 출시 직전 TourAPI 및 사용 중인 지도 SDK의 최신 데이터 처리 조건과 함께 재검토합니다.

### 게임 진행도 및 설정

다음 정보는 앱의 로컬 저장소에 저장될 수 있습니다.

- 탐험/POI 방문 진행도
- 미스터리/단서/해결 기록
- 동행 캐릭터 관계 및 기억 상태
- Daily/Weekly 목표 진행도
- 탐험 리마인더 설정

MVP baseline에서는 이 진행도를 Daily Town 자체 서버나 analytics 서비스로 자동 전송하지 않습니다.

## 3. Field Test 데이터

개발/필드테스트 모드에서는 사용자가 명시적으로 공유 기능을 실행할 때에만 파생 진단 JSON/리포트를 생성합니다.

구조화 export는 raw GPS 좌표, route geometry, 지도 API 키, provider exception payload, 영구 device/session identifier를 포함하지 않도록 설계되어 있습니다. 필드테스트 export의 기본 보관 기간은 제품 검토 완료 후 최대 30일입니다.

## 4. 제3자 서비스

Daily Town은 다음 외부 서비스를 사용할 수 있습니다.

- NAVER Maps SDK: 지도 표시
- Google Play services Location: Android 기기 위치 수집
- 한국관광공사 TourAPI: production POI 후보 조회(서비스키가 설정된 빌드)
- Google Play: 앱 배포 및 Play App Signing

출시 전 각 서비스의 최신 개인정보/데이터 처리 문서를 기준으로 Google Play Data Safety 응답과 본 방침을 일치시켜야 합니다.

## 5. Analytics 및 광고

초기 MVP/closed beta baseline에서는 별도 analytics, 광고 SDK, crash-reporting SDK를 추가하지 않습니다. 향후 도입할 경우 수집 항목, 목적, 보관 및 제3자 제공 여부를 먼저 검토하고 이 방침과 Data Safety를 업데이트합니다.

## 6. 삭제 및 보관

- 게임 진행도/설정은 사용자의 기기 앱 데이터에 저장됩니다.
- 앱 데이터 삭제 또는 앱 제거 시 Android가 관리하는 로컬 앱 데이터가 삭제될 수 있습니다.
- 명시적으로 공유한 field-test 파일은 프로젝트 운영 정책에 따라 제품 검토 완료 후 최대 30일 내 삭제합니다.

## 7. 아동 및 민감정보

MVP는 위치정보를 광고 또는 프로파일링 목적으로 사용하지 않습니다. 출시 대상 연령과 Families 적용 여부는 Play Console 콘텐츠/연령 정책 설정 시 별도로 확정합니다.

## 8. 변경

데이터 수집 범위, analytics/crash SDK, background location, production POI provider 또는 서버 동기화 기능이 변경되면 본 방침과 Play Data Safety 항목을 함께 재검토합니다.
