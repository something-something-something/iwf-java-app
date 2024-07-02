class StartForm extends HTMLElement {
	constructor() {
		super()
	}
	connectedCallback() {
		const shadow = this.attachShadow({ mode: 'open' });
		shadow.addEventListener('submit', (ev) => {
			ev.preventDefault();
			const textarea = this.shadowRoot?.querySelector('textarea');
			if (textarea !== null) {
				const instructions = textarea?.value;
				this.submitData(instructions)



			}


		})


		shadow.innerHTML = `
		<form>
		
		<textarea name="instructions"></textarea>
		<input type="submit">
		</form>
		`


	}


	/**
	 * @param {string | undefined} instructions
	 */
	async submitData(instructions) {
		const res = await fetch(window.location.href, {
			method: 'POST',
			headers: { 'content-type': 'application/json' },
			body: JSON.stringify({
				"instructions": JSON.parse(instructions ?? '{}')
			})
		});
		/**
		 * @type {import('./type.ts').JSON}
		 */
		const json = await res.json();
		const pre = document.createElement('pre');
		pre.innerText = JSON.stringify(json, null, '\t')
		this.shadowRoot?.append(pre)
	}
}

customElements.define('start-form', StartForm)