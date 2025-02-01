import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080/point/pessimistic';
const USER_ID = 1;  // 특정 사용자 ID 고정
const INITIAL_POINT = 10000; // 초기 포인트 (Mock 데이터)
const SPEND_AMOUNT = 6000;

export function setup() {
    const payload = JSON.stringify({ userId: USER_ID, amount: INITIAL_POINT });
    const headers = { 'Content-Type': 'application/json' };

    const res = http.post(`${BASE_URL}/credit`, payload, { headers });
}

export default function () {
    const payload = JSON.stringify({ userId: USER_ID, amount: SPEND_AMOUNT });
    const headers = { 'Content-Type': 'application/json' };

    const res = http.post(`${BASE_URL}/spend`, payload, { headers });

    if (res.status === 400) {
        console.log(`포인트 부족 - userId=${USER_ID}, 사용 요청=${SPEND_AMOUNT}`);
    } else {
        console.log(`포인트 사용 성공 - userId=${USER_ID}, 사용 요청=${SPEND_AMOUNT}`);
    }

    sleep(1);
}

export const options = {
    vus: 5,  // 동시 사용자 (5명)
    duration: '10s',  // 10초 동안 실행 (Smoke Test)
    iterations: 50, // 최대 50회 반복 실행 (추가 설정)
};
