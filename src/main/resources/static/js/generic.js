/**
 * @typedef {import('./type.ts').JSONType}  JSONType
 */

import { sendDataToWorkflow } from './sendDataToWorkflow.js';

class AribitaryInput extends HTMLElement{
	
	
	#shadow;

	#textAreaId;

	/**
	 * @type{JSONType}
	 */
	#example={}

	#description="Place Json Here"
	
	constructor(){
		super()
		this.#shadow=this.attachShadow({mode:'open'})
		this.#textAreaId=crypto.randomUUID();
		const form=document.createElement('form')
		form.classList.add('myform');
		const label=document.createElement('label')
		label.htmlFor=this.#textAreaId;

		const textarea=document.createElement('textarea');
		textarea.id=this.#textAreaId;
		textarea.name="json";

		const button=document.createElement('button');
		button.type='submit';
		button.textContent='submit data'

		form.appendChild(button);

		form.appendChild(label);
		form.appendChild(textarea);

		this.#shadow.appendChild(form);

		this.#shadow.addEventListener('submit',(ev)=>{
			ev.preventDefault();
			const form=this.#shadow.querySelector('.myform');
			console.log(form)
			if( form instanceof HTMLFormElement){
				const data=new FormData(form);
				const jsonField=data.get('json');
				console.log(jsonField);
				if(jsonField!==null){
					try{
						/**
						 * @type{JSONType}
						 */
						const data=JSON.parse(jsonField.toString());
						console.log(data)
						sendDataToWorkflow(this,data);
					}
					catch(err){
						alert('Could not parse Json');
					}
					
				}
				
			}
		});
		
	}

	connectedCallback(){
	}

	get exampleValue(){
		return this.#example
	}


	set exampleValue(data){
		this.#example=data
		const textarea=this.#shadow.getElementById(this.#textAreaId);
		if(textarea instanceof HTMLTextAreaElement){
			try{
				textarea.value=JSON.stringify(data);
			}catch(err){
				console.error(err);
			}
			
		}
	}


	get description(){
		return this.#description;
	}

	set description(value){
		this.#description=value
		const label=this.#shadow.querySelector('label');
		if(label instanceof HTMLLabelElement){
			try{
				label.textContent=value;
			}catch(err){
				console.error(err);
			}
			
		}
	}
}


customElements.define('generic-input',AribitaryInput)