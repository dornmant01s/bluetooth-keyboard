# ChannelKeyLauncher

블루투스/유선 **키보드 숫자**로 채널처럼 앱을 실행하고, **PageUp/PageDown(혹은 지정 키)**로 **+1/−1 채널 이동**하며,
실행 직전에 **채널 번호 OSD**(숫자 버블)를 짧게 띄우는 안드로이드 런처 보조 앱입니다.

## 빠른 시작
1. 레포 업로드 → GitHub Actions 자동 빌드 (워크플로명: `android-ci`)
2. APK 아티팩트 다운로드
3. 휴대폰 설치 후 **설정 → 접근성 → ChannelKeyLauncher** 활성화
4. 앱 열고 **채널 추가**에서 번호→앱 매핑
5. 키보드에서 숫자(예: `11`) 입력 → 0.8초 내 추가 입력 없으면 실행
6. **+1/−1 키** 기본: PageUp/PageDown (앱에서 재지정 가능)

## 주요 기능
- 다자리 숫자(예: 11, 120, 2025) 입력 지원 (입력 간격 800ms로 확정)
- 접근성 서비스 기반의 전역 키 입력 필터
- `TYPE_ACCESSIBILITY_OVERLAY`로 OSD 표시
- `PackageManager.getLaunchIntentForPackage(...)`로 앱 실행
- 매핑/키 바인딩은 SharedPreferences 저장

## 빌드 노트
- **Wrapper 없이도** 빌드되도록 워크플로에서 SDKMAN으로 Gradle 8.9를 설치해 실행합니다.
  - Gradle Wrapper가 필요하다면 로컬에서 `./gradlew wrapper --gradle-version 8.9`로 추가 후 푸시하세요.
- Android Gradle Plugin 8.5.2 / Kotlin 1.9.24 / compileSdk 35

## 설정 팁
- OSD 표시 시간(ms) 변경: `ChannelLaunchService.commitDelayMs`(입력 확정), `OverlayBubble.show(..., ms)` 조정
- +1/−1 키 변경: 앱의 **키 설정** 버튼으로 캡처

## 라이선스
- MIT (원하는 대로 포크/개조 OK)
