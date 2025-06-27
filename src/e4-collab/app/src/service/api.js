
//TODO sistemare chiamate rest

import CryptoJS from 'crypto-js';
import axiosConf from '@/service/axios.js';

const cryptKey = "irKUdHRurUbEEVCKCcgOnG1CzhQ9whfGwDt4xRyylooSLscpBhD6h5ovvRWNuJCcnhMft0kui6jKwfSFhzTZOEiL2i4CZKj"

export const getUserSessions = async function (username, from, to) {
			let uri = `/api/users/${username}/sessions`;
			let query = [];
			if (from != null) query.push(`from=${from.valueOf()}`);
			if (to != null) query.push(`to=${to.valueOf()}`);
			if (query.length) uri += '?' + query.join('&');

			const response = await fetch(uri, {
				method: 'GET',
				headers: {
					'Accept': 'application/json',
				}
			});

			if (response.ok) {
				return response.json();
			} else {
				let message = 'Failed to fetch sessions';
				if (response.reason) message += `: ${response.reason}`;
				throw new Error(message);
			}
		}

export const getUserSessionsIds = async function (username, from, to) {
			const sessions = await getUserSessions(username, from, to);
			return sessions.map(session => session.id);
		}

		export const getSession = async function (sessionId) {
			const response = await fetch(`/api/sessions/${sessionId}`,
				{
					method: 'GET',
					headers: {
						'Accept': 'application/json',
					}
				}
			);

			if (!response.ok) {
				let message = `Failed to fetch session ${sessionId}`;
				if (response.reason) message += `: ${response.reason}`;
				throw new Error(message);
			}

			return response.json();
		}

		export const downloadSessionZip = async function (sessionId) {
			const response = await fetch(`/api/sessions/${sessionId}/download`, {
				method: 'GET'
			});

			if (!response.ok) {
				let message = `Failed to download session zip {sessionId}`;
				if (response.reason) message += `: ${response.reason}`;
				throw new Error(message);
			}

			const data = await response.json();

			const a = document.createElement('a');
			a.download = data.id + '_' + data.startTimestamp + '.zip';

			const contentBytes = Uint8Array.from(atob(data.content).split("").map(x => x.charCodeAt()));
			a.href = URL.createObjectURL(new Blob([contentBytes], { type: 'application/zip' }));

			a.click();
			a.remove();
		}

		export const deleteSession = async function (sessionId) {
			const response = await fetch(`/api/sessions/${sessionId}`, {
				method: 'DELETE'
			});

			if (!response.ok) {
				let message = `Failed to delete session {sessionId}`;
				if (response.reason) message += `: ${response.reason}`;
				throw new Error(message);
			}
		}

		export const getUsers = async function () {
			const response = await fetch(`/api/users`, {
				method: 'GET',
				headers: {
					'Accept': 'application/json',
				}
			});

			if (!response.ok) {
				let message = 'Failed to fetch users';
				if (response.reason) message += `: ${response.reason}`;
				throw new Error(message);
			}

			return response.json();
		}

		export const setUserRole = async function (username, userRole) {
			const response = await fetch(`/api/users/${username}/role/${userRole}`, {
				method: 'POST',
			});

			if (!response.ok) {
				let message = `Failed to set user role ${userRole}`;
				if (response.reason) message += `: ${response.reason}`;
				throw new Error(message);
			}
		}

		export const getUserSummary = async function (username) {
			
			const response = await axiosConf.get("/security/users/"+username+"/summary");
			return response;
		}

		export const loginRest = async function (username, password) {

			let cryptPass = CryptoJS.AES.encrypt(password, cryptKey).toString();

			const response = await axiosConf.post("/security/login",{
				username: username,
				password: cryptPass
			})

			return response
		}

		export const checkSession = async function () {
			const response = await axiosConf.get("/security/checkSession")

			return response
		}

		export const signUpRest = async function (username, password, email) {

			let cryptPass = CryptoJS.AES.encrypt(password, cryptKey).toString();

			const response = await axiosConf.post("/security/signup",{
				username: username,
				password: cryptPass,
				email: email
			})

			return response
		}