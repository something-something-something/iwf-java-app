/**
 * @typedef {import('./type.ts').JSONType}  JSONType
 */


/**
 *
 * @param {HTMLElement} htmlelement
 * @param {JSONType} data
 */
export function sendDataToWorkflow(htmlelement, data) {
	const nextevent = new CustomEvent('submitToDisplayPromise', {
		composed: true,
		bubbles: true,
		detail: {
			data
		}
	});
	console.log(nextevent);
	htmlelement.dispatchEvent(nextevent);
}
