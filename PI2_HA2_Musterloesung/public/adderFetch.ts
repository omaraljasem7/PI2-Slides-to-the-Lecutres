//Sets up event listeners once the document has fully loaded.
document.addEventListener('DOMContentLoaded', async (): Promise<void> => {

    // Listen for the submit event on the add-form and process the addition of numbers.
    document.getElementById('add-form')?.addEventListener('submit', async (event: SubmitEvent): Promise<void> => {
        event.preventDefault();
        const a: string = (document.getElementById('a') as HTMLInputElement).value;
        const b: string = (document.getElementById('b') as HTMLInputElement).value;
        const c: string = (document.getElementById('c') as HTMLInputElement).value;
        await add(a, b, c);
    });

    // Listen for the submit event on the user-form and process user login.
    document.getElementById('user-form')?.addEventListener('submit', async (event: SubmitEvent): Promise<void> => {
        event.preventDefault();
        const username: string = (document.getElementById('username') as HTMLInputElement).value;
        await login(username)
    });
});

/**
 * Sends a POST request to add three numbers (a, b, c) provided as strings.
 * The result is displayed in the 'result' HTML element if the response is successful.
 *
 * @param a The first number as a string.
 * @param b The second number as a string.
 * @param c The third number as a string.
 */
async function add(a: string, b: string, c: string): Promise<void> {
    const res: Response = await fetch('http://localhost:8888/add', {
        method: 'post',
        mode: 'cors',
        headers: {
            "Content-type": "application/json"
        },
        credentials: 'include',
        body: JSON.stringify({a, b, c})
    });

    const data = await res.json();
    const result: HTMLElement | null = document.getElementById('result');
    if (result) {
        if (res.ok) {
            result.textContent = data.sum;
        } else {
            result.textContent = data.error || 'An error occurred';
        }
    }
}

/**
 * Sends a POST request to log in a user with a username. The logged-in username is displayed
 * in the 'login-name' HTML element if the response is successful.
 *
 * @param username The username to be sent for logging in.
 */
async function login(username: string): Promise<void> {
    const res: Response = await fetch('http://localhost:8888/login', {
        method: 'POST',
        mode: 'cors',
        headers: {
            "Content-type": "application/json"
        },
        credentials: 'include',
        body: JSON.stringify({username})
    })

    const data = await res.json();
    if (res.ok) {
        const result: HTMLElement | null = document.getElementById('login-name');
        if (result) {
            result.textContent = data.username;
        }
    }
}