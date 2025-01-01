# detalk-api

## 목차
1. [프로젝트 구성](#프로젝트-구성)
   - [패키지 구조](#패키지-구조)
   - [주요 라이브러리 및 버전](#주요-라이브러리-및-버전)
2. [명명 규칙](#명명-규칙)
   - [커밋 메시지](#커밋-메세지)
3. [환경 세팅](#환경-세팅)
   - [개발 환경](#개발-환경)
   - [운영 환경](#운영-환경)
4. [실행 방법](#실행-방법)
   - [개발 실행](#개발-실행)
   - [운영 실행](#운영-실행)
5. [배포 프로세스](#배포-프로세스)

---

## 프로젝트 구성

TODO

### 패키지 구조

TODO

### 주요 라이브러리 및 버전

- Java: 21
- Spring Boot: 3.x
- PostgreSQL: TODO

## 명명 규칙

### 커밋 메세지

```
type: message #issue

body

footer
```
  - `feat`: 새로운 기능 추가
  - `fix`: 버그 수정
  - `docs`: 문서 추가 및 수정
  - `style`: 코드 스타일 수정 (포매팅, 세미콜론 등)
  - `refactor`: 코드 리팩토링
  - `test`: 테스트 코드 추가 및 수정
  - `chore`: 기타 변경 사항 (빌드, 의존성 등)

## 환경 세팅

### 개발 환경

TODO

### 운영 환경

TODO

## 실행 방법

### 개발 실행

TODO

### 운영 실행

TODO

## 배포 프로세스

(임시)

1. 코드 병합

- PR을 통해 `main` 브랜치로 병합

2. 프로덕션 빌드

```bash
./gradlew clean build
```

3. 배포

```bash
java -jar build/libs/<프로젝트명>.jar
```
