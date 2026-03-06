Aion2 Sugo Notifier
An Android vibration alert app.

Operation Rules

Active time: Every day from 10:00 to 22:59

Alert times (minute:second):
Every hour at 16:00, 21:20, 46:00, 50:20
Example: 11:16:00, 11:21:20

Alert messages:

16 min, 46 min → “Sugo start time.”

21 min, 50 min → “Reward collection time.”

Vibration pattern:
Buzz Buzz, Buzz Buzz

App Usage

Launch the app

Tap Start Schedule

To stop, tap Stop Schedule

Build

From the project root:

./gradlew assembleDebug

Windows:

gradlew.bat assembleDebug

Generated APK:

app/build/outputs/apk/debug/app-debug.apk

# 슈고알리미 (Sugo Notifier)

안드로이드용 진동 알림 앱입니다.

## 동작 규칙

- 동작 시간대: 매일 `10:00 ~ 22:59`
- 알림 시각(분:초): 매시 `16:00`, `21:20`, `46:00`, `50:20`  ex) 11시16분00 초 ,  11시 21분 20초
- 알림 메시지:
  - `16분`, `46분` -> `슈고 시작시간입니다.`
  - `21분`, `50분` -> `보상수령시간입니다.`
- 진동 패턴: `웅웅 웅웅`

## 앱 사용

1. 앱 실행
2. `스케줄 시작` 버튼 클릭
3. 중지하려면 `스케줄 중지` 클릭

## 빌드

프로젝트 루트에서:

```bash
./gradlew assembleDebug
```

Windows:

```bat
gradlew.bat assembleDebug
```

생성 APK:

- `app/build/outputs/apk/debug/app-debug.apk`
