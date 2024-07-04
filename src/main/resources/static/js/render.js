customElements.define('get-display', class GetDisplay extends HTMLElement {

	/**
	 * @type{Set<string>}
	 */
	#uuidsWritten;

	constructor() {
		super();
		this.#uuidsWritten = new Set();

	}


	connectedCallback() {
		const shadow = this.attachShadow({ mode: 'open' });
		const evtSource = new EventSource("/display/test" + window.location.search);
		evtSource.addEventListener("displaystatus", async (ev) => {
console.log(ev)
			const data = JSON.parse(ev.data);
			const dispupid = data.displayUpdateuuid;
			if (!this.#uuidsWritten.has(dispupid)) {
				
				shadow.innerHTML = ``
				await import(data.display.module);
				this.#uuidsWritten.add(dispupid);
			const el=	document.createElement(data.display.elementName);
				shadow.append(el);
			}



		})







	}



});



