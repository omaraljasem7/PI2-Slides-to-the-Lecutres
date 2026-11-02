"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
//Sets up event listeners once the document has fully loaded.
document.addEventListener('DOMContentLoaded', () => __awaiter(void 0, void 0, void 0, function* () {
    var _a, _b;
    // Listen for the submit event on the add-form and process the addition of numbers.
    (_a = document.getElementById('add-form')) === null || _a === void 0 ? void 0 : _a.addEventListener('submit', (event) => __awaiter(void 0, void 0, void 0, function* () {
        event.preventDefault();
        const a = document.getElementById('a').value;
        const b = document.getElementById('b').value;
        const c = document.getElementById('c').value;
        yield add(a, b, c);
    }));
    // Listen for the submit event on the user-form and process user login.
    (_b = document.getElementById('user-form')) === null || _b === void 0 ? void 0 : _b.addEventListener('submit', (event) => __awaiter(void 0, void 0, void 0, function* () {
        event.preventDefault();
        const username = document.getElementById('username').value;
        yield login(username);
    }));
}));
/**
 * Sends a POST request to add three numbers (a, b, c) provided as strings.
 * The result is displayed in the 'result' HTML element if the response is successful.
 *
 * @param a The first number as a string.
 * @param b The second number as a string.
 * @param c The third number as a string.
 */
function add(a, b, c) {
    return __awaiter(this, void 0, void 0, function* () {
        const res = yield fetch('http://localhost:8888/add', {
            method: 'post',
            mode: 'cors',
            headers: {
                "Content-type": "application/json"
            },
            credentials: 'include',
            body: JSON.stringify({ a, b, c })
        });
        const data = yield res.json();
        const result = document.getElementById('result');
        if (result) {
            if (res.ok) {
                result.textContent = data.sum;
            } else {
                result.textContent = data.error || 'An error occurred';
            }
        }
    });
}
/**
 * Sends a POST request to log in a user with a username. The logged-in username is displayed
 * in the 'login-name' HTML element if the response is successful.
 *
 * @param username The username to be sent for logging in.
 */
function login(username) {
    return __awaiter(this, void 0, void 0, function* () {
        const res = yield fetch('http://localhost:8888/login', {
            method: 'POST',
            mode: 'cors',
            headers: {
                "Content-type": "application/json"
            },
            credentials: 'include',
            body: JSON.stringify({ username })
        });
        const data = yield res.json();
        if (res.ok) {
            const result = document.getElementById('login-name');
            if (result) {
                result.textContent = data.username;
            }
        }
    });
}
