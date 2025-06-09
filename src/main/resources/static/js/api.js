(() => {
	if (window.api == null) {
		window.api = {}
		api.getUserSessions = async function (username, from, to) {
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

		api.getUserSessionsIds = async function (username, from, to) {
			const sessions = await api.getUserSessions(username, from, to);
			return sessions.map(session => session.id);
		}

		api.getSession = async function (sessionId) {
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

		api.downloadSessionZip = async function (sessionId) {
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

		api.deleteSession = async function (sessionId) {
			const response = await fetch(`/api/sessions/${sessionId}`, {
				method: 'DELETE'
			});

			if (!response.ok) {
				let message = `Failed to delete session {sessionId}`;
				if (response.reason) message += `: ${response.reason}`;
				throw new Error(message);
			}
		}

		api.getUsers = async function () {
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

		api.setUserRole = async function (username, userRole) {
			const response = await fetch(`/api/users/${username}/role/${userRole}`, {
				method: 'POST',
			});

			if (!response.ok) {
				let message = `Failed to set user role ${userRole}`;
				if (response.reason) message += `: ${response.reason}`;
				throw new Error(message);
			}
		}

		api.getUserSummary = async function (username) {
			const response = await fetch(`/api/users/${username}/summary`, {
				method: 'GET',
				headers: {
					'Accept': 'application/json',
				}
			})

			if (!response.ok) {
				let message = `Failed to get user summary`;
				if (response.reason) message += `: ${response.reason}`;
				throw new Error(message);
			}

			return response.json();
		}
	}
})();