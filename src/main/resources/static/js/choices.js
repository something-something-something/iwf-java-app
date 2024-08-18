//import {sendDataToDisplay} from "sendData";

import { html } from 'htmlv1';
import { sendDataToWorkflow } from './sendDataToWorkflow.js';


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
			sendDataToWorkflow(this,{choice:formData?.get('choice')?.toString()??null})


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
		 * @type {import('./type.ts').JSONType}
		 */
		const json = await res.json();
		const pre = document.createElement('pre');
		pre.innerText = JSON.stringify(json, null, '\t')
		this.shadowRoot?.append(pre)
	}
});

