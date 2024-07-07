//import {sendDataToDisplay} from "sendData";

import { html } from 'htmlv1';


/**
 * @typedef {Object} Choice
 * @property {string} value
 * @property {string} humanText
 * */

customElements.define('choice-options', class StartForm extends HTMLElement {
	/**
	 * @type {Choice[]}
	 */
	#choices=[]
	
	constructor() {
		super()
	}
	connectedCallback() {
		const shadow = this.attachShadow({ mode: 'open' });
		shadow.addEventListener('submit', (ev) => {
			ev.preventDefault();
			let formData=undefined;
			for (let evt of ev.composedPath()){
				if(evt instanceof HTMLFormElement){
					formData=new FormData(evt);
					break;
				}
			}
			const nextevent=new CustomEvent('submitToDisplayPromise',{
				composed:true,
				bubbles:true,
				detail:{
					data:{
						choice:formData?.get('choice')?.toString()??null
					}
				}
			});
			console.log(nextevent)
			this.dispatchEvent(nextevent)
		


		})
		const choiceforms=this.#choices.map((ch)=>{
			console.log(ch)
			return `<form>
				<input type="hidden" name="choice" value="${ch.value}">
				<button type="submit">${ch.humanText}</button>
			</form>
			`
		}).join('')

		shadow.innerHTML = `
		<div> ${this.getAttribute('data-describe')}</div>
		${choiceforms}
		`


	}
	/**
	 * @param {Choice[]} ch
	 */
	set choices(ch){
		console.log(ch)
		this.#choices=structuredClone(ch);
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
});