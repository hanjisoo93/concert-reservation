import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080/point/pessimistic';
const USER_ID = 1;
const INITIAL_POINT = 1000000;
const SPEND_AMOUNT = 5000;

export function setup() {
    const payload = JSON.stringify({ userId: USER_ID, amount: INITIAL_POINT });
    const headers = { 'Content-Type': 'application/json' };

    const res = http.post(`${BASE_URL}/credit`, payload, { headers });

    check(res, {
        'setup request success': (r) => r.status === 200,
    });

    console.log(`Mock data inserted - userId=${USER_ID}, initial point=${INITIAL_POINT}`);
}

export default function () {
    const payload = JSON.stringify({ userId: USER_ID, amount: SPEND_AMOUNT });
    const headers = { 'Content-Type': 'application/json' };

    const res = http.post(`${BASE_URL}/spend`, payload, { headers });

    check(res, {
        'point spend success': (r) => r.status === 200,
        'insufficient points': (r) => r.status === 400,
    });

    if (res.status === 400) {
        console.log(`Insufficient points - userId=${USER_ID}, requested=${SPEND_AMOUNT}`);
    } else {
        console.log(`Point spend success - userId=${USER_ID}, requested=${SPEND_AMOUNT}`);
    }

    sleep(1);
}

export const options = {
    stages: [
        { duration: '2m', target: 50 },
        { duration: '3m', target: 100 },
        { duration: '2m', target: 50 },
    ],
};
