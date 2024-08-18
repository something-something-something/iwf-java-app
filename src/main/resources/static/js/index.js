customElements.define('start-form', class StartForm extends HTMLElement {
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
		<div class="goTo"></div>
		<pre></pre>
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
		 * @type {import('./type.ts').JSONType}
		 */
		const json = await res.json();
		const pre = this.shadowRoot?.querySelector('pre');
		const goto=this.shadowRoot?.querySelector('.goTo');
		if (pre !== null&&pre!==undefined&&goto!==undefined&&goto!==null) {
			
			let str=JSON.stringify(json, null, '\t')
			if(typeof json==='object' &&json!==null&&'uuid' in json){
				goto.innerHTML=`<a href="/display/listDisplays?workflowId=${json.uuid}" target="_blank">${json.uuid}</a>`;
			}
			else{
				goto.innerHTML=''
			}
			pre.innerText = str
		}


	}
});