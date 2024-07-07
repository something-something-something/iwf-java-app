import { sendDataToDisplay } from "sendData";
customElements.define('get-display', class GetDisplay extends HTMLElement {

	/**
	 * @type{Set<string>}
	 */
	#uuidsWritten;
	#identifier = crypto.randomUUID();


	constructor() {
		super();
		this.#uuidsWritten = new Set();

	}


	connectedCallback() {
		const shadow = this.attachShadow({ mode: 'open' });
		this.createSSE(shadow);


		this.addEventListener('submitToDisplayPromise', (ev) => {
			console.log(ev)
			if ('detail' in ev && typeof ev.detail === 'object' && ev.detail !== null) {
				const details = ev.detail;
				if ('data' in details) {

					const data = details.data;

					sendDataToDisplay(details.data)
				}
			}
		})





	}




	/**
	 * @param {ShadowRoot} shadow
	 */
	createSSE(shadow) {
		const evtSource = new EventSource("/display/test" + window.location.search);

		evtSource.addEventListener("closedByServer", (ev) => {
			evtSource.close();
			this.createSSE(shadow);
		});
		evtSource.addEventListener("displaystatus", (ev) => {

			const data = JSON.parse(ev.data);
			const dispupid = data.displayUpdateuuid;

			if (!this.#uuidsWritten.has(dispupid)) {
				console.log('getting lock');
				navigator.locks.request('updatedisplay' + this.#identifier, async (lock) => {

					if (!this.#uuidsWritten.has(dispupid)) {
						//console.log(ev)
						const data = JSON.parse(ev.data);
						const dispupid = data.displayUpdateuuid;


						shadow.innerHTML = ``;
						await import(data.display.module);
						this.#uuidsWritten.add(dispupid);
						const el = document.createElement(data.display.elementName);
						await customElements.whenDefined(data.display.elementName);
						for (let [propName, value] of Object.entries(data.display.properties)) {
							el[propName] = value;
						}
						for (let [propName, value] of Object.entries(data.display.attributes)) {
							el.setAttribute(propName, value);
						}
						shadow.append(el);
					}
				});
			}
		});
	}
});





