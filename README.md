# 콘서트 예약 서비스

---

- 대기열 시스템을 구축하고 작업 가능한 유저만 예약 서비스를 이용 할 수 있도록 합니다.
- 사용자는 좌석 예약 시 미리 충전한 잔액을 사용합니다.
- 좌석 예약 요청 시 미 결제 처리 상태더라도 일정 시간 동안 다른 유저가 해당 좌석에 접근 할 수 없도록 합니다.

### 주요 기능

---

1. 유저 대기열 토큰 기능
- 서비스를 이용 할 수 있는 토큰을 발급 받는 기능
- 토큰은 유저의 UUID 와 해당 유저의 대기열 순번, 만료 시간 등을 포함한다
- 모튼 API 는 대기열 검증을 통화한 토큰이 있어야 이용 가능하다.
- 대기열 상태는 기본적으로 폴링 방식으로 확인한다.

2. 예약 가능 날짜/좌석 조회
- 좌석 정보는 1~50 까지의 좌석으로 관리한다.

3. 좌석 예약 요청 기능
- 사용자가 좌석을 예약 하는 기능
- 좌석 예약과 동시에 해당 좌석은 그 유저에게 5분간 임시 배정 된다.
- 임시 배정된 좌석은 다른 사용자가 예약 할 수 없다.
- 5분 안에 결제가 이뤄지지 않으면 임시 배정된 좌석은 해제된다.

4. 잔액 조회/충전 요청

5. 결제 요청 기능
- 좌석 예약을 위한 결제 기능
- 결제 완료 시 좌석을 해당 유저에게 배정하며, 토큰을 만료 상태로 변경한다.

### 시퀀스 다이어그램
---
<details>
  <summary>유저 대기열 발급 토큰</summary>
  
  ![유저 대기열 토큰 발급](https://github.com/user-attachments/assets/78ceeffc-0556-45d9-80ec-40be5005246e)
</details>

<details>
  <summary>예약 가능 날짜/좌석 조회</summary>
  
  ![예약 가능 날짜좌석 조회](https://github.com/user-attachments/assets/7a7738a9-f8f3-47d2-8148-220a1045366a)
</details>

<details>
  <summary>좌석 예약</summary>
  
  ![좌석 예약](https://github.com/user-attachments/assets/386d811d-6299-44f4-bf63-02cac3139960)
</details>

<details>
  <summary>잔액 충전/조회</summary>
  
  ![잔액 충전조회](https://github.com/user-attachments/assets/9380f277-68a3-4dec-96bc-f1e510acb5a9)
</details>

<details>
  <summary>결제</summary>
  
  ![결제](https://github.com/user-attachments/assets/be0d4639-54d7-4524-b0b5-2d9032c4b1df)
</details>

